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
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.model.HospitalStaff;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
@Log4j2
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Resource
    public RedissonClient redissonClient;
    @Resource
    private HospitalStaffService hospitalStaffService;
    @Resource
    private HospitalMapper hospitalMapper;
    @Resource
    private GovDeptService govDeptService;
    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveHospital(Hospital hospital) {
        Integer createUserId = hospital.getCreateUserId();
        Long townCode = hospital.getTownCode();

        if (null == townCode) {
            throw new BusinessException("数据异常");
        }
        RLock rLock = redissonClient.getLock(Const.LOCK_HOSPITAL_REDIS + hospital.getName());
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                hospital.setHospitalNo(generateHospitalNoByRedis(townCode));
                baseMapper.insert(hospital);
                return generateAccountAndPassword(hospital);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增医院获取不到锁，区域代码:{}", createUserId, townCode);
        throw new BusinessException("请重试");
    }

    /**
     * 更新医院信息
     *
     * @param hospital 医院实体类
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateHospital(Hospital hospital) {
        return baseMapper.updateById(hospital);
    }

    /**
     * 删除医院
     *
     * @param id           医院id
     * @param createUserId 创建用户
     * @param govDeptId    部门id
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedHospital(Integer id, Integer createUserId, Integer govDeptId) {
        Hospital hospital = new Hospital();
        hospital.setId(id);
        hospital.setCreateUserId(createUserId);
        hospital.setGovDeptId(govDeptId);
        hospital.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(hospital);
    }

    /**
     * 获取医院列表
     *
     * @param pageRequest 分页
     * @param query       请求入参
     * @param govDeptId   部门id
     * @return IPage<Hospital> {@link IPage}
     */
    public IPage<Hospital> getHospitalList(PageRequest pageRequest, HospitalQuery query, Integer govDeptId) {
        return hospitalMapper.getHospitalListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId),
                query.getName(), query.getHospitalNo(), query.getType(),
                query.getKind(), query.getLevel(), query.getCode());
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        // 获取医院管理员信息
        HospitalStaff staff = hospitalStaffService.getByHospitalId(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        Hospital hospital = new Hospital()
                .setId(request.getId())
                .setStatus(request.getStatus());
        return hospitalMapper.updateById(hospital);
    }

    /**
     * 重置密码
     *
     * @param id 医院id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        Hospital hospital = hospitalMapper.selectById(id);
        if (null == hospital) {
            throw new BusinessException("数据异常");
        }
        HospitalStaff staff = hospitalStaffService.getByHospitalId(id);
        return resetAuthPassword(hospital, staff.getUserId());
    }

    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(Hospital hospital) {
        String password = PasswordGenerator.getHospitalAdminPwd(hospital.getHospitalNo());
        String username = hospital.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(hospital.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(hospital.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());

        ApiResult<UserDTO> apiResult = oauthServiceClient.addAdminUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        hospitalStaffService.saveStaff(hospital.getCreateUserId(), hospital.getId(), apiResult.getData().getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 生成医院编号
     *
     * @param code 地域代码
     * @return 编号
     */
    private String generateHospitalNo(Long code) {
        Hospital hospital = hospitalMapper.getLastHospitalByNo(code);
        if (null == hospital) {
            return StringUtils.join(code, "101");
        }
        return String.valueOf(Long.parseLong(hospital.getHospitalNo()) + 1);
    }

    /**
     * 重置密码
     *
     * @param hospital 医院
     * @param userId   用户id
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetAuthPassword(Hospital hospital, Integer userId) {
        String password = PasswordGenerator.getHospitalAdminPwd(hospital.getHospitalNo());
        String username = hospital.getName();

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
     * 通过Redis生成医院编号
     *
     * @param code 地域代码
     * @return 编号
     */
    private String generateHospitalNoByRedis(Long code) {
        // 查询redis是否存在
        String key = Const.GENERATE_HOSPITAL_SN + code;
        Object check = redisUtil.get(key);
        if (null == check) {
            Hospital hospital = hospitalMapper.getLastHospitalByNo(code);
            if (null == hospital) {
                // 数据库不存在，初始化Redis
                long resultCode = code * 1000 + 101;
                redisUtil.set(key, resultCode);
                return String.valueOf(resultCode);
            }
            // 获取当前数据库中最新的编号并且加一
            long resultCode = Long.parseLong(hospital.getHospitalNo()) + 1;
            // 缓存到redis中
            redisUtil.set(key, resultCode);
            return String.valueOf(resultCode);
        }
        // 自增一,并且返回
        return String.valueOf(redisUtil.incr(key, 1));
    }
}