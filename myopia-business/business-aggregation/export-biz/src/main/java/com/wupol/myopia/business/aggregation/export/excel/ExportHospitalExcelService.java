package com.wupol.myopia.business.aggregation.export.excel;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalExportDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * 导出医院
 *
 * @author Simple4H
 */
@Service("hospitalExcelService")
public class ExportHospitalExcelService extends BaseExportExcelFileService {

    @Resource
    private DistrictService districtService;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private HospitalAdminService hospitalAdminService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        Integer districtId = exportCondition.getDistrictId();
        List<HospitalExportDTO> exportList = new ArrayList<>();

        HospitalQuery query = new HospitalQuery();
        query.setDistrictId(districtId);
        List<Hospital> hospitalList = hospitalService.getBy(query);

        // 筛查机构
        List<Integer> orgIds = hospitalList.stream().map(Hospital::getAssociateScreeningOrgId).collect(Collectors.toList());
        Map<Integer, String> orgMap = screeningOrganizationService.getByIds(orgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));

        // 账号
        List<Integer> hospitalIds = hospitalList.stream().map(Hospital::getId).collect(Collectors.toList());
        List<HospitalAdmin> hospitalAdminList = hospitalAdminService.getByHospitalIds(hospitalIds);
        Map<Integer, List<Integer>> adminMap = hospitalAdminList.stream().collect(Collectors.groupingBy(HospitalAdmin::getHospitalId, Collectors.mapping(HospitalAdmin::getUserId, Collectors.toList())));

        List<Integer> userIds = hospitalAdminList.stream().map(HospitalAdmin::getUserId).collect(Collectors.toList());
        List<User> userLists = oauthServiceClient.getUserBatchByIds(userIds);
        Map<Integer, String> userMap = userLists.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        if (CollectionUtils.isEmpty(hospitalList)) {
            return new ArrayList<>();
        }
        boolean isAdmin = oauthServiceClient.getUserBatchByIds(Lists.newArrayList(exportCondition.getApplyExportFileUserId())).get(0).getUserType().equals(0);
        for (Hospital hospital : hospitalList) {
            HospitalExportDTO hospitalExportDTO = hospital.parseFromHospital();
            if (isAdmin) {
                AtomicReference<String> account = new AtomicReference<>(StringUtils.EMPTY);
                adminMap.get(hospital.getId()).forEach(s -> account.set(account + userMap.get(s) + "、"));
                account.set(account.get().substring(0, account.get().length() - 1));
                hospitalExportDTO.setAccountNo(account.get());
            } else {
                hospitalExportDTO.setCooperationType(StringUtils.EMPTY)
                        .setCooperationRemainTime(null)
                        .setCooperationStartTime(StringUtils.EMPTY)
                        .setCooperationEndTime(StringUtils.EMPTY);
            }
            hospitalExportDTO.setAddress(districtService.getAddressDetails(hospital.getProvinceCode(), hospital.getCityCode(), hospital.getAreaCode(), hospital.getTownCode(), hospital.getAddress()))
                    .setAssociateScreeningOrg(orgMap.getOrDefault(hospital.getAssociateScreeningOrgId(), StringUtils.EMPTY));
            exportList.add(hospitalExportDTO);
        }
        return exportList;
    }

    @Override
    public Class getHeadClass() {
        return HospitalExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        District district = districtService.checkAndGetDistrict(exportCondition.getDistrictId());
        return String.format(ExcelNoticeKeyContentConstant.HOSPITAL_EXCEL_NOTICE_KEY_CONTENT, districtService.getTopDistrictName(district.getCode()));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.checkAndGetDistrict(exportCondition.getDistrictId());
        // 设置文件名
        return ExcelFileNameConstant.HOSPITAL_FILE_NAME + district.getName();
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_HOSPITAL,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getDistrictId());
    }
}
