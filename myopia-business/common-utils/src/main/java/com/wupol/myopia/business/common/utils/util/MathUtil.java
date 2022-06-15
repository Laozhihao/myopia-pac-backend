package com.wupol.myopia.business.common.utils.util;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.function.Function;

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
        if (numerator == 0 || denominator == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);
        return n.multiply(hundred).divide(d, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 占比 （带%）
     */
    public String ratio(Integer numerator, Integer denominator) {
        if (ObjectsUtil.allNull(numerator,denominator)){
            return null;
        }
        DecimalFormat df = new DecimalFormat("0.00%");
        return ratio(numerator,denominator,df);
    }

    public <T> String ratio(Function<T,Integer> mapper,T t, Integer denominator) {
        DecimalFormat df = new DecimalFormat("0.00%");
        Integer numerator = mapper.apply(t);
        return ratio(numerator,denominator,df);
    }

    /**
     * 占比 （不带%）
     */
    public BigDecimal ratioNotSymbol(Integer numerator,Integer denominator){
        if(ObjectsUtil.hasNull(numerator,denominator)){
            return null;
        }
        return ratioNotSymbol(new BigDecimal(numerator),new BigDecimal(denominator));
    }

    public BigDecimal ratioNotSymbol(BigDecimal numerator,BigDecimal denominator){
        if (BigDecimalUtil.decimalEqual(numerator,"0") || BigDecimalUtil.decimalEqual(denominator,"0")){
            return new BigDecimal("0.00");
        }
        BigDecimal divide = numerator.multiply(new BigDecimal("100")).divide(denominator, 2, RoundingMode.HALF_UP);
       return BigDecimalUtil.getBigDecimalByFormat(divide,2);
    }

    public String num(Integer numerator, Integer denominator) {
        DecimalFormat df = new DecimalFormat("0.00");
        return ratio(numerator,denominator,df);
    }

    public BigDecimal numNotSymbol(Integer numerator, Integer denominator) {
        if(ObjectsUtil.hasNull(numerator,denominator)){
            return null;
        }
        if (numerator == 0 ||denominator == 0) {
            return new BigDecimal("0.00");
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator),2, RoundingMode.HALF_UP);
    }

    public String ratio(Integer numerator, Integer denominator,DecimalFormat df) {
        if(ObjectsUtil.hasNull(numerator,denominator,df)){
            return null;
        }
        if (numerator == 0 ||denominator == 0) {
            return df.format(new BigDecimal("0"));
        }
        BigDecimal divide = new BigDecimal(numerator).divide(new BigDecimal(denominator), 4, BigDecimal.ROUND_HALF_UP);
        return df.format(divide);
    }

    /**
     * 占比 （带%）
     */
    public String ratio(BigDecimal bigDecimal) {
        DecimalFormat df = new DecimalFormat("0.00%");
        return df.format(bigDecimal);
    }

    /**
     * 四舍五入
     *
     * @param num
     * @return
     */
    public double getFormatNumWith2Scale(Double num) {
        if (num == null) {
            num = 0.0D;
        }
        return  getFormatNum(2,num);
    }

    /**
     * 保留一位小数
     * @param num
     * @return
     */
    public double getFormatNumWith1Scale(Double num) {
        if (num == null) {
            num = 0.0D;
        }
        return  getFormatNum(1,num);
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
