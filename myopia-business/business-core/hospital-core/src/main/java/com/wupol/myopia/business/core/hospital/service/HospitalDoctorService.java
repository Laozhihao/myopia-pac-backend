package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        User user = getByPhone(phone);
        if (Objects.nonNull(user)) {
            // 新增时，手机号码已存在
            if (Objects.isNull(id)) {
                new BusinessException("手机号码已存在！");
            } else {
                Doctor doctor = getById(id);
                // 更新时，手机号码已存在
                if (!doctor.getUserId().equals(user.getId())) {
                    new BusinessException("手机号码已存在！");
                }
            }
        }
    }

    public User getByPhone(String phone) {
        List<User> users = oauthServiceClient.getUserBatchByPhones(Arrays.asList(phone), SystemCode.HOSPITAL_CLIENT.getCode());
        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    /**
     * 更新用户信息并保存
     *
     * @param doctor     医生信息
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO updateUserAndDoctor(DoctorDTO doctor) {

        Doctor oldDoctor = getById(doctor.getId());
        User oldUser = oauthServiceClient.getUserDetailByUserId(oldDoctor.getUserId());
        boolean usernameIsUpdate = false;

        UserDTO userDTO = new UserDTO();
        userDTO.setId(oldDoctor.getUserId())
                .setGender(doctor.getGender())
                .setRealName(doctor.getName())
                .setOrgId(doctor.getHospitalId());

        // 手机号码（即账号）已修改，重新生成密码
        if (StringUtils.isNotBlank(doctor.getPhone()) && (!doctor.getPhone().equals(oldUser.getPhone()))) {
            userDTO.setUsername(doctor.getPhone())
                    .setPassword(PasswordAndUsernameGenerator.getDoctorPwd(doctor.getPhone(), new Date()))
                    .setPhone(doctor.getPhone());
            usernameIsUpdate = true;
        }

        // 更新用户信息
        User newUser = oauthServiceClient.updateUser(userDTO);
        this.updateOrSave(doctor);
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO(newUser.getPhone(), userDTO.getPassword(), newUser.getRealName());
        return usernameIsUpdate ? usernameAndPasswordDTO : usernameAndPasswordDTO.setNoDisplay();
    }

    /**
     * 生成账号密码
     *
     * @param doctor     医生信息
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO generateUserAndSaveDoctor(DoctorDTO doctor) {
        String password = PasswordAndUsernameGenerator.getDoctorPwd(doctor.getPhone(), new Date());
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(doctor.getHospitalId())
                .setRealName(doctor.getName())
                .setGender(doctor.getGender())
                .setPhone(doctor.getPhone())
                .setUsername(doctor.getPhone())
                .setPassword(password)
                .setCreateUserId(doctor.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        doctor.setUserId(user.getId());
        this.updateOrSave(doctor);
        return new UsernameAndPasswordDTO(doctor.getPhone(), password, doctor.getName());
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
        oauthServiceClient.resetPwd(doctor.getUserId(), password);
        return new UsernameAndPasswordDTO(user.getUsername(), password, user.getRealName());
    }

    /**
     * 获取列表
     * @param pageRequest
     * @param query
     * @return
     */
    public IPage<DoctorDTO> getPage(PageRequest pageRequest, DoctorQuery query) {
        IPage<DoctorDTO> page = baseMapper.getByPage(pageRequest.toPage(), query);
        // 获取用户相关信息
        List<Integer> userIds = page.getRecords().stream().map(DoctorDTO::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        // 获取用户及图片信息
        page.getRecords().forEach(doctor -> {
            User user = userMap.get(doctor.getUserId());
            createDTO(doctor, user);
        });
        return page;
    }

    /**
     * 获取医生详情
     * @param id
     * @return
     */
    public DoctorDTO getDetails(Integer id) {
        DoctorDTO doctor = baseMapper.getById(id);
        User user = oauthServiceClient.getUserDetailByUserId(doctor.getUserId());
        return createDTO(doctor, user);
    }

    private DoctorDTO createDTO(DoctorDTO simple, User user) {
        simple.setGender(user.getGender())
                .setPhone(user.getPhone())
                .setStatus(user.getStatus())
                .setAvatarUrl(resourceFileService.getResourcePath(simple.getAvatarFileId()))
                .setSignUrl(resourceFileService.getResourcePath(simple.getSignFileId()));
        return simple;
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