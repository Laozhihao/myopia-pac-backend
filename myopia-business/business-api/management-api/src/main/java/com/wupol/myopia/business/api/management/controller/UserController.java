package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import com.wupol.myopia.business.management.service.UserService;
import com.wupol.myopia.business.management.validator.UserAddValidatorGroup;
import com.wupol.myopia.business.management.validator.UserUpdateValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/user")
public class UserController {

    @Autowired
    private OauthService oauthService;
    @Autowired
    private UserService userService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 分页获取用户列表
     *
     * @param queryParam     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public IPage<UserDTO> getUserListPage(UserDTOQuery queryParam,
                                          @RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUserListPage(queryParam, current, size, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 新增用户
     *
     * @param user 用户数据
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @PostMapping()
    public UserDTO addUser(@RequestBody @Validated(value = UserAddValidatorGroup.class) UserDTO user) {
        return userService.addUser(user, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 更新用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Object updateUser(@RequestBody @Validated(value = UserUpdateValidatorGroup.class) UserDTO user) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        userService.validatePermission(currentUser, user.getId());
        return userService.updateUser(user, currentUser);
    }

    /**
     * 重置管理端用户密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public UserDTO resetPwd(@PathVariable("userId") Integer userId) {
        userService.validatePermission(CurrentUserUtil.getCurrentUser(), userId);
        return userService.resetPwd(userId);
    }

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @GetMapping("/{userId}")
    public UserDTO getUserDetailByUserId(@PathVariable("userId") Integer userId) {
        UserDTO userDTO = oauthService.getUserDetailByUserId(userId);
        Assert.notNull(userDTO, "不存在该用户");
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Assert.isTrue(currentUser.isPlatformAdminUser() || currentUser.getOrgId().equals(userDTO.getOrgId()), "没有访问该用户信息权限");
        // 屏蔽密码
        userDTO.setPassword(null);
        // 管理端 - 平台管理员或政府部门人员用户
        if (SystemCode.MANAGEMENT_CLIENT.getCode().equals(userDTO.getSystemCode())) {
            GovDept govDept = govDeptService.getById(userDTO.getOrgId());
            return userDTO.setOrgName(govDept.getName()).setDistrictId(govDept.getDistrictId());
        }
        // 管理端 - 筛查机构管理员用户
        if (SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode().equals(userDTO.getSystemCode())) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(userDTO.getOrgId());
            return userDTO.setOrgName(screeningOrganization.getName()).setDistrictId(screeningOrganization.getDistrictId());
        }
        throw new BusinessException("不支持查询该用户");
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 用户状态
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @PutMapping("/{userId}/{status}")
    public UserDTO updateUserStatus(@PathVariable("userId") Integer userId, @PathVariable("status") Integer status) {
        userService.validatePermission(CurrentUserUtil.getCurrentUser(), userId);
        return userService.updateUserStatus(userId, status);
    }
}
