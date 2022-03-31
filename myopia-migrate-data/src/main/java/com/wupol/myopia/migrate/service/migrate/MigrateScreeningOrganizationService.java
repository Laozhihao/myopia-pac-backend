package com.wupol.myopia.migrate.service.migrate;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO;
import com.wupol.myopia.migrate.domain.model.SysDept;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.service.SysDeptService;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 迁移筛查机构数据
 *
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@Service
public class MigrateScreeningOrganizationService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private DeviceReportTemplateService deviceReportTemplateService;
    @Autowired
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 迁移筛查机构和筛查人员
     * 
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO>
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<ScreeningOrgAndStaffDO> migrateScreeningOrgAndScreeningStaff() {
        List<SysDept> deptList = sysDeptService.findByList(new SysDept());
        List<ScreeningOrgAndStaffDO> screeningOrganizationList = new ArrayList<>();
        deptList.forEach(sysDept -> {
            // 没有数据的不迁移
            if (sysStudentEyeService.count(new SysStudentEye().setDeptId(sysDept.getDeptId())) <= 0) {
                return;
            }
            // 迁移筛查机构
            ScreeningOrganization screeningOrganization = new ScreeningOrganization().setCreateUserId(1)
                    .setGovDeptId(1)
                    .setConfigType(ScreeningOrgConfigTypeEnum.CONFIG_TYPE_1.getType())
                    .setName(sysDept.getSimpleName())
                    // 默认为医院，迁移完后需要去管理平台修改
                    .setType(0)
                    // 普通二维码
                    .setQrCodeConfig("1");
            Long areaDistrictCode = districtService.getCodeByName(sysDept.getRegion());
            District areaDistrict = districtService.getByCode(areaDistrictCode);
            List<District> districtDetail = districtService.getDistrictPositionDetail(areaDistrictCode);
            screeningOrganization.setDistrictId(areaDistrict.getId())
                    .setDistrictDetail(JSON.toJSONString(districtDetail))
                    .initCooperationInfo();
            screeningOrganization.setStatus(screeningOrganization.getCooperationStopStatus());
            saveScreeningOrganization(screeningOrganization);
            // TODO：迁移筛查人员

            screeningOrganizationList.add(packageScreeningOrgAndStaffDO(sysDept.getDeptId(), screeningOrganization.getId(), screeningOrganization.getName(),
                    screeningOrganization.getDistrictId(), null, null));
        });
        // 需要
        return screeningOrganizationList;
    }

    /**
     * 封装筛查机构和筛查人员信息
     *
     * @param oldOrgId
     * @param screeningOrgId
     * @param screeningOrgName
     * @param districtId
     * @param staffId
     * @param staffName
     * @return com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO
     **/
    private ScreeningOrgAndStaffDO packageScreeningOrgAndStaffDO(String oldOrgId, Integer screeningOrgId, String screeningOrgName, Integer districtId, Integer staffId, String staffName) {
        return new ScreeningOrgAndStaffDO(oldOrgId, screeningOrgId, screeningOrgName, districtId, staffId, staffName);
    }

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    private void saveScreeningOrganization(ScreeningOrganization screeningOrganization) {
        String name = screeningOrganization.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名字不能为空");
        }
        if (Boolean.TRUE.equals(screeningOrganizationService.checkScreeningOrgName(name, null))) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        screeningOrganizationService.save(screeningOrganization);
        // 同步到oauth机构状态
        oauthServiceClient.addOrganization(new Organization(screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.SCREENING_ORGANIZATION_ADMIN, screeningOrganization.getStatus()));
        // 为筛查机构新增设备报告模板
        DeviceReportTemplate template = deviceReportTemplateService.getSortFirstTemplate();
        screeningOrgBindDeviceReportService.orgBindReportTemplate(template.getId(), screeningOrganization.getId(), screeningOrganization.getName());
        screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.PARENT_ACCOUNT, null);
    }
}
