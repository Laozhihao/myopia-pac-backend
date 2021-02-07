package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.model.HospitalAdmin;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    @Resource
    private HospitalAdminService hospitalAdminService;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    @Resource
    private SchoolService schoolService;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveHospital(Hospital hospital) {
        Integer createUserId = hospital.getCreateUserId();

        if (checkHospitalName(hospital.getName(), null)) {
            throw new BusinessException("医院名字重复，请确认");
        }

        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_HOSPITAL_REDIS, hospital.getName()));
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
        log.warn("用户id:{}新增医院获取不到锁，区域代码:{}", createUserId, hospital.getName());
        throw new BusinessException("请重试");
    }

    /**
     * 更新医院信息
     *
     * @param hospital 医院实体类
     * @return 医院实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public HospitalResponseDTO updateHospital(Hospital hospital) {
        if (checkHospitalName(hospital.getName(), hospital.getId())) {
            throw new BusinessException("医院名字重复，请确认");
        }

        if (null == hospital.getTownCode()){
            hospital.setTownCode(0L);
        }
        baseMapper.updateById(hospital);

        // 医院管理员
        HospitalAdmin admin = hospitalAdminService.getByHospitalId(hospital.getId());
        // 更新OAuth账号
        schoolService.updateOAuthName(admin.getUserId(), hospital.getName());

        Hospital h = baseMapper.selectById(hospital.getId());
        HospitalResponseDTO response = new HospitalResponseDTO();
        BeanUtils.copyProperties(h, response);
        response.setDistrictName(districtService.getDistrictName(h.getDistrictDetail()));
        // 行政区域名称
        response.setAddressDetail(districtService.getAddressDetails(
                h.getProvinceCode(), h.getCityCode(), h.getAreaCode(), h.getTownCode(), h.getAddress()));
        return response;
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
        hospital.setStatus(CommonConst.STATUS_IS_DELETED);
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
    public IPage<HospitalResponseDTO> getHospitalList(PageRequest pageRequest, HospitalQuery query, Integer govDeptId) {
        IPage<HospitalResponseDTO> hospitalListsPage = baseMapper.getHospitalListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), query.getName(), query.getType(),
                query.getKind(), query.getLevel(), query.getDistrictId(), query.getStatus());

        List<HospitalResponseDTO> records = hospitalListsPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return hospitalListsPage;
        }
        records.forEach(h -> {
            // 详细地址
            h.setAddressDetail(districtService.getAddressDetails(
                    h.getProvinceCode(), h.getCityCode(), h.getAreaCode(), h.getTownCode(), h.getAddress()));

            // 行政区域名称
            h.setDistrictName(districtService.getDistrictName(h.getDistrictDetail()));
        });
        return hospitalListsPage;
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
        oauthService.modifyUser(userDTO);
        Hospital hospital = new Hospital()
                .setId(request.getId())
                .setStatus(request.getStatus());
        return baseMapper.updateById(hospital);
    }

    /**
     * 重置密码
     *
     * @param id 医院id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        Hospital hospital = baseMapper.selectById(id);
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
        String password = PasswordGenerator.getHospitalAdminPwd();
        String username = hospital.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(hospital.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(username)
                .setCreateUserId(hospital.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());

        UserDTO user = oauthService.addAdminUser(userDTO);
        hospitalAdminService.saveAdmin(hospital.getCreateUserId(), hospital.getId(), user.getId(), hospital.getGovDeptId());
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
        String password = PasswordGenerator.getHospitalAdminPwd();
        String username = hospital.getName();
        oauthService.resetPwd(userId, password);
        return new UsernameAndPasswordDTO(username, password);
    }


    /**
     * 查询
     */
    public List<Hospital> getBy(HospitalQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<Hospital> getByPage(Page<?> page, HospitalQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 检查医院名称是否重复
     *
     * @param hospitalName 医院名称
     * @param id           医院ID
     * @return 是否重复
     */
    public Boolean checkHospitalName(String hospitalName, Integer id) {
        QueryWrapper<Hospital> queryWrapper = new QueryWrapper<Hospital>()
                .eq("name", hospitalName);
        if (null != id) {
            queryWrapper.ne("id", id);
        }
        return baseMapper.selectList(queryWrapper).size() > 0;
    }
}