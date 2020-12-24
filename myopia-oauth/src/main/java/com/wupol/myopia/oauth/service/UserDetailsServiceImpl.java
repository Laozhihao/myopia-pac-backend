package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.oauth.constant.AuthConstants;
import com.wupol.myopia.oauth.domain.dto.MemberDTO;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.UserDetail;
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
        String clientId = request.getParameter(AuthConstants.JWT_CLIENT_ID_KEY);
        UserDetail user;
        switch (clientId) {
            // 后台用户
            case AuthConstants.ADMIN_CLIENT_ID:
                //根据用户名获取对应系统用户
                UserDTO userDTO = (UserDTO) userService.getByUsername(username, 2);
                userDTO.setClientId(clientId);
                user = new UserDetail(userDTO);
                break;
            // 小程序会员
            case AuthConstants.WEAPP_CLIENT_ID:
                //根据用户名获取对应系统用户
                MemberDTO memberDTO = (MemberDTO) userService.getByUsername(username, 1);
                memberDTO.setClientId(clientId);
                user = new UserDetail(memberDTO);
                break;
            default:
                throw new BusinessException("访问客户端非法");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("该账户已被禁用!");
        } else if (!user.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
        return user;
    }

}
