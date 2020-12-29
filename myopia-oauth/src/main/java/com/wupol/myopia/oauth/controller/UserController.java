package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserRole;
import com.wupol.myopia.oauth.service.RoleService;
import com.wupol.myopia.oauth.service.UserRoleService;
import com.wupol.myopia.oauth.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRoleService userRoleService;

    @GetMapping("/list")
    public Object getUserListPage(UserDTO queryParam) {
        return userService.getUserListPage(queryParam);
    }

    @PostMapping()
    public Object addUser(@RequestBody UserDTO data) {
        // 参数判空校验
        // 系统编号非空且有效
        // 判断部门ID有效性——是否属于当前用户所属部门或其下面的部门（admin则无限制）
        // 角色ID有效性判断——是否都存在该角色、且是在所属部门下的（admin则无限制）、跟用户同系统的

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(data, user);
        userService.save(user.setPassword(new BCryptPasswordEncoder().encode(data.getPassword())));
        List<Integer> roleIds = data.getRoleIds();
        // 绑定角色
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new BusinessException("角色不能为空");
        }
        List<Role> roles = roleService.listByIds(roleIds);
        long size = roles.stream().filter(role -> role.getSystemCode().equals(user.getSystemCode())).count();
        if (size != roleIds.size()) {
            throw new BusinessException("无效角色");
        }
        List<UserRole> userRoles = roleIds.stream().map(roleId -> new UserRole().setUserId(user.getId()).setRoleId(roleId)).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
        return user;
    }

    @PutMapping()
    public Object modifyUser(@RequestBody User user) {
        return userService.updateById(user);
    }

    @PutMapping("/password/{userId}")
    public Object resetPwd(@PathVariable Integer userId) {
        return userService.updateById(new User().setId(userId).setPassword(new BCryptPasswordEncoder().encode("123456")));
    }
}
