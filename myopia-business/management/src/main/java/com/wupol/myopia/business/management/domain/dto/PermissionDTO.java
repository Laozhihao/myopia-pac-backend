package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /**
     * 是否拥有该权限
     */
    private Integer isHave;

    /**
     * 子权限
     */
    private List<PermissionDTO> child;

}
