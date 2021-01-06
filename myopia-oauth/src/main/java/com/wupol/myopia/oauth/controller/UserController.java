package com.wupol.myopia.oauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import com.wupol.myopia.oauth.service.UserRoleService;
import com.wupol.myopia.oauth.service.UserService;
import com.wupol.myopia.oauth.validator.UserValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/oauth/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;

    /**
     * 获取用户列表
     *
     * @param queryParam 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    @GetMapping("/list")
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
    public User modifyUser(@RequestBody UserDTO user) throws Exception {
        String pwd = user.getPassword();
        if (!StringUtils.isEmpty(pwd)) {
            user.setPassword(new BCryptPasswordEncoder().encode(pwd));
        }
        // TODO: 手机号不为空，则判断是否唯一
        if (!userService.updateById(user)) {
            throw new Exception("更新用户信息失败");
        }
        if (!userRoleService.updateByRoleIds(user.getId(), user.getRoleIds())) {
            throw new Exception("更新用户角色失败");
        }
        return user.setPassword(pwd);
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public User resetPwd(@PathVariable Integer userId) {
        return userService.resetPwd(userId);
    }

    /**
     * 管理端创建医院端、学校端、筛查端的管理员
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping("/admin")
    public User addAdminUser(@RequestBody @Validated(value = UserValidatorGroup.class) UserDTO userDTO) {
        return userService.addAdminUser(userDTO);
    }

    /**
     * 批量新增筛查人员
     *
     * @param userList 用户数据集合
     * @return java.util.List<java.lang.Integer>
     **/
    @PostMapping("/screening/batch")
    public List<Integer> addScreeningUserBatch(@RequestBody List<UserDTO> userList) {
        return userService.addScreeningUserBatch(userList);
    }

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch")
    public List<User> getUserBatchByIds(@RequestParam("userIds") List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return userService.listByIds(userIds);
    }
}
