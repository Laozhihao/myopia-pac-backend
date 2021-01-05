package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolDto;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolStaff;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Resource
    private SchoolStaffService schoolStaffService;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private SchoolMapper schoolMapper;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisUtil redisUtil;

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

        RLock rLock = redissonClient.getLock(Const.LOCK_SCHOOL_REDIS + townCode);
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                school.setSchoolNo(generateSchoolNoByRedis(townCode));
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

        SchoolStaff staff = schoolStaffService.getStaffBySchoolId(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        ApiResult apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        School school = new School().setId(request.getId()).setStatus(request.getStatus());
        return schoolMapper.updateById(school);
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest 分页
     * @param schoolQuery 请求体
     * @param govDeptId   部门ID
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolDto> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery, Integer govDeptId) {
        IPage<SchoolDto> schoolDtoIPage = schoolMapper.getSchoolListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), schoolQuery.getName(),
                schoolQuery.getSchoolNo(), schoolQuery.getType(), schoolQuery.getCode());
        List<SchoolDto> schools = schoolDtoIPage.getRecords();
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }
        schools.forEach(s -> s.setScreeningTime(0));
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
        School school = schoolMapper.selectById(id);
        if (null == school) {
            throw new BusinessException("数据异常");
        }
        SchoolStaff staff = schoolStaffService.getStaffBySchoolId(id);
        return resetOAuthPassword(school, staff.getUserId());
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(School school) {
        String password = PasswordGenerator.getSchoolAdminPwd(school.getSchoolNo());
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(school.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(school.getCreateUserId())
                .setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());

        ApiResult<UserDTO> apiResult = oauthServiceClient.addAdminUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        schoolStaffService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), apiResult.getData().getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 生成学校编号
     *
     * @param code 行政区代码
     * @return 编号
     */
    private String generateSchoolNo(Long code) {
        School school = schoolMapper.getLastSchoolByNo(code);
        if (null == school) {
            return StringUtils.join(code, "001");
        }
        return String.valueOf(Long.parseLong(school.getSchoolNo()) + 1);
    }

    /**
     * 重置密码
     *
     * @param school 学校
     * @param userId OAuth2 的userId
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(School school, Integer userId) {
        String password = PasswordGenerator.getSchoolAdminPwd(school.getSchoolNo());
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        ApiResult apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("远程调用异常");
        }
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 生成学校编号
     *
     * @param code 行政区代码
     * @return 编号
     */
    private String generateSchoolNoByRedis(Long code) {
        // 查询redis是否存在
        String key = Const.GENERATE_SCHOOL_SN + code;
        Object check = redisUtil.get(key);
        if (null == check) {
            School school = schoolMapper.getLastSchoolByNo(code);
            if (null == school) {
                // 数据库不存在，初始化Redis
                long resultCode = code * 100 + 1;
                redisUtil.set(key, resultCode);
                return String.valueOf(resultCode);
            }
            // 获取当前数据库中最新的编号并且加一
            long resultCode = Long.parseLong(school.getSchoolNo()) + 1;
            // 缓存到redis中
            redisUtil.set(key, resultCode);
            return String.valueOf(resultCode);
        }
        // 自增一,并且返回
        return String.valueOf(redisUtil.incr(key, 1));
    }

    /**
     * 获取导出数据
     */
    public List<School> getExportData(SchoolQuery query) {
        return baseMapper.getExportData(query);
    }
}