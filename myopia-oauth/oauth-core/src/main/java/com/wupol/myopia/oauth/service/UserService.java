package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.constant.OrgScreeningMap;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.oauth.domain.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
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
    @Autowired
    private DistrictPermissionService districtPermissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    /**
     * 根据用户名查询
     *
     * @param username   用户名
     * @param systemCode 系统编号
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    public User getByUsername(String username, Integer systemCode) {
        return findOne(new User().setUsername(username).setSystemCode(systemCode));
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
        Page<UserWithRole> page = new Page<>(queryParam.getCurrent(), queryParam.getSize());
        if (!StringUtils.isEmpty(queryParam.getRoleName())) {
            List<Integer> userIds = roleService.getUserIdList(new Role().setChName(queryParam.getRoleName()).setOrgId(queryParam.getOrgId()).setSystemCode(queryParam.getSystemCode()));
            if (CollectionUtils.isEmpty(userIds)) {
                return page;
            }
            queryParam.setUserIds(userIds);
        }
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
    public User addUser(UserDTO userDTO) {
        // 校验参数
        validateParam(userDTO.getPhone(), userDTO.getSystemCode());
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // TODO：家长端不用创建角色
        if (SystemCode.PATENT_CLIENT.getCode().equals(userDTO.getSystemCode())) {
            return userDTO.setId(user.getId());
        }
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
        List<UserRole> userRoles = roleIds.stream().distinct().map(roleId -> new UserRole().setUserId(user.getId()).setRoleId(roleId)).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
        return userDTO.setId(user.getId());
    }

    private void validateParam(String phone, Integer systemCode) {
        Assert.notNull(SystemCode.getByCode(systemCode), "系统编号为空或无效");
        if (StringUtils.isEmpty(phone)) {
            return;
        }
        User existUser = findOne(new User().setPhone(phone).setSystemCode(systemCode));
        Assert.isNull(existUser, "已经存在该手机号码");
    }

    /**
     * 管理端创建其他系统的用户(医院端、学校端、筛查端、筛查管理端)
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User addMultiSystemUser(UserDTO userDTO) {
        validateParam(userDTO.getPhone(), userDTO.getSystemCode());
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // 根据系统编号，绑定初始化的角色（每个端只有一个，非一个则报异常）
        boolean isScreeningAdmin = SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode().equals(user.getSystemCode());
        if (isScreeningAdmin) {
            // 根据筛查机构配置赋予权限
            boolean result = generateOrgRole(userDTO, user.getId());
            if (!result) {
                Role role = roleService.findOne(new Role().setSystemCode(userDTO.getSystemCode()));
                userRoleService.save(new UserRole().setUserId(user.getId()).setRoleId(role.getId()));
            }
        }
        return userDTO.setId(user.getId());
    }

    /**
     * 批量新增筛查人员
     *
     * @param userList 用户列表集合
     * @return java.util.List<User>
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<User> addScreeningUserBatch(List<UserDTO> userList) {
        long size = userList.stream().filter(x -> SystemCode.SCREENING_CLIENT.getCode().equals(x.getSystemCode())).count();
        if (size != userList.size()) {
            throw new ValidationException("存在无效系统编号");
        }
        List<User> users = userList.stream().map(x -> {
            User user = new User();
            BeanUtils.copyProperties(x, user);
            return user.setPassword(new BCryptPasswordEncoder().encode(x.getPassword()));
        }).collect(Collectors.toList());
        saveBatch(users);
        return users;
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

    /**
     * 更新用户信息
     *
     * @param user 新用户信息数据
     * @return com.wupol.myopia.oauth.domain.model.UserWithRole
     **/
    @Transactional(rollbackFor = Exception.class)
    public UserWithRole updateUser(UserDTO user) {
        Integer userId = user.getId();
        User existUser = getById(userId);
        Assert.notNull(existUser, "该用户不存在");
        if (StringUtils.hasLength(user.getPhone())) {
            User existPhone = findOne(new User().setPhone(user.getPhone()).setSystemCode(existUser.getSystemCode()));
            Assert.isTrue(Objects.isNull(existPhone) || existPhone.getId().equals(userId), "已经存在该手机号码");
        }
        if (StringUtils.hasLength(user.getPassword())) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }
        // 更新用户
        if (!updateById(user)) {
            throw new BusinessException("更新用户信息失败");
        }
        //筛查机构更新权限
        Integer orgConfigType = user.getOrgConfigType();
        updateScreeningOrgRolePermission(orgConfigType, userId);
        // 获取用户最新信息
        UserDTO newUser = new UserDTO();
        newUser.setId(userId);
        UserWithRole userWithRole = baseMapper.selectUserListWithRole(newUser).get(0);
        // 绑定新角色
        List<Integer> roleIds = user.getRoleIds();
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<Role> roles = roleService.listByIds(roleIds);
            long size = roles.stream().filter(role -> role.getSystemCode().equals(user.getSystemCode()) && role.getOrgId().equals(user.getOrgId())).count();
            if (size != roleIds.size()) {
                throw new BusinessException("无效角色");
            }
            userRoleService.remove(new UserRole().setUserId(userId));
            List<UserRole> userRoles = roleIds.stream().distinct().map(roleId -> new UserRole().setUserId(userId).setRoleId(roleId)).collect(Collectors.toList());
            userRoleService.saveBatch(userRoles);
            return userWithRole.setRoles(roles);
        }
        // 获取用户角色信息
        return userWithRole.setRoles(roleService.getRoleListByUserId(userId));

    }

    /**
     * 获取用户列表（仅支持按名称模糊查询）
     *
     * @param userName 用户名
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    public List<User> getUserListByNameLike(String userName) {
        Assert.notNull(userName, "用户名不能为空");
        UserDTO queryParam = new UserDTO();
        queryParam.setRealName(userName);
        return baseMapper.selectUserList(queryParam);
    }

    /**
     * 根据手机号码批量查询
     *
     * @param phones 手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    public List<User> getUserBatchByPhones(List<String> phones, Integer systemCode) {
        Assert.notEmpty(phones, "手机号码不能为空");
        Assert.notNull(systemCode, "系统编号不能为空");
        UserDTO queryParam = new UserDTO();
        queryParam.setSystemCode(systemCode);
        queryParam.setPhones(phones);
        return baseMapper.selectUserList(queryParam);
    }

    /**
     * 根据手机号码批量查询
     *
     * @param idCards 身份证ID
     * @param systemCode 系统编号
     * @param orgId 机构ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    public List<User> getUserBatchByIdCards(List<String> idCards, Integer systemCode, Integer orgId) {
        Assert.notEmpty(idCards, "身份证号码不能为空");
        Assert.notNull(systemCode, "系统编号不能为空");
        Assert.notNull(orgId, "机构ID不能为空");
        UserDTO queryParam = new UserDTO();
        queryParam.setSystemCode(systemCode);
        queryParam.setIdCards(idCards);
        queryParam.setOrgId(orgId);
        return baseMapper.selectUserList(queryParam);
    }

    /**
     * 根据机构orgId获取userId
     *
     * @param systemCode 系统编号
     * @param orgIds     机构ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    public List<User> getIdsByOrgIds(List<Integer> orgIds, Integer systemCode) {
        Assert.notEmpty(orgIds, "机构orgId不能为空");
        Assert.notNull(systemCode, "系统编号不能为空");
        UserDTO queryParam = new UserDTO();
        queryParam.setSystemCode(systemCode);
        queryParam.setOrgIds(orgIds);
        return baseMapper.selectUserList(queryParam);
    }

    /**
     * 生成角色并初始化权限
     *
     * @param userDTO userDTO
     * @param userId  userId
     * @return boolean 是否成功
     */
    private boolean generateOrgRole(UserDTO userDTO, Integer userId) {
        Integer orgConfigType = userDTO.getOrgConfigType();
        if (Objects.nonNull(orgConfigType)) {
            // 根据orgConfigType获取权限集合包
            List<Integer> permissionIds = districtPermissionService.getByTemplateType(OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(orgConfigType))
                    .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());

            Role role = saveOrgRole(userDTO, userId);
            roleService.assignRolePermission(role.getId(), permissionIds);
            return true;
        }
        return false;
    }

    /**
     * 生成机构角色
     *
     * @param userDTO userDTO
     * @param userId  userId
     * @return 角色
     */
    private Role saveOrgRole(UserDTO userDTO, Integer userId) {
        Role role = new Role();
        role.setOrgId(userDTO.getOrgId());
        role.setChName(userDTO.getUsername());
        role.setRoleType(RoleType.SCREENING_ORGANIZATION.getType());
        role.setSystemCode(userDTO.getSystemCode());
        role.setCreateUserId(userDTO.getCreateUserId());
        roleService.save(role);
        userRoleService.save(new UserRole().setUserId(userId).setRoleId(role.getId()));
        return role;
    }

    public void resetScreeningOrg(UserDTO userDTO) {
        User user = baseMapper.selectByOrgId(userDTO.getOrgId());
        Integer userId = user.getId();
        // 删除user_role
        userRoleService.remove(new UserRole().setUserId(userId));
        // 创建role和user_role
        Role role = saveOrgRole(userDTO, userId);
        // 更新权限
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(userDTO.getOrgConfigType()))
                .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(permissionIds)) {
            roleService.assignRolePermission(role.getId(), permissionIds);
        }
    }

    private void updateScreeningOrgRolePermission(Integer orgConfigType, Integer userId) {
        if (Objects.isNull(orgConfigType)) {
            return;
        }
        // 查询角色
        UserRole role = userRoleService.findOne(new UserRole().setUserId(userId));
        Integer roleId = role.getRoleId();
        // 删除改角色所有的所有权限
        rolePermissionService.remove(new RolePermission().setRoleId(roleId));
        // 查找模板的权限集合包
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(orgConfigType))
                .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(permissionIds)) {
            roleService.assignRolePermission(roleId, permissionIds);
        }
    }

    /**
     * 通过UserIds获取用户
     *
     * @param userIds 用户Ids
     * @return List<User>
     */
    public List<User> getByUserIds(List<Integer> userIds) {
        UserDTO queryParam = new UserDTO();
        queryParam.setUserIds(userIds);
        return baseMapper.selectUserList(queryParam);
    }
}