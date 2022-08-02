package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 待办填写情况
 *
 * @author xz 2022 07 06 12:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class QuestionBacklogRecordVO extends QuestionRecordVO {
    /**
     * 环境调查表状态，0：未完成，1：进行中。2：已完成
     */
    private Integer environmentalStatus;

    /**
     * 环境调查表Id，没有返回null
     */
    private Integer environmentalId;

    /**
     * 学校填写状态
     */
    private Boolean isSchoolSurveyDown;
}
