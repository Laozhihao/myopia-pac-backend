package com.wupol.myopia.oauth.service;

import com.wupol.myopia.oauth.constant.AuthConstants;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserDetail;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义用户认证和授权
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // clientId与systemCode保持一致，不一致需要在SystemCode.java的加上clientId，维持两者的map关系
        String clientId = request.getParameter(AuthConstants.JWT_CLIENT_ID_KEY);
        // 根据用户名获取对应系统用户
        User user = userService.getByUsername(username, Integer.parseInt(clientId));
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setClientId(clientId);
        UserDetail userDetail = new UserDetail(userDTO);
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
