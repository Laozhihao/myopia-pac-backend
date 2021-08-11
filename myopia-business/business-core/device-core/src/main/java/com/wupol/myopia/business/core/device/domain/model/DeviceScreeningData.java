package com.wupol.myopia.business.core.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("m_device_screening_data")
public class DeviceScreeningData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 数据归属的机构id
     */
    private Integer screeningOrgId;

    /**
     * 设备表id
     */
    private Integer deviceId;

    /**
     * 设备唯一id
     */
    private String deviceSn;

    /**
     * 患者id
     */
    private String patientId;

    /**
     * 受检者名字
     */
    private String patientName;

    /**
     * 受检者年龄段(未知=-1,1=(0M,12M] 2=(12M,36M], 3=(3y,6Y], 4=(6Y-20Y], 5=(20Y,100Y])
     */
    private Integer patientAgeGroup;

    /**
     * 受检者性别(性别 男=0  女=1  未知 = -1)
     */
    private Integer patientGender;

    /**
     * 受检者月龄
     */
    private Integer patientAge;

    /**
     * 受检者单位(可能是公司或者学校)
     */
    private String patientOrg;

    /**
     * 受检者身份Id
     */
    private String patientCid;

    /**
     * 受检者部门(班级)
     */
    private String patientDept;

    /**
     * 受检者电话
     */
    private String patientPno;

    /**
     * 筛查模式. 双眼模式=0 ; 左眼模式=1; 右眼模式=2; 未知=-1
     */
    private Integer checkMode;

    /**
     * 筛查结果(1=优, 2=良, 3=差,-1=未知)
     */
    private String checkResult;

    /**
     * 筛查方式(0=个体筛查,1=批量筛查)
     */
    private Integer checkType;

    /**
     * 左眼柱镜
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double leftCyl;

    /**
     * 右眼柱镜
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double rightCyl;

    /**
     * 左眼轴位
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double leftAxsi;

    /**
     * 右眼轴位
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double rightAxsi;

    /**
     * 左眼瞳孔半径
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double leftPr;

    /**
     * 右眼瞳孔半径
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double rightPr;

    /**
     * 左眼等效球镜度
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double leftPa;

    /**
     * 右眼等效球镜度
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double rightPa;

    /**
     * 左眼球镜
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double leftSph;

    /**
     * 右眼球镜
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double rightSph;

    /**
     * 瞳距
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double pd;

    /**
     * 是否筛查(-1=未知,1=是,0=否)
     */
    private Integer doCheck;

    /**
     * 左垂直⽅向斜视度数
     */
    private Integer leftAxsiV;

    /**
     * 右垂直⽅向斜视度数
     */
    private Integer rightAxsiV;

    /**
     * 左⽔平⽅向斜视度数
     */
    private Integer leftAxsiH;

    /**
     * 右⽔平⽅向斜视度数
     */
    private Integer rightAxsiH;

    /**
     * 红光反射左眼
     */
    private Integer redReflectLeft;

    /**
     * 红光反射右眼
     */
    private Integer redReflectRight;

    /**
     * 筛查时间
     */
    private Date screeningTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

}
