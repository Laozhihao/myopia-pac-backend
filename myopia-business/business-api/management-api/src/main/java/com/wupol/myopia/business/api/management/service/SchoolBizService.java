package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.builder.ScreeningOrgBizBuilder;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningSchoolOrgVO;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.ScreeningSchoolOrgDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.service.SchoolAdminService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolVisionStatisticItem;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校
 *
 * @author Simple4H
 */
@Service
public class SchoolBizService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolAdminService schoolAdminService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private StudentService studentService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private StatRescreenService statRescreenService;
    @Autowired
    private ScreeningResultStatisticService screeningResultStatisticService;
    @Autowired
    private OverviewService overviewService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;

    /**
     * 根据层级Id获取学校列表（带是否有计划字段）
     *
     * @param schoolQueryDTO 条件
     * @return List<SchoolResponseDTO>
     */
    public List<SchoolResponseDTO> getSchoolListByDistrictId(SchoolQueryDTO schoolQueryDTO) {
        Assert.notNull(schoolQueryDTO.getDistrictId(), "层级id不能为空");
        schoolQueryDTO.setDistrictIds(districtService.getProvinceAllDistrictIds(schoolQueryDTO.getDistrictId())).setDistrictId(null);
        // 查询
        List<School> schoolList = schoolService.getBy(schoolQueryDTO);
        // 为空直接返回
        if (CollectionUtils.isEmpty(schoolList)) {
            return Collections.emptyList();
        }
        // 获取已有计划的学校ID列表
        List<Integer> havePlanSchoolIds = getHavePlanSchoolIds(schoolQueryDTO);
        // 封装DTO
        return schoolList.stream().map(school -> {
            SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
            BeanUtils.copyProperties(school, schoolResponseDTO);
            schoolResponseDTO.setAlreadyHavePlan(havePlanSchoolIds.contains(school.getId()));
            return schoolResponseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取学校的筛查记录列表
     *
     * @param pageRequest 通用分页
     * @param schoolId    学校ID
     * @return {@link IPage}
     */
    public IPage<ScreeningPlanResponseDTO> getScreeningRecordLists(PageRequest pageRequest, Integer schoolId, CurrentUser currentUser) {

        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(planSchoolList)) {
            return new Page<>();
        }

        // 通过planIds查询计划
        IPage<ScreeningPlanResponseDTO> planPages = screeningPlanService
                .getListByIds(pageRequest, planSchoolList.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toList()), !currentUser.isPlatformAdminUser());

        List<ScreeningPlanResponseDTO> plans = planPages.getRecords();

        if (!CollectionUtils.isEmpty(plans)) {
            Set<Integer> planIds = plans.stream().map(ScreeningPlanResponseDTO::getId).collect(Collectors.toSet());
            // 学校统计信息
            List<ScreeningResultStatistic> screeningResultStatisticList = screeningResultStatisticService.getByPlanIdsAndSchoolId(Lists.newArrayList(planIds),schoolId);
            Map<Integer, List<ScreeningResultStatistic>> statisticMaps = screeningResultStatisticList
                    .stream()
                    .collect(Collectors.groupingBy(ScreeningResultStatistic::getScreeningPlanId));

            // 获取筛查机构
            List<Integer> orgIds = plans.stream()
                    .map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toList());
            // 机构
            List<ScreeningOrganization> orgLists = screeningOrganizationService.getByIds(orgIds);
            Map<Integer, String> orgMaps = orgLists.stream()
                    .collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));
            // 学校
            List<School> schoolList = schoolService.getByIds(orgIds);
            Map<Integer, String> schoolNameList = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));

            // 获取计划对应的学校信息
            Map<Integer, ScreeningPlanSchool> planSchoolMap = planSchoolList.stream().collect(Collectors.toMap(ScreeningPlanSchool::getScreeningPlanId, Function.identity()));

            // 封装DTO
            plans.forEach(plan -> {
                if (Objects.equals(plan.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType())) {
                    plan.setOrgName(orgMaps.get(plan.getScreeningOrgId()));
                } else if (Objects.equals(plan.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType())) {
                    plan.setOrgName(schoolNameList.get(plan.getScreeningOrgId()));
                }
                List<ScreeningResultStatistic> screeningResultStatistics = statisticMaps.get(plan.getId());
                if (CollUtil.isEmpty(screeningResultStatistics)) {
                    plan.setItems(new ArrayList<>());
                } else {
                    SchoolVisionStatisticItem item = new SchoolVisionStatisticItem();
                    ScreeningPlanSchool screeningPlanSchool = planSchoolMap.get(plan.getId());
                    ScreeningResultStatistic screeningResultStatistic = screeningResultStatistics.get(0);
                    BeanUtils.copyProperties(screeningResultStatistic, item);
                    item.setHasRescreenReport(statRescreenService.hasRescreenReport(plan.getId(), screeningResultStatistic.getSchoolId()));
                    item.setQualityControllerName(screeningPlanSchool.getQualityControllerName());
                    item.setQualityControllerCommander(screeningPlanSchool.getQualityControllerCommander());
                    plan.setItems(Lists.newArrayList(item));
                }
            });
        }
        return planPages;
    }

    /**
     * 根据是否需要查询学校是否已有计划，返回时间段内已有计划的学校id
     *
     * @param query 条件
     * @return List<Integer>
     */
    private List<Integer> getHavePlanSchoolIds(SchoolQueryDTO query) {
        if (Objects.nonNull(query.getNeedCheckHavePlan()) && Objects.equals(query.getNeedCheckHavePlan(),Boolean.TRUE)) {
            return screeningPlanSchoolService.getHavePlanSchoolIds(query.getDistrictIds(), null, query.getScreeningOrgId(), query.getStartTime(), query.getEndTime(),query.getScreeningType());
        }
        return Collections.emptyList();
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest    分页
     * @param schoolQueryDTO 请求体
     * @param currentUser    当前用户
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolResponseDTO> getSchoolList(PageRequest pageRequest, SchoolQueryDTO schoolQueryDTO,
                                                  CurrentUser currentUser) {

        String createUser = schoolQueryDTO.getCreateUser();
        List<Integer> userIds = new ArrayList<>();

        if (Objects.equals(schoolQueryDTO.getAllProvince(), Boolean.FALSE)
                && currentUser.isOverviewUser()
                && CollectionUtils.isEmpty(overviewService.getBindSchool(currentUser.getOrgId()))) {
            return new Page<>();
        }

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTO query = new UserDTO();
            query.setRealName(createUser);
            List<User> userListPage = oauthServiceClient.getUserListByName(query);
            if (!CollectionUtils.isEmpty(userListPage)) {
                userIds = userListPage.stream().map(User::getId).collect(Collectors.toList());
            }
        }
        TwoTuple<Integer, Integer> resultDistrictId = packageSearchList(currentUser, schoolQueryDTO.getDistrictId(), schoolQueryDTO.getAllProvince());
        setSchoolQueryDTO(currentUser, schoolQueryDTO);

        // 查询
        IPage<SchoolResponseDTO> schoolDtoIPage = schoolService.getSchoolListByCondition(pageRequest,
                schoolQueryDTO, resultDistrictId.getFirst(), userIds, resultDistrictId.getSecond());

        List<SchoolResponseDTO> schools = schoolDtoIPage.getRecords();
        List<Integer> schoolIdList = schools.stream().map(School::getId).collect(Collectors.toList());

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }
        // 获取创建人的名字
        List<Integer> createUserIds = schools.stream().map(School::getCreateUserId).collect(Collectors.toList());
        List<User> userLists = oauthServiceClient.getUserBatchByIds(createUserIds);
        Map<Integer, User> userDTOMap = userLists.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 学生统计
        List<StudentCountDTO> studentCountDTOS = studentService.countStudentBySchoolId(schoolIdList);
        Map<Integer, Integer> studentCountMaps = studentCountDTOS.stream()
                .collect(Collectors.toMap(StudentCountDTO::getSchoolId, StudentCountDTO::getCount));

        // 学校筛查次数
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolIds(schoolIdList);
        Map<Integer, Long> planSchoolMaps = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getSchoolId, Collectors.counting()));

        // 封装DTO
        schools.forEach(getSchoolDtoConsumer(currentUser, userDTOMap, studentCountMaps, planSchoolMaps));
        return schoolDtoIPage;
    }

    /**
     * 行政区域搜索条件
     *
     * @param currentUser 当前用户
     * @param districtId  行政区域ID
     * @return TwoTuple<Integer, Integer>
     */
    private TwoTuple<Integer, Integer> packageSearchList(CurrentUser currentUser, Integer districtId, Boolean allProvince) {
        // 管理员看到全部
        if (currentUser.isPlatformAdminUser()) {
            // 不为空说明是搜索条件
            if (null != districtId) {
                return new TwoTuple<>(districtId, null);
            }
        } else if (currentUser.isScreeningUser() || (currentUser.isHospitalUser() && (Objects.nonNull(currentUser.getScreeningOrgId())))) {
            return getScreeningDistrict(currentUser.getScreeningOrgId(), districtId, allProvince);
        } else if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            return districtService.getDistrictPrefix(govDept.getDistrictId());
        } else if (currentUser.isOverviewUser()) {
            return getOverviewDistrict(currentUser.getOrgId(), districtId, allProvince);
        }
        return new TwoTuple<>(districtId, null);
    }

    /**
     * 总揽账号行政区域
     *
     * @param orgId       总揽账号Id
     * @param districtId  区域Id
     * @param allProvince 是否全省
     *
     * @return TwoTuple<Integer, Integer>
     */
    private TwoTuple<Integer, Integer> getOverviewDistrict(Integer orgId, Integer districtId,
                                                           Boolean allProvince) {
        if (Objects.nonNull(districtId)) {
            // 不为空说明是搜索条件
            return new TwoTuple<>(districtId, null);
        }
        if (Objects.equals(allProvince, Boolean.TRUE)) {
            Overview overview = overviewService.getById(orgId);
            return districtService.getDistrictPrefix(overview.getDistrictId());
        } else {
            return new TwoTuple<>(null, null);
        }
    }

    private TwoTuple<Integer, Integer> getScreeningDistrict(Integer screeningOrgId, Integer districtId,
                                                            Boolean allProvince) {
        if (Objects.nonNull(districtId)) {
            // 不为空说明是搜索条件
            return new TwoTuple<>(districtId, null);
        }

        if (Objects.equals(allProvince, Boolean.TRUE)) {
            ScreeningOrganization org = screeningOrganizationService.getById(screeningOrgId);
            return districtService.getDistrictPrefix(org.getDistrictId());
        } else {
            return new TwoTuple<>(null, null);
        }
    }

    /**
     * 封装DTO
     *
     * @param currentUser      当前登录用户
     * @param userMap          用户信息
     * @param studentCountMaps 学生统计
     * @param planSchoolMaps   学校筛查统计
     * @return Consumer<SchoolDto>
     */
    private Consumer<SchoolResponseDTO> getSchoolDtoConsumer(CurrentUser currentUser, Map<Integer, User> userMap,
                                                             Map<Integer, Integer> studentCountMaps, Map<Integer, Long> planSchoolMaps) {
        return school -> {
            // 创建人
            school.setCreateUser(userMap.get(school.getCreateUserId()).getRealName());

            // 判断是否能更新
            if (currentUser.isHospitalUser()) {
                school.setCanUpdate(school.getGovDeptId().equals(currentUser.getScreeningOrgId()));
            } else {
                school.setCanUpdate(school.getGovDeptId().equals(currentUser.getOrgId()));
            }

            // 行政区名字
            school.setDistrictName(districtService.getDistrictName(school.getDistrictDetail()));

            // 筛查次数
            school.setScreeningCount(planSchoolMaps.getOrDefault(school.getId(), 0L));

            // 学生统计
            school.setStudentCount(studentCountMaps.getOrDefault(school.getId(), 0));

            // 详细地址
            school.setAddressDetail(districtService.getAddressDetails(
                    school.getProvinceCode(), school.getCityCode(), school.getAreaCode(), school.getTownCode(), school.getAddress()));
        };
    }

    public Set<Integer> getAllSchoolDistrictIdsByScreeningPlanIds(List<Integer> screeningPlanIds) {
        if (CollectionUtils.isEmpty(screeningPlanIds)) {
            return Collections.emptySet();
        }
        Set<Integer> schoolIds = screeningPlanSchoolService.getSchoolIdsByPlanIds(screeningPlanIds);
        return schoolService.getAllSchoolDistrictIdsBySchoolIds(schoolIds);
    }

    /**
     * 学校管理员用户账号列表
     *
     * @param schoolId 学校Id
     * @return List<OrgAccountListDTO>
     */
    public List<OrgAccountListDTO> getAccountList(Integer schoolId) {
        List<OrgAccountListDTO> accountList = new LinkedList<>();
        List<SchoolAdmin> schoolAdminList = schoolAdminService.findByList(new SchoolAdmin().setSchoolId(schoolId));
        if (CollectionUtils.isEmpty(schoolAdminList)) {
            return accountList;
        }
        List<Integer> userIds = schoolAdminList.stream().map(SchoolAdmin::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        schoolAdminList.forEach(adminUser -> {
            User user = userMap.get(adminUser.getUserId());
            OrgAccountListDTO account = new OrgAccountListDTO();
            account.setUserId(adminUser.getUserId());
            account.setOrgId(schoolId);
            account.setUsername(user.getUsername());
            account.setStatus(user.getStatus());
            accountList.add(account);
        });
        return accountList;
    }

    /**
     * 添加学校管理员账号账号
     *
     * @param schoolId 学校ID
     * @return UsernameAndPasswordDTO
     */
    public UsernameAndPasswordDTO addSchoolAdminUserAccount(Integer schoolId) {
        School school = schoolService.getById(schoolId);
        if (Objects.isNull(school)) {
            throw new BusinessException("不存在该学校");
        }
        // 获取该筛查机构已经有多少个账号
        List<SchoolAdmin> adminList = schoolAdminService.findByList(new SchoolAdmin().setSchoolId(schoolId));
        if (CollectionUtils.isEmpty(adminList)) {
            throw new BusinessException("数据异常，无主账号");
        }
        school.setName(school.getName() + "0" + adminList.size());

        SchoolAdmin schoolAdmin = adminList.stream().sorted(Comparator.comparing(SchoolAdmin::getCreateTime)).collect(Collectors.toList()).get(0);
        String mainUsername = oauthServiceClient.getUserDetailByUserId(schoolAdmin.getUserId()).getUsername();
        String username;
        if (adminList.size() < 10) {
            username = mainUsername + "0" + adminList.size();
        } else {
            username = mainUsername + adminList.size();
        }
        return schoolService.generateAccountAndPassword(school, username);
    }

    /**
     * 设置条件
     *
     * @param currentUser    当前登录用户
     * @param schoolQueryDTO 条件
     */
    private void setSchoolQueryDTO(CurrentUser currentUser, SchoolQueryDTO schoolQueryDTO) {

        if (currentUser.isOverviewUser()) {
            if (Objects.equals(schoolQueryDTO.getAllProvince(), Boolean.FALSE)) {
                schoolQueryDTO.setSchoolIds(overviewService.getBindSchool(currentUser.getOrgId()));
                schoolQueryDTO.setIsOverviewUser(Boolean.TRUE);
            }
            return;
        }

        if (currentUser.isPlatformAdminUser()) {
            return;
        }
        if (Objects.equals(schoolQueryDTO.getAllProvince(), Boolean.TRUE)) {
            return;
        }

        if (!currentUser.isScreeningUser()) {
            return;
        }

        // 自己创建的学校
        schoolQueryDTO.setCreateByUserId(currentUser.getId());

        // 自己筛查的学校
        List<ScreeningPlanSchool> planSchools = screeningPlanSchoolService.getByOrgId(currentUser.getScreeningOrgId());
        if (CollectionUtils.isEmpty(planSchools)) {
            return;
        }
        schoolQueryDTO.setSchoolIds(planSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList()));
    }

    /**
     * 转换成ScreeningOrgResponseDTO
     *
     * @param id 学校Id
     *
     * @return ScreeningOrgResponseDTO
     */
    public ScreeningOrgResponseDTO school2ScreeningOrgResponseDTO(Integer id) {
        School school = schoolService.getBySchoolId(id);
        if (Objects.isNull(school)) {
            throw new BusinessException("获取学校异常！");
        }
        ScreeningOrgResponseDTO responseDTO = new ScreeningOrgResponseDTO();
        BeanUtils.copyProperties(school, responseDTO);
        return responseDTO;
    }

    /**
     * 获取筛查机构（学校）
     * @param pageRequest
     * @param query
     * @param user
     */
    public IPage<ScreeningSchoolOrgVO> getScreeningOrganizationList(PageRequest pageRequest, ScreeningSchoolOrgDTO query, CurrentUser user) {
        if (user.isOverviewUser()) {
//            query.setIds(overviewService.getBindScreeningOrganization(user.getOrgId()));
            if (CollectionUtils.isEmpty(query.getIds())) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
        }
        return getSchoolList(pageRequest,query);
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest
     * @param query
     */
    public IPage<ScreeningSchoolOrgVO> getSchoolList(PageRequest pageRequest, ScreeningSchoolOrgDTO query){
        //当前区域及下级以下区域
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(query.getDistrictId());
        TwoTuple<Date, Date> startAndEndTime = getStartAndEndTime(query);
        IPage<School> schoolPage = schoolService.listByCondition(pageRequest, query, districtIds,startAndEndTime.getFirst(),startAndEndTime.getSecond());

        IPage<ScreeningSchoolOrgVO> screeningSchoolOrgVoPage = new Page<>(schoolPage.getCurrent(),schoolPage.getSize(),schoolPage.getTotal());
        // 为空直接返回
        List<School> schoolList = schoolPage.getRecords();
        if (CollectionUtils.isEmpty(schoolList)) {
            return screeningSchoolOrgVoPage;
        }

        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        List<ScreeningSchoolOrgVO> screeningSchoolOrgVOList = schoolList.stream()
                .map(school -> ScreeningOrgBizBuilder.getScreeningSchoolOrgVO(haveTaskOrgIds, school.getId(),school.getName(),null))
                .collect(Collectors.toList());
        screeningSchoolOrgVoPage.setRecords(screeningSchoolOrgVOList);
        return screeningSchoolOrgVoPage;
    }

    private TwoTuple<Date, Date> getStartAndEndTime(ScreeningSchoolOrgDTO query) {
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
     * 获取有筛查任务的筛查机构ID集合
     * @param query
     */
    private List<Integer> getHaveTaskOrgIds(ScreeningSchoolOrgDTO query) {
        if (Objects.nonNull(query.getNeedCheckHaveTask()) && Objects.equals(query.getNeedCheckHaveTask(),Boolean.TRUE)) {
            List<ScreeningTaskOrgDTO> haveTaskOrgIds = screeningTaskOrgService.getHaveTaskOrgIds(query.getGovDeptId(), query.getStartTime(), query.getEndTime());
            return haveTaskOrgIds.stream()
                    .filter(screeningTaskOrgDTO -> Objects.equals(screeningTaskOrgDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType()))
                    .map(ScreeningTaskOrgDTO::getScreeningOrgId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
