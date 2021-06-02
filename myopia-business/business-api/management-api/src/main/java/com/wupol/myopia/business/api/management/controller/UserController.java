package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.UserQueryDTO;
import com.wupol.myopia.business.api.management.domain.vo.UserVO;
import com.wupol.myopia.business.api.management.service.UserService;
import com.wupol.myopia.business.api.management.validator.UserAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UserUpdateValidatorGroup;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/user")
public class UserController {

    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private UserService userService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private DistrictService districtService;

    /**
     * 分页获取用户列表
     *
     * @param queryParam     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public IPage<UserVO> getUserListPage(UserQueryDTO queryParam,
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
    public User addUser(@RequestBody @Validated(value = UserAddValidatorGroup.class) UserQueryDTO user) {
        return userService.addUser(user, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 更新用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public UserVO updateUser(@RequestBody @Validated(value = UserUpdateValidatorGroup.class) UserQueryDTO user) {
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
    public UserVO resetPwd(@PathVariable("userId") Integer userId) {
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
    public UserVO getUserDetailByUserId(@PathVariable("userId") Integer userId) {
        User user = oauthServiceClient.getUserDetailByUserId(userId);
        Assert.notNull(user, "不存在该用户");
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Assert.isTrue(currentUser.isPlatformAdminUser() || currentUser.getOrgId().equals(user.getOrgId()), "没有访问该用户信息权限");
        UserVO userVO = JSON.parseObject(JSON.toJSONString(user), UserVO.class);
        // 屏蔽密码
        userVO.setPassword(null);
        // 管理端 - 平台管理员或政府部门人员用户
        if (SystemCode.MANAGEMENT_CLIENT.getCode().equals(user.getSystemCode())) {
            GovDept govDept = govDeptService.getById(user.getOrgId());
            if (Objects.nonNull(govDept.getDistrictId())) {
                userVO.setDistrictDetail(districtService.getDistrictPositionDetailById(govDept.getDistrictId()));
            }
            return userVO.setOrgName(govDept.getName()).setDistrictId(govDept.getDistrictId());
        }
        // 管理端 - 筛查机构管理员用户
        if (SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode().equals(user.getSystemCode())) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(user.getOrgId());
            if (Objects.nonNull(screeningOrganization.getDistrictId())) {
                userVO.setDistrictDetail(districtService.getDistrictPositionDetailById(screeningOrganization.getDistrictId()));
            }
            return userVO.setOrgName(screeningOrganization.getName()).setDistrictId(screeningOrganization.getDistrictId());
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
    public User updateUserStatus(@PathVariable("userId") Integer userId, @PathVariable("status") Integer status) {
        userService.validatePermission(CurrentUserUtil.getCurrentUser(), userId);
        return userService.updateUserStatus(userId, status);
    }
}
