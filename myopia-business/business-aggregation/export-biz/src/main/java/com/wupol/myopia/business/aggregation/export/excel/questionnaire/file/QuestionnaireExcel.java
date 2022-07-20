package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;

import java.io.File;
import java.util.List;

/**
 * 问卷excel接口
 *
 * @author hang.yuan 2022/7/18 11:29
 */
public interface QuestionnaireExcel {

    /**
     * 生成excel文件
     *
     * @param exportCondition 导出条件
     * @return 文件
     */
    default File generateExcelFile(ExportCondition exportCondition) {

        return null;
    }


    /**
     * 问卷类型
     *
     * @return 问卷类型
     */
    Integer getType();

    /**
     * 表头信息
     *
     * @return 表头信息
     */
    List<List<String>> getHead();


    /**
     * 获取导出数据
     *
     * @param exportCondition 导出条件
     * @param dataList 数据集合
     */
    default void getExcelData(ExportCondition exportCondition, List dataList) {
        // do something get excel data
    }


}
