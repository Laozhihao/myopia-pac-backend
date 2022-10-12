package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 学校学生查询条件
 *
 * @author hang.yuan 2022/10/11 11:00
 */
@Data
public class SchoolStudentQueryDTO implements Serializable {

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 学号
     */
    private String sno;

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 班级Id
     */
    private Integer classId;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 预警等级
     */
    private String visionLabels;

    /**
     * 入学年份
     */
    private Integer year;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 视力类型
     */
    private Integer visionType;

    /**
     * 屈光类型
     */
    private Integer refractionType;

}
