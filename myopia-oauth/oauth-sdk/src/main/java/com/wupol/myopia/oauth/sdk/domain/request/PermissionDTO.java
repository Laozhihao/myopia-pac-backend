package com.wupol.myopia.oauth.sdk.domain.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 权限资源表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限资源ID
     */
    private Integer id;

    /**
     * 权限资源名称
     */
    private String name;

    /**
     * 对应页面或按钮的name（权限资源为页面时，该值不能为空）
     */
    private String menuBtnName;

    /**
     * 功能接口地址（权限资源为功能时，该值不能为空）
     * put:/management/permission/template/**
     * get:/management/district/all
     */
    private String apiUrl;

    /**
     * 是否为菜单：0-否、1-是
     */
    private Integer isMenu;

    /**
     * 是否为页面：0-功能、1-页面
     */
    private Integer isPage;

    /**
     * 顺序
     */
    private Integer order;

    /**
     * 上级权限资源ID
     */
    private Integer pid;

    /**
     * 系统编号
     */
    private Integer systemCode;

    private List<PermissionDTO> child;

    private Integer districtLevel;

    private Integer roleId;

    private Integer isHave;
}
