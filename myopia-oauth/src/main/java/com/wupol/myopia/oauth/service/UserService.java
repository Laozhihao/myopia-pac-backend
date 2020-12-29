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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;

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
        Page<UserWithRole> page = new Page<>(queryParam.getCurrent(), queryParam.getSize());
        return baseMapper.selectUserListWithRole(page, queryParam);
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User addUser(UserDTO userDTO) {
        // TODO：手机号码不能重复

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
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
     * 新增各个系统的管理员
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
     * 重置密码
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @Transactional(rollbackFor = Exception.class)
    public User resetPwd(Integer userId) {
        String pwd = PasswordGenerator.getManagementUserPwd();
        User user = new User().setId(userId).setPassword(new BCryptPasswordEncoder().encode(pwd));
        updateById(user);
        user = getById(userId);
        return user.setPassword(pwd);
    }
}
