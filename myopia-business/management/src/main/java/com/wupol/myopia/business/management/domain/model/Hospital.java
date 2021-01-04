package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 医院表
 *
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_hospital")
public class Hospital implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 根据规则创建ID
     */
    private String hospitalNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 部门id
     */
    private Integer govDeptId;

    /**
     * 医院名称
     */
    @NotBlank(message = "医院名称不能为空")
    private String name;

    /**
     * 等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他
     */
    @NotNull(message = "医院等级不能为空")
    private Integer level;

    /**
     * 等级描述
     */
    @NotBlank(message = "医院等级描述不能为空")
    private String levelDesc;

    /**
     * 医院类型 0-定点医院 1-非定点医院
     */
    @NotNull(message = "医院医院类型不能为空")
    private Integer type;

    /**
     * 医院性质 0-公立 1-私立
     */
    @NotNull(message = "医院医院性质不能为空")
    private Integer kind;

    /**
     * 省代码
     */
    private Long provinceCode;

    /**
     * 市代码
     */
    @NotNull(message = "市代码不能为空")
    private Long cityCode;

    /**
     * 区代码
     */
    @NotNull(message = "区代码不能为空")
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @NotNull(message = "镇/乡代码不能为空")
    private Long townCode;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /**
     * 说明
     */
    private String remark;

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


}
