package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;

import java.util.List;
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
     * @return 通知内容
     */
    String getNoticeKeyContent(ExportCondition exportCondition);

    /**
     * 文件名称
     *
     * @param exportCondition 导出条件
     * @return 文件名称
     */
    String getFileName(ExportCondition exportCondition);

    /**
     * 获取地区学校
     * @param districtId 地区ID
     */
    default String getDistrictKey(Integer districtId){
        return "";
    }

    /**
     *  锁值
     *
     * @param exportCondition 导出条件
     * @return 锁值
     */
    String getLockKey(ExportCondition exportCondition);

    /**
     * 获取问卷类型
     * @return 获取问卷类型
     */
    Map<Integer,String> getQuestionnaireType();

    /**
     * 前置处理
     * @param exportCondition 导出条件
     */
    default void preProcess(ExportCondition exportCondition){
    }

    /**
     * 获取条件值
     * @param exportCondition 导出条件
     * @return 获取条件值
     */
    default List<Integer> getConditionValue(ExportCondition exportCondition){
        return UserAnswerFacade.defaultValue(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId());
    }
}
