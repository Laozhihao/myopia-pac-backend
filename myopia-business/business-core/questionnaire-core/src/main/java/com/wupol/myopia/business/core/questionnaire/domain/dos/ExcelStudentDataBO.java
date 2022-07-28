package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出excel数据对象
 *
 * @author hang.yuan 2022/7/25 18:21
 */
@Data
public class ExcelStudentDataBO {
    /**
     * 学生ID
     */
    private Integer studentId;
    /**
     * 学生数据集合
     */
    private List<AnswerDataBO> dataList;

    @Data
    public static class AnswerDataBO{
        /**
         * 问题ID
         */
        private Integer questionId;
        /**
         * 答案
         */
        private String answer;
    }

    public Map<Integer,String> getAnswerDataMap(){
        if (CollectionUtils.isEmpty(dataList)){
            return Maps.newHashMap();
        }
        return dataList.stream().collect(Collectors.toMap(AnswerDataBO::getQuestionId,AnswerDataBO::getAnswer,(a1,a2)->a2));
    }


}
