package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 权限资源表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Permission implements Serializable {

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

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private List<Permission> child;

    @JsonIgnore
    private Integer districtLevel;

    @JsonIgnore
    private Integer roleId;

    private Integer isHave;
}
