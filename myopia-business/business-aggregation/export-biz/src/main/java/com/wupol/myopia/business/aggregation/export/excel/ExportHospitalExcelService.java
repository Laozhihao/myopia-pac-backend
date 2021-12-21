package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.constant.HospitalEnum;
import com.wupol.myopia.business.core.hospital.constant.HospitalLevelEnum;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalExportDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        Integer districtId = exportCondition.getDistrictId();
        List<HospitalExportDTO> exportList = new ArrayList<>();

        HospitalQuery query = new HospitalQuery();
        query.setDistrictId(districtId);
        List<Hospital> list = hospitalService.getBy(query);

        List<Integer> orgIds = list.stream().map(Hospital::getAssociateScreeningOrgId).collect(Collectors.toList());
        Map<Integer, String> orgMap = screeningOrganizationService.getByIds(orgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        for (Hospital hospital : list) {
            HospitalExportDTO exportVo = new HospitalExportDTO()
                    .setName(hospital.getName())
                    .setAddress(districtService.getAddressDetails(hospital.getProvinceCode(), hospital.getCityCode(), hospital.getAreaCode(), hospital.getTownCode(), hospital.getAddress()))
                    .setLevel(HospitalLevelEnum.getLevel(hospital.getLevel()))
                    .setType(HospitalEnum.getTypeName(hospital.getType()))
                    .setKind(HospitalEnum.getKindName(hospital.getKind()))
                    .setRemark(hospital.getRemark())
                    .setAccountNo(hospital.getName())
                    .setServiceType(HospitalEnum.getServiceTypeName(hospital.getServiceType()))
                    .setCooperationType(HospitalEnum.getCooperationTypeName(hospital.getCooperationType()))
                    .setCooperationRemainTime(hospital.getCooperationRemainTime())
                    .setCooperationStartTime(Objects.nonNull(hospital.getCooperationStartTime()) ? DateFormatUtil.format(hospital.getCooperationStartTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                    .setCooperationEndTime(Objects.nonNull(hospital.getCooperationEndTime()) ? DateFormatUtil.format(hospital.getCooperationEndTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                    .setAssociateScreeningOrg(orgMap.getOrDefault(hospital.getAssociateScreeningOrgId(), StringUtils.EMPTY))
                    .setCreateTime(DateFormatUtil.format(hospital.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            exportList.add(exportVo);
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
