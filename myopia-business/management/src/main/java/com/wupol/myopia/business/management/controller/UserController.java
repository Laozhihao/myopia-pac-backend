package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private UserService userService;

    /**
     * 分页获取用户列表 TODO: 支持根据日期查询
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
        return userService.getUserListPage(queryParam, current, size, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 新增用户 TODO: 参数判空校验
     *
     * @param user 用户数据
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @PostMapping()
    public UserDTO addUser(@RequestBody UserDTO user) {
        return userService.addUser(user, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 更新用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Object updateUser(@RequestBody UserDTO user) {
        // TODO：如果部门ID不为空，需要判断是否合法（为当前登录用户所属部门或名下子部门）
        // TODO: 不能更新自己、非管理员或者admin用户不能修改用户
        // 该接口不允许更新密码
        return oauthServiceClient.modifyUser(user.setPassword(null));
    }

    /**
     * 重置管理端用户密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public Object resetPwd(@PathVariable("userId") Integer userId) {
        // TODO: 获取用户详情，判断用户是否存在，用户所属部门是否属于当前登录用户的下面
        return oauthServiceClient.resetPwd(userId);
    }

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @GetMapping("/{userId}")
    public UserDTO getUserDetailByUserId(@PathVariable("userId") Integer userId) {
        // TODO: 只能获取自己所属部门下的用户
        return oauthServiceClient.getUserDetailByUserId(userId).getData();
    }
}
