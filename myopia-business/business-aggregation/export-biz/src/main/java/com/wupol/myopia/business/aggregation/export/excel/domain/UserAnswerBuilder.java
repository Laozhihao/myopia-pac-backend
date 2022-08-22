package com.wupol.myopia.business.aggregation.export.excel.domain;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户答案构建类
 *
 * @author hang.yuan 2022/8/18 21:49
 */
@UtilityClass
public class UserAnswerBuilder {

    private static final String QUOTE = "\"";
    private static final String OTHER = "other";

    /**
     * 获取条件值的默认值
     *
     * @param noticeId 通知ID
     * @param taskId   任务ID
     * @param planId   计划ID
     */
    public static List<Integer> defaultValue(Integer noticeId, Integer taskId, Integer planId) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(noticeId);
        list.add(taskId);
        list.add(planId);
        return list;
    }

    /**
     * 获取性别的rec数据
     *
     * @param gender 性别
     */
    public static String getGenderRecData(Integer gender) {
        if (Objects.equals(gender, 0)) {
            return "1";
        }
        if (Objects.equals(gender, 1)) {
            return "2";
        }
        return StrUtil.EMPTY;
    }


    /**
     * 根据常见病ID获取值
     *
     * @param commonDiseaseId 常见病ID
     * @param start           开始下标
     * @param end             结束下标
     */
    public static String getValue(String commonDiseaseId, Integer start, Integer end) {
        return numberFormat(commonDiseaseId.substring(start, end));
    }

    /**
     * 数字格式化
     *
     * @param num 数字
     */
    public static String numberFormat(String num) {
        if (StrUtil.isNotBlank(num)) {
            return String.valueOf(Integer.valueOf(num));
        }
        return StrUtil.EMPTY;
    }

    /**
     * 文本格式化
     *
     * @param str 文本
     */
    public static String textFormat(String str) {
        if (StrUtil.isNotBlank(str)) {
            return QUOTE + str + QUOTE;
        }
        return "\"\"";
    }


    /**
     * qes字段加引号
     *
     * @param qesField qes字段
     */
    public static String getQesFieldStr(String qesField) {
        if (StrUtil.isNotBlank(qesField)) {
            return QUOTE + qesField.toLowerCase() + QUOTE;
        }
        return null;
    }


    /**
     * 获取rec答案数据
     *
     * @param qesField  qes字段
     * @param recAnswer rec答案
     */
    public static String getRecAnswerData(String qesField, String recAnswer) {
        if (qesField.toLowerCase().contains(OTHER)) {
            return textFormat(recAnswer);
        } else {
            return StrUtil.isNotBlank(recAnswer) ? recAnswer : StrUtil.EMPTY;
        }
    }



}
