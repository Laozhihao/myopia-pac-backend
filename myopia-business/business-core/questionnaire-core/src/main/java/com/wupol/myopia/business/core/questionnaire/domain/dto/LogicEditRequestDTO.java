package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 罗辑题
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LogicEditRequestDTO {

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

    /**
     * 跳转Ids
     */
    private List<JumpIdsDO> jumpIds;

}
