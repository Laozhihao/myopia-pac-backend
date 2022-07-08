package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 问卷学校填写情况
 *
 * @author xz 2022 07 06 12:30
 */
@Data
public class QuestionSchoolRecordVO {
    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 区域id
     */
    private Integer areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 筛查机构名称，没有返回null
     */
    private String orgName;

    /**
     * 筛查机构id，没有返回null
     */
    private Integer orgId;

    /**
     * 0：未完成，1：进行中。2：已完成 学校填写状态
     */
    private Integer schoolSurveyStatus;

    /**
     * 0：未完成，1：进行中。2：已完成 学生专项填写状态
     */
    private Integer studentSpecialSurveyStatus;

    /**
     * 0：未完成，1：进行中。2：已完成 学生环境填写状态
     */
    private Integer studentEnvironmentSurveyStatus;
}
