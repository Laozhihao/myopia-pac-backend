package com.wupol.myopia.business.core.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_device")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备唯一id
     */
    private String deviceSn;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 销售名字
     */
    private String salespersonName;

    /**
     * 销售电话
     */
    private String salespersonPhone;

    /**
     * 绑定机构id
     */
    private Integer bindingScreeningOrgId;

    /**
     * 客户名字
     */
    private String customerName;

    /**
     * 客户电话
     */
    private String customerPhone;

    /**
     * 销售时间
     */
    private Date saleDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态: 启用1、禁用0
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}