package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolBizService;
import com.wupol.myopia.business.api.management.domain.ScreeningOrganizationDTO;
import com.wupol.myopia.business.api.management.domain.builder.ScreeningOrgBizBuilder;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningSchoolOrgVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.dto.DeviceGrantedDTO;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.model.ScreeningConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.dto.ConfigurationReportRequestDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import com.wupol.myopia.business.core.device.domain.vo.DeviceReportTemplateVO;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.OrgCooperationHospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.core.screening.flow.facade.VisionScreeningResultFacade;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
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
import java.math.BigDecimal;
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
    private ScreeningPlanSchoolBizService screeningPlanSchoolBizService;
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

    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private UserQuestionRecordService userQuestionRecordService;

    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private DeviceService deviceService;

    @Autowired
    private VisionScreeningResultFacade visionScreeningResultFacade;


    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveScreeningOrganization(ScreeningOrganizationDTO screeningOrganization, CurrentUser user) {
        String name = screeningOrganization.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名字不能为空");
        }
        checkTemplateId(screeningOrganization);
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

        UsernameAndPasswordDTO usernameAndPasswordDTO = screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.PARENT_ACCOUNT, null);

        ScreeningOrganizationStaffQueryDTO screeningOrganizationStaffQueryDTO = new ScreeningOrganizationStaffQueryDTO();
        screeningOrganizationStaffQueryDTO.setScreeningOrgId(screeningOrganization.getId());
        screeningOrganizationStaffQueryDTO.setCreateUserId(user.getId());
        screeningOrganizationStaffQueryDTO.setGovDeptId(user.getOrgId());
        screeningOrganizationStaffQueryDTO.setType(ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL);
        screeningOrganizationStaffQueryDTO.setRealName(ScreeningOrganizationStaff.AUTO_CREATE_STAFF_DEFAULT_NAME);
        screeningOrganizationStaffQueryDTO.setUserName(usernameAndPasswordDTO.getUsername());
        screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaffQueryDTO);

        // 筛查机构绑定模板(保存)
        if (screeningOrganization.getTemplateId()!=null){
            screeningOrgBindDeviceReportService.orgBindReportTemplate(screeningOrganization.getTemplateId(), screeningOrganization.getId(), screeningOrganization.getName());
        }

        return usernameAndPasswordDTO;
    }

    /**
     * 获取筛查记录列表
     *
     * @param request 分页入参
     * @param orgId   机构ID
     * @param currentUser   当前登录用户
     * @return {@link IPage}
     */
    public IPage<ScreeningOrgPlanResponseDTO> getRecordLists(PageRequest request, Integer orgId, CurrentUser currentUser) {
        // 获取筛查计划
        IPage<ScreeningOrgPlanResponseDTO> planPages = screeningPlanService.getPageByOrgId(request, orgId, !currentUser.isPlatformAdminUser(), ScreeningOrgTypeEnum.ORG.getType());
        List<ScreeningOrgPlanResponseDTO> planRecords = planPages.getRecords();
        if (CollectionUtils.isEmpty(planRecords)) {
            return planPages;
        }
        ScreeningOrganization org = screeningOrganizationService.getById(orgId);
        planRecords.forEach(plan -> {
            plan.setScreeningStatus(ScreeningOrganizationService.getScreeningStatus(plan.getStartTime(), plan.getEndTime(), plan.getReleaseStatus()));
            plan.setIsCanLink(Objects.equals(org.getConfigType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_0.getType())
                    && Objects.equals(plan.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType())
                    && Objects.equals(plan.getScreeningType(), ScreeningTypeEnum.VISION.getType())
                    && Objects.equals(plan.getScreeningTaskId(), 0));
        });
        return planPages;
    }

    /**
     * 组装复测数据
     *
     * @param detail
     * @param schoolId
     * @param reviewCountMap
     * @return
     */
    private RecordDetails buildReScreening(RecordDetails detail, Integer schoolId,
                                           Map<Integer, Integer> reviewCountMap, Map<Integer, Integer> reScreeningCountMap){
        detail.setHasRescreenReport(reScreeningCountMap.getOrDefault(schoolId, CommonConst.ZERO) > 0);
        detail.setRescreenNum(Optional.ofNullable(reviewCountMap).map(x -> x.getOrDefault(schoolId, CommonConst.ZERO)).orElse(CommonConst.ZERO));
        detail.setRescreenRatio(MathUtil.ratio(detail.getRescreenNum(),detail.getRealScreeningNumbers()));
        detail.setRealScreeningRatio(MathUtil.ratio(detail.getRealScreeningNumbers(),detail.getPlanScreeningNumbers()));
        return detail;
    }

    /**
     * 组装问卷相关
     *
     * @param detail
     * @param schoolMap
     * @param schoolId
     * @return
     */
    private RecordDetails buildQuestion(RecordDetails detail,
                                        Map<Integer, List<UserQuestionRecord>> schoolMap,
                                        Integer schoolId,
                                        Map<Integer, List<ScreeningPlanSchoolStudent>> userGradeIdMap,
                                        Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap) {
        Map<Integer, List<UserQuestionRecord>> schoolStudentMap = CollectionUtils
                .isEmpty(schoolMap.get(schoolId)) ? Maps.newHashMap() : schoolMap.get(schoolId).stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));
        if (detail.getPlanScreeningNumbers() == 0) {
            detail.setQuestionnaire(CommonConst.PERCENT_ZERO);
            detail.setQuestionnaireStudentCount(0);
        } else {
            detail.setQuestionnaireStudentCount(schoolStudentMap.keySet().size());
            BigDecimal questionNum = MathUtil.divide(schoolStudentMap.keySet().size(), detail.getPlanScreeningNumbers());
            detail.setQuestionnaire( questionNum.equals(BigDecimal.ZERO) ? CommonConst.PERCENT_ZERO : questionNum.toString() + "%");
        }
        if (!CollectionUtils.isEmpty(gradeIdMap.get(schoolId)) && detail.getPlanScreeningNumbers() != 0) {
            detail.setGradeQuestionnaireInfos(GradeQuestionnaireInfo.buildGradeInfo(schoolId, gradeIdMap, userGradeIdMap,Boolean.TRUE));
        }
        return detail;
    }

    /**
     * 验证模板ID
     * @param screeningOrganization 机构扩展类
     */
    private void checkTemplateId(ScreeningOrganizationDTO screeningOrganization){
        if (Objects.equals(screeningOrganization.getConfigType(), ScreeningOrgConfigTypeEnum.CONFIG_TYPE_2.getType())
                || Objects.equals(screeningOrganization.getConfigType(),ScreeningOrgConfigTypeEnum.CONFIG_TYPE_3.getType())){
            if(screeningOrganization.getTemplateId()==null)
                Assert.notNull(screeningOrganization.getTemplateId(), "请输入模板ID！");
        }
    }

    /**
     * 更新筛查机构
     *
     * @param currentUser           当前登录用户
     * @param screeningOrganization 筛查机构实体咧
     * @return 筛查机构
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrgResponseDTO updateScreeningOrganization(CurrentUser currentUser, ScreeningOrganizationDTO screeningOrganization) {
        checkTemplateId(screeningOrganization);
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


        // 筛查机构绑定模板(更新)
        if (screeningOrganization.getTemplateId()!=null){
            ConfigurationReportRequestDTO configurationReportRequestDTO = new ConfigurationReportRequestDTO();
            configurationReportRequestDTO.setScreeningOrgId(screeningOrganization.getId());
            configurationReportRequestDTO.setTemplateId(screeningOrganization.getTemplateId());
            screeningOrgBindDeviceReportService.configurationReport(configurationReportRequestDTO);
        }

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
        List<Integer> orgIdList = orgListsRecords.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
        Map<Integer, Integer> staffCountMap = screeningOrganizationStaffService.countByOrgIds(orgIdList, currentUser.isPlatformAdminUser() ? null : ScreeningOrganizationStaff.GENERAL_SCREENING_PERSONNEL);
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        // 筛查次数
        List<ScreeningPlan> planLists = screeningPlanService.getReleasePlanByOrgIds(orgIdList, ScreeningOrgTypeEnum.ORG.getType());
        Map<Integer, Long> orgPlanMaps = planLists.stream().collect(Collectors.groupingBy(ScreeningPlan::getScreeningOrgId, Collectors.counting()));
        // 合作医院
        Map<Integer, Integer> screeningOrgCooperationHospitalCountMap = orgCooperationHospitalService.countByScreeningOrgIdList(orgIdList);


        // 获取报告模板ID
        Map<Integer, Integer> templateMap = screeningOrgBindDeviceReportService.getAllByOrgIds(orgIdList).stream()
                .collect(Collectors.toMap(ScreeningOrgBindDeviceReport::getScreeningOrgId, ScreeningOrgBindDeviceReport::getTemplateId));

        // 封装DTO
        orgListsRecords.forEach(orgResponseDTO -> {
            // 同一部门才能更新
            setCanUpdate(currentUser, orgResponseDTO);
            // 筛查人员
            orgResponseDTO.setStaffCount(staffCountMap.getOrDefault(orgResponseDTO.getId(), CommonConst.ZERO));
            // 区域名字
            orgResponseDTO.setDistrictName(districtService.getDistrictName(orgResponseDTO.getDistrictDetail()));
            // 筛查次数
            orgResponseDTO.setScreeningTime(orgPlanMaps.getOrDefault(orgResponseDTO.getId(), CommonConst.ZERO_L));
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(orgResponseDTO.getId()));
            // 详细地址
            orgResponseDTO.setAddressDetail(districtService.getAddressDetails(
                    orgResponseDTO.getProvinceCode(), orgResponseDTO.getCityCode(), orgResponseDTO.getAreaCode(), orgResponseDTO.getTownCode(), orgResponseDTO.getAddress()));
            // 合作医院
            orgResponseDTO.setCountCooperationHospital(screeningOrgCooperationHospitalCountMap.getOrDefault(orgResponseDTO.getId(), CommonConst.ZERO));
            // 对应模板ID
            orgResponseDTO.setTemplateId(templateMap.get(orgResponseDTO.getId()));

        });
        return orgLists;
    }

    /**
     * 获取筛查机构列表(下拉框)
     *
     * @param pageRequest 分页
     * @param query       筛查机构列表请求体
     * @return IPage<ScreeningOrgResponse> {@link IPage}
     */
    public IPage<ScreeningSchoolOrgVO> getOrgList(PageRequest pageRequest,
                                               ScreeningOrganizationQueryDTO query){

        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(query.getDistrictId());
        TwoTuple<Date, Date> startAndEndTime = getStartAndEndTime(query);

        // 查询
        IPage<ScreeningOrganization> orgPage = screeningOrganizationService.listByCondition(pageRequest, query, districtIds,startAndEndTime.getFirst(),startAndEndTime.getSecond());

        IPage<ScreeningSchoolOrgVO> screeningOrgResponsePage = new Page<>(orgPage.getCurrent(),orgPage.getSize(),orgPage.getTotal());
        // 为空直接返回
        List<ScreeningOrganization> records = orgPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return screeningOrgResponsePage;
        }
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);

        List<ScreeningSchoolOrgVO> orgResponseDTOList = records.stream()
                .map(screeningOrganization -> ScreeningOrgBizBuilder.getScreeningSchoolOrgVO(haveTaskOrgIds,screeningOrganization.getId(),screeningOrganization.getName(),screeningOrganization.getPhone()))
                .collect(Collectors.toList());
        screeningOrgResponsePage.setRecords(orgResponseDTOList);
        return screeningOrgResponsePage;
    }

    private TwoTuple<Date,Date> getStartAndEndTime(ScreeningOrganizationQueryDTO query){
        TwoTuple<Date,Date> tuple = TwoTuple.of(null, null);
        if (Objects.nonNull(query.getStartTime()) && Objects.nonNull(query.getEndTime())){
            Date startTime = DateUtil.parse(query.getStartTime().toString()+" 00:00:00", DatePattern.NORM_DATETIME_PATTERN);
            Date endTime = DateUtil.parse(query.getStartTime().toString()+" 23:59:59", DatePattern.NORM_DATETIME_PATTERN);
            tuple.setFirst(startTime);
            tuple.setSecond(endTime);
        }
        return tuple;
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
        if (Objects.nonNull(query.getNeedCheckHaveTask()) && Objects.equals(query.getNeedCheckHaveTask(),Boolean.TRUE)) {
            List<ScreeningTaskOrgDTO> haveTaskOrgIds = screeningTaskOrgService.getHaveTaskOrgIds(query.getGovDeptId(), query.getStartTime(), query.getEndTime());
            return haveTaskOrgIds.stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().collect(Collectors.toList());

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

    /**
     * 获取筛查计划的学校信息
     * @param screeningPlanId 筛查计划ID
     */
    public ScreeningRecordItems getRecordSchoolInfo(Integer screeningPlanId) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        List<Integer> dataSubmitConfig = Optional.ofNullable(screeningOrganizationService.getOrgById(screeningPlan.getScreeningOrgId())).map(ScreeningOrganization::getDataSubmitConfig).orElse(new ArrayList<>());

        ScreeningRecordItems screeningRecordItems = new ScreeningRecordItems();
        List<RecordDetails> details = Lists.newArrayList();

        List<ScreeningPlanSchoolDTO> schoolVos = screeningPlanSchoolBizService.getSchoolVoListsByPlanId(screeningPlanId, StringUtils.EMPTY);
        if (CollUtil.isEmpty(schoolVos)){
            screeningRecordItems.setSchoolCount(0);
            screeningRecordItems.setStaffCount(0);
            screeningRecordItems.setDetails(details);
            return screeningRecordItems;
        }
        List<Integer> schoolIds = schoolVos.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());

        // 设置学校总数
        screeningRecordItems.setSchoolCount(schoolIds.size());

        // 筛查人员名单
        List<Integer> createUserIds = visionScreeningResultService.getCreateUserIdByPlanId(screeningPlanId);
        if (!CollectionUtils.isEmpty(createUserIds)) {
            List<User> userLists = oauthServiceClient.getUserBatchByIds(createUserIds);
            screeningRecordItems.setStaffCount(createUserIds.size());
            screeningRecordItems.setStaffName(userLists.stream().map(User::getRealName).collect(Collectors.toList()));
        } else {
            screeningRecordItems.setStaffCount(0);
        }

        // 学校名称
        List<School> schools = schoolService.getSchoolByIds(schoolIds);
        Map<Integer, School> schoolMaps = schools.stream().collect(Collectors.toMap(School::getId, Function.identity()));
        // 计划筛查学生数
        Map<Integer, Integer> planStudentCountMap = schoolVos.stream().collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchoolDTO::getStudentCount));

        // 调查问卷数据
        List<UserQuestionRecord> userQuestionRecords = userQuestionRecordService.findRecordByPlanIdAndUserType(Lists.newArrayList(screeningPlanId), QuestionnaireUserType.STUDENT.getType(), QuestionnaireStatusEnum.FINISH.getCode());
        Map<Integer, List<UserQuestionRecord>> schoolMap =userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        Set<Integer> planStudentIds = userQuestionRecords.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        Map<Integer, List<ScreeningPlanSchoolStudent>> userGradeIdMap = CollectionUtils.isEmpty(planStudentIds) ? Maps.newHashMap() : screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds)).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));

        Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap = schoolGradeService.getBySchoolIds(schoolIds).stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));
        Map<Integer, ScreeningPlanSchoolDTO> schoolVoMaps = schoolVos.stream().collect(Collectors.toMap(ScreeningPlanSchoolDTO::getSchoolId, Function.identity()));

        // 筛查数据
        Map<Integer, Integer> reviewCountMap = statConclusionService.getReviewCountMap(screeningPlanId, schoolIds);
        Map<Integer, Integer> screeningResultCountMap = visionScreeningResultFacade.getScreeningResultCountMap(screeningPlan, new HashSet<>(schoolIds));

        // 复测
        Map<Integer, Integer> reScreeningCountMap = statRescreenService.getScreeningResultCountMap(screeningPlanId, new HashSet<>(schoolIds));

        // 封装detail
        schoolIds.forEach(schoolId -> buildRecordDetails(screeningPlanId, screeningPlan, details, schoolMaps, planStudentCountMap, schoolMap, userGradeIdMap, gradeIdMap, schoolVoMaps, reviewCountMap, screeningResultCountMap, schoolId, reScreeningCountMap, dataSubmitConfig));
        screeningRecordItems.setDetails(details);
        return screeningRecordItems;
    }

    /**
     * 构建记录详情
     */
    private void buildRecordDetails(Integer screeningPlanId, ScreeningPlan screeningPlan, List<RecordDetails> details,
                                    Map<Integer, School> schoolMaps, Map<Integer, Integer> planStudentCountMap,
                                    Map<Integer, List<UserQuestionRecord>> schoolMap, Map<Integer, List<ScreeningPlanSchoolStudent>> userGradeIdMap,
                                    Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap, Map<Integer, ScreeningPlanSchoolDTO> schoolVoMaps,
                                    Map<Integer, Integer> reviewCountMap, Map<Integer, Integer> schoolScreeningResultMap, Integer schoolId, Map<Integer, Integer> reScreeningCountMap,
                                    List<Integer> dataSubmitConfig) {
        RecordDetails detail = new RecordDetails();
        detail.setSchoolId(schoolId);
        Optional.ofNullable(schoolMaps.get(schoolId)).ifPresent(school -> detail.setSchoolName(school.getName()));
        detail.setDataSubmitConfig(dataSubmitConfig);
        detail.setRealScreeningNumbers(schoolScreeningResultMap.getOrDefault(schoolId, CommonConst.ZERO));
        detail.setPlanScreeningNumbers(planStudentCountMap.get(schoolId));
        detail.setScreeningPlanId(screeningPlanId);
        detail.setStartTime(screeningPlan.getStartTime());
        detail.setEndTime(screeningPlan.getEndTime());
        detail.setPlanTitle(screeningPlan.getTitle());
        buildQuestion(detail, schoolMap, schoolId, userGradeIdMap, gradeIdMap);
        detail.setQualityControllerName(schoolVoMaps.get(schoolId).getQualityControllerName());
        detail.setQualityControllerCommander(schoolVoMaps.get(schoolId).getQualityControllerCommander());
        buildReScreening(detail, schoolId, reviewCountMap, reScreeningCountMap);
        details.add(detail);
    }

    /**
     * 获取筛查机构信息
     *
     * @param id id
     *
     * @return ScreeningOrgResponseDTO
     */
    public ScreeningOrgResponseDTO getScreeningOrgDetails(Integer id) {
        ScreeningOrgResponseDTO screeningOrgDetails = screeningOrganizationService.getScreeningOrgDetails(id);
        setGrantedDevice(screeningOrgDetails);
        return screeningOrgDetails;
    }

    /**
     * 设置已授权的设备列表
     *
     * @param screeningOrgDetails 筛查机构返回体
     */
    private void setGrantedDevice(ScreeningOrgResponseDTO screeningOrgDetails) {
        ScreeningConfig screeningConfig = screeningOrgDetails.getScreeningConfig();
        if (Objects.isNull(screeningConfig)) {
            screeningConfig = new ScreeningConfig();
        }
        List<Device> deviceList = deviceService.getByOrgIdAndOrgType(screeningOrgDetails.getId(), OrgTypeEnum.SCREENING.getCode());
        if (CollectionUtils.isEmpty(deviceList)) {
            return;
        }

        screeningConfig.setGrantedDeviceList(deviceList.stream().map(s->{
            DeviceGrantedDTO grantedDTO = new DeviceGrantedDTO();
            grantedDTO.setDeviceSn(s.getDeviceSn());
            grantedDTO.setType(s.getType());
            grantedDTO.setBluetoothMac(s.getBluetoothMac());
            grantedDTO.setStatus(s.getStatus());
            return grantedDTO;
        }).collect(Collectors.toList()));
        screeningOrgDetails.setScreeningConfig(screeningConfig);
    }
}