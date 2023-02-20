package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.DeviceBizService;
import com.wupol.myopia.business.api.management.service.DeviceScreeningDataBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.ObjectUtil;
import com.wupol.myopia.business.common.utils.util.VS550Util;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.domain.vo.DeviceReportTemplateVO;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/29 17:39
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/device/data")
public class DeviceScreeningDataController {

    @Autowired
    private DeviceScreeningDataBizService deviceScreeningDataBizService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;
    @Autowired
    private DeviceBizService deviceBizService;



    @GetMapping("/list")
    public IPage<DeviceScreeningDataAndOrgDTO> queryDeptPage(DeviceScreeningDataQueryDTO query, @Validated PageRequest pageRequest) {
        checkAndHandleParam(query);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            query.setScreeningOrgId(user.getScreeningOrgId());
            query.setOrgType(OrgTypeEnum.SCREENING.getCode());
        } else if (!user.isPlatformAdminUser()) {
            throw new BusinessException("无访问权限", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
        }
        IPage<DeviceScreeningDataAndOrgDTO> page = deviceScreeningDataBizService.getPage(query, pageRequest);


        if (Objects.nonNull(page) && !CollectionUtils.isEmpty(page.getRecords())) {
            List<DeviceScreeningDataAndOrgDTO> records = page.getRecords();

            // 获取机构对应的配置
            List<Integer> orgIds = records.stream().map(DeviceScreeningData::getScreeningOrgId).collect(Collectors.toList());
            Map<Integer, Integer> configTypes = screeningOrganizationService.getByIds(orgIds).stream()
                    .collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getConfigType));

            // 获取报告类型
            Map<Integer, Integer> deviceReportTemplateVOS = screeningOrgBindDeviceReportService.getByOrgIds(orgIds).stream()
                    .collect(Collectors.toMap(DeviceReportTemplateVO::getScreeningOrgId, DeviceReportTemplateVO::getTemplateType));

            records.forEach(r -> {
                r.setTemplateType(deviceReportTemplateVOS.get(r.getScreeningOrgId()));

                //右眼球镜-展示使用
                r.setRightSphDisplay(deviceBizService.resolvingPower(configTypes.get(r.getScreeningOrgId()),r.getRightSph()));
                //左眼球镜-展示使用
                r.setLeftSphDisplay(deviceBizService.resolvingPower(configTypes.get(r.getScreeningOrgId()),r.getLeftSph()));

                //右眼柱镜-展示使用
                r.setRightCylDisplay(deviceBizService.resolvingPower(configTypes.get(r.getScreeningOrgId()),r.getRightCyl()));
                //左眼柱镜-展示使用
                r.setLeftCylDisplay(deviceBizService.resolvingPower(configTypes.get(r.getScreeningOrgId()),r.getLeftCyl()));

                if ( Objects.equals(configTypes,ScreeningOrgConfigTypeEnum.CONFIG_TYPE_4.getType())){
                    //右眼等效球镜
                    r.setRightPa(VS550Util.computerSE(r.getRightSphDisplay(), r.getRightCylDisplay()));
                    //左眼等效球镜
                    r.setLeftPa(VS550Util.computerSE(r.getLeftSphDisplay(), r.getLeftCylDisplay()));

                }

            });
        }
        return page;
    }
    private void checkAndHandleParam(DeviceScreeningDataQueryDTO query) {
        if (ObjectUtil.hasSomeNull(query.getCylStart(), query.getCylEnd()) || ObjectUtil.hasSomeNull(query.getSphStart(), query.getSphEnd())) {
            throw new BusinessException("参数异常");
        }
        // 排序柱镜
        if (Objects.nonNull(query.getCylStart()) && (query.getCylStart().compareTo(query.getCylEnd()) > 0)) {
            BigDecimal temp = query.getCylStart();
            query.setCylStart(query.getCylEnd());
            query.setCylEnd(temp);
        }
        // 排序球镜
        if (Objects.nonNull(query.getSphStart()) && (query.getSphStart().compareTo(query.getSphEnd()) > 0)) {
            BigDecimal temp = query.getSphStart();
            query.setSphStart(query.getSphEnd());
            query.setSphEnd(temp);
        }
    }

}
