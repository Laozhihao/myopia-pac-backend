package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolScreeningCountVO;
import com.wupol.myopia.business.management.domain.vo.StudentCountVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Resource
    private SchoolAdminService schoolAdminService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    @Resource
    private UserService userService;

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

        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_SCHOOL_REDIS, schoolNo));
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                baseMapper.insert(school);
                initGradeAndClass(school.getId(), school.getType(), createUserId);
                return generateAccountAndPassword(school);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        }
        log.error("用户id:{}新增学校获取不到锁，学校名称:{}", createUserId, schoolNo);
        throw new BusinessException("请重试");
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
        baseMapper.updateById(school);

        // 获取学校管理员
        SchoolAdmin admin = schoolAdminService.getAdminBySchoolId(school.getId());
        // 更新OAuth账号
        updateOAuthName(admin.getUserId(), school.getName());

        School s = baseMapper.selectById(school.getId());
        SchoolResponseDTO dto = new SchoolResponseDTO();
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
     * 获取学校列表
     *
     * @param pageRequest 分页
     * @param schoolQuery 请求体
     * @param currentUser 当前用户
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolResponseDTO> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery,
                                                  CurrentUser currentUser) {

        String createUser = schoolQuery.getCreateUser();
        List<Integer> userIds = new ArrayList<>();

//        Integer districtId = districtService.filterQueryDistrictId(currentUser, schoolQuery.getDistrictId());

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTOQuery query = new UserDTOQuery();
            query.setRealName(createUser);
            List<UserDTO> userListPage = oauthService.getUserList(query);
            if (!CollectionUtils.isEmpty(userListPage)) {
                userIds = userListPage.stream().map(UserDTO::getId).collect(Collectors.toList());
            }
        }

        // 查询
        IPage<SchoolResponseDTO> schoolDtoIPage = baseMapper.getSchoolListByCondition(pageRequest.toPage(),
                schoolQuery.getName(), schoolQuery.getSchoolNo(),
                schoolQuery.getType(), schoolQuery.getDistrictId(), userIds);

        List<SchoolResponseDTO> schools = schoolDtoIPage.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }

        // 获取创建人的名字
        List<Integer> createUserIds = schools.stream().map(School::getCreateUserId).collect(Collectors.toList());
        List<UserDTO> userDTOList = oauthService.getUserBatchByIds(createUserIds);
        Map<Integer, UserDTO> userDTOMap = userDTOList.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        // 筛查统计
        List<SchoolScreeningCountVO> countVOS = screeningPlanSchoolService.countScreeningTime();
        Map<Integer, Integer> countMaps = countVOS.stream()
                .collect(Collectors
                        .toMap(SchoolScreeningCountVO::getSchoolId,
                                SchoolScreeningCountVO::getCount));

        // 学生统计
        List<StudentCountVO> studentCountVOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = studentCountVOS.stream()
                .collect(Collectors.toMap(StudentCountVO::getSchoolNo, StudentCountVO::getCount));

        // 封装DTO
        schools.forEach(getSchoolDtoConsumer(currentUser, userDTOMap, countMaps, studentCountMaps));
        return schoolDtoIPage;
    }

    /**
     * 封装DTO
     *
     * @param currentUser      当前登录用户
     * @param userDTOMap       用户信息
     * @param countMaps        筛查统计
     * @param studentCountMaps 学生统计
     * @return Consumer<SchoolDto>
     */
    private Consumer<SchoolResponseDTO> getSchoolDtoConsumer(CurrentUser currentUser, Map<Integer, UserDTO> userDTOMap, Map<Integer, Integer> countMaps, Map<String, Integer> studentCountMaps) {
        return s -> {
            // 创建人
            s.setCreateUser(userDTOMap.get(s.getCreateUserId()).getRealName());

            // 判断是否能更新
            s.setCanUpdate(s.getGovDeptId().equals(currentUser.getOrgId()));

            // 行政区名字
            s.setDistrictName(districtService.getDistrictName(s.getDistrictDetail()));

            // 筛查次数
            s.setScreeningCount(countMaps.getOrDefault(s.getId(), 0));

            // 学生统计
            s.setStudentCount(studentCountMaps.getOrDefault(s.getSchoolNo(), 0));

            // 详细地址
            s.setAddressDetail(districtService.getAddressDetails(
                    s.getProvinceCode(), s.getCityCode(), s.getAreaCode(), s.getTownCode(), s.getAddress()));
        };
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

        UserDTO user = oauthService.addAdminUser(userDTO);
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
    public Object getScreeningRecordLists(PageRequest pageRequest, Integer schoolId) {

        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(planSchoolList)) {
            return new Page<>();
        }

        // 通过planIds查询计划
        IPage<ScreeningPlanResponse> planPages = screeningPlanService
                .getListByIds(pageRequest, planSchoolList.stream()
                        .map(ScreeningPlanSchool::getScreeningPlanId)
                        .collect(Collectors.toList()));

        List<ScreeningPlanResponse> plans = planPages.getRecords();

        if (!CollectionUtils.isEmpty(plans)) {
            List<Integer> planIds = plans.stream().map(ScreeningPlan::getId).collect(Collectors.toList());
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
            plans.forEach(p -> {
                p.setOrgName(orgMaps.get(p.getScreeningOrgId()));
                p.setItems(Lists.newArrayList(statisticMaps.get(p.getId())));
            });

        }
        return planPages;
    }

    /**
     * 批量通过学校获取
     *
     * @param schoolNos 学校编码Lists
     * @return Map<String, String>
     */
    public Map<String, School> getNameBySchoolNos(List<String> schoolNos) {
        if (CollectionUtils.isEmpty(schoolNos)) {
            return Maps.newHashMap();
        }
        List<School> schoolNo = baseMapper.selectList(new QueryWrapper<School>().in("school_no", schoolNos));

        if (CollectionUtils.isEmpty(schoolNo)) {
            return Maps.newHashMap();
        }
        return schoolNo.stream().collect(Collectors.toMap(School::getSchoolNo, Function.identity()));
    }

    /**
     * 模糊查询所有学校名称
     *
     * @param query 查询条件
     * @return List<School>
     */
    public List<School> getBy(SchoolQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 通过名字获取学校
     *
     * @param name 名字
     * @return List<School>
     */
    public List<School> getBySchoolName(String name) {
        return baseMapper.selectList(new QueryWrapper<School>().like("name", name));
    }

    /**
     * 学校编号是否被使用
     *
     * @param schoolNo 学校编号
     * @return Boolean.TRUE-使用 Boolean.FALSE-没有使用
     */
    public Boolean checkSchoolNo(String schoolNo) {
        List<School> schoolList = baseMapper.selectList(new QueryWrapper<School>().eq("school_no", schoolNo));
        if (CollectionUtils.isEmpty(schoolList)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 通过学校编号获取学校
     *
     * @param schoolNo 学校编号
     * @return School
     */
    public School getBySchoolNo(String schoolNo) {
        return baseMapper.selectOne(new QueryWrapper<School>().eq("school_no", schoolNo));
    }

    /**
     * 批量通过学校编号获取学校
     *
     * @param schoolNos 学校编号
     * @return School
     */
    public List<School> getBySchoolNos(List<String> schoolNos) {
        return baseMapper.selectList(new QueryWrapper<School>()
                .in("school_no", schoolNos));
    }

    /**
     * 通过districtId获取学校
     *
     * @param districtId 行政区域ID
     * @return List<School>
     */
    public List<School> getByDistrictId(Integer districtId) {
        return baseMapper.selectList(new QueryWrapper<School>().like("district_id", districtId));
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<School> getByPage(Page<?> page, SchoolQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 批量通过id获取
     *
     * @param ids 学校id
     * @return List<School>
     */
    public List<School> getByIds(List<Integer> ids) {
        return baseMapper.selectList(new QueryWrapper<School>().in("id", ids));
    }

    /**
     * 检查学校名称是否重复
     *
     * @param schoolName 学校名称
     * @param id         学校ID
     * @return 是否重复
     */
    public Boolean checkSchoolName(String schoolName, Integer id) {
        QueryWrapper<School> queryWrapper = new QueryWrapper<School>()
                .eq("name", schoolName);

        if (null != id) {
            queryWrapper.ne("id", id);
        }

        return baseMapper.selectList(queryWrapper).size() > 0;
    }

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return SchoolResponseDTO
     */
    public SchoolResponseDTO getBySchoolId(Integer id) {
        SchoolResponseDTO responseDTO = new SchoolResponseDTO();
        School s = baseMapper.selectById(id);
        BeanUtils.copyProperties(s, responseDTO);
        responseDTO.setAddressDetail(districtService.getAddressDetails(
                s.getProvinceCode(), s.getCityCode(), s.getAreaCode(), s.getTownCode(), s.getAddress()));
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
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.ONE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.TWO_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.THREE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.SIX_PRIMARY_SCHOOL.getName()));
                break;
            case 1:
                // 初级中学
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.THREE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getName()));
                break;
            case 2:
                // 高级中学
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_HIGH_SCHOOL.getName()));
                break;
            case 3:
                // 完全中学
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.THREE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_HIGH_SCHOOL.getName()));
                break;
            case 4:
                // 九年一贯制学校
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.ONE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.TWO_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.THREE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.SIX_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.THREE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getName()));
                break;
            case 5:
                // 十二年一贯制学校
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.ONE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.TWO_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.THREE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.SIX_PRIMARY_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.THREE_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_HIGH_SCHOOL.getName()));
                break;
            case 6:
                // 职业高中
                schoolGrades = Lists.newArrayList(
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL.getName()),
                        new SchoolGrade(createUserId, schoolId, GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL.getName()));
                break;
            case 7:
                // 其他
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
}