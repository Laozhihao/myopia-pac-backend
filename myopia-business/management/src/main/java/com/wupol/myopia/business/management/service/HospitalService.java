package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Resource
    private HospitalStaffService hospitalStaffService;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private GovDeptService govDeptService;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveHospital(Hospital hospital) {
        if (null == hospital.getTownCode()) {
            throw new BusinessException("数据异常");
        }
        hospital.setHospitalNo(generateHospitalNo(hospital.getTownCode()));
        baseMapper.insert(hospital);
        return generateAccountAndPassword(hospital);
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
        // TODO: 获取登陆用户id, 部门id
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
        return hospitalMapper.getHospitalListByCondition(pageRequest.toPage(), govDeptService.getAllSubordinate(govDeptId),
                query.getName(), query.getHospitalNo(), query.getType(), query.getKind(), query.getLevel(), query.getCode());
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

    private String generateHospitalNo(Integer code) {
        Hospital hospital = hospitalMapper.getLastHospitalByNo(code);
        if (null == hospital) {
            return StringUtils.join(code, "101");
        }
        return String.valueOf(Long.parseLong(hospital.getHospitalNo()) + 1);
    }
}