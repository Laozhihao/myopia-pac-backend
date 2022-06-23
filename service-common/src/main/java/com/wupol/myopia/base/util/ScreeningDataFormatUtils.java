package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 筛查数据格式化工具
 *
 * @author Simple4H
 */
@UtilityClass
public class ScreeningDataFormatUtils {

    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    public static String genEyeBiometric(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "D" : "--" : "--";
    }

    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    public static String genBiometricAxis(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "°" : "--" : "--";
    }


    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singleEyeDateFormat(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Objects.isNull(date) ? "--" : decimalFormat.format(date);
    }

    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singleEyeSEFormat(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if (Objects.isNull(date)) {
            return "--";
        }
        String formatVal = decimalFormat.format(date);
        if (StringUtils.isNotBlank(formatVal) && BigDecimalUtil.moreThanAndEqual(formatVal, "0")) {
            return "+" + formatVal;
        }
        return formatVal;
    }

    /**
     * 格式化眼压数据
     *
     * @param data 眼数据
     * @return String
     */
    public static String ipDateFormat(Object data) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Objects.isNull(data) ? "--" : decimalFormat.format(data) + "mmHg";
    }

    /**
     * 格式化等级数据
     *
     * @param data 眼数据
     * @return String
     */
    public static String levelDateFormat(Object data) {
        return Objects.isNull(data) ? "--" : data + "级";
    }

    /**
     * 初步结果 单眼
     *
     * @param diagnosis 0-正常 1-"（疑似）异常"
     * @return String
     */
    public static String singleDiagnosis2String(Integer diagnosis) {
        if (Objects.isNull(diagnosis)) {
            return StringUtils.EMPTY;
        }
        if (0 == diagnosis) {
            return "正常";
        }
        if (1 == diagnosis) {
            return "（疑似）异常";
        }
        return StringUtils.EMPTY;
    }

    /**
     * 生成单眼度数String，后缀为°
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleEyeDegree(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0");
        return Objects.nonNull(val) ? decimalFormat.format(val) + "°" : "--";
    }

    /**
     * 单眼后缀为D
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleSuffixDStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (Objects.nonNull(val)) {
            String formatVal = decimalFormat.format(val);
            if (StringUtils.isNotBlank(formatVal) && BigDecimalUtil.moreThanAndEqual(formatVal, "0")) {
                return "+" + formatVal + "D";
            }
            return formatVal + "D";
        }
        return "--";
    }

    /**
     * 单眼后缀为mm
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleSuffixMMStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (StringUtils.isNotBlank((CharSequence) val) ? decimalFormat.format(new BigDecimal((String) val)) + "mm" : "--");
    }

    /**
     * 单眼后缀为um
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleSuffixUMStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (StringUtils.isNotBlank((CharSequence) val) ? decimalFormat.format(new BigDecimal((String) val)) + "um" : "--");
    }

    /**
     * 设置身高
     *
     * @param height 身高
     * @return 身高
     */
    public static String getHeight(Object height) {
        if (Objects.isNull(height)) {
            return "--";
        }
        return StringUtils.isNotBlank(String.valueOf(height)) ? new BigDecimal(String.valueOf(height)).setScale(1, RoundingMode.DOWN) + "cm" : "--";
    }

    /**
     * 设置体重
     *
     * @param weight 体重
     * @return 体重
     */
    public static String getWeight(Object weight) {
        if (Objects.isNull(weight)) {
            return "--";
        }
        return StringUtils.isNotBlank(String.valueOf(weight)) ? new BigDecimal(String.valueOf(weight)).setScale(1, RoundingMode.DOWN) + "kg" : "--";
    }

    /**
     * 获取戴镜类型
     *
     * @param obj 数据
     * @return 戴镜类型
     */
    public static String getGlassesType(Object obj) {
        if (Objects.nonNull(obj)) {
            return StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode((Integer) obj), "--");
        }
        return "--";
    }
}
