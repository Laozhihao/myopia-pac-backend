package com.wupol.myopia.business.core.school.management.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学校学生列表
 *
 * @author hang.yuan 2022/10/11 11:33
 */
@Data
public class SchoolStudentListVO implements Serializable {

    /**
     * 学校学生ID
     */
    private Integer id;
    /**
     * 学生ID
     */
    private Integer studentId;
    /**
     * 学号
     */
    private String sno;

    /**
     * 学年
     */
    private String yearStr;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 视力情况
     */
    private String vision;

    /**
     * 屈光情况
     */
    private String refraction;

    /**
     * 近视矫正
     */
    private Integer correction;

    /**
     * 视力预警
     */
    private Integer visionLabel;
}
