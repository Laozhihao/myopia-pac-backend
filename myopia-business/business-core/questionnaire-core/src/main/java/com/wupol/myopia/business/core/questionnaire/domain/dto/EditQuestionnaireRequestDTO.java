package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 编辑问卷DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class EditQuestionnaireRequestDTO {

    /**
     * 问卷Id
     */
    @NotNull(message = "问卷Id不能为空")
    private Integer questionnaireId;

    /**
     * 问题详情
     */
    private List<Detail> detail;


    /**
     * 问题详情
     */
    @Getter
    @Setter
    public static class Detail {

        /**
         * 问题Id
         */
        private Integer id;

        /**
         * 自定义问题的序号
         */
        private String serialNumber;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 是否不展示题目序号
         */
        private Boolean isNotShowNumber;

        /**
         * 是否逻辑题
         */
        private Boolean isLogic;

        /**
         * 跳转题目
         */
        private List<JumpIdsDO> jumpIds;

        /**
         * 孩子节点
         */
        private List<Detail> questionList;

        /**
         * 是否隐藏
         */
        private Boolean isHidden;

        /**
         * qes内容
         */
        private List<QesDataDO> qesData;
    }


}
