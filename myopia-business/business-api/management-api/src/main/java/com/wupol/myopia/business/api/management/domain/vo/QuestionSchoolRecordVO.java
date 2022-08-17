package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 问卷学校填写情况
 *
 * @author xz 2022 08 01 12:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class QuestionSchoolRecordVO extends QuestionRecordVO {
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

    /**
     * 学校填写状态
     */
    private Boolean isSchoolSurveyDown;

    /**
     * 学生专项填写状态
     */
    private Boolean isStudentSpecialSurveyDown;

    /**
     * 学生环境填写状态
     */
    private Boolean isStudentEnvironmentSurveyDown;
}
