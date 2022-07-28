package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.domain.dos.ExcelDataConditionBO;

import java.io.IOException;
import java.nio.file.Paths;
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
     * @param fileName 文件保存路径
     * @return 文件
     */
    default void generateExcelFile(ExportCondition exportCondition,String fileName) throws IOException {

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
    List<List<String>> getHead(List<Integer> questionnaireIds);


    /**
     * 获取导出数据
     *
     * @param excelDataConditionBO 导出条件
     * @param dataList 数据集合
     */
    default void getExcelData(ExcelDataConditionBO excelDataConditionBO, List dataList) {
        // do something get excel data
    }


    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件父路径
     * @param fileName   文件名
     * @return 文件保存路径
     **/
    default String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

}
