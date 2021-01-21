package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolDto;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolAdmin;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Value(value = "${oem.province.code}")
    private Long provinceCode;

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

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveSchool(School school) {

        Integer createUserId = school.getCreateUserId();
        Long townCode = school.getTownCode();
        if (null == townCode) {
            throw new BusinessException("数据异常");
        }

        // 初始化省代码
        school.setProvinceCode(provinceCode);
        // 设置行政区域名
        school.setDistrictName(districtService.getDistrictNameById(school.getDistrictId()));

        RLock rLock = redissonClient.getLock(Const.LOCK_SCHOOL_REDIS + townCode);
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                baseMapper.insert(school);
                return generateAccountAndPassword(school);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        }
        log.warn("用户id:{}新增学校获取不到锁，区域代码:{}", createUserId, townCode);
        throw new BusinessException("请重试");
    }

    /**
     * 更新学校
     *
     * @param school 学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public School updateSchool(School school) {
        // 设置行政区域名
        school.setDistrictName(districtService.getDistrictNameById(school.getDistrictId()));
        baseMapper.updateById(school);
        return baseMapper.selectById(school.getId());
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
        school.setStatus(Const.STATUS_IS_DELETED);
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
    public IPage<SchoolDto> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery,
                                          CurrentUser currentUser) {

        Integer orgId = currentUser.getOrgId();
        String createUser = schoolQuery.getCreateUser();
        List<Integer> userIds = new ArrayList<>();

        Integer districtId = districtService.getDistrictId(currentUser, schoolQuery.getDistrictId());

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTOQuery query = new UserDTOQuery();
            query.setRealName(createUser);
            Page<UserDTO> userListPage = oauthService.getUserListPage(query);
            List<UserDTO> records = userListPage.getRecords();
            if (!CollectionUtils.isEmpty(userListPage.getRecords())) {
                userIds = records.stream().map(UserDTO::getId).collect(Collectors.toList());
            }
        }

        // 查询
        IPage<SchoolDto> schoolDtoIPage = baseMapper.getSchoolListByCondition(pageRequest.toPage(),
                schoolQuery.getName(), schoolQuery.getSchoolNo(),
                schoolQuery.getType(), districtId, userIds);

        List<SchoolDto> schools = schoolDtoIPage.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }

        // 获取创建人的名字
        List<Integer> createUserIds = schools.stream().map(School::getCreateUserId).collect(Collectors.toList());
        Map<Integer, UserDTO> userDTOMap = userService.getUserMapByIds(createUserIds);

        // 封装DTO
        schools.forEach(s -> {
            // 创建人
            s.setCreateUser(userDTOMap.get(s.getCreateUserId()).getRealName());
            s.setScreeningCount(0);
            // 判断是否能更新
            if (s.getGovDeptId().equals(orgId)) {
                s.setCanUpdate(Boolean.TRUE);
            }
        });
        return schoolDtoIPage;
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

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        oauthService.modifyUser(userDTO);
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 获取导出数据
     */
    public List<School> getExportData(SchoolQuery query) {
        return baseMapper.getBy(query);
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
        return screeningPlanService.getListByIds(pageRequest, planSchoolList.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toList()));
    }

    /**
     * 获取学校的筛查记录详情
     *
     * @param id 筛查记录详情ID
     * @return 详情
     */
    public Object getScreeningRecordDetail(Integer id) {
        return schoolVisionStatisticService.getByPlanId(id);
    }

    /**
     * 批量通过学校获取
     *
     * @param schoolNos 学校编码Lists
     * @return Map<String, String>
     */
    public Map<String, String> getNameBySchoolNos(List<String> schoolNos) {
        if (CollectionUtils.isEmpty(schoolNos)) {
            return Maps.newHashMap();
        }
        List<School> schoolNo = baseMapper.selectList(new QueryWrapper<School>().in("school_no", schoolNos));

        if (CollectionUtils.isEmpty(schoolNo)) {
            return Maps.newHashMap();
        }
        return schoolNo.stream().collect(Collectors.toMap(School::getSchoolNo, School::getName));
    }


    /**
     * 模糊查询所有学校名称
     * @param nameLike 模糊查询
     * @param deptId    机构id
     * @return
     */
    public List<String> getBySchoolName(String nameLike, Integer deptId) {
        SchoolQuery query = new SchoolQuery().setNameLike(nameLike);
        query.setGovDeptId(deptId);
        return baseMapper.getBy(query).stream().map(School::getName).collect(Collectors.toList());
    }
}