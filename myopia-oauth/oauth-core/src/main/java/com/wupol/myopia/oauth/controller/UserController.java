package com.wupol.myopia.oauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import com.wupol.myopia.oauth.service.UserService;
import com.wupol.myopia.oauth.validator.UserValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表 - 分页
     *
     * @param queryParam 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    @GetMapping("/page")
    public IPage<UserWithRole> getUserListPage(UserDTO queryParam) {
        return userService.getUserListPage(queryParam);
    }

    /**
     * 新增用户
     * TODO: 参数判空校验
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping()
    public User addUser(@RequestBody UserDTO userDTO) {
        return userService.addUser(userDTO);
    }

    /**
     * 修改用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public UserWithRole updateUser(@RequestBody UserDTO user) {
        return userService.updateUser(user);
    }

    @PutMapping("/realname")
    public Integer updateUserRealName(String realName, Integer byOrgId, Integer bySystemCode, Integer byUserType) {
        return userService.updateUserRealName(realName, byOrgId, bySystemCode, byUserType);
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public User resetPwd(@PathVariable Integer userId, String password) {
        return userService.resetPwd(userId, password);
    }

    /**
     * 管理端创建其他系统的用户(医院端、学校端、筛查端)
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping("/multi/system")
    public User addMultiSystemUser(@RequestBody @Validated(value = UserValidatorGroup.class) UserDTO userDTO) {
        return userService.addMultiSystemUser(userDTO);
    }

    /**
     * 批量新增筛查人员
     *
     * @param userList 用户数据集合
     * @return java.util.List<java.lang.Integer>
     **/
    @PostMapping("/screening/batch")
    public List<User> addScreeningUserBatch(@RequestBody List<UserDTO> userList) {
        return userService.addScreeningUserBatch(userList);
    }

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/id")
    public List<User> getUserBatchByIds(@RequestParam("userIds") List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return userService.listByIds(userIds);
    }

    /**
     * 根据手机号码批量获取用户
     *
     * @param phones 手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/phone")
    public List<User> getUserBatchByPhones(@RequestParam("phones") List<String> phones, @RequestParam("systemCode") Integer systemCode) {
        if (CollectionUtils.isEmpty(phones)) {
            return new ArrayList<>();
        }
        return userService.getUserBatchByPhones(phones, systemCode);
    }

    /**
     * 根据身份证号码批量获取用户
     *
     * @param idCards 手机号码集合
     * @param systemCode 系统编号
     * @param orgId 机构ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/idCard")
    public List<User> getUserBatchByIdCard(@RequestParam("idCards") List<String> idCards, @RequestParam("systemCode") Integer systemCode, @RequestParam("orgId") Integer orgId) {
        if (CollectionUtils.isEmpty(idCards)) {
            return new ArrayList<>();
        }
        return userService.getUserBatchByIdCards(idCards, systemCode, orgId);
    }

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/{userId}")
    public User getUserDetailByUserId(@PathVariable("userId") Integer userId) {
        return userService.getById(userId);
    }

    /**
     * 获取用户列表（仅支持按名称模糊查询）【可跨端查询】
     *
     * @param queryParam 搜索参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/getByName")
    public List<User> getUserListByNameLike(UserDTO queryParam) {
        return userService.getUserListByNameLike(queryParam.getRealName());
    }

    @GetMapping("/list")
    public List<User> getUserList(@SpringQueryMap UserDTO param) {
        return userService.getUserList(param);
    }

    /**
     * 统计
     *
     * @param queryParam 查询条件
     * @return java.lang.Integer
     **/
    @GetMapping("/count")
    public Integer count(UserDTO queryParam) {
        return userService.count(queryParam);
    }

    /**
     * 根据机构orgId获取userId
     *
     * @param orgIds     机构ID
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/orgIds")
    public List<User> getIdsByOrgIds(@RequestParam("orgIds") List<Integer> orgIds, @RequestParam("systemCode") Integer systemCode, @RequestParam("userType") Integer userType) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return userService.getIdsByOrgIds(orgIds, systemCode, userType);
    }

    /**
     * 通过UserIds获取用户列表
     *
     * @param userIds 用户Ids
     * @return 用户列表
     */
    @GetMapping("/batch/userIds")
    public List<User> getByUserIds(@RequestParam("userIds") List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return userService.getByUserIds(userIds);
    }

    /**
     * 批量更新用户状态
     *
     * @param user 用户数据
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PutMapping("/status/batch")
    public boolean updateUserStatusBatch(@RequestBody UserDTO user) {
        Assert.isTrue(Objects.nonNull(user) && !CollectionUtils.isEmpty(user.getUserIds()), "用户Id不能为空");
        return userService.updateBatchById(user.getUserIds().stream().map(userId -> new User().setId(userId).setStatus(user.getStatus())).collect(Collectors.toList()));
    }

    /**
     * 移除医院管理员关联的筛查机构管理员角色
     *
     * @param hospitalId 医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @DeleteMapping("/hospital/associated/role")
    public void removeHospitalUserAssociatedScreeningOrgAdminRole(@NotNull(message = "hospitalId不能为空") Integer hospitalId,
                                                                  @NotNull(message = "associateScreeningOrgId不能为空") Integer associateScreeningOrgId) {
        userService.removeHospitalUserAssociatedScreeningOrgAdminRole(hospitalId, associateScreeningOrgId);
    }

    /**
     * 给医院管理员添加关联的筛查机构管理员角色
     *
     * @param hospitalId 医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @PostMapping("/hospital/associated/role")
    public void addHospitalUserAssociatedScreeningOrgAdminRole(@NotNull(message = "hospitalId不能为空") Integer hospitalId,
                                                               @NotNull(message = "associateScreeningOrgId不能为空") Integer associateScreeningOrgId) {
        userService.addHospitalUserAssociatedScreeningOrgAdminRole(hospitalId, associateScreeningOrgId);
    }

    /**
     * 更新医生用户的角色
     *
     * @param hospitalId 医院ID
     * @param serviceType 服务类型
     * @return void
     **/
    @PutMapping("/hospital/role")
    public void updateHospitalRole(@NotNull(message = "hospitalId不能为空") Integer hospitalId,
                                 @NotNull(message = "serviceType不能为空") Integer serviceType) {
        userService.updateHospitalRoleBatch(hospitalId, serviceType);
    }
}
