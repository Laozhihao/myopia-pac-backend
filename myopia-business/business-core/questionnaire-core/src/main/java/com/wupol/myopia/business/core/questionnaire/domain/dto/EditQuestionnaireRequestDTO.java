package com.wupol.myopia.business.core.questionnaire.domain.dto;

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
        private Integer partId;

        /**
         * 自定义问题的序号
         */
        private String serialNumber;

        /**
         * 跳转题目
         */
        private JumpIdsDO jumpIds;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 孩子节点
         */
        private List<Detail> questionList;
    }


}
