package com.wupol.myopia.business.aggregation.student.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolStaffTypeEnum;
import com.wupol.myopia.business.core.school.domain.dos.AccountInfo;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffSaveRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
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
import java.util.Objects;
import java.util.Optional;

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


    /**
     * 保存员工
     *
     * @param requestDTO 请求DTO
     * @param schoolId   学校ID
     *
     * @return List<UsernameAndPasswordDTO>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<UsernameAndPasswordDTO> saveSchoolStaff(Integer schoolId, SchoolStaffSaveRequestDTO requestDTO) {

        Integer id = requestDTO.getId();
        SchoolStaff staff = schoolStaffService.getById(id);
        if (Objects.isNull(staff)) {
            staff = new SchoolStaff();
        }

        staff.setId(id);
        staff.setSchoolId(schoolId);
        staff.setStaffName(requestDTO.getStaffName());
        staff.setGender(requestDTO.getGender());
        staff.setPhone(requestDTO.getPhone());
        staff.setIdCard(requestDTO.getIdCard());
        staff.setStaffType(SchoolStaffTypeEnum.SCHOOL_DOCTOR.getType());
        staff.setRemark(requestDTO.getRemark());

        // 学校管理后台
        TwoTuple<UsernameAndPasswordDTO, AccountInfo> school = saveSchoolAccount(schoolId, requestDTO, staff.getAccountInfo());

        // 筛查APP
        TwoTuple<UsernameAndPasswordDTO, AccountInfo> app = saveAppAccount(schoolId, requestDTO, staff.getAccountInfo());

        staff.setAccountInfo(Lists.newArrayList(app.getSecond(), school.getSecond()));
        schoolStaffService.saveOrUpdate(staff);
        return Lists.newArrayList(app.getFirst(), school.getFirst());
    }

    /**
     * 保存学校账号
     *
     * @param schoolId     学校Id
     * @param requestDTO   请求入参
     * @param accountInfos 账号信息
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveSchoolAccount(Integer schoolId, SchoolStaffSaveRequestDTO requestDTO, List<AccountInfo> accountInfos) {

        String password = StringUtils.substring(requestDTO.getPhone(), -4) + StringUtils.substring(requestDTO.getIdCard(), -4);

        Integer systemCode = SystemCode.SCHOOL_CLIENT.getCode();
        TwoTuple<UserDTO, Boolean> userDTO = getUserDTO(accountInfos, systemCode);
        UserDTO appUserDTO = userDTO.getFirst();

        String username = getSchoolUsername(userDTO);
        appUserDTO.setOrgId(schoolId).setUsername(username).setPassword(password).setSystemCode(systemCode).setCreateUserId(CommonConst.USER_ID).setRealName(requestDTO.getStaffName()).setGender(requestDTO.getGender()).setRemark(requestDTO.getRemark());
        return saveUser(userDTO.getSecond(), appUserDTO, username, password, systemCode);
    }

    /**
     * 保存筛查端账号
     *
     * @param schoolId     学校Id
     * @param requestDTO   请求入参
     * @param accountInfos 账号信息
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveAppAccount(Integer schoolId, SchoolStaffSaveRequestDTO requestDTO, List<AccountInfo> accountInfos) {

        String username = requestDTO.getPhone();
        String password = StringUtils.substring(requestDTO.getPhone(), -4) + StringUtils.substring(requestDTO.getIdCard(), -4);

        Integer systemCode = SystemCode.SCREENING_CLIENT.getCode();

        TwoTuple<UserDTO, Boolean> userDTO = getUserDTO(accountInfos, systemCode);
        UserDTO appUserDTO = userDTO.getFirst();

        appUserDTO.setOrgId(schoolId).setUsername(username).setPassword(password).setSystemCode(systemCode).setCreateUserId(CommonConst.USER_ID).setRealName(requestDTO.getStaffName()).setGender(requestDTO.getGender()).setPhone(requestDTO.getPhone()).setIdCard(requestDTO.getIdCard()).setRemark(requestDTO.getRemark()).setUserType(UserType.SCREENING_STAFF_TYPE_SCHOOL_DOCTOR.getType());
        return saveUser(userDTO.getSecond(), appUserDTO, username, password, systemCode);
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
        Optional<AccountInfo> result = accountInfos.stream().filter(s -> Objects.equals(s.getSystemCode(), systemCode)).findFirst();
        if (result.isPresent()) {
            Integer userId = result.get().getUserId();
            User user = oauthServiceClient.getUserDetailByUserId(userId);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return new TwoTuple<>(userDTO, false);
        }
        return new TwoTuple<>(new UserDTO(), true);
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
            return "jsfkxd0";
        }
        return "jsfkxd" + userList.size();
    }

    /**
     * 保存用户
     *
     * @param isInsert   是否插入
     * @param userDTO    userDTO
     * @param username   账号
     * @param password   密码
     * @param systemCode 系统编码
     *
     * @return TwoTuple<UsernameAndPasswordDTO, AccountInfo> left-账号密码 right-账号信息
     */
    private TwoTuple<UsernameAndPasswordDTO, AccountInfo> saveUser(Boolean isInsert, UserDTO userDTO, String username, String password, Integer systemCode) {
        User user;
        if (Objects.equals(isInsert, Boolean.TRUE)) {
            user = oauthServiceClient.addMultiSystemUser(userDTO);
        } else {
            user = oauthServiceClient.updateUser(userDTO);
        }

        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
        usernameAndPasswordDTO.setSystemCode(systemCode);
        usernameAndPasswordDTO.setUsername(username);
        usernameAndPasswordDTO.setPassword(password);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setUserId(user.getId());
        accountInfo.setSystemCode(systemCode);

        return new TwoTuple<>(usernameAndPasswordDTO, accountInfo);
    }
}
