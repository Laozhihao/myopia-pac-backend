package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.service.SchoolAdminService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolVisionStatisticItem;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    @Autowired
    private GovDeptService govDeptService;

    @Autowired
    private StudentService studentService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Autowired
    private StatRescreenService statRescreenService;

    /**
     * 更新学校
     *
     * @param school      学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolResponseDTO updateSchool(School school) {

        if (schoolService.checkSchoolName(school.getName(), school.getId())) {
            throw new BusinessException("学校名称重复，请确认");
        }

        SchoolResponseDTO dto = new SchoolResponseDTO();
        School checkSchool = schoolService.getById(school.getId());

        // 获取学校管理员
        SchoolAdmin admin = schoolAdminService.getAdminBySchoolId(school.getId());
        // 更新OAuth账号
        schoolService.updateOAuthName(admin.getUserId(), school.getName());

        // 名字更新重置密码
        if (!StringUtils.equals(checkSchool.getName(), school.getName())) {
            dto.setUsername(school.getName());
        }
        District district = districtService.getById(school.getDistrictId());
        school.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        schoolService.updateById(school);
        // 更新筛查计划中的学校
        screeningPlanSchoolService.updateSchoolNameBySchoolId(school.getId(), school.getName());
        School s = schoolService.getById(school.getId());
        BeanUtils.copyProperties(s, dto);
        dto.setDistrictName(districtService.getDistrictName(s.getDistrictDetail()));
        dto.setAddressDetail(districtService.getAddressDetails(
                s.getProvinceCode(), s.getCityCode(), s.getAreaCode(), s.getTownCode(), s.getAddress()));
        // 判断是否能更新
        dto.setCanUpdate(s.getGovDeptId().equals(school.getGovDeptId()));
        dto.setStudentCount(school.getStudentCount())
                .setScreeningCount(school.getScreeningCount())
                .setCreateUser(school.getCreateUser());
        return dto;
    }


    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return SchoolResponseDTO
     */
    public SchoolResponseDTO getBySchoolId(Integer id) {
        SchoolResponseDTO responseDTO = new SchoolResponseDTO();
        School school = schoolService.getById(id);
        BeanUtils.copyProperties(school, responseDTO);
        responseDTO.setAddressDetail(districtService.getAddressDetails(
                school.getProvinceCode(), school.getCityCode(), school.getAreaCode(), school.getTownCode(), school.getAddress()));
        return responseDTO;
    }

    /**
     * 根据层级Id获取学校列表（带是否有计划字段）
     *
     * @param schoolQueryDTO 条件
     * @return List<SchoolResponseDTO>
     */
    public List<SchoolResponseDTO> getSchoolListByDistrictId(SchoolQueryDTO schoolQueryDTO) {
        Assert.notNull(schoolQueryDTO.getDistrictId(), "层级id不能为空");
        schoolQueryDTO.setDistrictIds(districtService.getProvinceAllDistrictIds(schoolQueryDTO.getDistrictId())).setDistrictId(null);
        schoolQueryDTO.setStatus(CommonConst.STATUS_NOT_DELETED);
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
    public IPage<ScreeningPlanResponseDTO> getScreeningRecordLists(PageRequest pageRequest, Integer schoolId) {

        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(planSchoolList)) {
            return new Page<>();
        }

        // 通过planIds查询计划
        IPage<ScreeningPlanResponseDTO> planPages = screeningPlanService
                .getListByIds(pageRequest, planSchoolList.stream()
                        .map(ScreeningPlanSchool::getScreeningPlanId)
                        .collect(Collectors.toList()));

        List<ScreeningPlanResponseDTO> plans = planPages.getRecords();

        if (!CollectionUtils.isEmpty(plans)) {
            List<Integer> planIds = plans.stream().map(ScreeningPlanResponseDTO::getId).collect(Collectors.toList());
            // 学校统计信息
            List<SchoolVisionStatistic> schoolStatistics = schoolVisionStatisticService
                    .getByPlanIdsAndSchoolId(planIds, schoolId);
            Map<Integer, SchoolVisionStatistic> statisticMaps = schoolStatistics
                    .stream()
                    .collect(Collectors.toMap(SchoolVisionStatistic::getScreeningPlanId, Function.identity()));

            // 获取筛查机构
            List<Integer> orgIds = plans.stream()
                    .map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toList());
            List<ScreeningOrganization> orgLists = screeningOrganizationService.getByIds(orgIds);
            Map<Integer, String> orgMaps = orgLists.stream()
                    .collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));

            // 获取计划对应的学校信息
            Map<Integer, ScreeningPlanSchool> planSchoolMap = planSchoolList.stream().collect(Collectors.toMap(ScreeningPlanSchool::getScreeningPlanId, Function.identity()));

            // 封装DTO
            plans.forEach(plan -> {
                plan.setOrgName(orgMaps.get(plan.getScreeningOrgId()));
                SchoolVisionStatistic schoolVisionStatistic = statisticMaps.get(plan.getId());
                if (null == schoolVisionStatistic) {
                    plan.setItems(new ArrayList<>());
                } else {
                    SchoolVisionStatisticItem item = new SchoolVisionStatisticItem();
                    ScreeningPlanSchool screeningPlanSchool = planSchoolMap.get(plan.getId());
                    BeanUtils.copyProperties(schoolVisionStatistic, item);
                    item.setHasRescreenReport(statRescreenService.hasRescreenReport(plan.getId(), schoolVisionStatistic.getSchoolId()));
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
        if (Objects.nonNull(query.getNeedCheckHavePlan()) && query.getNeedCheckHavePlan()) {
            return screeningPlanSchoolService.getHavePlanSchoolIds(query.getDistrictIds(), null, query.getScreeningOrgId(), query.getStartTime(), query.getEndTime());
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

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTO query = new UserDTO();
            query.setRealName(createUser);
            List<User> userListPage = oauthServiceClient.getUserList(query);
            if (!CollectionUtils.isEmpty(userListPage)) {
                userIds = userListPage.stream().map(User::getId).collect(Collectors.toList());
            }
        }
        TwoTuple<Integer, Integer> resultDistrictId = packageSearchList(currentUser, schoolQueryDTO.getDistrictId());

        // 查询
        IPage<SchoolResponseDTO> schoolDtoIPage = schoolService.getSchoolListByCondition(pageRequest,
                schoolQueryDTO, resultDistrictId, userIds, resultDistrictId.getSecond());

        List<SchoolResponseDTO> schools = schoolDtoIPage.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }
        // 获取创建人的名字
        List<Integer> createUserIds = schools.stream().map(School::getCreateUserId).collect(Collectors.toList());
        List<User> userLists = oauthServiceClient.getUserBatchByIds(createUserIds);
        Map<Integer, User> userDTOMap = userLists.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 学生统计
        List<StudentCountDTO> studentCountDTOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = studentCountDTOS.stream()
                .collect(Collectors.toMap(StudentCountDTO::getSchoolNo, StudentCountDTO::getCount));

        // 学校筛查次数
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService
                .getBySchoolIds(schools.stream().map(School::getId)
                        .collect(Collectors.toList()));
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
    private TwoTuple<Integer, Integer> packageSearchList(CurrentUser currentUser, Integer districtId) {
        // 管理员看到全部
        if (currentUser.isPlatformAdminUser()) {
            // 不为空说明是搜索条件
            if (null != districtId) {
                return new TwoTuple<>(districtId, null);
            }
        } else if (currentUser.isScreeningUser()) {
            if (null != districtId) {
                // 不为空说明是搜索条件
                return new TwoTuple<>(districtId, null);
            }
            // 只能看到所属的省级数据
            ScreeningOrganizationAdmin orgAdmin = screeningOrganizationAdminService.getByOrgId(currentUser.getOrgId());
            ScreeningOrganization org = screeningOrganizationService.getById(orgAdmin.getScreeningOrgId());
            return districtService.getDistrictPrefix(org.getDistrictId());
        } else if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            return districtService.getDistrictPrefix(govDept.getDistrictId());
        }
        return new TwoTuple<>(districtId, null);
    }

    /**
     * 封装DTO
     *
     * @param currentUser      当前登录用户
     * @param userMap       用户信息
     * @param studentCountMaps 学生统计
     * @param planSchoolMaps   学校筛查统计
     * @return Consumer<SchoolDto>
     */
    private Consumer<SchoolResponseDTO> getSchoolDtoConsumer(CurrentUser currentUser, Map<Integer, User> userMap,
                                                             Map<String, Integer> studentCountMaps, Map<Integer, Long> planSchoolMaps) {
        return school -> {
            // 创建人
            school.setCreateUser(userMap.get(school.getCreateUserId()).getRealName());

            // 判断是否能更新
            school.setCanUpdate(school.getGovDeptId().equals(currentUser.getOrgId()));

            // 行政区名字
            school.setDistrictName(districtService.getDistrictName(school.getDistrictDetail()));

            // 筛查次数
            school.setScreeningCount(planSchoolMaps.getOrDefault(school.getId(), 0L));

            // 学生统计
            school.setStudentCount(studentCountMaps.getOrDefault(school.getSchoolNo(), 0));

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

}
