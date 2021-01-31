package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserRole;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService extends BaseService<UserMapper, User> {

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;

    /**
     * 根据用户名查询
     *
     * @param username   用户名
     * @param systemCode 系统编号
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    public User getByUsername(String username, Integer systemCode) {
        try {
            return findOne(new User().setUsername(username).setSystemCode(systemCode));
        } catch (IOException e) {
            throw new BusinessException("获取用户异常", e);
        }
    }

    /**
     * 分页查询用户
     *
     * @param queryParam 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    public IPage<UserWithRole> getUserListPage(UserDTO queryParam) {
        if (Objects.isNull(queryParam.getCurrent()) || Objects.isNull(queryParam.getSize())) {
            throw new BusinessException("页码或页数为空");
        }
        // 防止跨系统查询
        Assert.notNull(queryParam.getSystemCode(), "systemCode不能为空");
        if (!StringUtils.isEmpty(queryParam.getRoleName())) {
            List<Integer> userIds = roleService.getUserIdList(new Role().setChName(queryParam.getRoleName()).setOrgId(queryParam.getOrgId()).setSystemCode(queryParam.getSystemCode()));
            queryParam.setUserIds(userIds);
        }
        Page<UserWithRole> page = new Page<>(queryParam.getCurrent(), queryParam.getSize());
        IPage<UserWithRole> userPage = baseMapper.selectUserListWithRole(page, queryParam);
        // 获取角色信息
        List<UserWithRole> userWithRoles = userPage.getRecords();
        userWithRoles.forEach(userWithRole -> userWithRole.setRoles(roleService.getRoleListByUserId(userWithRole.getId())));
        return userPage.setRecords(userWithRoles);
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User addUser(UserDTO userDTO) throws IOException {
        User existUser = findOne(new User().setPhone(userDTO.getPhone()).setSystemCode(userDTO.getSystemCode()));
        Assert.isNull(existUser, "已经存在该手机号");
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // 绑定角色，判断角色ID与部门ID有效性 —— 是否都存在该角色、且是在所属部门下的、跟用户同系统端的
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
        return userDTO.setId(user.getId());
    }

    /**
     * 管理端创建医院端、学校端管理员，创建筛查端的筛查人员
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User addAdminUser(UserDTO userDTO) {
        if (Objects.isNull(SystemCode.getByCode(userDTO.getSystemCode()))) {
            throw new ValidationException("无效系统编号");
        }
        // 同端手机号码不能重复

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // copy一份admin的权限，生成该医院的角色【需要初始化时插入admin的角色及其权限、系统全部权限】

        // 绑定角色

        return user;
    }

    /**
     * 批量新增筛查人员
     *
     * @param userList 用户列表集合
     * @return java.util.List<java.lang.Integer>
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> addScreeningUserBatch(List<UserDTO> userList) {
        long size = userList.stream().filter(x -> SystemCode.SCREENING_CLIENT.getCode().equals(x.getSystemCode())).count();
        if (size != userList.size()) {
            throw new ValidationException("存在无效系统编号");
        }
        List<User> users = userList.stream().map(x -> {
            User user = new User();
            BeanUtils.copyProperties(x, user);
            return user.setPassword(PasswordGenerator.getScreeningUserPwd(x.getPhone(), x.getIdCard())).setUsername(x.getPhone());
        }).collect(Collectors.toList());
        saveBatch(users);
        return users.stream().map(User::getId).collect(Collectors.toList());
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @param password 密码
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User resetPwd(Integer userId, String password) {
        Assert.notNull(password, "密码不能为空");
        User user = new User().setId(userId).setPassword(new BCryptPasswordEncoder().encode(password));
        Assert.isTrue(updateById(user), "重置密码失败");
        return user;
    }

    /** 修改用户信息 */
    @Transactional(rollbackFor = Exception.class)
    public UserWithRole updateUser(UserDTO user) throws Exception {
        String pwd = user.getPassword();
        if (!StringUtils.isEmpty(pwd)) {
            user.setPassword(new BCryptPasswordEncoder().encode(pwd));
        }
        // TODO: 手机号不为空，则判断是否唯一
        if (!updateById(user)) {
            throw new Exception("更新用户信息失败");
        }
        UserDTO newUser = new UserDTO();
        newUser.setId(user.getId());
        List<UserWithRole> userWithRoles = baseMapper.selectUserListWithRole(newUser);
        return userWithRoles.get(0);

    }

    /**
     * 获取用户列表（支持模糊查询）
     *
     * @param queryParam 搜索参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    public List<User> getUserListWithLike(UserDTO queryParam) {
        Assert.notNull(queryParam, "查询参数不能为空");
        // 防止全表查询
        Assert.notNull(queryParam.getSystemCode(), "systemCode不能为空");
        return baseMapper.selectUserList(queryParam);
    }
}