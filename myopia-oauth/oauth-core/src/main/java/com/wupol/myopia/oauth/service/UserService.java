package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.*;
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
import java.util.Collections;
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
        // 家长端不用创建角色
        if (SystemCode.PARENT_CLIENT.getCode().equals(userDTO.getSystemCode())) {
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

    public Integer updateUserRealName(String realName, Integer byOrgId, Integer bySystemCode, Integer byUserType) {
        return baseMapper.updateUserRealName(realName, byOrgId, bySystemCode, byUserType);
    }

    /**
     * 检验参数
     *
     * @param phone 手机号码
     * @param systemCode 系统编号
     * @return void
     **/
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
        // 1. 筛查机构管理员、医院管理员角色
        if (SystemCode.MANAGEMENT_CLIENT.getCode().equals(userDTO.getSystemCode())) {
            createPlatformOrgAdminRole(userDTO);
            return userDTO;
        }
        // 2. 医生
        if (SystemCode.HOSPITAL_CLIENT.getCode().equals(userDTO.getSystemCode())) {
            createDoctorRole(userDTO);
            return userDTO;
        }
        return userDTO;
    }

    /**
     * 创建医生用户角色
     *
     * @param userDTO 用户信息
     * @return void
     **/
    private void createDoctorRole(UserDTO userDTO) {
        Integer systemCode = userDTO.getSystemCode();
        if (!SystemCode.HOSPITAL_CLIENT.getCode().equals(systemCode)) {
            return;
        }
        Integer serviceType = userDTO.getOrgConfigType();
        Assert.notNull(serviceType, "机构的服务类型不能为空");
        // 转化复合服务类型
        if (HospitalServiceType.RESIDENT_PRESCHOOL.getType().equals(serviceType)) {
            createDoctorRole(userDTO.setOrgConfigType(HospitalServiceType.RESIDENT.getType()));
            createDoctorRole(userDTO.setOrgConfigType(HospitalServiceType.PRESCHOOL.getType()));
            return;
        }
        // 1.获取角色（初始化已插入医生的角色，每个角色类型仅一条，所有医生共用）
        Role role = roleService.findOne(new Role().setSystemCode(systemCode).setRoleType(PermissionTemplateType.getRoleTypeByHospitalServiceType(serviceType)));
        Assert.notNull(role, "未初始化医生角色");
        // 2.绑定角色
        userRoleService.save(new UserRole().setUserId(userDTO.getId()).setRoleId(role.getId()));
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
        // 已经存在该机构的角色，则直接给用户绑定该角色，一个机构下仅生成一个角色。（获取第一个是为了兼容历史数据：一个机构下有多个角色）
        Role role = roleService.getOrgFirstOneRole(userDTO.getOrgId(), systemCode, roleType);
        if (Objects.nonNull(role)) {
            userRoleService.save(new UserRole().setUserId(userId).setRoleId(role.getId()));
            bindScreeningPermission(userDTO.getAssociateScreeningOrgId(), userDTO.getId());
            return;
        }
        // 生成筛查机构管理员角色
        if (isScreeningOrgAdmin) {
            generateScreeningOrgAdminUserRole(userDTO);
            return;
        }
        // 生成医院管理员角色
        generateHospitalAdminUserRole(userDTO);
        // 给医院绑定关联筛查机构的角色
        bindScreeningPermission(userDTO.getAssociateScreeningOrgId(), userDTO.getId());
    }

    private void bindScreeningPermission(Integer associateScreeningOrgId, Integer userId) {
        if (Objects.nonNull(associateScreeningOrgId)) {
            Role screeningOrgAdminRole = roleService.getScreeningOrgFirstOneRole(associateScreeningOrgId);
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
        Assert.notNull(getById(userId), "该用户不存在");
        checkPhone(user.getPhone(), userId, user.getSystemCode(), user.getUserType());
        if (StringUtils.hasLength(user.getPassword())) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }
        // 更新用户
        if (!updateById(user)) {
            throw new BusinessException("更新用户信息失败");
        }
        // 获取用户最新信息
        UserWithRole userWithRole = UserWithRole.parseFromUser(getById(userId));
        // 更新角色
        if (SystemCode.MANAGEMENT_CLIENT.getCode().equals(user.getSystemCode()) && UserType.SCREENING_ORGANIZATION_ADMIN.getType().equals(user.getUserType())) {
            // 更新筛查机构管理员权限
            updateScreeningOrgAdminRolePermission(user.getOrgConfigType(), user.getOrgId());
        } else if (SystemCode.HOSPITAL_CLIENT.getCode().equals(user.getSystemCode())) {
            // 更新医生角色
            updateDoctorRole(userId, user.getOrgId(), user.getOrgConfigType());
        } else {
            // 平台管理员、政府人员绑定新角色
            List<Integer> roleIds = user.getRoleIds();
            List<Role> roles = CollectionUtils.isEmpty(roleIds) ? roleService.getRoleListByUserId(userId) : updateRole(roleIds, userId, user.getSystemCode(), user.getOrgId());
            userWithRole.setRoles(roles);
        }
        return userWithRole;
    }

    /**
     * 检查手机号码
     *
     * @param phone 手机号码
     * @param userId 用户ID
     * @param systemCode 系统编号
     * @return void
     **/
    private void checkPhone(String phone, Integer userId, Integer systemCode, Integer userType) {
        if (StringUtils.isEmpty(phone)) {
            return;
        }
        User existPhone = findOne(new User().setPhone(phone).setSystemCode(systemCode).setUserType(userType));
        if (Objects.nonNull(existPhone) && !existPhone.getId().equals(userId)) {
            log.error("已经存在该手机号码:{},SystemCode:{}", phone, systemCode);
            throw new BusinessException("已经存在该手机号码");
        }
    }

    /**
     * 更新角色
     *
     * @param roleIds 角色ID集合
     * @param userId 用户ID
     * @param systemCode 系统编号
     * @param orgId 机构ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    private List<Role> updateRole(List<Integer> roleIds, Integer userId, Integer systemCode, Integer orgId) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<Role> roles = roleService.listByIds(roleIds);
        long size = roles.stream().filter(role -> role.getSystemCode().equals(systemCode) && role.getOrgId().equals(orgId)).count();
        if (size != roleIds.size()) {
            throw new BusinessException("无效角色");
        }
        userRoleService.remove(new UserRole().setUserId(userId));
        List<UserRole> userRoles = roleIds.stream().distinct().map(roleId -> new UserRole().setUserId(userId).setRoleId(roleId)).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
        return roles;
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
     * 获取用户列表
     * @param queryParam
     * @return
     */
    public List<User> getUserList(UserDTO queryParam) {
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
    public List<User> getIdsByOrgIds(List<Integer> orgIds, Integer systemCode, Integer userType) {
        Assert.notEmpty(orgIds, "机构orgId不能为空");
        Assert.notNull(systemCode, "系统编号不能为空");
        UserDTO queryParam = new UserDTO();
        queryParam.setSystemCode(systemCode);
        queryParam.setOrgIds(orgIds);
        queryParam.setUserType(userType);
        return baseMapper.selectUserList(queryParam);
    }

    /**
     * 生成筛查机构管理员角色并初始其化权限
     *
     * @param userDTO userDTO
     * @return boolean 是否成功
     */
    private void generateScreeningOrgAdminUserRole(UserDTO userDTO) {
        Integer orgConfigType = userDTO.getOrgConfigType();
        Assert.notNull(orgConfigType, "筛查机构配置类型为空");
        generateUserRole(userDTO, OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(orgConfigType), RoleType.SCREENING_ORGANIZATION.getType(), userDTO.getUsername());
    }

    /**
     * 生成筛查机构管理员角色并初始其化权限
     *
     * @param userDTO userDTO
     * @return boolean 是否成功
     */
    private void generateHospitalAdminUserRole(UserDTO userDTO) {
        Integer orgConfigType = userDTO.getOrgConfigType();
        Assert.notNull(orgConfigType, "服务配置类型为空");
        generateUserRole(userDTO, PermissionTemplateType.getTemplateTypeByHospitalAdminServiceType(orgConfigType), RoleType.HOSPITAL_ADMIN.getType(), userDTO.getUsername());
    }

    /**
     * 生成用户角色
     *
     * @param userDTO 用户信息
     * @param permissionTemplateType 平板类型
     * @param roleType 角色类型
     * @param roleName 角色名称
     * @return void
     **/
    private void generateUserRole(UserDTO userDTO, Integer permissionTemplateType, Integer roleType, String roleName) {
        // 生成角色，并给用户分配角色
        Role role = createRoleAndAssignToUser(userDTO, roleType, roleName);
        // 根据orgConfigType获取权限集合包
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(permissionTemplateType)
                .stream().map(DistrictPermission::getPermissionId).collect(Collectors.toList());
        // 给角色分配权限
        roleService.assignRolePermission(role.getId(), permissionIds);
    }

    /**
     * 生成筛查机构管理员角色
     *
     * @param userDTO 用户信息
     * @param roleType 角色类型
     * @param roleName 角色名称
     * @return 角色
     */
    private Role createRoleAndAssignToUser(UserDTO userDTO, Integer roleType, String roleName) {
        Role role = new Role()
                .setOrgId(userDTO.getOrgId())
                .setChName(StringUtils.hasText(roleName) ? roleName : userDTO.getUsername())
                .setRoleType(roleType)
                .setSystemCode(userDTO.getSystemCode())
                .setCreateUserId(userDTO.getCreateUserId());
        roleService.save(role);
        userRoleService.save(new UserRole().setUserId(userDTO.getId()).setRoleId(role.getId()));
        return role;
    }

    /**
     * 更新筛查机构用户角色权限
     *
     * @param orgConfigType 配置类型
     * @param screeningOrgId 筛查机构ID
     * @return void
     **/
    private void updateScreeningOrgAdminRolePermission(Integer orgConfigType, Integer screeningOrgId) {
        Assert.notNull(orgConfigType, "配置类型不能为空");
        Assert.notNull(screeningOrgId, "筛查机构ID不能为空");
        List<User> userList = findByList(new User().setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setOrgId(screeningOrgId).setUserType(UserType.SCREENING_ORGANIZATION_ADMIN.getType()));
        userList.forEach(x -> updateScreeningOrgRolePermission(orgConfigType, x.getId()));
    }

    private void updateHospitalAdminRolePermission(Integer serviceType, Integer hospitalId) {
        Assert.notNull(serviceType, "配置类型不能为空");
        Assert.notNull(hospitalId, "医院ID不能为空");
        roleService.updateRolePermissionByHospital(hospitalId, PermissionTemplateType.getTemplateTypeByHospitalAdminServiceType(serviceType));
    }

    /**
     * 更新筛查机构用户角色权限
     *
     * @param orgConfigType 配置类型
     * @param userId 用户ID
     * @return void
     **/
    private void updateScreeningOrgRolePermission(Integer orgConfigType, Integer userId) {
        if (Objects.isNull(orgConfigType)) {
            return;
        }
        updateRolePermission(userId, OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(orgConfigType));
    }

    /**
     * 更新用户角色权限
     * @param userId
     * @param templateType
     */
    private void updateRolePermission(Integer userId, Integer templateType) {
        // 查询角色
        UserRole role = userRoleService.findOne(new UserRole().setUserId(userId));
        Integer roleId = role.getRoleId();
        // 删除改角色所有的所有权限
        rolePermissionService.remove(new RolePermission().setRoleId(roleId));
        // 查找模板的权限集合包
        List<Integer> permissionIds = districtPermissionService.getByTemplateType(templateType)
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

    /**
     * 移除医院管理员关联的筛查机构管理员角色
     *
     * @param hospitalId 医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void removeHospitalUserAssociatedScreeningOrgAdminRole(Integer hospitalId, Integer associateScreeningOrgId) {
        List<User> userList = findByList(new User().setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setUserType(UserType.HOSPITAL_ADMIN.getType()).setOrgId(hospitalId));
        Role role = roleService.getOrgFirstOneRole(associateScreeningOrgId, SystemCode.MANAGEMENT_CLIENT.getCode(), RoleType.SCREENING_ORGANIZATION.getType());
        Integer roleId = role.getId();
        userList.forEach(user -> userRoleService.remove(new UserRole().setUserId(user.getId()).setRoleId(roleId)));
    }

    /**
     * 给医院管理员添加关联的筛查机构管理员角色
     *
     * @param hospitalId 医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void addHospitalUserAssociatedScreeningOrgAdminRole(Integer hospitalId, Integer associateScreeningOrgId) {
        List<User> userList = findByList(new User().setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setUserType(UserType.HOSPITAL_ADMIN.getType()).setOrgId(hospitalId));
        Role role = roleService.getOrgFirstOneRole(associateScreeningOrgId, SystemCode.MANAGEMENT_CLIENT.getCode(), RoleType.SCREENING_ORGANIZATION.getType());
        Integer roleId = role.getId();
        List<UserRole> userRoles = userList.stream().map(user -> new UserRole().setUserId(user.getId()).setRoleId(roleId)).collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
    }

    /**
     * 更新医生用户的角色
     *
     * @param hospitalId 医院ID
     * @param serviceType 服务类型
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void updateHospitalRoleBatch(Integer hospitalId, Integer serviceType) {
        // 更新医院管理员角色
        updateHospitalAdminRolePermission(serviceType, hospitalId);
        // 更新医生角色
        List<User> userList = findByList(new User().setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode()).setOrgId(hospitalId));
        userList.forEach(user -> updateDoctorRole(user.getId(), hospitalId, serviceType));
    }

    /**
     * 更新医生角色
     *
     * @param userId 用户ID
     * @param serviceType 服务类型
     * @return void
     **/
    private void updateDoctorRole(Integer userId, Integer orgId, Integer serviceType) {
        userRoleService.remove(new UserRole().setUserId(userId));
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgConfigType(serviceType)
                .setOrgId(orgId)
                .setSystemCode(SystemCode.HOSPITAL_CLIENT.getCode())
                .setId(userId);
        createDoctorRole(userDTO);
    }
}