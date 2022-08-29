package com.wupol.myopia.business.core.questionnaire.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 答案数据处理工具
 *
 * @author hang.yuan 2022/8/25 22:45
 */
@UtilityClass
public class AnswerUtil {

    private static final String QUOTE = "\"";


    /**
     * 数字格式化
     *
     * @param num 数字
     * @param range 小数点
     */
    public static String numberFormat(String num,Integer range) {
        if (StrUtil.isNotBlank(num)) {
            if (Objects.nonNull(range) && range > 0){
                return String.format("%."+range+"f", new Double(num));
            }
            return String.valueOf(Integer.valueOf(num));
        }

        return StrUtil.EMPTY;
    }

    /**
     * 数字格式化
     *
     * @param num 数字
     */
    public static String numberFormat(String num) {
        return numberFormat(num,0);
    }

    /**
     * 数字格式化
     *
     * @param num 数字
     */
    public static String numberFormat(Integer num) {
        return numberFormat(Optional.ofNullable(num).map(Objects::toString).orElse(StrUtil.EMPTY),0);
    }
    /**
     * 数字格式化
     *
     * @param num 数字
     * @param range 小数点
     */
    public static String numberFormat(BigDecimal num,Integer range) {
        return numberFormat(Optional.ofNullable(num).map(BigDecimal::toString).orElse(StrUtil.EMPTY),range);
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
     * 根据常见病ID获取值
     *
     * @param commonDiseaseId 常见病ID
     * @param start           开始下标
     * @param end             结束下标
     */
    public static String getValue(String commonDiseaseId, Integer start, Integer end) {
        if (StrUtil.isBlank(commonDiseaseId)){
            return StrUtil.EMPTY;
        }
        return AnswerUtil.numberFormat(commonDiseaseId.substring(start, end),null);
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

}
