package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.SecurityUserDetails;
import com.wupol.myopia.oauth.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 自定义用户认证和授权
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizationService organizationService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // clientId与systemCode一致，若不一致需要在SystemCode.java里加上clientId属性，维持两者的map关系
        String clientId = request.getParameter(AuthConstants.CLIENT_ID_KEY);
        Integer systemCode = Integer.parseInt(clientId);
        // 检查账号密码
        User user = validateAccount(systemCode, username);
        // 判断是否分配角色
        List<Role> roles = validateRole(systemCode, user);
        // 生成用户明细，将作为accessToken的payload的一部分
        SecurityUserDetails userDetail = new SecurityUserDetails(user, roles, clientId);
        if (!userDetail.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!userDetail.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!userDetail.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return userDetail;
    }

    /**
     * 校验账号
     *
     * @param systemCode 系统编号
     * @param username 用户名
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    private User validateAccount(Integer systemCode, String username) {
        // 0-6岁客户端，实际上使用的是医院端
        if (SystemCode.PRESCHOOL_CLIENT.getCode().equals(systemCode)) {
            systemCode = SystemCode.HOSPITAL_CLIENT.getCode();
        }
        User user = userService.getByUsername(username, systemCode);
        if (Objects.isNull(user)) {
            throw new AuthenticationCredentialsNotFoundException("账号或密码错误!");
        }
        if (!organizationService.getOrgStatus(user.getOrgId(), user.getSystemCode(), user.getUserType())) {
            throw new AccountExpiredException("该账号未在服务期内，请联系管理员！");
        }
        return user;
    }

    /**
     * 校验用户角色信息
     *
     * @param systemCode 系统编号
     * @param user 用户
     * @return java.util.List<java.lang.Integer>
     **/
    private List<Role> validateRole(Integer systemCode, User user) {
        // 非管理端和筛查管理端的用户不需要校验角色  学校端、筛查端、家长端用户不需要校验角色
        if (SystemCode.SCHOOL_CLIENT.getCode().equals(systemCode) || SystemCode.SCREENING_CLIENT.getCode().equals(systemCode)
            || SystemCode.PARENT_CLIENT.getCode().equals(systemCode)) {
            return Collections.emptyList();
        }
        List<Role> roles = roleService.getUsableRoleByUserId(user.getId(), systemCode, user.getUserType());
        // 0-6岁系统客户端
        if (SystemCode.PRESCHOOL_CLIENT.getCode().equals(systemCode)    // 0-6岁端必须要有0-6角色
                && !roles.stream().filter(role -> RoleType.PRESCHOOL_DOCTOR.getType().equals(role.getRoleType())).findFirst().isPresent()) {
            throw new AuthenticationCredentialsNotFoundException("该账号未分配0-6岁眼检查系统权限!");
        } else if (SystemCode.HOSPITAL_CLIENT.getCode().equals(systemCode)  // 眼健康系统必须要有眼健康系统角色
                && !roles.stream().filter(role -> RoleType.RESIDENT_DOCTOR.getType().equals(role.getRoleType())).findFirst().isPresent()) { // 医院端
            throw new AuthenticationCredentialsNotFoundException("该账号未分配居民眼健康系统权限!");
        }
        if (CollectionUtils.isEmpty(roles)) {
            throw new AuthenticationCredentialsNotFoundException("该账号未分配权限!");
        }
        return roles;
    }

}
