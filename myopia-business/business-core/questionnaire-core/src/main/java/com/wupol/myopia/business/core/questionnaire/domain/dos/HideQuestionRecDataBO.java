package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 隐藏问题rec数据
 *
 * @author hang.yuan 2022/7/27 13:47
 */
@NoArgsConstructor
@Data
public class HideQuestionRecDataBO {

    /**
     * 问题ID
     */
    private Integer questionId;

    /**
     * 问题类型
     */
    private String type;
    /**
     * qes数据
     */
    private List<QesDataBO> qesData;

    public HideQuestionRecDataBO(Integer questionId, String type) {
        this.questionId = questionId;
        this.type = type;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class QesDataBO{
        /**
         * qes字段
         */
        private String qesField;

        /**
         * qes序号
         */
        private String qesSerialNumber;
    }
}
