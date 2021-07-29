package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 处理后筛查数据（包括学校ID）
 * @author Alix
 * @Date 2021/3/5
 **/

@Data
@Accessors(chain = true)
public class StatConclusionExportDTO extends StatConclusion {

    /**
     * 筛查计划--参与筛查的学生名字
     */
    private String studentName;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 筛查计划--执行的学校编号
     */
    private String schoolNo;

    /**
     * 筛查计划--执行的学校名字
     */
    private String schoolName;

    /**
     * 筛查计划--年级名称
     */
    private String gradeName;

    /**
     * 筛查计划--年级名称
     */
    private String className;

    /**
     * 筛查计划--参与筛查的学生编号
     */
    private String studentNo;

    /**
     * 筛查计划--参与筛查的学生身份证号码
     */
    private String idCard;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 省代码
     */
    private Long provinceCode;

    /**
     * 市代码
     */
    private Long cityCode;

    /**
     * 区代码
     */
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    private Long townCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 筛查结果--视力检查结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionDataDO visionData;

    /**
     * 筛查结果--电脑验光
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ComputerOptometryDO computerOptometry;

    /**
     * 筛查结果--33cm眼位
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private OcularInspectionDataDO ocularInspectionData;

    /**
     * 筛查结果--眼压
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private EyePressureDataDO eyePressureData;

    /**
     * 筛查结果--眼底
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FundusDataDO fundusData;

    /**
     * 筛查结果--裂隙灯检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SlitLampDataDO slitLampData;

    /**
     * 筛查结果--小瞳验光
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private PupilOptometryDataDO pupilOptometryData;

    /**
     * 筛查结果--盲及视力损害分类
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisualLossLevelDataDO visualLossLevelData;

    /**
     * 筛查结果--生物测量
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BiometricDataDO biometricData;

    /**
     * 筛查结果--全身疾病在眼部的表现
     */
    private String systemicDiseaseSymptom;
}