package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

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
    private String studentId;

    /**
     * 学校学生Id
     */
    private String schoolStudentId;

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
     * 视力预警
     */
    private String warningLevel;

    /**
     * 课桌
     */
    private String desk;

    /**
     * 课椅
     */
    private String chair;

    /**
     * 座位
     */
    private Boolean seat;

    /**
     * 是否绑定公众号在线档案
     */
    private Boolean isBindMp;

    /**
     * 最新筛查日期
     */
    private String screeningTime;
}