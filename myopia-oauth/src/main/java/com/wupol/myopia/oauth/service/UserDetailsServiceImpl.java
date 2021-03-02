package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.SecurityUserDetails;
import com.wupol.myopia.oauth.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 自定义用户认证和授权
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // clientId与systemCode一致，若不一致需要在SystemCode.java里加上clientId属性，维持两者的map关系
        String clientId = request.getParameter(AuthConstants.CLIENT_ID_KEY);
        // 获取用户相关信息
        Integer systemCode = Integer.parseInt(clientId);
        User user = userService.getByUsername(username, systemCode);
        // TODO: 临时处理，允许筛查机构管理员登录
        if (Objects.isNull(user) && SystemCode.MANAGEMENT_CLIENT.getCode().equals(systemCode)) {
            user = userService.getByUsername(username, SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());
        }
        if (Objects.isNull(user)) {
            throw new AuthenticationCredentialsNotFoundException("账号或密码错误!");
        }
        // 判断是否分配角色
        List<Role> roles = roleService.getUsableRoleByUserId(user.getId());
        if (CollectionUtils.isEmpty(roles)) {
            throw new AuthenticationCredentialsNotFoundException("该账号未分配权限!");
        }
        List<Integer> roleTypes = roles.stream().map(Role::getRoleType).distinct().collect(Collectors.toList());
        // 生成用户明细，将作为accessToken的payload的一部分
        SecurityUserDetails userDetail = new SecurityUserDetails(user, roleTypes, clientId);
        if (!userDetail.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!userDetail.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!userDetail.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return userDetail;
    }

}
