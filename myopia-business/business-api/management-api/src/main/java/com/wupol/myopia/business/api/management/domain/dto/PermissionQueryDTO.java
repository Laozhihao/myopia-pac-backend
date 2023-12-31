package com.wupol.myopia.business.api.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.api.management.validator.PermissionAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.PermissionUpdateValidatorGroup;
import com.wupol.myopia.oauth.sdk.domain.request.PermissionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class PermissionQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限资源ID
     */
    private Integer id;

    /**
     * 权限资源名称
     */
    @NotBlank(message = "权限资源名称不能为空", groups = {PermissionAddValidatorGroup.class, PermissionUpdateValidatorGroup.class})
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
    @NotNull(message = "新增项值不能为空", groups = {PermissionAddValidatorGroup.class, PermissionUpdateValidatorGroup.class})
    private Integer isPage;

    /**
     * 顺序
     */
    @NotNull(message = "权限资源排序值不能为空", groups = {PermissionAddValidatorGroup.class, PermissionUpdateValidatorGroup.class})
    private Integer order;

    /**
     * 上级权限资源ID
     */
    @NotNull(message = "上级权限资源ID不能为空", groups = {PermissionAddValidatorGroup.class, PermissionUpdateValidatorGroup.class})
    private Integer pid;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 是否拥有该权限
     */
    private Integer isHave;

    /**
     * 子权限
     */
    private List<PermissionQueryDTO> child;

    public PermissionDTO convertToPermissionDTO() {
        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(this, permissionDTO);
        return permissionDTO;
    }
}
