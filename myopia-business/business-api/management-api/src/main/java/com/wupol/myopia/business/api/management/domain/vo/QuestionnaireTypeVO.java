package com.wupol.myopia.business.api.management.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 问卷类型数据
 *
 * @author hang.yuan 2022/7/21 20:41
 */
@Data
public class QuestionnaireTypeVO {
    /**
     * 问卷类型列表
     */
    private List<QuestionnaireType> questionnaireTypeList;
    /**
     * 选中的问卷类型集合
     */
    private List<Integer> selectList;
    /**
     * 暂无数据的集合
     */
    private List<Integer> noDataList;
    /**
     * 无qes文件集合
     */
    private List<Integer> noQesList;


    @Data
    @AllArgsConstructor
    public static class QuestionnaireType{
        /**
         * 问卷类型key
         */
        private Integer key;
        /**
         * 问卷类型描述
         */
        private String label;
    }
}
