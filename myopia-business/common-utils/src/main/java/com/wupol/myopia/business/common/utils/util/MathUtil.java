package com.wupol.myopia.business.common.utils.util;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.NumberCommonConst;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 计算工具
 */
@UtilityClass
public class MathUtil {

    /**
     * 除法，乘100后，四舍五入保留两位小数位。如45.34%，保存45.34
     * @param numerator 分子
     * @param denominator 分母
     * @return
     */
    public BigDecimal divide(Integer numerator, Integer denominator) {
        if (Objects.equals(numerator,NumberCommonConst.ZERO_INT) || Objects.equals(denominator,NumberCommonConst.ZERO_INT)) {
            return BigDecimal.ZERO;
        }
        BigDecimal hundred = new BigDecimal(NumberCommonConst.ONE_HUNDRED_INT);
        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);
        return n.multiply(hundred).divide(d, NumberCommonConst.TWO_INT, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 除法，乘100后，四舍五入保留两位小数位。如45.34%，保存45.34
     * @param numerator
     * @param denominator
     * @return
     */
    public Float divideFloat(Integer numerator, Integer denominator) {
        if (denominator.equals(0)) {
            return 0.0f;
        }
        return divide(numerator, denominator).floatValue();
    }

    /**
     * 占比 （带%）
     * @param numerator
     * @param denominator
     */
    public String ratio(Integer numerator, Integer denominator) {
        if (ObjectsUtil.hasNull(numerator,denominator)){
            return NumberCommonConst.ZERO_RATIO;
        }
        DecimalFormat df = new DecimalFormat(NumberCommonConst.ZERO_RATIO);
        return ratio(numerator,denominator,df);
    }

    /**
     * 占比 （带%）
     * @param bigDecimal
     */
    public String ratio(BigDecimal bigDecimal) {
        DecimalFormat df = new DecimalFormat(NumberCommonConst.ZERO_RATIO);
        return df.format(bigDecimal);
    }

    /**
     * 占比 （不带%）
     * @param numerator
     * @param denominator
     */
    public BigDecimal ratioNotSymbol(Integer numerator,Integer denominator){
        if(ObjectsUtil.hasNull(numerator,denominator)){
            return null;
        }
        return ratioNotSymbol(new BigDecimal(numerator),new BigDecimal(denominator));
    }

    /**
     * 占比 （不带%）
     * @param numerator
     * @param denominator
     */
    public BigDecimal ratioNotSymbol(BigDecimal numerator,BigDecimal denominator){
        if (BigDecimalUtil.decimalEqual(numerator,NumberCommonConst.ZERO_STR) || BigDecimalUtil.decimalEqual(denominator,NumberCommonConst.ZERO_STR)){
            return new BigDecimal(NumberCommonConst.DECIMAL_ZERO);
        }
        BigDecimal divide = numerator.multiply(new BigDecimal(NumberCommonConst.ONE_HUNDRED_STR)).divide(denominator, NumberCommonConst.TWO_INT, RoundingMode.HALF_UP);
       return BigDecimalUtil.getBigDecimalByFormat(divide,NumberCommonConst.TWO_INT);
    }

    /**
     * 数据格式化，返回字符串（带小数点,不带%）
     * @param numerator
     * @param denominator
     */
    public String num(Integer numerator, Integer denominator) {
        if(ObjectsUtil.hasNull(numerator,denominator)){
            return NumberCommonConst.DECIMAL_ZERO;
        }
        DecimalFormat df = new DecimalFormat(NumberCommonConst.DECIMAL_ZERO);
        return ratio(numerator,denominator,df);
    }

    /**
     * 数据格式化，返回数字（带小数点,不带%）
     * @param numerator
     * @param denominator
     */
    public BigDecimal numNotSymbol(Integer numerator, Integer denominator) {
        if(ObjectsUtil.hasNull(numerator,denominator)){
            return null;
        }
        if (Objects.equals(numerator,NumberCommonConst.ZERO_INT)  || Objects.equals(denominator,NumberCommonConst.ZERO_INT) ) {
            return new BigDecimal(NumberCommonConst.DECIMAL_ZERO);
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator),NumberCommonConst.TWO_INT, RoundingMode.HALF_UP);
    }

    /**
     * 数据格式化
     * @param numerator
     * @param denominator
     * @param df
     */
    public String ratio(Integer numerator, Integer denominator,DecimalFormat df) {
        if (Objects.equals(numerator,NumberCommonConst.ZERO_INT) ||Objects.equals(denominator,NumberCommonConst.ZERO_INT)) {
            return df.format(new BigDecimal(NumberCommonConst.ZERO_STR));
        }
        BigDecimal divide = new BigDecimal(numerator).divide(new BigDecimal(denominator), NumberCommonConst.FOUR_INT, BigDecimal.ROUND_HALF_UP);
        return df.format(divide);
    }

    /**
     * 四舍五入,保留二位小数
     *
     * @param num
     * @return
     */
    public double getFormatNumWith2Scale(Double num) {
        if (num == null) {
            num = 0.0D;
        }
        return  getFormatNum(NumberCommonConst.TWO_INT,num);
    }


    /**
     * 格式化数字（四舍五入）
     * @param scale 保留几位小数
     * @param num  原数字
     * @return
     */
    private double getFormatNum(int scale,double num) {
        BigDecimal bigDecimal = BigDecimal.valueOf(num);
        bigDecimal = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
}
