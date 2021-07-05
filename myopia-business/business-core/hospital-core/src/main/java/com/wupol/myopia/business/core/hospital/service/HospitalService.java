package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医院Service
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Resource
    private HospitalAdminService hospitalAdminService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private DistrictService districtService;

    @Resource
    private OrgCooperationHospitalService orgCooperationHospitalService;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveHospital(Hospital hospital) {
        if (checkHospitalName(hospital.getName(), null)) {
            throw new BusinessException("医院名字重复，请确认");
        }
        District district = districtService.getById(hospital.getDistrictId());
        hospital.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        baseMapper.insert(hospital);
        return generateAccountAndPassword(hospital);
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
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        Integer hospitalId = request.getId();
        Integer status = request.getStatus();
        // 获取医院管理员信息
        HospitalAdmin staff = hospitalAdminService.getByHospitalId(hospitalId);
        // 更新OAuth2
        UserDTO userDTO = new UserDTO();

        userDTO.setId(staff.getUserId())
                .setStatus(status);
        oauthServiceClient.updateUser(userDTO);
        Hospital hospital = new Hospital()
                .setId(hospitalId)
                .setStatus(status);
        // 从合作医院中移除
        if (CommonConst.STATUS_BAN.equals(status)) {
            orgCooperationHospitalService.deletedHospital(hospitalId);
        }
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

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(hospital.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(username)
                .setCreateUserId(hospital.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
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
        oauthServiceClient.resetPwd(userId, password);
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
    public boolean checkHospitalName(String hospitalName, Integer id) {
        return baseMapper.getByNameNeId(hospitalName, id).size() > 0;
    }

    /**
     * 获取医院列表
     *
     * @param page       分页请求
     * @param govDeptId  政府机构Id
     * @param name       医院名称
     * @param type       医院类型
     * @param kind       医院性质
     * @param level      医院等级
     * @param districtId 行政区域Id
     * @param status     状态
     * @return {@link IPage}
     */
    public IPage<HospitalResponseDTO> getHospitalListByCondition(Page<?> page, List<Integer> govDeptId,
                                                                 String name, Integer type, Integer kind, Integer level,
                                                                 Integer districtId, Integer status) {
        return baseMapper.getHospitalListByCondition(page, govDeptId, name, type, kind, level, districtId, status);
    }

    /**
     * 筛查机构合作医院列表查询
     *
     * @param name    名称
     * @param codePre 代码前缀
     * @return List<HospitalResponseDTO>
     */
    public List<HospitalResponseDTO> getHospitalByName(String name, Integer codePre) {
        return baseMapper.getHospitalByName(name, codePre);
    }
}