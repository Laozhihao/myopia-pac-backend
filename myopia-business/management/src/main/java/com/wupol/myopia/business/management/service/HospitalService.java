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
import com.wupol.myopia.business.management.domain.model.HospitalAdmin;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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

    @Value(value = "${oem.province.code}")
    private Long provinceCode;

    @Resource
    private HospitalAdminService hospitalAdminService;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private GovDeptService govDeptService;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Resource
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

        // 初始化省代码
        hospital.setProvinceCode(provinceCode);

        if (null == townCode) {
            throw new BusinessException("数据异常");
        }
        RLock rLock = redissonClient.getLock(Const.LOCK_HOSPITAL_REDIS + hospital.getName());
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
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
     * @return 医院实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public Hospital updateHospital(Hospital hospital) {
        baseMapper.updateById(hospital);
        return baseMapper.selectById(hospital.getId());
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
                query.getName(), query.getType(), query.getKind(),
                query.getLevel(), query.getCode());
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
        HospitalAdmin staff = hospitalAdminService.getByHospitalId(request.getId());
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
        HospitalAdmin staff = hospitalAdminService.getByHospitalId(id);
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
            throw new BusinessException("创建管理员信息异常" + apiResult.getMessage());
        }
        hospitalAdminService.saveAdmin(hospital.getCreateUserId(), hospital.getId(), apiResult.getData().getId(), hospital.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
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
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("远程调用异常");
        }
        return new UsernameAndPasswordDTO(username, password);
    }


    /**
     * 获取导出数据
     */
    public List<Hospital> getExportData(HospitalQuery query) {
        return baseMapper.getExportData(query);
    }
}