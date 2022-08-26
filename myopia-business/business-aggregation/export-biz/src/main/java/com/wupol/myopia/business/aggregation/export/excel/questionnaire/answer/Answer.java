package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateRecDataBO;

import java.util.List;

/**
 * 答案接口
 *
 * @author hang.yuan 2022/8/25 10:22
 */
public interface Answer {

    /**
     * 获取用户类型
     * @return 用户类型
     */
    Integer getUserType();

    /**
     * 获取rec数据
     *
     * @param generateDataCondition 生成数据条件
     * @return 生成rec数据
     */
    List<GenerateRecDataBO> getRecData(GenerateDataCondition generateDataCondition);

    /**
     * 获取Excel数据
     *
     * @param generateDataCondition 生成数据条件
     */
    GenerateExcelDataBO getExcelData(GenerateDataCondition generateDataCondition);

    /**
     * 导出REC文件
     * @param fileName 文件夹或者文件名
     * @param generateRecDataBO 生成数据条件
     * @param recFileName 文件类型
     *
     */
    void exportRecFile(String fileName, GenerateRecDataBO generateRecDataBO, String recFileName);

    /**
     * 获取rec问卷名称
     * @param schoolId 学校ID
     * @param questionnaireType 问卷类型
     * @return  问卷类型
     */
    String getRecFileName(Integer schoolId, Integer questionnaireType);
}
