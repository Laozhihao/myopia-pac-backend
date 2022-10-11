package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 眼健康数据DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class EyeHealthResponseDTO implements Serializable {

    /**
     * 学生Id
     */
    private Integer studentId;

    /**
     * 学校学生Id
     */
    private Integer schoolStudentId;

    /**
     * 学籍号
     */
    private String sno;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年级
     */
    private String gradeName;

    /**
     * 班级
     */
    private String className;

    /**
     * 戴镜情况
     */
    private String wearingGlasses;

    /**
     * 视力低下情况
     */
    private String lowVision;

    /**
     * 屈光情况
     */
    private String refractiveResult;

    /**
     * 近视矫正
     */
    private String visionCorrection;

    /**
     * 戴镜建议
     */
    private String glassesSuggest;

    /**
     * 视力预警
     */
    private String warningLevel;

    /**
     * 座位建议
     */
    private Boolean seatSuggest;

    /**
     * 身高
     */
    private String height;

    /**
     * 课桌
     */
    private String desk;

    /**
     * 课椅
     */
    private String chair;

    /**
     * 是否绑定公众号在线档案
     */
    private Boolean isBindMp;

    /**
     * 最新筛查日期
     */
    private Date screeningTime;

    /**
     * 是否建议就诊
     */
    private Boolean isRecommendVisit;

    /**
     * 是否就诊
     */
    private Boolean isHavaReport;

    /**
     * 是否有黑板距离
     */
    private Boolean haveBlackboardDistance;
}