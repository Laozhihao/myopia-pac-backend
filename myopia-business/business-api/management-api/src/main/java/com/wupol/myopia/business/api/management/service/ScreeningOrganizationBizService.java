package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.OrgCooperationHospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.RecordDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningRecordItems;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.*;
import com.wupol.myopia.business.core.screening.organization.service.*;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查机构
 *
 * @author Simple4H
 */
@Service
public class ScreeningOrganizationBizService {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;
    @Resource
    private DistrictService districtService;
    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Resource
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Resource
    private DistrictBizService districtBizService;
    @Resource
    private OrgCooperationHospitalService orgCooperationHospitalService;
    @Resource
    private HospitalService hospitalService;
    @Resource
    private HospitalBizService hospitalBizService;
    @Resource
    private StatRescreenService statRescreenService;
    @Resource
    private DeviceReportTemplateService deviceReportTemplateService;
    @Resource
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;
    @Autowired
    private OverviewScreeningOrganizationService overviewScreeningOrganizationService;
    @Autowired
    private OverviewService overviewService;

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveScreeningOrganization(ScreeningOrganization screeningOrganization, CurrentUser user) {

        String name = screeningOrganization.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名字不能为空");
        }

        if (Boolean.TRUE.equals(screeningOrganizationService.checkScreeningOrgName(name, null))) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        screeningOrganizationService.save(screeningOrganization);
        if (user.isOverviewUser()) {
            // 总览机构：保存总览机构-筛查机构关系，更新缓存信息
            overviewScreeningOrganizationService.save(new OverviewScreeningOrganization().setOverviewId(user.getOrgId()).setScreeningOrganizationId(screeningOrganization.getId()));
            overviewService.removeOverviewCache(user.getOrgId());
        }
        // 同步到oauth机构状态
        oauthServiceClient.addOrganization(new Organization(screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.SCREENING_ORGANIZATION_ADMIN, screeningOrganization.getStatus()));
        // 为筛查机构新增设备报告模板
        DeviceReportTemplate template = deviceReportTemplateService.getSortFirstTemplate();
        screeningOrgBindDeviceReportService.orgBindReportTemplate(template.getId(), screeningOrganization.getId(), screeningOrganization.getName());

