package com.wupol.myopia.business.core.government.domian.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.validator.GovDeptAddValidatorGroup;
import com.wupol.myopia.business.management.validator.GovDeptUpdateValidatorGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 政府部门表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_government_department")
public class GovDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @NotNull(message = "ID不能为空", groups = GovDeptUpdateValidatorGroup.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 部门名称
     */
    @NotNull(message = "部门名称不能为空", groups = {GovDeptAddValidatorGroup.class, GovDeptUpdateValidatorGroup.class})
    private String name;

    /**
     * 上级部门ID
     */
    private Integer pid;

    /**
     * 所属行政区ID
     */
    @NotNull(message = "所属行政区ID不能为空", groups = {GovDeptAddValidatorGroup.class, GovDeptUpdateValidatorGroup.class})
    private Integer districtId;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

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

    /**
     * 下级部门
     */
    @TableField(exist = false)
    private List<GovDept> child;

    /**
     * 创建人名
     */
    @TableField(exist = false)
    private String createUserName;

    /**
     * 部门人数
     */
    @TableField(exist = false)
    private Integer userCount;
}
