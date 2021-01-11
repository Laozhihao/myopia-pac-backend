package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.validator.RoleAddValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleQueryValidatorGroup;
import com.wupol.myopia.business.management.validator.RoleUpdateValidatorGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空", groups = { RoleUpdateValidatorGroup.class })
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    @NotNull(message = "部门ID不能为空", groups = { RoleQueryValidatorGroup.class, RoleAddValidatorGroup.class })
    private Integer orgId;

    /**
     * 英文名
     */
    private String enName;

    /**
     * 中文名
     */
    @NotBlank(message = "角色名称不能为空", groups = { RoleAddValidatorGroup.class })
    private String chName;

    /**
     * 角色类型：0-admin、1-机构管理员、2-普通用户
     */
    @NotNull(message = "角色类型不能为空", groups = { RoleAddValidatorGroup.class })
    private Integer roleType;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    @NotNull(message = "状态不能为空", groups = { RoleAddValidatorGroup.class })
    private Integer status;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 备注
     */
    private String remark;

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


}
