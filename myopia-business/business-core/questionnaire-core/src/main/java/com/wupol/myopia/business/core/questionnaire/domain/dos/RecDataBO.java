package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导出rec数据
 *
 * @author hang.yuan 2022/8/17 17:29
 */
@Data
public class RecDataBO {
    /**
     * 学生ID
     */
    private Integer studentId;

    /**
     * 年级编码
     */
    private String gradeCode;


    private List<RecAnswerDataBO> recAnswerDataBOList;


    @Data
    @NoArgsConstructor
    public static class RecAnswerDataBO{

        /**
         * qes字段
         */
        private String qesField;

        /**
         * rec答案
         */
        private String recAnswer;

        public RecAnswerDataBO(String qesField, String recAnswer) {
            this.qesField = qesField;
            this.recAnswer = recAnswer;
        }
    }
}
