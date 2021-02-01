package com.wupol.myopia.oauth.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWithRole extends User {
    /**
     * 创建人真实姓名
     **/
    private String createUserName;
    /**
     * 用户拥有的所有角色
     **/
    private List<Role> roles;
}
