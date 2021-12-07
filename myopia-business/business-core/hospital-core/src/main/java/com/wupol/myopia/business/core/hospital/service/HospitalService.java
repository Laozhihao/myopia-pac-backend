package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
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
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        // 设置医院状态
        hospital.setStatus(hospital.getCooperationStopStatus());
        baseMapper.insert(hospital);
        // oauth系统中增加医院状态信息
        oauthServiceClient.addOrganization(new Organization(hospital.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.HOSPITAL_ADMIN, hospital.getStatus()));
        return generateAccountAndPassword(hospital, StringUtils.EMPTY, hospital.getAssociateScreeningOrgId());
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
        List<HospitalAdmin> hospitalAdminList = hospitalAdminService.findByList(new HospitalAdmin().setHospitalId(hospitalId));
        // 更新用户状态
        UserDTO user = new UserDTO();
        user.setUserIds(hospitalAdminList.stream().map(HospitalAdmin::getUserId).collect(Collectors.toList()));
        user.setStatus(request.getStatus());
        oauthServiceClient.updateUserStatusBatch(user);
        // 禁用医院，从合作医院中移除
        if (CommonConst.STATUS_BAN.equals(status)) {
            orgCooperationHospitalService.deletedHospital(hospitalId);
        }
        // 更新医院状态
        Hospital hospital = new Hospital()
                .setId(hospitalId)
                .setStatus(status);
        return baseMapper.updateById(hospital);
    }

    /**
     * 更新医院管理员用户状态
     *
     * @param request 用户信息
     * @return boolean
     **/
    public boolean updateHospitalAdminUserStatus(StatusRequest request) {
        HospitalAdmin hospitalAdmin = hospitalAdminService.findOne(new HospitalAdmin().setHospitalId(request.getId()).setUserId(request.getUserId()));
        Assert.notNull(hospitalAdmin, "不存在该用户");
        UserDTO user = new UserDTO();
        user.setId(request.getUserId());
        user.setStatus(request.getStatus());
        oauthServiceClient.updateUser(user);
        return true;
    }

    /**
     * 重置密码
     *
     * @param request 请求参数
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(ResetPasswordRequest request) {
        HospitalAdmin hospitalAdmin = hospitalAdminService.findOne(new HospitalAdmin().setHospitalId(request.getId()).setUserId(request.getUserId()));
        if (Objects.isNull(hospitalAdmin)) {
            throw new BusinessException("该账号不存");
        }
        return resetAuthPassword(request.getUsername(), hospitalAdmin.getUserId());
    }

    /**
     * 生成账号密码
     *
     * @param hospital 医院
     * @param name     子账号名称
     * @param associateScreeningOrgId 关联筛查机构的ID
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO generateAccountAndPassword(Hospital hospital, String name, Integer associateScreeningOrgId) {
        String password = PasswordAndUsernameGenerator.getHospitalAdminPwd();
        String username = StringUtils.isBlank(name) ? PasswordAndUsernameGenerator.getHospitalAdminUserName(hospitalAdminService.count() + 1) : name;
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(hospital.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(hospital.getName())
                .setCreateUserId(hospital.getCreateUserId())
                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode())
                .setUserType(UserType.HOSPITAL_ADMIN.getType());
        userDTO.setAssociateScreeningOrgId(associateScreeningOrgId);
        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        hospitalAdminService.saveAdmin(hospital.getCreateUserId(), hospital.getId(), user.getId(), hospital.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 重置密码
     *
     * @param username 用户名
     * @param userId   用户id
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetAuthPassword(String username, Integer userId) {
        String password = PasswordAndUsernameGenerator.getHospitalAdminPwd();
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

    /**
     * 处理医院状态，将已过合作时间但未处理为禁止的医院设置为禁止
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleHospitalStatus(Date date) {
        List<Hospital> hospitals = getCooperationStopAndUnhandleHospital(date);
        int result = 0;
        for (Hospital hospital : hospitals) {
            // 更新机构状态成功
            if (updateHospitalStatus(hospital.getId(), StatusConstant.DISABLE, date, StatusConstant.ENABLE) > 0) {
                // 更新oauth上机构的状态
                oauthServiceClient.updateOrganization(new Organization(hospital.getId(), SystemCode.MANAGEMENT_CLIENT, UserType.HOSPITAL_ADMIN, StatusConstant.DISABLE));
                result++;
            }
        }
        return result;
    }

    /**
     * 获取已过合作时间但未处理为禁止的医院
     * @return
     */
    public List<Hospital> getCooperationStopAndUnhandleHospital(Date date) {
        return baseMapper.getByCooperationTimeAndStatus(date, StatusConstant.ENABLE);
    }

    /**
     * CAS更新医院状态，当且仅当源状态为sourceStatus，合作结束时间早于stopDate时更新状态为targetStatus，且限定id
     * @param id
     * @param targetStatus
     * @param stopDate
     * @param sourceStatus
     * @return
     */
    public int updateHospitalStatus(Integer id, Integer targetStatus, Date stopDate, Integer sourceStatus) {
        return baseMapper.updateHospitalStatus(id, targetStatus, stopDate, sourceStatus);
    }

}