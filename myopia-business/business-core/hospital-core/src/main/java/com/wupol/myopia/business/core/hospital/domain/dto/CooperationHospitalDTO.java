package com.wupol.myopia.business.core.hospital.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 筛查端合作医院DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CooperationHospitalDTO {

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
     * 行政区域-省Code
     */
    private Integer districtProvinceCode;

    /**
     * 行政区域JSON
     */
    private String districtDetail;

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
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 头像
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer avatarFileId;

    /**
     * 说明
     */
    private String remark;

    /**
     * 是否合作医院 0-否 1-是
     */
    private Integer isCooperation;

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
     * 医院Id
     */
    private Integer hospitalId;

    /**
     * 筛查机构Id
     */

    private Integer screeningOrgId;
    /**
     * 行政区域
     */
    private String districtName;

    /**
     * 地址
     */
    private String addressDetail;

    /**
     * 是否置顶
     */
    private Integer isTop;
}
