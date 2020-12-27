package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.SecurityUserDetails;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
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
    private UserRoleService userRoleService;
    @Autowired
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // clientId与systemCode一致，若不一致需要在SystemCode.java里加上clientId属性，维持两者的map关系
        String clientId = request.getParameter(AuthConstants.JWT_CLIENT_ID_KEY);
        // 获取用户相关信息
        User user = userService.getByUsername(username, Integer.parseInt(clientId));
        // 判断是否分配角色
        try {
            List<UserRole> roles = userRoleService.findByList(new UserRole().setUserId(user.getId()));
            if (CollectionUtils.isEmpty(roles)) {
                throw new AuthenticationCredentialsNotFoundException("该账号未分配权限!");
            }
        } catch (IOException e) {
            logger.error("获取用户角色异常", e);
            throw new AuthenticationServiceException("该账号未分配角色!");
        }
        // 获取权限
        List<Permission> permissions = permissionService.getUserPermissionByUserId(user.getId());
        List<String> permissionPaths = permissions.stream()
                .filter(x -> x.getIsPage().equals(AuthConstants.IS_API_PERMISSION) && !StringUtils.isEmpty(x.getApiUrl()))
                .map(Permission::getApiUrl)
                .distinct().collect(Collectors.toList());
        SecurityUserDetails userDetail = new SecurityUserDetails(user, permissionPaths, clientId);
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
