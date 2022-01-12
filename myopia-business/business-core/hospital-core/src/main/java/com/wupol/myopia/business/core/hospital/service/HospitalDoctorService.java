package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.DoctorMapper;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private HospitalService hospitalService;

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

    /**
     * 更新医生信息
     * @param doctor
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO updateDoctor(DoctorDTO doctor) {
        // 若修改手机号，保证手机号合法&唯一
        if (StringUtils.isNotBlank(doctor.getPhone())) {
            checkPhone(doctor.getPhone(), doctor.getId());
        }
        return updateUserAndDoctor(doctor);
    }

    /**
     * 检验手机号
     * @param phone
     * @param id
     */
    private void checkPhone(String phone, Integer id) {
        if (StringUtils.isBlank(phone) || phone.length() != 11) {
            throw new BusinessException("无效的手机号码！");
        }
        User user = getByPhone(phone);
        if (Objects.isNull(user)) {
            return;
        }
        // 新增时，手机号码已存在
        if (Objects.isNull(id)) {
            throw new BusinessException("该手机号已被使用！");
        }
        Doctor doctor = getById(id);
        // 更新时，手机号码已存在
        if (!doctor.getUserId().equals(user.getId())) {
            throw new BusinessException("该手机号已被使用！");
        }

    }

    /**
     * 通过手机号获取医生用户
     * @param phone
     * @return
     */
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
        Hospital hospital = hospitalService.getById(Objects.isNull(doctor.getHospitalId()) ? oldDoctor.getHospitalId() : doctor.getHospitalId());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(oldDoctor.getUserId())
                .setGender(doctor.getGender())
                .setRealName(doctor.getName())
                .setOrgId(doctor.getHospitalId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode())
                .setUserType(UserType.OTHER.getType());
        // 医生当前的医院配置
        userDTO.setOrgConfigType(hospital.getServiceType());

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
        Hospital hospital = hospitalService.getById(doctor.getHospitalId());
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(doctor.getHospitalId())
                .setRealName(doctor.getName())
                .setGender(doctor.getGender())
                .setPhone(doctor.getPhone())
                .setUsername(doctor.getPhone())
                .setPassword(password)
                .setCreateUserId(doctor.getCreateUserId())
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode())
                .setUserType(UserType.OTHER.getType());
        // 医生当前的医院配置
        userDTO.setOrgConfigType(hospital.getServiceType());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        doctor.setUserId(user.getId());
        this.save(doctor);
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
        page.setRecords(createDTOList(page.getRecords()));
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

    public DoctorDTO getDetailsByUserId(Integer userId) {
        DoctorDTO doctor = baseMapper.getByUserId(userId);
        User user = oauthServiceClient.getUserDetailByUserId(doctor.getUserId());
        return createDTO(doctor, user);
    }

    private List<DoctorDTO> createDTOList(List<DoctorDTO> sources) {
        List<DoctorDTO> target = new ArrayList<>();
        // 获取用户相关信息
        List<Integer> userIds = sources.stream().map(DoctorDTO::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        // 获取用户及图片信息
        sources.forEach(doctor -> {
            User user = userMap.get(doctor.getUserId());
            target.add(createDTO(doctor, user));
        });
        return target;
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
     * 修改用户信息
     * @param userId
     * @return
     */
    public boolean repair(Integer userId) {
        List<DoctorDTO> list = baseMapper.getAll();
        long phone = 10000000000L;
        for (DoctorDTO doctor : list) {
            if (Objects.nonNull(doctor.getUserId())) {
                continue;
            }
            doctor.setPhone(++phone + "");
            String password = PasswordAndUsernameGenerator.getDoctorPwd(doctor.getPhone(), doctor.getCreateTime());
            Hospital hospital = hospitalService.getById(doctor.getHospitalId());
            UserDTO userDTO = new UserDTO();
            userDTO.setOrgId(doctor.getHospitalId())
                    .setRealName(doctor.getName())
                    .setGender(doctor.getGender())
                    .setPhone(doctor.getPhone())
                    .setUsername(doctor.getPhone())
                    .setPassword(password)
                    .setCreateUserId(userId)
                    .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode());
            // 医生当前的医院配置
            userDTO.setOrgConfigType(hospital.getServiceType());

            User user = oauthServiceClient.addMultiSystemUser(userDTO);
            doctor.setUserId(user.getId());
            this.updateOrSave(doctor);
        }
        return true;
    }

    /**
     * 获取医生列表
     * @param query 查询条件
     * @return
     */
    public List<DoctorDTO> getDoctorDTOList(DoctorQuery query)  {
        List<DoctorDTO> list = baseMapper.getDoctorVoList(query);
        return createDTOList(list);
    }

    /**
     * 检验当前操作是否合规
     * @param id
     */
    public void checkId(Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            Doctor doctor = super.getById(id);
            if (Objects.isNull(doctor) || !user.getOrgId().equals(doctor.getHospitalId())) {
                throw new BusinessException("非法请求", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
            }
        }
    }

    /**
     * 获取医生名称
     * @param doctorIds
     * @return
     */
    public Map<Integer, String> getDoctorNameByIds(Set<Integer> doctorIds) {
        if (CollectionUtils.isEmpty(doctorIds)) {
            return MapUtils.EMPTY_SORTED_MAP;
        }
        return baseMapper.getDoctorNameByIds(doctorIds).stream().collect(Collectors.toMap(Doctor::getId, Doctor::getName));
    }

    /**
     * 获取医师名称包含name的医师doctorId
     * @param hospitalId
     * @param name
     * @return
     */
    public Set<Integer> getDoctorIdByName(Integer hospitalId, String name) {
        if (StringUtils.isBlank(name)) {
            return SetUtils.EMPTY_SET;
        }
        return baseMapper.getDoctorIdByName(hospitalId, name).stream().map(Doctor::getId).collect(Collectors.toSet());
    }

}