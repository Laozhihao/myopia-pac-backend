package com.wupol.myopia.business.aggregation.export.excel;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 导出行政区域的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("screeningOrganizationExcelService")
public class ExportScreeningOrganizationExcelService extends BaseExportExcelFileService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.getById(exportCondition.getDistrictId());
        return String.format(ExcelFileNameConstant.SCREENING_ORG_EXCEL_FILE_NAME, districtService.getTopDistrictName(district.getCode()));
    }

    /**
     * 获取生成Excel的数据
     *
     * @param exportCondition 导出条件
     * @return java.util.List
     **/
    @Override
    public List getExcelData(ExportCondition exportCondition) {
        // 查询数据
        ScreeningOrganizationQueryDTO query = new ScreeningOrganizationQueryDTO();
        query.setDistrictId(exportCondition.getDistrictId());
        List<ScreeningOrganization> list = screeningOrganizationService.getBy(query);

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Integer> orgIds = list.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());

        List<ScreeningOrganizationAdmin> orgAdminList = screeningOrganizationAdminService.getByOrgIds(orgIds);
        Map<Integer, List<Integer>> adminMap = orgAdminList.stream().collect(Collectors.groupingBy(ScreeningOrganizationAdmin::getScreeningOrgId, Collectors.mapping(ScreeningOrganizationAdmin::getUserId, Collectors.toList())));

        List<Integer> userIds = orgAdminList.stream().map(ScreeningOrganizationAdmin::getUserId).collect(Collectors.toList());
        List<User> userLists = oauthServiceClient.getUserBatchByIds(userIds);
        Map<Integer, String> userMap = userLists.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        boolean isAdmin = oauthServiceClient.getUserBatchByIds(Lists.newArrayList(exportCondition.getApplyExportFileUserId())).get(0).getUserType().equals(0);
        List<ScreeningOrganizationExportDTO> exportList = new ArrayList<>();
        for (ScreeningOrganization item : list) {
            ScreeningOrganizationExportDTO exportVo = item.parseFromScreeningOrg();
            if (isAdmin) {
                AtomicReference<String> account = new AtomicReference<>(StringUtils.EMPTY);
                adminMap.get(item.getId()).forEach(s -> account.set(account + userMap.get(s) + "、"));
                account.set(account.get().substring(0, account.get().length() - 1));
                exportVo.setAccount(account.get());
            } else {
                exportVo.setCooperationType(StringUtils.EMPTY)
                        .setCooperationRemainTime(null)
                        .setCooperationStartTime(StringUtils.EMPTY)
                        .setCooperationEndTime(StringUtils.EMPTY);
            }
            exportVo.setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(districtService.getAddressDetails(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()));
            exportList.add(exportVo);
        }
        return exportList;
    }

    /**
     * 获取Excel表头类
     *
     * @return java.lang.Class
     **/
    @Override
    public Class getHeadClass() {
        return ScreeningOrganizationExportDTO.class;
    }

    /**
     * 获取通知的关键内容
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        District district = districtService.getById(exportCondition.getDistrictId());
        String districtFullName = districtService.getTopDistrictName(district.getCode());
        return String.format(ExcelNoticeKeyContentConstant.SCREENING_ORG_NOTICE_KEY_CONTENT, districtFullName);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_ORG,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getDistrictId());
    }
}
