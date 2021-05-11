package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.screening.organization.domain.handler.NotificationConfigTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 筛查机构表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_organization")
public class ScreeningOrganization extends AddressCode implements Serializable, HasName {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 部门id
     */
    private Integer govDeptId;

    /**
     * 行政区域ID
     */
    private Integer districtId;

    /**
     * 行政区域JSON
     */
    private String districtDetail;

    /**
     * 筛查机构名称
     */
    @NotNull(message = "筛查机构名称不能为空")
    private String name;

    /**
     * 筛查机构类型 0-医院,1-妇幼保健院,2-疾病预防控制中心,3-社区卫生服务中心,4-乡镇卫生院,5-中小学生保健机构,6-其他
     */
    @NotNull(message = "筛查机构类型不能为空")
    private Integer type;

    /**
     * 机构类型描述
     */
    private String typeDesc;

    /**
     * 配置 0-省级配置 1-单点配置
     */
    private Integer configType;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 说明
     */
    private String remark;

    /**
     * 告知书配置
     */
    @TableField(typeHandler = NotificationConfigTypeHandler.class)
    private NotificationConfig notificationConfig;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

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
     * 筛查人员统计
     */
    @TableField(exist = false)
    private Integer staffCount;

    /**
     * 筛查次数
     */
    @TableField(exist = false)
    private Long screeningTime;

}
