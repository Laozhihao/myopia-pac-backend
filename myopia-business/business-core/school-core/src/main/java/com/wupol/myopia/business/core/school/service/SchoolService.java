package com.wupol.myopia.business.core.school.service;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校Service
 *
 * @author HaoHao
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Resource
    private SchoolAdminService schoolAdminService;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private StudentService studentService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    @Resource
    private GovDeptService govDeptService;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveSchool(School school) {

        Integer createUserId = school.getCreateUserId();
        String schoolNo = school.getSchoolNo();
        if (StringUtils.isBlank(schoolNo)) {
            throw new BusinessException("数据异常");
        }

        if (checkSchoolName(school.getName(), null)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        baseMapper.insert(school);
        initGradeAndClass(school.getId(), school.getType(), createUserId);
        return generateAccountAndPassword(school);
    }

    /**
     * 更新学校
     *
     * @param school      学校实体类
     * @param currentUser 当前登录用户
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolResponseDTO updateSchool(School school, CurrentUser currentUser) {

        if (checkSchoolName(school.getName(), school.getId())) {
            throw new BusinessException("学校名称重复，请确认");
        }

        SchoolResponseDTO dto = new SchoolResponseDTO();
        School checkSchool = baseMapper.selectById(school.getId());

        // 获取学校管理员
        SchoolAdmin admin = schoolAdminService.getAdminBySchoolId(school.getId());
        // 更新OAuth账号
        updateOAuthName(admin.getUserId(), school.getName());

        // 名字更新重置密码
        if (!StringUtils.equals(checkSchool.getName(), school.getName())) {
            dto.setUpdatePassword(Boolean.TRUE);
            dto.setUsername(school.getName());
            // 重置密码
            String password = PasswordGenerator.getSchoolAdminPwd();
            oauthService.resetPwd(admin.getUserId(), password);
            dto.setPassword(password);
        }
        baseMapper.updateById(school);
        // 更新筛查计划中的学校
        screeningPlanSchoolService.updateSchoolNameBySchoolId(school.getId(), school.getName());
        School s = baseMapper.selectById(school.getId());
        BeanUtils.copyProperties(s, dto);
        dto.setDistrictName(districtService.getDistrictName(s.getDistrictDetail()));
        dto.setAddressDetail(districtService.getAddressDetails(
                s.getProvinceCode(), s.getCityCode(), s.getAreaCode(), s.getTownCode(), s.getAddress()));
        // 判断是否能更新
        dto.setCanUpdate(s.getGovDeptId().equals(currentUser.getOrgId()));
        dto.setStudentCount(school.getStudentCount())
                .setScreeningCount(school.getScreeningCount())
                .setCreateUser(school.getCreateUser());
        return dto;
    }

    /**
     * 删除学校
     *
     * @param id 学校id
     * @return 删除数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedSchool(Integer id) {
        School school = new School();
        school.setId(id);
        school.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(school);
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        SchoolAdmin staff = schoolAdminService.getAdminBySchoolId(request.getId());
        if (null == staff) {
            log.error("更新学校状态异常，找不到学校管理员。学校ID:{}", request.getId());
            throw new BusinessException("数据异常!");
        }
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        oauthService.modifyUser(userDTO);
        School school = new School().setId(request.getId()).setStatus(request.getStatus());
        return baseMapper.updateById(school);
    }

    /**
     * 获取学校的筛查记录详情
     *
     * @param schoolIds 筛查记录详情ID
     * @return 详情
     */
    public List<School> getSchoolByIdsAndName(List<Long> schoolIds, String schoolName) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(School::getId, schoolIds);
        if (StringUtils.isNotBlank(schoolName)) {
            queryWrapper.like(School::getName, schoolName);
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest 分页
     * @param SchoolQueryDTO 请求体
     * @param currentUser 当前用户
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolResponseDTO> getSchoolList(PageRequest pageRequest, SchoolQueryDTO SchoolQueryDTO,
                                                  CurrentUser currentUser) {

        String createUser = SchoolQueryDTO.getCreateUser();
        List<Integer> userIds = new ArrayList<>();

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTOQuery query = new UserDTOQuery();
            query.setRealName(createUser);
            List<UserDTO> userListPage = oauthService.getUserList(query);
            if (!CollectionUtils.isEmpty(userListPage)) {
                userIds = userListPage.stream().map(UserDTO::getId).collect(Collectors.toList());
            }
        }
        TwoTuple<Integer, Integer> resultDistrictId = packageSearchList(currentUser, SchoolQueryDTO.getDistrictId());

        // 查询
        IPage<SchoolResponseDTO> schoolDtoIPage = baseMapper.getSchoolListByCondition(pageRequest.toPage(),
                SchoolQueryDTO.getName(), SchoolQueryDTO.getSchoolNo(),
                SchoolQueryDTO.getType(), resultDistrictId.getFirst(), userIds, resultDistrictId.getSecond());

        List<SchoolResponseDTO> schools = schoolDtoIPage.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }
        // 获取创建人的名字
        List<Integer> createUserIds = schools.stream().map(School::getCreateUserId).collect(Collectors.toList());
        List<UserDTO> userDTOList = oauthService.getUserBatchByIds(createUserIds);
        Map<Integer, UserDTO> userDTOMap = userDTOList.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        // 学生统计
        List<StudentCountDTO> StudentCountDTOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = StudentCountDTOS.stream()
                .collect(Collectors.toMap(StudentCountDTO::getSchoolNo, StudentCountDTO::getCount));

        // 学校筛查次数
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService
                .getBySchoolIds(schools.stream().map(School::getId)
                        .collect(Collectors.toList()));
        Map<Integer, Long> planSchoolMaps = planSchoolList.stream().collect(Collectors.groupingBy(ScreeningPlanSchool::getSchoolId, Collectors.counting()));

        // 封装DTO
        schools.forEach(getSchoolDtoConsumer(currentUser, userDTOMap,
                studentCountMaps, planSchoolMaps));
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
            return getTwoTuple(org.getDistrictId());
        } else if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            return getTwoTuple(govDept.getDistrictId());
        }
        return new TwoTuple<>(districtId, null);
    }

    /**
     * 获取前缀
     *
     * @param districtId 行政区域ID
     * @return TwoTuple<Integer, Integer>
     */
    private TwoTuple<Integer, Integer> getTwoTuple(Integer districtId) {
        District district = districtService
                .getProvinceDistrictTreePriorityCache(districtService
                        .getById(districtId).getCode());
        String pre = String.valueOf(district.getCode()).substring(0, 2);
        return new TwoTuple<>(null, Integer.valueOf(pre));
    }

    /**
     * 封装DTO
     *
     * @param currentUser      当前登录用户
     * @param userDTOMap       用户信息
     * @param studentCountMaps 学生统计
     * @param planSchoolMaps   学校筛查统计
     * @return Consumer<SchoolDto>
     */
    private Consumer<SchoolResponseDTO> getSchoolDtoConsumer(CurrentUser currentUser, Map<Integer, UserDTO> userDTOMap,
                                                             Map<String, Integer> studentCountMaps, Map<Integer, Long> planSchoolMaps) {
        return school -> {
            // 创建人
            school.setCreateUser(userDTOMap.get(school.getCreateUserId()).getRealName());

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
     * 重置密码
     *
     * @param id 医院id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        School school = baseMapper.selectById(id);
        if (null == school) {
            throw new BusinessException("数据异常");
        }
        SchoolAdmin admin = schoolAdminService.getAdminBySchoolId(id);
        return resetOAuthPassword(school, admin.getUserId());
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(School school) {
        String password = PasswordGenerator.getSchoolAdminPwd();
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(school.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(username)
                .setCreateUserId(school.getCreateUserId())
                .setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());

        UserDTO user = oauthService.addMultiSystemUser(userDTO);
        schoolAdminService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), user.getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 重置密码
     *
     * @param school 学校
     * @param userId OAuth2 的userId
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(School school, Integer userId) {
        String password = PasswordGenerator.getSchoolAdminPwd();
        String username = school.getName();
        oauthService.resetPwd(userId, password);
        return new UsernameAndPasswordDTO(username, password);
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
            List<Integer> orgIds = plans
                    .stream().map(ScreeningPlan::getScreeningOrgId).collect(Collectors.toList());
            List<ScreeningOrganization> orgLists = screeningOrganizationService.getByIds(orgIds);
            Map<Integer, String> orgMaps = orgLists
                    .stream()
                    .collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));

            // 封装DTO
            plans.forEach(plan -> {
                plan.setOrgName(orgMaps.get(plan.getScreeningOrgId()));
                SchoolVisionStatistic schoolVisionStatistic = statisticMaps.get(plan.getId());
                if (null == schoolVisionStatistic) {
                    plan.setItems(new ArrayList<>());
                } else {
                    plan.setItems(Lists.newArrayList(schoolVisionStatistic));
                }
            });
        }
        return planPages;

    }

    /**
     * 获取学校的筛查记录详情
     *
     * @param schoolIds 筛查记录详情ID
     * @return 详情
     */
    public List<School> getSchoolByIds(List<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        return baseMapper.selectBatchIds(schoolIds);
    }

    /**
     * 模糊查询所有学校名称
     *
     * @param query 查询条件
     * @return List<School>
     */
    public List<School> getBy(SchoolQueryDTO query) {
        return baseMapper.getBy(query);
    }

    /**
     * 通过名字获取学校
     *
     * @param name 名字
     * @return List<School>
     */
    public List<School> getBySchoolName(String name) {
        return baseMapper.getByName(name);
    }

    /**
     * 学校编号是否被使用
     *
     * @param schoolId 学校ID
     * @param schoolNo 学校编号
     * @return Boolean.TRUE-使用 Boolean.FALSE-没有使用
     */
    public Boolean checkSchoolNo(Integer schoolId, String schoolNo) {
        return baseMapper.getByNoNeId(schoolNo, schoolId).size() > 0;
    }

    /**
     * 通过学校编号获取学校
     *
     * @param schoolNo 学校编号
     * @return School
     */
    public School getBySchoolNo(String schoolNo) {
        return baseMapper.getBySchoolNo(schoolNo);
    }

    /**
     * 批量通过学校编号获取学校
     *
     * @param schoolNos 学校编号
     * @return School
     */
    public List<School> getBySchoolNos(List<String> schoolNos) {
        return baseMapper.getBySchoolNos(schoolNos);
    }

    /**
     * 通过districtId获取学校
     *
     * @param districtId 行政区域ID
     * @return List<School>
     */
    public List<School> getByDistrictId(Integer districtId) {
        return baseMapper.getByDistrictId(districtId);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<School> getByPage(Page<?> page, SchoolQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 批量通过id获取
     *
     * @param ids 学校id
     * @return List<School>
     */
    public List<School> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 检查学校名称是否重复
     *
     * @param schoolName 学校名称
     * @param id         学校ID
     * @return 是否重复
     */
    public Boolean checkSchoolName(String schoolName, Integer id) {
        return baseMapper.getByNameNeId(schoolName, id).size() > 0;
    }

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return SchoolResponseDTO
     */
    public SchoolResponseDTO getBySchoolId(Integer id) {
        SchoolResponseDTO responseDTO = new SchoolResponseDTO();
        School school = baseMapper.selectById(id);
        BeanUtils.copyProperties(school, responseDTO);
        responseDTO.setAddressDetail(districtService.getAddressDetails(
                school.getProvinceCode(), school.getCityCode(), school.getAreaCode(), school.getTownCode(), school.getAddress()));
        return responseDTO;
    }

    /**
     * 初始化学校年级和班级信息
     *
     * @param schoolId     学校ID
     * @param type         学校类型
     * @param createUserId 创建人
     */
    private void initGradeAndClass(Integer schoolId, Integer type, Integer createUserId) {
        List<SchoolGrade> schoolGrades = new ArrayList<>();
        switch (type) {
            case 0:
                // 小学
                schoolGrades = getPrivateSchool(schoolId, createUserId);
                break;
            case 1:
                // 初级中学
                schoolGrades = getJuniorSchool(schoolId, createUserId);
                break;
            case 2:
                // 高级中学
                schoolGrades = getHighSchool(schoolId, createUserId);
                break;
            case 3:
                // 完全中学
                schoolGrades = Lists.newArrayList(Iterables
                        .concat(getJuniorSchool(schoolId, createUserId),
                                getHighSchool(schoolId, createUserId)));
                break;
            case 4:
                // 九年一贯制学校
                schoolGrades = Lists.newArrayList(Iterables
                        .concat(getPrivateSchool(schoolId, createUserId),
                                getJuniorSchool(schoolId, createUserId)));
                break;
            case 5:
            case 7:
                // 其他
                // 十二年一贯制学校
                schoolGrades = Lists.newArrayList(Iterables
                        .concat(getPrivateSchool(schoolId, createUserId),
                                getJuniorSchool(schoolId, createUserId),
                                getHighSchool(schoolId, createUserId)));
                break;
            case 6:
                // 职业高中
                schoolGrades = getVocationalHighSchool(schoolId, createUserId);
                break;
        }
        if (!CollectionUtils.isEmpty(schoolGrades)) {
            // 批量新增年级
            if (schoolGradeService.saveBatch(schoolGrades)) {
                // 批量新增班级
                batchCreateClass(createUserId, schoolId,
                        schoolGrades.stream().map(SchoolGrade::getId)
                                .collect(Collectors.toList()));
            }
        }
    }

    /**
     * 获取小学
     *
     * @param schoolId     学校ID
     * @param createUserId 创建人ID
     * @return List<SchoolGrade>
     */
    private List<SchoolGrade> getPrivateSchool(Integer schoolId, Integer createUserId) {
        return Lists.newArrayList(
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.ONE_PRIMARY_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.TWO_PRIMARY_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.THREE_PRIMARY_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.SIX_PRIMARY_SCHOOL.getName()));
    }

    /**
     * 初级中学
     *
     * @param schoolId     学校ID
     * @param createUserId 创建人ID
     * @return List<SchoolGrade>
     */
    private List<SchoolGrade> getJuniorSchool(Integer schoolId, Integer createUserId) {
        return Lists.newArrayList(
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.THREE_JUNIOR_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getName()));
    }

    /**
     * 高级中学
     *
     * @param schoolId     学校ID
     * @param createUserId 创建人ID
     * @return List<SchoolGrade>
     */
    private List<SchoolGrade> getHighSchool(Integer schoolId, Integer createUserId) {
        return Lists.newArrayList(
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_HIGH_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_HIGH_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_HIGH_SCHOOL.getName()));
    }

    /**
     * 职业高中
     *
     * @param schoolId     学校ID
     * @param createUserId 创建人ID
     * @return List<SchoolGrade>
     */
    private List<SchoolGrade> getVocationalHighSchool(Integer schoolId, Integer createUserId) {
        return Lists.newArrayList(
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL.getName()),
                new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL.getName()));
    }

    /**
     * 批量新增班级信息
     *
     * @param createUserId 创建人
     * @param schoolId     学校ID
     * @param gradeIds     年级ids
     */
    private void batchCreateClass(Integer createUserId, Integer schoolId, List<Integer> gradeIds) {

        gradeIds.forEach(g -> {
            ArrayList<SchoolClass> schoolClasses = Lists.newArrayList(
                    new SchoolClass(g, createUserId, schoolId, "一班", 35),
                    new SchoolClass(g, createUserId, schoolId, "二班", 35),
                    new SchoolClass(g, createUserId, schoolId, "三班", 35),
                    new SchoolClass(g, createUserId, schoolId, "其他", 35));
            schoolClassService.saveBatch(schoolClasses);
        });
    }

    /**
     * 更新OAuh2 username
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public void updateOAuthName(Integer userId, String username) {
        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username);
        oauthService.modifyUser(userDTO);
    }

    /**
     * 根据层级Id获取学校列表（带是否有计划字段）
     *
     * @param SchoolQueryDTO 条件
     * @return List<SchoolResponseDTO>
     */
    public List<SchoolResponseDTO> getSchoolListByDistrictId(SchoolQueryDTO SchoolQueryDTO) {
        Assert.notNull(SchoolQueryDTO.getDistrictId(), "层级id不能为空");
        SchoolQueryDTO.setDistrictIds(districtService.getProvinceAllDistrictIds(SchoolQueryDTO.getDistrictId())).setDistrictId(null);
        SchoolQueryDTO.setStatus(CommonConst.STATUS_NOT_DELETED);
        // 查询
        List<School> schoolList = getBy(SchoolQueryDTO);
        // 为空直接返回
        if (CollectionUtils.isEmpty(schoolList)) {
            return Collections.emptyList();
        }
        // 获取已有计划的学校ID列表
        List<Integer> havePlanSchoolIds = getHavePlanSchoolIds(SchoolQueryDTO);
        // 封装DTO
        return schoolList.stream().map(school -> {
            SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
            BeanUtils.copyProperties(school, schoolResponseDTO);
            schoolResponseDTO.setAlreadyHavePlan(havePlanSchoolIds.contains(school.getId()));
            return schoolResponseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取筛查计划关联学校的层级ID
     *
     * @param screeningPlanIds 计划ID
     * @return Set<Integer>
     */
    public Set<Integer> getAllSchoolDistrictIdsByScreeningPlanIds(List<Integer> screeningPlanIds) {
        if (CollectionUtils.isEmpty(screeningPlanIds)) {
            return Collections.emptySet();
        }
        return baseMapper.selectDistrictIdsByScreeningPlanIds(screeningPlanIds);
    }
}