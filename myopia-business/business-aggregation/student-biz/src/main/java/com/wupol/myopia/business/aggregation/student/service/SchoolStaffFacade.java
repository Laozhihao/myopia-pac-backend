package com.wupol.myopia.business.aggregation.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolStaffTypeEnum;
import com.wupol.myopia.business.core.school.domain.dos.AccountInfo;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffListResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffSaveRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.SchoolStaffService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校员工
 *
 * @author Simple4H
 */
@Service
public class SchoolStaffFacade {

    @Resource
    private SchoolStaffService schoolStaffService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private SchoolService schoolService;

    /**
     * 获取学校员工
     *
     * @param request  分页
     * @param schoolId 学校Id
     *
     * @return IPage<SchoolStaff>
     */
    public IPage<SchoolStaffListResponseDTO> getSchoolStaff(PageRequest request, Integer schoolId) {
        IPage<SchoolStaff> schoolStaff = schoolStaffService.getSchoolStaff(request, schoolId);
        List<SchoolStaff> staffList = schoolStaff.getRecords();
        if (CollectionUtils.isEmpty(staffList)) {
            return new Page<>();
        }
        List<Integer> userIds = staffList.stream()
                .map(SchoolStaff::getAccountInfo)
                .flatMap(List::stream)
                .map(AccountInfo::getUserId).collect(Collectors.toList());
        Map<Integer, String> userMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getUsername));


        IPage<SchoolStaffListResponseDTO> returnPage = new Page<>();
        BeanUtils.copyProperties(schoolStaff, returnPage);

        returnPage.setRecords(staffList.stream().map(s -> {
            AccountInfo info = s.getAccountInfo().stream()
                    .filter(accountInfo -> Objects.equals(accountInfo.getSystemCode(), SystemCode.SCHOOL_CLIENT.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("员工数据异常"));
            SchoolStaffListResponseDTO responseDTO = new SchoolStaffListResponseDTO();
            BeanUtils.copyProperties(s, responseDTO);
            responseDTO.setUsername(userMap.get(info.getUserId()));
            return responseDTO;
        }).collect(Collectors.toList()));
        return returnPage;
    }

    /**
     * 保存员工
     *
     * @param user       登录用户
     * @param schoolId   学校ID
     * @param requestDTO 请求DTO
     *
     * @return List<UsernameAndPasswordDTO>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<UsernameAndPasswordDTO> saveSchoolStaff(CurrentUser user, Integer schoolId, SchoolStaffSaveRequestDTO requestDTO) {

        boolean isSameIdCardAndPhone = false;
        Integer id = requestDTO.getId();
        preCheckStaff(user, schoolId, requestDTO);
        SchoolStaff staff = new SchoolStaff();
        if (Objects.nonNull(id)) {
            staff = schoolStaffService.getById(id);
            if (StringUtils.equals(requestDTO.getPhone(), staff.getPhone()) && StringUtils.equals(requestDTO.getIdCard(), staff.getIdCard())) {
                isSameIdCardAndPhone = true;
            }
        }

        staff.setId(id);
        staff.setSchoolId(schoolId);
        staff.setStaffName(requestDTO.getStaffName());
        staff.setGender(IdCardUtil.getGender(requestDTO.getIdCard()));
        staff.setPhone(requestDTO.getPhone());
        staff.setIdCard(requestDTO.getIdCard());
        staff.setStaffType(SchoolStaffTypeEnum.SCHOOL_DOCTOR.getType());
        staff.setRemark(requestDTO.getRemark());

        // 学校管理后台
        TwoTuple<UsernameAndPasswordDTO, AccountInfo> school = saveSchoolAccount(schoolId, requestDTO, staff.getAccountInfo(), user.getId(), isSameIdCardAndPhone);

        // 筛查APP
        TwoTuple<UsernameAndPasswordDTO, AccountInfo> app = saveAppAccount(schoolId, requestDTO, staff.getAccountInfo(), user.getId(), isSameIdCardAndPhone);

        staff.setAccountInfo(Lists.newArrayList(app.getSecond(), school.getSecond()));
        schoolStaffService.saveOrUpdate(staff);
        return Lists.newArrayList(app.getFirst(), school.getFirst());
    }

    /**
     * 更新状态
     *
     * @param id     id
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void editStatus(Integer id, Integer status) {
        SchoolStaff schoolStaff = schoolStaffService.getById(id);
        if (Objects.isNull(schoolStaff) || CollectionUtils.isEmpty(schoolStaff.getAccountInfo())) {
            throw new BusinessException("数据异常");
        }
        List<Integer> userIds = schoolStaff.getAccountInfo().stream().map(AccountInfo::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByIds(userIds);

        if (CollectionUtils.isEmpty(userList)) {
            throw new BusinessException("数据异常");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserIds(userIds).setStatus(status);
        oauthServiceClient.updateUserStatusBatch(userDTO);

        schoolStaff.setStatus(status);
        schoolStaffService.updateById(schoolStaff);
    }

    /**
     * 重置密码
     *
     * @param id id
     *
     * @return List<UsernameAndPasswordDTO>
     */
    public List<UsernameAndPasswordDTO> resetPassword(Integer id) {
        SchoolStaff schoolStaff = schoolStaffService.getById(id);
        if (Objects.isNull(schoolStaff) || CollectionUtils.isEmpty(schoolStaff.getAccountInfo())) {
            throw new BusinessException("数据异常");
        }
        String password = StringUtils.substring(schoolStaff.getPhone(), -4) + StringUtils.substring(schoolStaff.getIdCard(), -4);
        return schoolStaff.getAccountInfo().stream().map(s -> {
            UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
            if (Objects.equals(s.getSystemCode(), SystemCode.SCHOOL_CLIENT.getCode())) {
                usernameAndPasswordDTO.setUsername(oauthServiceClient.getUserDetailByUserId(s.getUserId()).getUsername());
            } else {
                usernameAndPasswordDTO.setUsername(schoolStaff.getPhone());
            }
            usernameAndPasswordDTO.setPassword(password);
            usernameAndPasswordDTO.setSystemCode(s.getSystemCode());
            oauthServiceClient.resetPwd(s.getUserId(), password);
            return usernameAndPasswordDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 是否超过人数配置
     *
     * @param currentUser 登录用户
     * @param schoolId    学校Id
     *
     * @return 是否超过人数配置
     */
    public Boolean isMoreThanConfig(CurrentUser currentUser, Integer schoolId) {
        School school = schoolService.getById(schoolId);

        if (Objects.isNull(school)) {
            throw new BusinessException("学校信息异常!");
        }
        if (!currentUser.isPlatformAdminUser()) {
            Integer staffCount = schoolStaffService.countStaffBySchool(schoolId);
            return school.getVisionTeamCount() < staffCount;
        }
        return false;
    }


    /**
     * 校验
     *
     * @param currentUser 当前用户
     * @param schoolId    学校Id
     * @param requestDTO  请求入参
     */
    private void preCheckStaff(CurrentUser currentUser, Integer schoolId, SchoolStaffSaveRequestDTO requestDTO) {
        Integer id = requestDTO.getId();
        // 是否超过配置
        if (Boolean.TRUE.equals(isMoreThanConfig(currentUser, schoolId))) {
            throw new BusinessException("人数是否超出限制");
        }
        // 检查身份证、手机是否重复
        if (Boolean.TRUE.equals(schoolStaffService.checkByPhone(requestDTO.getPhone(), id))) {
            throw new BusinessException("该手机号码已用，请更换!");
        }

        if (Boolean.TRUE.equals(schoolStaffService.checkByIdCard(requestDTO.getIdCard(), id))) {
            throw new BusinessException("该身份证已用，请更换！");
        }
        List<User> userPhones = oauthServiceClient.getUserBatchByPhones(Lists.newArrayList(requestDTO.getPhone()), SystemCode.SCREENING_CLIENT.getCode());
        if (CollectionUtils.isEmpty(userPhones)) {
            return;
        }

        // 新增的话，存在则抛出异常
        if (Objects.isNull(id)) {
            throw new BusinessException("手机号码重复");
        } else {
            SchoolStaff staff = schoolStaffService.getById(id);
            AccountInfo staffAccount = staff.getAccountInfo().stream().filter(s -> Objects.equals(s.getSystemCode(), SystemCode.SCREENING_CLIENT.getCode())).findFirst().orElseThrow(() -> new BusinessException("数据异常"));
            // 过滤属于自己的
            if (userPhones.stream().filter(s -> !Objects.equals(s.getId(), staffAccount.getUserId())).anyMatch(s -> StringUtils.equals(s.getPhone(), requestDTO.getPhone()))) {
                throw new BusinessException("手机号码重复");
            }
        }
    }

    /**
     * 保存学校账号
     *
     * @param schoolId             学校Id
     * @param requestDTO           请求入参
     * @param accountInfos         账号信息
     * @param createUserId         创建人
     * @param isSameIdCardAndPhone 是否相同的身份证和手机号码
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveSchoolAccount(Integer schoolId, SchoolStaffSaveRequestDTO requestDTO,
                                                                            List<AccountInfo> accountInfos, Integer createUserId,
                                                                            boolean isSameIdCardAndPhone) {

        String password = StringUtils.substring(requestDTO.getPhone(), -4) + StringUtils.substring(requestDTO.getIdCard(), -4);

        Integer systemCode = SystemCode.SCHOOL_CLIENT.getCode();
        TwoTuple<UserDTO, Boolean> userDTO = getUserDTO(accountInfos, systemCode);
        UserDTO appUserDTO = userDTO.getFirst();

        String username = getSchoolUsername(userDTO);
        appUserDTO.setOrgId(schoolId)
                .setUsername(username)
                .setPassword(password)
                .setSystemCode(systemCode)
                .setCreateUserId(createUserId)
                .setRealName(requestDTO.getStaffName())
                .setRemark(requestDTO.getRemark());
        return saveUser(userDTO.getSecond(), appUserDTO, username, password, systemCode, isSameIdCardAndPhone);
    }

    /**
     * 保存筛查端账号
     *
     * @param schoolId             学校Id
     * @param requestDTO           请求入参
     * @param accountInfos         账号信息
     * @param createUserId         创建人
     * @param isSameIdCardAndPhone 是否相同IdCard和手机号码
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveAppAccount(Integer schoolId, SchoolStaffSaveRequestDTO requestDTO,
                                                                         List<AccountInfo> accountInfos, Integer createUserId,
                                                                         Boolean isSameIdCardAndPhone) {

        String username = requestDTO.getPhone();
        String password = StringUtils.substring(requestDTO.getPhone(), -4) + StringUtils.substring(requestDTO.getIdCard(), -4);

        Integer systemCode = SystemCode.SCREENING_CLIENT.getCode();

        TwoTuple<UserDTO, Boolean> userDTO = getUserDTO(accountInfos, systemCode);
        UserDTO appUserDTO = userDTO.getFirst();

        appUserDTO.setOrgId(schoolId)
                .setUsername(username)
                .setPassword(password)
                .setSystemCode(systemCode)
                .setCreateUserId(createUserId)
                .setRealName(requestDTO.getStaffName())
                .setGender(IdCardUtil.getGender(requestDTO.getIdCard()))
                .setPhone(requestDTO.getPhone())
                .setIdCard(requestDTO.getIdCard())
                .setRemark(requestDTO.getRemark())
                .setUserType(UserType.SCREENING_STAFF_TYPE_SCHOOL_DOCTOR.getType());
        return saveUser(userDTO.getSecond(), appUserDTO, username, password, systemCode, isSameIdCardAndPhone);
    }

    /**
     * 获取用户信息
     *
     * @param accountInfos 账号信息
     * @param systemCode   系统编码
     *
     * @return left-UserDTO right-是否插入
     */
    private TwoTuple<UserDTO, Boolean> getUserDTO(List<AccountInfo> accountInfos, Integer systemCode) {
        if (CollectionUtils.isEmpty(accountInfos)) {
            return new TwoTuple<>(new UserDTO(), true);
        }
        AccountInfo result = accountInfos.stream().filter(s -> Objects.equals(s.getSystemCode(), systemCode)).findFirst().orElseThrow(() -> new BusinessException("数据异常！"));
        Integer userId = result.getUserId();
        User user = oauthServiceClient.getUserDetailByUserId(userId);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return new TwoTuple<>(userDTO, false);
    }

    /**
     * 获取学校端的用户名
     *
     * @param userDTO userDTO
     *
     * @return 用户名
     */
    private String getSchoolUsername(TwoTuple<UserDTO, Boolean> userDTO) {
        if (Objects.equals(userDTO.getSecond(), Boolean.FALSE)) {
            return userDTO.getFirst().getUsername();
        }
        UserDTO user = new UserDTO();
        user.setSystemCode(SystemCode.SCREENING_CLIENT.getCode()).setUserType(UserType.SCREENING_STAFF_TYPE_SCHOOL_DOCTOR.getType());
        List<User> userList = oauthServiceClient.getUserList(user);
        if (CollectionUtils.isEmpty(userList)) {
            return CommonConst.SCHOOL_USERNAME_PREFIX + "0";
        }
        return CommonConst.SCHOOL_USERNAME_PREFIX + userList.size();
    }

    /**
     * 保存用户
     *
     * @param isInsert             是否插入
     * @param userDTO              userDTO
     * @param username             账号
     * @param password             密码
     * @param systemCode           系统编码
     * @param isSameIdCardAndPhone 是否相同的身份证和手机号码
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveUser(Boolean isInsert, UserDTO userDTO,
                                                                   String username, String password,
                                                                   Integer systemCode, boolean isSameIdCardAndPhone) {
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
        User user;
        if (Objects.equals(isInsert, Boolean.TRUE)) {
            user = oauthServiceClient.addMultiSystemUser(userDTO);
            usernameAndPasswordDTO.setDisplay(true);
        } else {
            usernameAndPasswordDTO.setDisplay(!isSameIdCardAndPhone);
            user = oauthServiceClient.updateUser(userDTO);
        }

        usernameAndPasswordDTO.setSystemCode(systemCode);
        usernameAndPasswordDTO.setUsername(username);
        usernameAndPasswordDTO.setPassword(password);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setUserId(user.getId());
        accountInfo.setSystemCode(systemCode);

        return new TwoTuple<>(usernameAndPasswordDTO, accountInfo);
    }
}
