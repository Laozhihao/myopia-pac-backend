package com.wupol.myopia.oauth.domain.model;

import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.CurrentUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
public class SecurityUserDetails implements UserDetails {

    private String username;

    private String password;

    private Boolean enabled;

    private String clientId;

    private Collection<SimpleGrantedAuthority> authorities;

    private CurrentUser userBaseInfo;

    public SecurityUserDetails(User user, List<String> permissions, String clientId) {
        this.setUsername(user.getUsername());
        this.setPassword(AuthConstants.BCRYPT + user.getPassword());
        this.setEnabled(Integer.valueOf(0).equals(user.getStatus()));
        this.setClientId(clientId);
        this.userBaseInfo = new CurrentUser();
        BeanUtils.copyProperties(user, this.userBaseInfo);
        if (!CollectionUtils.isEmpty(permissions)) {
            authorities = new ArrayList<>();
            permissions.forEach(apiPath -> authorities.add(new SimpleGrantedAuthority(String.valueOf(apiPath))));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}