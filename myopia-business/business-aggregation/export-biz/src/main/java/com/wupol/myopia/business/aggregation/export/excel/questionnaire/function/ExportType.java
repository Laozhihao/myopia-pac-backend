package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;

import java.util.Map;

/**
 * 导出类型接口
 *
 * @author hang.yuan 2022/7/20 14:24
 */
public interface ExportType {

    /**
     * 导出类型
     *
     * @return 导出类型
     */
    Integer getType();

    /**
     * 通知内容
     *
     * @param exportCondition 导出条件
     */
    String getNoticeKeyContent(ExportCondition exportCondition);

    /**
     * 文件名称
     *
     * @param exportCondition 导出条件
     */
    String getFileName(ExportCondition exportCondition);

    /**
     *  锁值
     *
     * @param exportCondition 导出条件
     */
    String getLockKey(ExportCondition exportCondition);

    /**
     * 获取问卷类型
     */
    Map<Integer,String> getQuestionnaireType();
}
