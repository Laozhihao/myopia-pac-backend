package com.wupol.myopia.base.util;

import com.wupol.myopia.base.constant.VisionDamageTypeLevelEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * 筛查数据格式化工具
 *
 * @author Simple4H
 */
@UtilityClass
public class ScreeningDataFormatUtils {

    private static final String EMPTY_RESULT = "--";

    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    public static String genEyeBiometric(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "D" : EMPTY_RESULT : EMPTY_RESULT;
    }

    /**
     * 角膜曲率（单眼）
     *
     * @param val1 值1
     * @return String
     */
    public static String genBiometricAxis(Object val1) {
        return Objects.nonNull(val1) ? StringUtils.isNotBlank(String.valueOf(val1)) ? val1 + "°" : EMPTY_RESULT : EMPTY_RESULT;
    }


    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singleEyeDateFormat(BigDecimal date,int scale) {
        DecimalFormat decimalFormat ;
        if (Objects.equals(scale,1)){
            decimalFormat = new DecimalFormat("0.0");
        }else if (Objects.equals(scale,2)){
            decimalFormat = new DecimalFormat("0.00");
        }else {
            decimalFormat = new DecimalFormat("0");
        }
        return Optional.ofNullable(date).map(d-> getFormatValue(scale, decimalFormat, d)).orElse(EMPTY_RESULT);
    }

    /**
     * 数据格式化
     * @param scale
     * @param decimalFormat
     * @param d
     */
    private static String getFormatValue(int scale, DecimalFormat decimalFormat, BigDecimal d) {
        String format = decimalFormat.format(d);
        if (Objects.equals(scale,1) ||Objects.equals(scale,0)){
            return format;
        }else {
            return singleEyeFormat(format);
        }
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
            return EMPTY_RESULT;
        }
        String formatVal = decimalFormat.format(date);
        if (StringUtils.isNotBlank(formatVal) && BigDecimalUtil.moreThanAndEqual(formatVal, "0")) {
            return "+" + formatVal;
        }
        return formatVal;
    }

    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singlePlusEyeDateFormatTwo(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return Objects.isNull(date) ? "--" : singleEyeFormat(decimalFormat.format(date));
    }

    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singleEyeDateFormatZero(BigDecimal date) {
        DecimalFormat decimalFormat = new DecimalFormat("0");
        return Objects.isNull(date) ? "--" : decimalFormat.format(date);
    }

    /**
     * 单眼数据格式化
     *
     * @param date 左眼数据
     * @return String
     */
    public static String singleEyeFormat(String date) {
        if (BigDecimalUtil.moreThanAndEqual(date, "0")) {
            return "+" + date;
        }
        return date;
    }

    /**
     * 格式化眼压数据
     *
     * @param data 眼数据
     * @return String
     */
    public static String ipDateFormat(Object data) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Objects.isNull(data) ? EMPTY_RESULT : decimalFormat.format(data) + "mmHg";
    }

    /**
     * 格式化等级数据
     *
     * @param data 眼数据
     * @return String
     */
    public static String levelDateFormat(Object data,Boolean isHaiNan) {
        if (Objects.equals(isHaiNan,Boolean.TRUE)){
            return Objects.isNull(data) ? EMPTY_RESULT : data + "级";
        }else {
            return Objects.isNull(data) ? EMPTY_RESULT :VisionDamageTypeLevelEnum.getByCode(data,EMPTY_RESULT);
        }
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
        return Objects.nonNull(val) ? decimalFormat.format(val) + "°" : EMPTY_RESULT;
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
        return EMPTY_RESULT;
    }

    /**
     * 单眼后缀为mm
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleSuffixMMStr(Object val) {
        return generateSingleSuffix(val, "mm");
    }

    /**
     * 单眼后缀为um
     *
     * @param val 值
     * @return String
     */
    public static String generateSingleSuffixUMStr(Object val) {
        return generateSingleSuffix(val, "um");
    }

    /**
     * 生成单眼描述
     *
     * @param val 值
     * @param str 后缀
     *
     * @return String
     */
    private static String generateSingleSuffix(Object val, String str) {
        if (Objects.isNull(val)) {
            return EMPTY_RESULT;
        }
        String value = ((String) val).trim().replace(" ", "");
        if (StringUtils.equals(value, "null")) {
            return EMPTY_RESULT;
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return (StringUtils.isNotBlank(value) ? decimalFormat.format(new BigDecimal(value)) + str : EMPTY_RESULT);
    }

    /**
     * 设置身高
     *
     * @param height 身高
     * @return 身高
     */
    public static String getHeight(Object height) {
        if (Objects.isNull(height)) {
            return EMPTY_RESULT;
        }
        return StringUtils.isNotBlank(String.valueOf(height)) ? new BigDecimal(String.valueOf(height)).setScale(1, RoundingMode.DOWN) + "cm" : EMPTY_RESULT;
    }

    /**
     * 设置体重
     *
     * @param weight 体重
     * @return 体重
     */
    public static String getWeight(Object weight) {
        if (Objects.isNull(weight)) {
            return EMPTY_RESULT;
        }
        return StringUtils.isNotBlank(String.valueOf(weight)) ? new BigDecimal(String.valueOf(weight)).setScale(1, RoundingMode.DOWN) + "kg" : EMPTY_RESULT;
    }

    /**
     * 获取戴镜类型
     *
     * @param obj 数据
     * @return 戴镜类型
     */
    public static String getGlassesType(Object obj) {
        if (Objects.nonNull(obj)) {
            return StringUtils.defaultIfBlank(GlassesTypeEnum.getDescByCode((Integer) obj), EMPTY_RESULT);
        }
        return EMPTY_RESULT;
    }

    public static String generateComputerOptometrySingleSuffixDStr(Object val) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (Objects.nonNull(val)) {
            return decimalFormat.format(val);
        }
        return EMPTY_RESULT;
    }
}
