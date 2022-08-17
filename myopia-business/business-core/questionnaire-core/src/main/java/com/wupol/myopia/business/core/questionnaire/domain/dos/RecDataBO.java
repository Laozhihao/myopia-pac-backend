package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;

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
    public static class RecAnswerDataBO{

        /**
         * 问题ID
         */
        private Integer questionId;

        /**
         * 操作ID
         */
        private String optionId;
        /**
         * qes字段
         */
        private String qesField;

        /**
         * rec答案
         */
        private String recAnswer;
    }
}
