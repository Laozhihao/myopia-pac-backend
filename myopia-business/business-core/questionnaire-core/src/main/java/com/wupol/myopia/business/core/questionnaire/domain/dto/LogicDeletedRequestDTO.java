package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 逻辑题删除
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LogicDeletedRequestDTO {

    /**
     * 问卷Id
     */
    @NotNull(message = "问卷Id不能为空")
    private Integer questionnaireId;

    /**
     * 问题Id
     */
    @NotNull(message = "问题Id不能为空")
    private Integer questionId;

}
