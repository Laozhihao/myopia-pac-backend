package com.wupol.myopia.business.core.device.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class DeviceScreeningDataExportDTO implements Serializable {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("姓名")
    private String patientName;

    /**
     * 受检者性别(性别 男=0  女=1  未知 = -1)
     */
    @ExcelProperty("性别")
    private String patientGender;
    /**
     * 受检者月龄
     */
    @ExcelProperty("年龄")
    private Integer patientAge;

    /**
     * 受检者年龄段(未知=-1,1=(0M,12M] 2=(12M,36M], 3=(3y,6Y], 4=(6Y-20Y], 5=(20Y,100Y])
     */
    @ExcelProperty("年龄段")
    private String patientAgeGroup;

    /**
     * 受检者单位(可能是公司或者学校)
     */
    @ExcelProperty("单位")
    private String patientOrg;

    /**
     * 受检者部门(班级)
     */
    @ExcelProperty("部门")
    private String patientDept;

    /**
     * 受检者电话
     */
    @ExcelProperty("手机号")
    private String patientPno;

    /**
     * 筛查模式. 双眼模式=0 ; 左眼模式=1; 右眼模式=2; 未知=-1
     */
    @ExcelProperty("筛查模式")
    private String checkMode;

    /**
     * 筛查方式(0=个体筛查,1=批量筛查)
     */
    @ExcelProperty("筛查类型")
    private String checkType;

    /**
     * 右眼球镜
     */
    @ExcelProperty("球镜（右）")
    private String rightSph;


    /**
     * 右眼柱镜
     */
    @ExcelProperty("柱镜（右）")
    private String rightCyl;


    /**
     * 右眼轴位
     */
    @ExcelProperty("轴位（右）")
    private Double rightAxsi;


    /**
     * 右眼等效球镜度
     */
    @ExcelProperty("等效球镜（右）")
    private String rightPa;


    /**
     * 左眼球镜
     */
    @ExcelProperty("球镜（左）")
    private String leftSph;

    /**
     * 左眼柱镜
     */
    @ExcelProperty("柱镜（左）")
    private String leftCyl;

    /**
     * 左眼轴位
     */
    @ExcelProperty("轴位（左）")
    private Double leftAxsi;

    /**
     * 左眼等效球镜度
     */
    @ExcelProperty("等效球镜（左）")
    private String leftPa;


    /**
     * 右眼瞳孔半径x2
     */
    @ExcelProperty("瞳孔直径（右）")
    private Double rightPr;


    /**
     * 左眼瞳孔半径x2
     */
    @ExcelProperty("瞳孔直径（左）")
    private Double leftPr;


    /**
     * 右垂直⽅向斜视度数
     */
    @ExcelProperty("垂直斜视（右）")
    private Integer rightAxsiV;


    /**
     * 左垂直⽅向斜视度数
     */
    @ExcelProperty("垂直斜视（左）")
    private Integer leftAxsiV;


    /**
     * 右⽔平⽅向斜视度数
     */
    @ExcelProperty("水平斜视（右）")
    private Integer rightAxsiH;


    /**
     * 左⽔平⽅向斜视度数
     */
    @ExcelProperty("水平斜视（左）")
    private Integer leftAxsiH;


    /**
     * 红光反射右眼
     */
    @ExcelProperty("红光反射（右）")
    private Integer redReflectRight;

    /**
     * 红光反射左眼
     */
    @ExcelProperty("红光反射（左）")
    private Integer redReflectLeft;

    /**
     * 瞳距
     */
    @ExcelProperty("瞳距")
    private Double pd;

    /**
     * 筛查结果(1=优, 2=良, 3=差,-1=未知)
     */
    @ColumnWidth(50)
    @ExcelProperty("测试结果")
    private String checkResult;

}