        UsernameAndPasswordDTO usernameAndPasswordDTO = screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.PARENT_ACCOUNT, null);

        ScreeningOrganizationStaffQueryDTO screeningOrganizationStaffQueryDTO = new ScreeningOrganizationStaffQueryDTO();
        screeningOrganizationStaffQueryDTO.setScreeningOrgId(screeningOrganization.getId());
        screeningOrganizationStaffQueryDTO.setCreateUserId(user.getId());
        screeningOrganizationStaffQueryDTO.setGovDeptId(user.getOrgId());
        screeningOrganizationStaffQueryDTO.setType(ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL);
        screeningOrganizationStaffQueryDTO.setRealName(ScreeningOrganizationStaff.AUTO_CREATE_STAFF_DEFAULT_NAME);
        screeningOrganizationStaffQueryDTO.setUserName(usernameAndPasswordDTO.getUsername());
        screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaffQueryDTO);

        return usernameAndPasswordDTO;
    }

    /**
     * 获取筛查记录列表
     *
     * @param request 分页入参
     * @param orgId   机构ID
     * @return {@link IPage}
     */
    public IPage<ScreeningOrgPlanResponseDTO> getRecordLists(PageRequest request, Integer orgId) {

        // 获取筛查计划
        IPage<ScreeningOrgPlanResponseDTO> planPages = screeningPlanService.getPageByOrgId(request, orgId);
        List<ScreeningOrgPlanResponseDTO> tasks = planPages.getRecords();
        if (CollectionUtils.isEmpty(tasks)) {
            return planPages;
        }
        tasks.forEach(taskResponse -> extractedDTO(taskResponse, orgId));
        return planPages;
    }

    /**
     * 封装DTO
     *
     * @param planResponse 筛查端-记录详情
     * @param orgId        机构ID
     */
    private void extractedDTO(ScreeningOrgPlanResponseDTO planResponse, Integer orgId) {
        ScreeningRecordItems response = new ScreeningRecordItems();
        List<RecordDetails> details = new ArrayList<>();

        Integer planId = planResponse.getId();
        List<ScreeningPlanSchoolDTO> schoolVos = screeningPlanSchoolService.getSchoolVoListsByPlanId(planId, StringUtils.EMPTY);
        Map<Integer, ScreeningPlanSchoolDTO> schoolVoMaps = schoolVos.stream()
                .collect(Collectors.toMap(ScreeningPlanSchoolDTO::getSchoolId, Function.identity()));

        // 设置筛查状态
        planResponse.setScreeningStatus(screeningOrganizationService.getScreeningStatus(planResponse.getStartTime(), planResponse.getEndTime()));

        // 获取学校ID
        List<Integer> schoolIds = schoolVos.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        // 学生统计
        Map<Integer, Integer> planStudentMaps = schoolVos.stream()
                .collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchoolDTO::getStudentCount));

        // 设置学校总数
        response.setSchoolCount(schoolIds.size());

        // 学校名称
        List<School> schools = schoolService.getByIds(schoolIds);
        Map<Integer, School> schoolMaps = schools.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        List<Integer> createUserIds = visionScreeningResultService.getCreateUserIdByPlanId(planId, orgId);
        // 员工信息
        if (!CollectionUtils.isEmpty(createUserIds)) {
            List<User> userLists = oauthServiceClient.getUserBatchByIds(createUserIds);
            response.setStaffCount(createUserIds.size());
            response.setStaffName(userLists.stream().map(User::getRealName).collect(Collectors.toList()));
        } else {
            response.setStaffCount(0);
        }

        // 封装DTO
        schoolIds.forEach(schoolId -> {
            RecordDetails detail = new RecordDetails();
            detail.setSchoolId(schoolId);
            if (null != schoolMaps.get(schoolId)) {
                detail.setSchoolName(schoolMaps.get(schoolId).getName());
            }
            detail.setRealScreeningNumbers(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(schoolId, orgId, planId).size());
            detail.setPlanScreeningNumbers(planStudentMaps.get(schoolId));
            detail.setScreeningPlanId(planId);
            detail.setStartTime(planResponse.getStartTime());
            detail.setEndTime(planResponse.getEndTime());
            detail.setPlanTitle(planResponse.getTitle());
            detail.setQualityControllerName(schoolVoMaps.get(schoolId).getQualityControllerName());
            detail.setQualityControllerCommander(schoolVoMaps.get(schoolId).getQualityControllerCommander());
            detail.setHasRescreenReport(statRescreenService.hasRescreenReport(planId, schoolId));
            details.add(detail);
        });
        response.setDetails(details);
        planResponse.setItems(response);
    }

    /**
     * 更新筛查机构
     *
     * @param currentUser           当前登录用户
     * @param screeningOrganization 筛查机构实体咧
     * @return 筛查机构
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrgResponseDTO updateScreeningOrganization(CurrentUser currentUser, ScreeningOrganization screeningOrganization) {

        if (screeningOrganizationService.checkScreeningOrgName(screeningOrganization.getName(), screeningOrganization.getId())) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        ScreeningOrgResponseDTO response = new ScreeningOrgResponseDTO();
        ScreeningOrganization checkOrg = screeningOrganizationService.getById(screeningOrganization.getId());

        // 机构管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(screeningOrganization.getId());
        // 更新OAuth账号
        UserDTO userDTO = new UserDTO();
        userDTO.setId(admin.getUserId())
                .setPhone(screeningOrganization.getPhone())
                .setOrgId(screeningOrganization.getId())
                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode())
                .setUserType(UserType.SCREENING_ORGANIZATION_ADMIN.getType())
                .setRealName(screeningOrganization.getName());
        userDTO.setOrgConfigType(screeningOrganization.getConfigType());
        oauthServiceClient.updateUser(userDTO);
        // 更新筛查机构管理员用户名称
        if (StringUtils.isNotBlank(screeningOrganization.getName())) {
            oauthServiceClient.updateUserRealName(screeningOrganization.getName(), screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT.getCode(),
                    UserType.SCREENING_ORGANIZATION_ADMIN.getType());
        }

        // 名字更新
        if (!StringUtils.equals(checkOrg.getName(), screeningOrganization.getName())) {
            response.setUsername(screeningOrganization.getName());
        }

        screeningOrganizationService.updateById(screeningOrganization);
        ScreeningOrganization organization = screeningOrganizationService.getById(screeningOrganization.getId());
        // 同步到oauth机构状态
        if (Objects.nonNull(screeningOrganization.getStatus())) {
            oauthServiceClient.updateOrganization(new Organization(screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT,
                    UserType.SCREENING_ORGANIZATION_ADMIN, screeningOrganization.getStatus()));
        }

        BeanUtils.copyProperties(organization, response);
        response.setDistrictName(districtService.getDistrictName(organization.getDistrictDetail()));
        // 详细地址
        response.setAddressDetail(districtService.getAddressDetails(
                organization.getProvinceCode(), organization.getCityCode(), organization.getAreaCode(), organization.getTownCode(), organization.getAddress()));
        response.setScreeningTime(screeningOrganization.getScreeningTime())
                .setStaffCount(screeningOrganization.getStaffCount());
        // 是否能更新
        setCanUpdate(currentUser, response);
        return response;
    }

    /**
     * 设置是否能更新
     *
     * @param currentUser 当前用户
     * @param response    请求
     */
    private void setCanUpdate(CurrentUser currentUser, ScreeningOrgResponseDTO response) {
        if (currentUser.isPlatformAdminUser() || currentUser.isOverviewUser() || response.getCreateUserId().equals(currentUser.getId())) {
            response.setCanUpdate(true);
        }
    }

    /**
     * 获取筛查机构列表
     *
     * @param pageRequest 分页
     * @param query       筛查机构列表请求体
     * @param currentUser 当前登录用户
     * @return IPage<ScreeningOrgResponse> {@link IPage}
     */
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest,
                                                                       ScreeningOrganizationQueryDTO query,
                                                                       CurrentUser currentUser){
        Integer districtId = districtBizService.filterQueryDistrictId(currentUser, query.getDistrictId());
        // 查询
        IPage<ScreeningOrgResponseDTO> orgLists = screeningOrganizationService.getByCondition(pageRequest, query, districtId);

        // 为空直接返回
        List<ScreeningOrgResponseDTO> orgListsRecords = orgLists.getRecords();
        if (CollectionUtils.isEmpty(orgListsRecords)) {
            return orgLists;
        }
        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService
                .getOrgStaffMapByIds(orgListsRecords.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()),currentUser.isPlatformAdminUser() ? null : ScreeningOrganizationStaff.GENERAL_SCREENING_PERSONNEL);
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);

        // 筛查次数
        List<ScreeningPlan> planLists = screeningPlanService
                .getByOrgIds(orgListsRecords.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));
        Map<Integer, Long> orgPlanMaps = planLists.stream().collect(Collectors
                .groupingBy(ScreeningPlan::getScreeningOrgId, Collectors.counting()));
        // 封装DTO
        orgListsRecords.forEach(orgResponseDTO -> {
            // 同一部门才能更新
            setCanUpdate(currentUser, orgResponseDTO);
            // 筛查人员
            List<ScreeningOrganizationStaff> staffLists = staffMaps.get(orgResponseDTO.getId());
            if (!CollectionUtils.isEmpty(staffLists)) {
                orgResponseDTO.setStaffCount(staffLists.size());
            } else {
                orgResponseDTO.setStaffCount(0);
            }
            // 区域名字
            orgResponseDTO.setDistrictName(districtService.getDistrictName(orgResponseDTO.getDistrictDetail()));

            // 筛查次数
            orgResponseDTO.setScreeningTime(orgPlanMaps.getOrDefault(orgResponseDTO.getId(), 0L));
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(orgResponseDTO.getId()));

            // 详细地址
            orgResponseDTO.setAddressDetail(districtService.getAddressDetails(
                    orgResponseDTO.getProvinceCode(), orgResponseDTO.getCityCode(), orgResponseDTO.getAreaCode(), orgResponseDTO.getTownCode(), orgResponseDTO.getAddress()));
            Integer countCooperationHospital = orgCooperationHospitalService.countCooperationHospital(orgResponseDTO.getId());
            if (Objects.isNull(countCooperationHospital) || countCooperationHospital == 0) {
                orgResponseDTO.setCountCooperationHospital(0);
            } else {
                orgResponseDTO.setCountCooperationHospital(countCooperationHospital);
            }

        });
        return orgLists;
    }

    /**
     * 根据部门ID获取筛查机构列表（带是否已有任务）
     *
     * @param query 筛查机构列表请求体
     * @return List<ScreeningOrgResponse>
     */
    public List<ScreeningOrgResponseDTO> getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQueryDTO query) {
        Assert.notNull(query.getGovDeptId(), "部门id不能为空");
        query.setStatus(CommonConst.STATUS_NOT_DELETED);
        // 查询
        List<ScreeningOrganization> screeningOrganizationList = screeningOrganizationService.getBy(query);
        // 为空直接返回
        if (CollectionUtils.isEmpty(screeningOrganizationList)) {
            return Collections.emptyList();
        }
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        // 封装DTO
        return screeningOrganizationList.stream().map(organization -> {
            ScreeningOrgResponseDTO orgResponseDTO = new ScreeningOrgResponseDTO();
            BeanUtils.copyProperties(organization, orgResponseDTO);
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(organization.getId()));
            return orgResponseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据是否需要查询机构是否已有任务，返回时间段内已有任务的机构id
     *
     * @param query 条件
     * @return List<Integer>
     */
    private List<Integer> getHaveTaskOrgIds(ScreeningOrganizationQueryDTO query) {
        if (Objects.nonNull(query.getNeedCheckHaveTask()) && query.getNeedCheckHaveTask()) {
            return screeningTaskOrgService.getHaveTaskOrgIds(query.getGovDeptId(), query.getStartTime(), query.getEndTime());
        }
        return Collections.emptyList();
    }

    /**
     * 获取合作医院列表
     *
     * @param pageRequest    分页请求
     * @param screeningOrgId 筛查机构Id
     * @return IPage<CooperationHospitalDTO>
     */
    public IPage<CooperationHospitalDTO> getCooperationHospitalList(PageRequest pageRequest, Integer screeningOrgId) {

        // 筛查机构获取合作医院列表
        IPage<CooperationHospitalDTO> cooperationHospitalPage = orgCooperationHospitalService.getCooperationHospitalListByPage(pageRequest, screeningOrgId);
        List<CooperationHospitalDTO> cooperationHospitalList = cooperationHospitalPage.getRecords();
        if (CollectionUtils.isEmpty(cooperationHospitalList)) {
            return cooperationHospitalPage;
        }

        // 装换成<医院Id，是否置顶>
        Map<Integer, Integer> cooperationHospitalMap = cooperationHospitalList.stream()
                .collect(Collectors.toMap(CooperationHospitalDTO::getHospitalId, CooperationHospitalDTO::getIsTop));

        // 查询医院
        List<Integer> hospitalIds = cooperationHospitalList.stream()
                .map(CooperationHospitalDTO::getHospitalId).collect(Collectors.toList());
        List<Hospital> hospitalList = hospitalService.listByIds(hospitalIds);
        Map<Integer, Hospital> hospitalMap = hospitalList.stream()
                .collect(Collectors.toMap(Hospital::getId, Function.identity()));

        // 封装DTO
        cooperationHospitalList.forEach(cp -> {
            Integer id = cp.getId();
            Hospital hospital = hospitalMap.get(cp.getHospitalId());
            BeanUtils.copyProperties(hospital, cp);
            cp.setId(id);
            cp.setDistrictName(districtService.getDistrictName(hospital.getDistrictDetail()));
            cp.setAddressDetail(districtService.getAddressDetails(
                    hospital.getProvinceCode(), hospital.getCityCode(), hospital.getAreaCode(), hospital.getTownCode(), hospital.getAddress()));
            cp.setIsTop(cooperationHospitalMap.get(cp.getHospitalId()));
        });
        return cooperationHospitalPage;
    }


    /**
     * 筛查机构合作医院列表查询
     *
     * @param orgId 筛查机构ID
     * @param name  名称
     * @return IPage<HospitalResponseDTO>
     */
    public List<HospitalResponseDTO> getHospitalList(Integer orgId, String name) {
        // 筛查角色的只能看到全省
        ScreeningOrganizationAdmin orgAdmin = screeningOrganizationAdminService.getByOrgId(orgId);
        ScreeningOrganization org = screeningOrganizationService.getById(orgAdmin.getScreeningOrgId());
        Integer codePre = districtService.getDistrictPrefix(org.getDistrictId()).getSecond();

        List<HospitalResponseDTO> hospitalList = hospitalBizService.getHospitalByName(name, codePre);
        // 查询当前筛查机构下已经添加的合作医院
        List<OrgCooperationHospital> cooperationHospitalList = orgCooperationHospitalService.getCooperationHospitalList(orgId);
        if (CollectionUtils.isEmpty(cooperationHospitalList)) {
            return hospitalList;
        }
        List<Integer> hospitalIds = cooperationHospitalList.stream().map(OrgCooperationHospital::getHospitalId).collect(Collectors.toList());
        hospitalList.forEach(h -> {
            if (hospitalIds.contains(h.getId())) {
                h.setIsAdd(true);
            }
        });
        return hospitalList;
    }

    /**
     * 获取筛查机构的行政区域
     *
     * @param orgId 筛查机构Id
     * @return List<District>
     */
    public List<District> getDistrictTree(Integer orgId) {
        ScreeningOrganization organization = screeningOrganizationService.getById(orgId);
        District district = districtService.getById(organization.getDistrictId());
        return Collections.singletonList(districtService.getProvinceDistrictTreePriorityCache(district.getCode()));
    }

    /**
     * 添加账号
     *
     * @param screeningOrgId 筛查机构ID
     * @return UsernameAndPasswordDTO
     */
    public UsernameAndPasswordDTO addAccount(Integer screeningOrgId) {
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(screeningOrgId);
        if (Objects.isNull(screeningOrganization)) {
            throw new BusinessException("筛查机构异常");
        }
        // 获取该筛查机构已经有多少个账号
        List<ScreeningOrganizationAdmin> orgList = screeningOrganizationAdminService.getListOrgList(screeningOrgId);
        if (CollectionUtils.isEmpty(orgList)) {
            throw new BusinessException("数据异常");
        }
        ScreeningOrganizationAdmin screeningOrganizationAdmin = orgList.stream().sorted(Comparator.comparing(ScreeningOrganizationAdmin::getCreateTime)).collect(Collectors.toList()).get(0);
        String mainUsername = oauthServiceClient.getUserDetailByUserId(screeningOrganizationAdmin.getUserId()).getUsername();
        String username;
        if (orgList.size() < 10) {
            username = mainUsername + "0" + orgList.size();
        } else {
            username = mainUsername + orgList.size();
        }
        return screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.CHILD_ACCOUNT, username);
    }

    /**
     * 通过机构类型获取权限
     *
     * @param configType 配置
     * @return List<String>
     */
    public List<String> getPermissionByConfigType(Integer configType) {
        return oauthServiceClient.getPermissionByConfigType(configType);
    }
}
