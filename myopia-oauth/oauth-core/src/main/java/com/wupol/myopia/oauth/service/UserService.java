package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.constant.OrgScreeningMap;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.oauth.domain.model.*;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
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
        if (Objects.nonNull(existUser)) {
            log.error("已经存在该手机号码:{},systemCode:{}", phone, systemCode);
            throw new BusinessException("已经存在该手机号码");
        }
    }

    /**
     * 管理端创建其他系统的用户(医院端医生用户、学校端老师用户、筛查端筛查人员用户、平台-筛查管理员、平台-医院管理员)
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
        userDTO.setId(user.getId());
        // 创建角色
        createPlatformOrgAdminRole(userDTO);
        return userDTO;
    }

    /**
     * 创建平台机构管理员角色
     *
     * @param userDTO 用户数据
     * @return void
     **/
    private void createPlatformOrgAdminRole(UserDTO userDTO) {
        // 非平台机构管理员则忽略，这里仅创建筛查机构管理员、医院管理员角色
        Integer userType = userDTO.getUserType();
        Integer systemCode = userDTO.getSystemCode();
        if (!SystemCode.MANAGEMENT_CLIENT.getCode().equals(systemCode) || !UserType.isPlatformOrgAdminUser(userType)) {
            return;
        }
        boolean isScreeningOrgAdmin = UserType.SCREENING_ORGANIZATION_ADMIN.getType().equals(userType);
        Integer roleType = isScreeningOrgAdmin ? RoleType.SCREENING_ORGANIZATION.getType() : RoleType.HOSPITAL_ADMIN.getType();
        Integer userId = userDTO.getId();
        // 已经存在该机构的角色，则直接给用户绑定该角色
        Role role = roleService.getOrgFirstOneRole(userDTO.getOrgId(), systemCode, roleType);
        if (Objects.nonNull(role)) {
            userRoleService.save(new UserRole().setUserId(userId).setRoleId(role.getId()));
            return;
        }
        // 生成筛查机构管理员角色
        if (isScreeningOrgAdmin) {
            generateScreeningOrgAdminUserRole(userDTO, userId);
            return;
        }
        // 生成医院管理员角色
        generateOrgAdminUserRole(userDTO, userId, PermissionTemplateType.HOSPITAL_ADMIN.getType(), roleType);
        // 给医院绑定关联筛查机构的角色
        if (Objects.nonNull(userDTO.getAssociateScreeningOrgId())) {
            Role screeningOrgAdminRole = roleService.getScreeningOrgFirstOneRole(userDTO.getAssociateScreeningOrgId());
            userRoleService.save(new UserRole().setUserId(userId).setRoleId(screeningOrgAdminRole.getId()));
        }
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
     * @param userId   用户ID
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
            if (Objects.nonNull(existPhone) && !existPhone.getId().equals(userId)) {
                log.error("已经存在该手机号码:{},SystemCode:{}", user.getPhone(), existUser.getSystemCode());
                throw new BusinessException("已经存在该手机号码");
            }
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
     * @param phones     手机号码集合
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
     * @param idCards    身份证ID
     * @param systemCode 系统编号
     * @param orgId      机构ID
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
     * 生成筛查机构管理员角色并初始其化权限
     *
     * @param userDTO userDTO
     * @param userId  userId
     * @return boolean 是否成功
     */
    private void generateScreeningOrgAdminUserRole(UserDTO userDTO, Integer userId) {
        Integer orgConfigType = userDTO.getOrgConfigType();
        Assert.notNull(orgConfigType, "筛查机构配置类型为空");
        generateOrgAdminUserRole(userDTO, userId, OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(orgConfigType), RoleType.SCREENING_ORGANIZATION.getType());
    }

    private void generateOrgAdminUserRole(UserDTO userDTO, Integer userId, Integer permissionTemplateType, Integer roleType) {
        // 生成角色，并给用户分配角色
        Role role = createScreeningOrgAdminRoleAndAssignUserRole(userDTO, userId, roleType);
        // 根据orgConfigType获取权限集合包
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(permissionTemplateType)
                .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
        // 给角色分配权限
        roleService.assignRolePermission(role.getId(), permissionIds);
    }

    /**
     * 生成筛查机构管理员角色
     *
     * @param userDTO userDTO
     * @param userId  userId
     * @return 角色
     */
    private Role createScreeningOrgAdminRoleAndAssignUserRole(UserDTO userDTO, Integer userId, Integer roleType) {
        Role role = new Role()
                .setOrgId(userDTO.getOrgId())
                .setChName(userDTO.getUsername())
                .setRoleType(roleType)
                .setSystemCode(userDTO.getSystemCode())
                .setCreateUserId(userDTO.getCreateUserId());
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
        generateScreeningOrgAdminUserRole(userDTO, userId);
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