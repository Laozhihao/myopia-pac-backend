package com.wupol.myopia.business.common.utils.util;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.util.BigDecimalUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/11/29
 **/
@UtilityClass
public class VS550Util {

    /**
     * VS666数据格式化
     *
     * @param value 值
     * @return VS666数据格式化
     */
    public static Double getDisplayValue(Double value) {
        if (Objects.isNull(value)) {
            return null;
        }
        TwoTuple<Double, Double> splitDouble = splitDouble(value);
        double result = 0d;
        Double absValue = Math.abs(splitDouble.getSecond());
        if (absValue.compareTo(0.125) < 0) {
            result = 0.00;
        }
        if (BigDecimalUtil.isBetweenLeft(absValue, 0.125, 0.375)) {
            result = 0.25;
        }
        if (BigDecimalUtil.isBetweenLeft(absValue, 0.375, 0.625)) {
            result = 0.50;
        }
        if (BigDecimalUtil.isBetweenLeft(absValue, 0.625, 0.875)) {
            result = 0.75;
        }
        if (absValue.compareTo(0.875) >= 0) {
            result = 1.00;
        }
        if (value.compareTo(0d) < 0) {
            result = result * (-1d);
        }
        return splitDouble.getFirst() + result;
    }

    /**
     * 拆分Double成两部分
     *
     * @param value 值
     * @return left-整数 right-小数
     */
    private static TwoTuple<Double, Double> splitDouble(Double value) {
        //整数部分
        int intNum = (int) Double.parseDouble(value.toString());
        BigDecimal valueDecimal = new BigDecimal(value.toString());

        BigDecimal intBigDecimal = new BigDecimal(intNum);
        //小数部分
        double decimalNum = valueDecimal.subtract(intBigDecimal).doubleValue();
        return new TwoTuple<>((double) intNum, decimalNum);
    }
    /**
     * 计算等效球镜
     * 根据：MedicalRecordService中的 computerSE 改编
     *
     * @param ds 球镜
     * @param dc 柱镜
     * @return 等效球镜
     */
    public static Double computerSE(Double ds, Double dc) {
        if (StringUtils.allHasLength(ds.toString(), dc.toString())) {
            return new BigDecimal(ds).add(new BigDecimal(dc).multiply(new BigDecimal("0.5")))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0.00;
    }
}
