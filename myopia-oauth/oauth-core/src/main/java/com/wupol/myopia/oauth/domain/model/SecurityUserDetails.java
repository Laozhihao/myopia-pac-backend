package com.wupol.myopia.oauth.domain.model;

import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private CurrentUser userInfo;

    public SecurityUserDetails(User user, List<Role> roles, String clientId) {
        this.setUsername(user.getUsername());
        this.setPassword(AuthConstants.BCRYPT + user.getPassword());
        this.setEnabled(Integer.valueOf(0).equals(user.getStatus()));
        this.setClientId(clientId);
        this.userInfo = new CurrentUser();
        BeanUtils.copyProperties(user, this.userInfo);
        // 如若为问卷系统的虚拟用户，需要生成一个虚拟的唯一id，学生若为-QuestionnaireUserId-10000000
        // 学校端为-QuestionnaireUserId
        if (SystemCode.QUESTIONNAIRE.getCode().equals(user.getSystemCode())) {
            this.userInfo.setQuestionnaireUserId(user.getId());
            //学生跟学校需要set Id
            if (UserType.QUESTIONNAIRE_STUDENT.getType().equals(user.getUserType())
                    || UserType.QUESTIONNAIRE_SCHOOL.getType().equals(user.getUserType())) {
                this.userInfo.setId(UserType.QUESTIONNAIRE_STUDENT.getType().equals(user.getUserType()) ?
                        -user.getId() - 10000000 : -user.getId());
            }
        }
        this.userInfo.setRoleTypes(roles.stream().map(Role::getRoleType).distinct().collect(Collectors.toList()));
        this.userInfo.setClientId(clientId);
        Role screenRole = roles.stream().filter(role -> RoleType.SCREENING_ORGANIZATION.getType().equals(role.getRoleType())).findFirst().orElse(null);
        if (Objects.nonNull(screenRole)) {
            this.userInfo.setScreeningOrgId(screenRole.getOrgId());
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