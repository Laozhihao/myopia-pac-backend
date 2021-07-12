package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.constant.ExportAddressKey;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrganizationEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

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

        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService
                .getOrgStaffMapByIds(list.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));

        // 创建人姓名
        Set<Integer> createUserIds = list.stream()
                .map(ScreeningOrganization::getCreateUserId)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = getUserMapByIds(createUserIds);

        List<ScreeningOrganizationExportDTO> exportList = new ArrayList<>();
        for (ScreeningOrganization item : list) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            ScreeningOrganizationExportDTO exportVo = new ScreeningOrganizationExportDTO();
            exportVo.setName(item.getName())
                    .setType(ScreeningOrganizationEnum.getTypeName(item.getType()))
                    .setConfigType(ScreeningOrgConfigTypeEnum.getTypeName(item.getConfigType()))
                    .setPhone(item.getPhone())
                    .setRemark(item.getRemark())
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
            List<ScreeningPlan> planResult = screeningPlanService.getByOrgId(item.getId());
            exportVo.setScreeningCount(CollectionUtils.isEmpty(planResult) ? 0 : planResult.size());
            if (Objects.nonNull(staffMaps.get(item.getId()))) {
                exportVo.setPersonSituation(staffMaps.get(item.getId()).size());
            } else {
                exportVo.setPersonSituation(0);
            }
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
        return String.format(ExcelFileNameConstant.SCREENING_ORG_NOTICE_KEY_CONTENT, districtFullName);
    }
}
