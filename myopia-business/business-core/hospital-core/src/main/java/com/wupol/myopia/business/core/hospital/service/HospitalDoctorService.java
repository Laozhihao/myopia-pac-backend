package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.DoctorMapper;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 医院-医生
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalDoctorService extends BaseService<DoctorMapper, Doctor> {

    @Autowired
    private ResourceFileService resourceFileService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    /**
     * 保存医生
     *
     * @param doctor 医生信息
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveDoctor(DoctorDTO doctor) {
        // 保证手机号合法&唯一
        checkPhone(doctor.getPhone(), null);
        return generateUserAndSaveDoctor(doctor);
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO updateDoctor(DoctorDTO doctor) {
        // 若修改手机号，保证手机号合法&唯一
        if (StringUtils.isNotBlank(doctor.getPhone())) {
            checkPhone(doctor.getPhone(), doctor.getId());
        }
        return updateUserAndDoctor(doctor);
    }

    private void checkPhone(String phone, Integer id) {
        if (StringUtils.isBlank(phone) || phone.length() != 11) {
            new BusinessException("无效的手机号码！");
        }
        List<User> users = getByPhone(phone);
        if (CollectionUtils.isNotEmpty(users) && users.size() > 1) {
            new BusinessException("手机号码已存在！");
        }
    }

    public List<User> getByPhone(String phone) {
        UserDTO userDTO = new UserDTO();
        userDTO.setPhone(phone).setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());
        return oauthServiceClient.getUserList(userDTO);
    }

    /**
     * 更新用户信息并保存
     *
     * @param doctor     医生信息
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO updateUserAndDoctor(Doctor doctor) {

        UserDTO userDTO = new UserDTO();
        userDTO.setId(doctor.getId());
        Doctor oldDoctor = getById(doctor.getId());
        int userIsUpdate = 0;   // 0：未修改；> 0 已修改；> 1，密码已修改，需反显
        // 手机号码（即账号）已修改，重新生成密码
        if (StringUtils.isNotBlank(doctor.getPhone()) && (!doctor.getPhone().equals(oldDoctor.getPhone()))) {
            userDTO.setUsername(doctor.getPhone())
                    .setPassword(PasswordAndUsernameGenerator.getDoctorPwd(doctor.getPhone(), new Date()))
                    .setPhone(doctor.getPhone());
            userIsUpdate += 2;
        }
        // 用户名修改
        if (StringUtils.isNotBlank(doctor.getName()) && (!doctor.getName().equals(oldDoctor.getName()))) {
            userDTO.setRealName(doctor.getName());
            userIsUpdate += 1;
        }

        // 用户内容更新
        if (userIsUpdate > 0) {
            oauthServiceClient.updateUser(userDTO);
        }

        this.updateOrSave(doctor);
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO(doctor.getPhone(), userDTO.getPassword());
        return userIsUpdate > 1 ? usernameAndPasswordDTO : usernameAndPasswordDTO.setNoDisplay();
    }

    /**
     * 生成账号密码
     *
     * @param doctor     医生信息
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO generateUserAndSaveDoctor(Doctor doctor) {
        String password = PasswordAndUsernameGenerator.getDoctorPwd(doctor.getPhone(), new Date());
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(doctor.getHospitalId())
                .setUsername(doctor.getPhone())
                .setPhone(doctor.getPhone())
                .setPassword(password)
                .setRealName(doctor.getName())
                .setCreateUserId(doctor.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        doctor.setUserId(user.getId());
        this.updateOrSave(doctor);
        return new UsernameAndPasswordDTO(doctor.getPhone(), password);
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return UserDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateStatus(StatusRequest request) {
        Doctor doctor = baseMapper.selectById(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO();
        userDTO.setId(doctor.getUserId())
                .setStatus(request.getStatus());
        return oauthServiceClient.updateUser(userDTO);
    }

    /**
     * 重置密码
     *
     * @param request 入参
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(ResetPasswordRequest request) {
        Doctor doctor = baseMapper.selectById(request.getId());
        User user = oauthServiceClient.getUserDetailByUserId(doctor.getUserId());
        String password = PasswordAndUsernameGenerator.getDoctorPwd(user.getPhone(), doctor.getCreateTime());
        User newUser = oauthServiceClient.resetPwd(doctor.getUserId(), password);
        return new UsernameAndPasswordDTO(newUser.getUsername(), password);
    }

    /**
     * 获取医生详情
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public Doctor getDoctor(Integer hospitalId, Integer doctorId) {
        DoctorQuery query = new DoctorQuery();
        query.setHospitalId(hospitalId).setId(doctorId);
        return baseMapper.getBy(query)
                .stream().findFirst().orElseThrow(()-> new BusinessException("未找到该医生"));
    }

    /**
     * 获取医生，带Vo
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public DoctorDTO getDoctorVo(Integer hospitalId, Integer doctorId) {
        Doctor doctor = getDoctor(hospitalId, doctorId);
        DoctorDTO doctorVo = new DoctorDTO();
        BeanUtils.copyProperties(doctor, doctorVo);
        return doctorVo.setAvatarUrl(resourceFileService.getResourcePath(doctor.getAvatarFileId()))
                .setSignUrl(resourceFileService.getResourcePath(doctor.getSignFileId()));
    }

    /**
     * 更新医生信息   TODO 待删除
     * @param doctor 医生信息
     * @return
     */
    @Deprecated
    public void saveDoctor(CurrentUser user, Doctor doctor) {
        doctor.setHospitalId(user.getOrgId())
                .setDepartmentId(-1);
        if (!saveOrUpdate(doctor)) {
            throw new BusinessException("保存医生信息失败");
        }
    }

    /**
     * 删除医生
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public void deleteDoctor(Integer hospitalId, Integer doctorId) {
        Doctor doctor = getDoctor(hospitalId, doctorId);
        if (!removeById(doctor.getId())) {
            throw new BusinessException("删除失败");
        }
    }

    /**
     * 获取医生列表
     * @param query 查询条件
     * @return
     */
    public List<DoctorDTO> getDoctorVoList(DoctorQuery query)  {
        List<DoctorDTO> list = baseMapper.getDoctorVoList(query);
        list.forEach(item-> {
            if (Objects.nonNull(item.getAvatarFileId()) && item.getAvatarFileId() != 0) {
                item.setAvatarUrl(resourceFileService.getResourcePath(item.getAvatarFileId()));
            }
        });
        return list;
    }
}