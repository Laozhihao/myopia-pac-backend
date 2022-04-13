package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_vision_screening_result", autoResultMap = true)
public class VisionScreeningResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查结果--所属的任务id
     */
    private Integer taskId;

    /**
     * 筛查结果--执行的机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查结果--学校id
     */
    private Integer schoolId;

    /**
     * 筛查结果--所属的学生id(存储着学生归档数据的id)
     */
    private Integer screeningPlanSchoolStudentId;

    /**
     * 筛查结果--创建用户id
     */
    private Integer createUserId;

    /**
     * 筛查结果--学生id
     */
    private Integer studentId;

    /**
     * 筛查结果--所属的计划id
     */
    private Integer planId;
    /**
     * 筛查结果--所属的地区id
     */
    private Integer districtId;

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
     * 筛查结果--生物测量
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BiometricDataDO biometricData;

    /**
     * 筛查结果--其他眼病
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private OtherEyeDiseasesDO otherEyeDiseases;

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
     * 筛查结果--身高体重
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private HeightAndWeightDataDO heightAndWeightData;

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
     * 筛查结果--龋齿
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SaprodontiaDataDO saprodontiaData;

    /**
     * 筛查结果--脊柱
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SpineDataDO spineData;

    /**
     * 筛查结果--血压
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BloodPressureDataDO bloodPressureData;

    /**
     * 筛查结果--疾病史(汉字)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> diseasesHistoryData;

    /**
     * 筛查结果--隐私项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private PrivacyDataDO privacyData;

    /**
     *
     * 筛查不准确说明
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private DeviationDO deviationData;


    /**
     * 筛查结果--全身疾病在眼部的表现
     */
    private String systemicDiseaseSymptom;

    /**
     * 筛查结果--是否复筛（0否，1是）
     */
    private Boolean isDoubleScreen;

    /**
     * 是否发送短信通知 0-否 1-是
     */
    private Boolean isNotice;

    /**
     * 筛查类型--来自筛查计划，筛查计划强一致 （0：视力筛查，1；常见病） app：常见病新增 2022-04-07
     */
    private Integer screeningType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
