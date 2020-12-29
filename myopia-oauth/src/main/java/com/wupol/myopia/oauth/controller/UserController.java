package com.wupol.myopia.oauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserRole;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
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
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping()
    public User addUser(@RequestBody UserDTO userDTO) {
        // 参数判空校验
        // 手机号码不能重复

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userService.save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // 绑定角色，判断角色ID与部门ID有效性——是否都存在该角色、且是在所属部门下的、跟用户同系统端的
        List<Integer> roleIds = userDTO.getRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new BusinessException("角色不能为空");
        }
        List<Role> roles = roleService.listByIds(roleIds);
        long size = roles.stream().filter(role -> role.getSystemCode().equals(user.getSystemCode()) && role.getOrgId().equals(user.getOrgId())).count();
        if (size != roleIds.size()) {
            throw new BusinessException("无效角色");
        }
        List<UserRole> userRoles = roleIds.stream().map(roleId -> new UserRole().setUserId(user.getId()).setRoleId(roleId)).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
        return user;
    }

    /**
     * 修改用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Object modifyUser(@RequestBody UserDTO user) {
        return userService.updateById(user);
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public User resetPwd(@PathVariable Integer userId) {
        String pwd = PasswordGenerator.getManagementUserPwd();
        User user = new User().setId(userId).setPassword(new BCryptPasswordEncoder().encode(pwd));
        userService.updateById(user);
        user = userService.getById(userId);
        return user.setPassword(pwd);
    }

    @PostMapping("/admin")
    public User addAdminUser(@RequestBody UserDTO userDTO) {
        // 参数判空校验
        // 系统编号非空且有效
        // 同端手机号码不能重复
        // 判断部门ID有效性——是否属于当前用户所属部门或其下面的部门（admin则无限制）

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userService.save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // 绑定角色

        return user;
    }
}
