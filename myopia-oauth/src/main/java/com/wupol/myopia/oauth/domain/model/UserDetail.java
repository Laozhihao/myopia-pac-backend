package com.wupol.myopia.oauth.domain.model;

import com.wupol.myopia.oauth.constant.AuthConstants;
import com.wupol.myopia.oauth.domain.dto.MemberDTO;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
public class UserDetail implements UserDetails {

    private Integer id;

    private String username;

    private String password;

    private Boolean enabled;

    private String clientId;

    private Collection<SimpleGrantedAuthority> authorities;

    public UserDetail(UserDTO user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setPassword(AuthConstants.BCRYPT + user.getPassword());
        this.setEnabled(Integer.valueOf(1).equals(user.getStatus()));
        this.setClientId(user.getClientId());
        if (user.getRoles() != null) {
            authorities = new ArrayList<>();
            user.getRoles().forEach(roleId -> authorities.add(new SimpleGrantedAuthority(String.valueOf(roleId))));
        }
    }

    public UserDetail(MemberDTO member){
        this.setId(member.getId());
        this.setUsername(member.getUsername());
        this.setPassword(AuthConstants.BCRYPT + member.getPassword());
        this.setEnabled( Integer.valueOf(1).equals(member.getStatus()));
        this.setClientId(member.getClientId());
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