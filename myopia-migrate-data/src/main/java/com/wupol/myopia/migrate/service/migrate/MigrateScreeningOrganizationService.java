package com.wupol.myopia.migrate.service.migrate;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.model.ScreeningConfig;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO;
import com.wupol.myopia.migrate.domain.model.SysDept;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.service.SysDeptService;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 迁移筛查机构数据
 * 注意：
 *      1.默认为单点筛查机构、医院类型、合作有效期1年、仅有普通二维码权限
 *      2.没有区/县/镇行政区域信息，会报错
 *
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@Log4j2
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
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    /**
     * 迁移筛查机构和筛查人员
     * 
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO>
     **/
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<ScreeningOrgAndStaffDO> migrateScreeningOrgAndScreeningStaff() {
        log.info("==  筛查机构-开始.....  ==");
        List<SysDept> deptList = sysDeptService.findByList(new SysDept());
        List<ScreeningOrgAndStaffDO> screeningOrganizationList = new ArrayList<>();
        deptList.forEach(sysDept -> {
            // 没有筛查数据的不迁移
            if (sysStudentEyeService.count(new SysStudentEye().setDeptId(sysDept.getDeptId())) <= 0) {
                log.warn("{}没有筛查数据，不迁移", sysDept.getSimpleName());
                return;
            }
            // 迁移筛查机构
            ScreeningOrganization screeningOrganization = saveScreeningOrganization(sysDept);
            // 封装筛查机构和筛查人员信息以备用
            screeningOrganizationList.add(packageScreeningOrgAndStaffDO(sysDept.getDeptId(), screeningOrganization.getId(), screeningOrganization.getName(), screeningOrganization.getDistrictId()));
        });
        log.info("==  筛查机构-完成  ==");
        return screeningOrganizationList;
    }


    /**
     * 保存筛查机构
     *
     * @param sysDept 筛查机构
     * @return ScreeningOrganization 新机构
     */
    private ScreeningOrganization saveScreeningOrganization(SysDept sysDept) {
        String name = getOrgName(sysDept.getSimpleName());
        // 存在同名的机构，则不新增
        ScreeningOrganization existScreeningOrg = screeningOrganizationService.findOne(new ScreeningOrganization().setName(name));
        if (Objects.nonNull(existScreeningOrg)) {
            return existScreeningOrg;
        }
        sysDept.setSimpleName(name);
        ScreeningOrganization screeningOrganization = getScreeningOrganization(sysDept);
        screeningOrganizationService.save(screeningOrganization);
        // 为筛查机构新增设备报告模板
        DeviceReportTemplate template = deviceReportTemplateService.getSortFirstTemplate();
        screeningOrgBindDeviceReportService.orgBindReportTemplate(template.getId(), screeningOrganization.getId(), screeningOrganization.getName());
        // 生成账号密码
        UsernameAndPasswordDTO usernameAndPasswordDTO = screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.PARENT_ACCOUNT, null);
        // 生成TA筛查人员
        createAutoStaff(screeningOrganization.getId(), usernameAndPasswordDTO.getUsername());
        // 同步到oauth机构状态
        oauthServiceClient.addOrganization(new Organization(screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.SCREENING_ORGANIZATION_ADMIN, screeningOrganization.getStatus()));
        return screeningOrganization;
    }

    private void createAutoStaff(Integer orgId, String userName) {
        ScreeningOrganizationStaffQueryDTO screeningOrganizationStaffQueryDTO = new ScreeningOrganizationStaffQueryDTO();
        screeningOrganizationStaffQueryDTO.setScreeningOrgId(orgId);
        screeningOrganizationStaffQueryDTO.setCreateUserId(1);
        screeningOrganizationStaffQueryDTO.setGovDeptId(1);
        screeningOrganizationStaffQueryDTO.setType(ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL);
        screeningOrganizationStaffQueryDTO.setRealName(ScreeningOrganizationStaff.AUTO_CREATE_STAFF_DEFAULT_NAME);
        screeningOrganizationStaffQueryDTO.setUserName(userName);
        screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaffQueryDTO);
    }

    /**
     * 获取机构名称，优先使用系统已经存在的筛查机构名
     *
     * @param oldName 机构名称
     * @return java.lang.String
     **/
    private static String getOrgName(String oldName) {
        Assert.hasText(oldName, "筛查机构名字不能为空");
        Map<String, String> nameMap = new HashMap<>(2);
        nameMap.put("昆明康特森眼科医院", "康特森");
        return Optional.ofNullable(nameMap.get(oldName)).orElse(oldName);
    }

    /**
     * 获取自动创建筛查人员的信息
     *
     * @param screeningOrgId 筛查机构ID
     * @return com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff
     **/
    private ScreeningOrganizationStaff getAutoCreateScreeningStaff(Integer screeningOrgId) {
        // 山西版筛查人员仅有姓名无其他有效信息，不足以生成筛查人员，故获取自动生成的筛查人员
        ScreeningOrganizationStaff screeningOrganizationStaff = new ScreeningOrganizationStaff()
                .setScreeningOrgId(screeningOrgId)
                .setType(ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL);
        ScreeningOrganizationStaff orgStaff = screeningOrganizationStaffService.findOne(screeningOrganizationStaff);
        if (Objects.nonNull(orgStaff)) {
            return orgStaff;
        }
        // 降级处理
        List<ScreeningOrganizationStaff> staffList = screeningOrganizationStaffService.findByList(new ScreeningOrganizationStaff().setScreeningOrgId(screeningOrgId));
        return staffList.get(0);
    }

    /**
     * 获取新筛查机构实体
     *
     * @param sysDept 查机构信息
     * @return com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization
     **/
    private ScreeningOrganization getScreeningOrganization(SysDept sysDept) {
        ScreeningOrganization screeningOrganization = new ScreeningOrganization().setCreateUserId(1)
                .setGovDeptId(1)
                // 默认为单点配置
                .setConfigType(ScreeningOrgConfigTypeEnum.CONFIG_TYPE_1.getType())
                .setName(sysDept.getSimpleName())
                // 默认为医院，迁移完后需要去管理平台修改
                .setType(0)
                // 普通二维码
                .setQrCodeConfig("1")
                // 筛查类型
                .setScreeningTypeConfig(String.valueOf(ScreeningTypeEnum.VISION.getType()))
                // 可筛查项目配置
                .setScreeningConfig(JSON.parseObject("{\"screeningTypeList\":[0],\"channel\":\"Official\",\"medicalProjectList\":[\"vision\",\"computer_optometry\",\"other_eye_diseases\"]}", ScreeningConfig.class));
        // 设置行政区域信息
        Long areaDistrictCode = districtService.getCodeByName(sysDept.getRegion(), null);
        District areaDistrict = districtService.getByCode(areaDistrictCode);
        List<District> districtDetail = districtService.getDistrictPositionDetail(areaDistrictCode);
        screeningOrganization.setDistrictId(areaDistrict.getId())
                .setDistrictDetail(JSON.toJSONString(districtDetail))
                .initCooperationInfo();
        screeningOrganization.setStatus(screeningOrganization.getCooperationStopStatus());
        return screeningOrganization;
    }

    /**
     * 封装筛查机构和筛查人员信息
     *
     * @param oldOrgId          旧筛查机构ID
     * @param screeningOrgId    筛查机构ID
     * @param screeningOrgName  筛查机构名称
     * @param districtId        行政区域ID
     * @return com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO
     **/
    private ScreeningOrgAndStaffDO packageScreeningOrgAndStaffDO(String oldOrgId, Integer screeningOrgId, String screeningOrgName, Integer districtId) {
        // 获取自动创建的筛查人员信息（由于筛查人员缺少身份证和手机号码数据，不迁移）
        ScreeningOrganizationStaff screeningOrganizationStaff = getAutoCreateScreeningStaff(screeningOrgId);
        ScreeningOrganizationAdmin orgAdmin = screeningOrganizationAdminService.getByOrgId(screeningOrgId);
        // 获取筛查最多的筛查人员名称作为质检人员名称
        return new ScreeningOrgAndStaffDO(oldOrgId, screeningOrgId, screeningOrgName, orgAdmin.getUserId(), districtId, screeningOrganizationStaff.getUserId());
    }
}
