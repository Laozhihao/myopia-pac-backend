package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.util.StrUtil;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 筛查结论计算工具 (产品说判断结论时没数据不给任何值)
 */
@UtilityClass
public class StatUtil {

    private static final String MINUS_3 = "-3.00";
    private static final String MINUS_0_5 = "-0.50";
    private static final String MINUS_6 = "-6.00";

    /**
     * 初筛数据完整性判断
     * <p>
     * 1，配镜情况：没有配镜，需要：裸眼视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 2，配镜情况：佩戴框架眼镜，需要裸眼视力、矫正视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 3，配镜情况：佩戴隐形眼镜，需要裸眼视力、矫正视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 4，配镜情况：佩戴角膜塑形镜，需要矫正视力。
     * <p>
     * 复测数据完整性判断
     * <p>
     * <p>
     * <p>
     * 1、复测随机抽取满足条件的已筛查学生的6% （一开始是5%，调整为5%，确保：纳入发生率统计的复测学生达到5%。）
     * <p>
     * 数据是否有效或者完整
     *
     * @param visionData        视力筛查结果
     * @param computerOptometry 电脑验光数据
     */
    public static boolean isCompletedData(VisionDataDO visionData, ComputerOptometryDO computerOptometry) {
        if (visionData == null || visionData.getLeftEyeData() == null || visionData.getLeftEyeData().getGlassesType() == null) {
            return false;
        }
        Integer glassesType = visionData.getLeftEyeData().getGlassesType();
        if (WearingGlassesSituation.NOT_WEARING_GLASSES_KEY.equals(glassesType)) {
            return visionData.validNakedVision() && Objects.nonNull(computerOptometry) && computerOptometry.valid();
        } else if (WearingGlassesSituation.WEARING_FRAME_GLASSES_KEY.equals(glassesType) || WearingGlassesSituation.WEARING_CONTACT_LENS_KEY.equals(glassesType)) {
            return visionData.validNakedVision() && visionData.validCorrectedVision() && Objects.nonNull(computerOptometry) && computerOptometry.valid();
        } else if (WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(glassesType)) {
            return visionData.validCorrectedVision();
        } else {
            return false;
        }
    }

    /**
     * 判断是否视力低下
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static Boolean isLowVision(Float nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 0) {
            return null;
        }
        return isLowVision(nakedVision.toString(), age);
    }

    public static Boolean isLowVision(String nakedVision, Integer age) {
        if (StrUtil.isBlank(nakedVision) || age == null || age < 0) {
            return null;
        }
        return isLowVision(new BigDecimal(nakedVision), age);
    }

    public static Boolean isLowVision(BigDecimal nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 0) {
            return null;
        }
        if (age > 0 && age < 3 && BigDecimalUtil.lessThanAndEqual(nakedVision,"4.6")) {
            return true;
        }
        if (age == 3 && BigDecimalUtil.lessThanAndEqual(nakedVision,"4.7")) {
            return true;
        }
        if (age == 4 && BigDecimalUtil.lessThanAndEqual(nakedVision,"4.8")) {
            return true;
        }
        return age >= 5 && BigDecimalUtil.lessThanAndEqual(nakedVision,"4.9");
    }

    /**
     * 视力低下等级 (TODO:是视力等级,还是预警等级)
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static WarningLevel getNakedVisionWarningLevel(Float nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 6) {
            return null;
        }
        return getNakedVisionWarningLevel(nakedVision.toString(), age);
    }

    public static WarningLevel getNakedVisionWarningLevel(String nakedVision, Integer age) {
        if (StrUtil.isBlank(nakedVision) || age == null || age < 6) {
            return null;
        }
        return getNakedVisionWarningLevel(new BigDecimal(nakedVision), age);
    }

    public static WarningLevel getNakedVisionWarningLevel(BigDecimal nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 6) {
            return null;
        }

        if (BigDecimalUtil.decimalEqual(nakedVision, "4.9")) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.isBetweenAll(nakedVision, "4.6", "4.8")) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5")) {
            return WarningLevel.THREE;
        }
        return WarningLevel.NORMAL;
    }

    /**
     * 平均视力 (初筛数据完整才使用)
     * @param statConclusions
     */
    public static TwoTuple<BigDecimal,BigDecimal> calculateAverageVision(List<StatConclusion> statConclusions) {
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsValid())).collect(Collectors.toList());

        int sumSize = statConclusions.size();
        double sumVisionL = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).sum();
        BigDecimal avgVisionL = BigDecimalUtil.divide(String.valueOf(sumVisionL), String.valueOf(sumSize),1);

        double sumVisionR = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).sum();
        BigDecimal avgVisionR = BigDecimalUtil.divide(String.valueOf(sumVisionR), String.valueOf(sumSize),1);

        return new TwoTuple<>(avgVisionL,avgVisionR);
    }


    /**
     * 是否近视
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     * @param age         年龄
     * @param nakedVision 裸眼视力
     */
    public static boolean isMyopia(Float sphere, Float cylinder, Integer age, Float nakedVision) {
        if (Objects.nonNull(age)) {
            if (age < 6 && Objects.nonNull(nakedVision) && BigDecimalUtil.lessThan(new BigDecimal(nakedVision),"4.9")) {
                return true;
            }
            if (Objects.nonNull(nakedVision) && age >= 6 && BigDecimalUtil.lessThan(new BigDecimal(nakedVision),"5.0")) {
                return true;
            }
        }
        return Objects.requireNonNull(getMyopiaWarningLevel(sphere, cylinder, age, nakedVision)).code > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }

    /**
     * 是否近视
     *
     * @param myopiaWarningLevel 近视预警级别
     */
    public static boolean isMyopia(MyopiaLevelEnum myopiaWarningLevel) {
        return myopiaWarningLevel.code > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }

    /**
     * 是否近视
     *
     * @param myopiaWarningLevel 近视预警级别
     */
    public static boolean isMyopia(Integer myopiaWarningLevel) {
        if (Objects.isNull(myopiaWarningLevel)) {
            return false;
        }
        return myopiaWarningLevel > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }


    /**
     * 屈光不正率 屈光不正：
     *              【3＜*＜5】4岁儿童等效球镜（SE）> +4.00D或 SE<-3.00D或柱镜（散光）>|2.00|D
     *              【5＜*＜7】5-6岁儿童等效球镜（SE）> +3.50D或 SE<-1.50D或柱镜（散光）>|1.50|D
     *
     *              针对0-6岁眼保健平台
     *              24月龄。柱镜（散光） >|2.00|D，等效球镜（SE） >+4.50D，等效球镜（SE） <-3.50D
     *              36月龄。屈光不正：柱镜（散光） >|2.00|D，等效球镜（SE） >+4.00D，等效球镜（SE） <-3.00D
     *
     *              屈光不正学生数，占比：屈光不正数 / 纳入统计的学生数 * 100%
     */

    /**
     * 是否屈光不正
     *
     * @param sphere 球镜
     * @param cyl    柱镜
     * @param age    年龄
     */
    public static Boolean isRefractiveError(Float sphere, Float cyl, Integer age) {
        if (sphere == null || cyl == null || age == null) {
            return Boolean.FALSE;
        }
        return isRefractiveError(sphere.toString(), cyl.toString(), age);
    }

    public static Boolean isRefractiveError(BigDecimal sphere, BigDecimal cyl, Integer age) {
        if (sphere == null || cyl == null || age == null) {
            return Boolean.FALSE;
        }
        return isRefractiveError(sphere.toString(), cyl.toString(), age);
    }

    public static Boolean isRefractiveError(String sphere, String cyl, Integer age) {
        BigDecimal se = getSphericalEquivalent(sphere, cyl);
        age = age == null ? 0 : age;
        switch (age) {
            case 2:
                return refractiveError2(se, cyl);
            case 3:
            case 4:
                return refractiveError34(se, cyl);
            case 5:
            case 6:
                return refractiveError56(se, cyl);
            default:
                return Boolean.FALSE;
        }
    }

    private static Boolean refractiveError34(BigDecimal se, String cyl) {
        return (se != null && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00")))
                || (StrUtil.isNotBlank(cyl) && BigDecimalUtil.moreThan(new BigDecimal(cyl).abs(), new BigDecimal("2.00").abs()));
    }

    private static Boolean refractiveError56(BigDecimal se, String cyl) {
        return (se != null && (BigDecimalUtil.lessThan(se, "-3.50") || BigDecimalUtil.moreThan(se, "4.50")))
                || (StrUtil.isNotBlank(cyl) && BigDecimalUtil.moreThan(new BigDecimal(cyl).abs(), new BigDecimal("1.50").abs()));
    }

    private static Boolean refractiveError2(BigDecimal se, String cyl) {
        return (se != null && (BigDecimalUtil.lessThan(se, "-1.50") || BigDecimalUtil.moreThan(se, "3.50")))
                || (StrUtil.isNotBlank(cyl) && BigDecimalUtil.moreThan(new BigDecimal(cyl).abs(), new BigDecimal("2.00").abs()));
    }


    /**
     * 1、计入矫正
     * 2、未佩戴眼镜计入未矫
     *
     * @param leftNakedVision  左眼裸视
     * @param rightNakedVision 右眼裸视
     */
    public static Boolean correction(Float leftNakedVision, Float rightNakedVision) {
        return leftNakedVision < 4.9f || rightNakedVision < 4.9f;
    }

    /**
     * 3、计入足矫
     *
     * @param wearingGlasses 戴镜
     * @param leftVision     左眼视力
     * @param rightVision    右眼视力
     */
    public static Boolean footOrthosis(Boolean wearingGlasses, Float leftVision, Float rightVision) {
        return wearingGlasses && leftVision < 4.9f && rightVision < 4.9f;
    }

    /**
     * 4、计入欠矫
     *
     * @param wearingGlasses 戴镜
     * @param leftVision     左眼视力
     * @param rightVision    右眼视力
     */
    public static Boolean undercorrection(Boolean wearingGlasses, Float leftVision, Float rightVision) {
        return wearingGlasses && (leftVision <= 4.9f || rightVision <= 4.9f);
    }

    /**
     * 屈光参差：双眼球镜度（远视、近视）差值>1.50D
     *
     * @param leftSph  左眼球镜度
     * @param rightSph 右眼球镜度
     */
    public static boolean isAnisometropiaVision(String leftSph, String rightSph) {
        if (StrUtil.isNotBlank(leftSph) && StrUtil.isNotBlank(rightSph)) {
            return isAnisometropiaVision(new BigDecimal(leftSph),new BigDecimal(rightSph));
        }
        return Boolean.FALSE;
    }

    public static Boolean isAnisometropiaVision(BigDecimal leftSph, BigDecimal rightSph){
        if (ObjectsUtil.allNotNull(leftSph,rightSph)) {
            BigDecimal diffSph = leftSph.abs().subtract(rightSph.abs()).abs();
            return BigDecimalUtil.moreThan(diffSph, "1.50");
        }
        return Boolean.FALSE;
    }

    /**
     * 屈光参差：双眼柱镜度（散光）差值>1.00D
     *
     * @param leftCyl  左眼柱镜度
     * @param rightCyl 右眼柱镜度
     */
    public static Boolean isAnisometropiaAstigmatism(String leftCyl, String rightCyl) {
        if (StrUtil.isNotBlank(leftCyl) && StrUtil.isNotBlank(rightCyl)) {
            return isAnisometropiaAstigmatism(new BigDecimal(leftCyl),new BigDecimal(rightCyl));
        }
        return Boolean.FALSE;
    }
    public static Boolean isAnisometropiaAstigmatism(BigDecimal leftCyl, BigDecimal rightCyl) {
        if (ObjectsUtil.allNotNull(leftCyl,rightCyl)) {
            BigDecimal diffCyl = leftCyl.abs().subtract(rightCyl.abs()).abs();
            return BigDecimalUtil.moreThan(diffCyl, "1.00");
        }
        return Boolean.FALSE;
    }

    /**
     * 是否散光
     *
     * @param cyl 柱镜
     * @return
     */
    public boolean isAstigmatism(String cyl) {
        return StrUtil.isNotBlank(cyl) && BigDecimalUtil.moreThanAndEqual(new BigDecimal(cyl).abs(), "0.50");
    }

    /**
     * 是否散光
     *
     * @param astigmatismWarningLevel 散光预警级别
     * @return
     */
    public static Boolean isAstigmatism(AstigmatismLevelEnum astigmatismWarningLevel) {
        return isWarningLevelGreatThanZero(astigmatismWarningLevel);
    }

    /**
     * 判断预警级别是否大于0
     *
     * @param warningLevel 预警级别
     * @return
     */
    private static boolean isWarningLevelGreatThanZero(AstigmatismLevelEnum warningLevel) {
        return warningLevel.code > AstigmatismLevelEnum.ZERO.getCode();
    }

    /**
     * 散光等级
     *
     * @param cyl 柱镜
     */
    public static AstigmatismLevelEnum getAstigmatismWarningLevel(Float cyl) {
        if(Objects.isNull(cyl)){
            return AstigmatismLevelEnum.ZERO;
        }
        return getAstigmatismWarningLevel(cyl.toString());
    }

    public static AstigmatismLevelEnum getAstigmatismWarningLevel(String cyl) {
        if (StrUtil.isBlank(cyl)) {
            return AstigmatismLevelEnum.ZERO;
        }
        return getAstigmatismWarningLevel(new BigDecimal(cyl));
    }

    public static AstigmatismLevelEnum getAstigmatismWarningLevel(BigDecimal cyl) {
        if (Objects.isNull(cyl)) {
            return AstigmatismLevelEnum.ZERO;
        }
        BigDecimal cylAbs = cyl.abs();

        if (BigDecimalUtil.isBetweenAll(cylAbs, "0.50", "2.00")) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT;
        }
        if (BigDecimalUtil.isBetweenRight(cylAbs, "2.00", "4.00")) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE;
        }
        if (BigDecimalUtil.moreThan(cylAbs, "4.00")) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH;
        }
        return AstigmatismLevelEnum.ZERO;
    }

    public static Integer getAstigmatismLevel(BigDecimal cyl) {
        AstigmatismLevelEnum astigmatismWarningLevel = getAstigmatismWarningLevel(cyl);
        if (Objects.nonNull(astigmatismWarningLevel)) {
            return astigmatismWarningLevel.code;
        }
        return AstigmatismLevelEnum.ZERO.code;
    }


    /**
     * 是否远视
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static Boolean isHyperopia(Float sphere, Float cylinder, Integer age) {
        if (ObjectsUtil.allNotNull(sphere, cylinder, age)) {
            return isHyperopia(sphere.toString(), cylinder.toString(), age);
        }
        return Boolean.FALSE;
    }

    public static Boolean isHyperopia(String sphere, String cylinder, Integer age) {
        return isHyperopia(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    public static Boolean isHyperopia(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (Objects.isNull(age)) {
            return false;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (se == null) {
            return false;
        }

        if (age < 4 && BigDecimalUtil.moreThan(se, "3.00")) {
            return true;
        }
        if ((age < 6 && age >= 4) && BigDecimalUtil.moreThan(se, "2.00")) {
            return true;
        }
        if ((age < 8 && age >= 6) && BigDecimalUtil.moreThan(se, "1.50")) {
            return true;
        }
        if (age >= 8 && BigDecimalUtil.moreThan(se, "0.50")) {
            return true;
        }
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        return hyperopiaWarningLevel != null && hyperopiaWarningLevel.code > HyperopiaLevelEnum.ZERO.code;
    }

    /**
     * 是否远视
     *
     * @param hyperopiaWarningLevel 远视预警级别
     * @return
     */
    public static boolean isHyperopia(HyperopiaLevelEnum hyperopiaWarningLevel) {
        return hyperopiaWarningLevel.code > HyperopiaLevelEnum.ZERO.code;
    }


    /**
     * 返回远视预警级别
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static HyperopiaLevelEnum getHyperopiaWarningLevel(Float sphere, Float cylinder, Integer age) {
        return getHyperopiaWarningLevel(sphere.toString(), cylinder.toString(), age);
    }

    public static HyperopiaLevelEnum getHyperopiaWarningLevel(String sphere, String cylinder, Integer age) {
        return getHyperopiaWarningLevel(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    public static HyperopiaLevelEnum getHyperopiaWarningLevel(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (sphere == null || cylinder == null || age == null) {
            return null;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (se == null) {
            return null;
        }
        if (age >= 12) {
            if (BigDecimalUtil.isBetweenRight(se, "0.50", "3.00")) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT;
            }
            if (BigDecimalUtil.isBetweenRight(se, "3.00", "6.00")) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_MIDDLE;
            }
            if (BigDecimalUtil.moreThan(se, "6.00")) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH;
            }
            return HyperopiaLevelEnum.ZERO;
        }
        return null;
    }

    /**
     * 获取远视Level
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return Integer
     */
    public static Integer getHyperopiaLevel(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (sphere == null || cylinder == null || age == null) {
            return null;
        }
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        if (Objects.nonNull(hyperopiaWarningLevel)) {
            return hyperopiaWarningLevel.code;
        }
        return HyperopiaLevelEnum.ZERO.code;
    }


    /**
     * 近视预警级别
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     * @param age         年龄
     * @param nakedVision 裸眼视力
     */
    public static MyopiaLevelEnum getMyopiaWarningLevel(Float sphere, Float cylinder, Integer age, Float nakedVision) {
        if (ObjectsUtil.hasNull(sphere, cylinder, nakedVision)) {
            return null;
        }
        return getMyopiaWarningLevel(sphere.toString(), cylinder.toString(), age, nakedVision.toString());
    }

    public static MyopiaLevelEnum getMyopiaWarningLevel(String sphere, String cylinder, Integer age, String nakedVision) {
        if (ObjectsUtil.hasNull(sphere, cylinder, nakedVision)) {
            return null;
        }
        return getMyopiaWarningLevel(new BigDecimal(sphere), new BigDecimal(cylinder), age, new BigDecimal(nakedVision));
    }

    public static MyopiaLevelEnum getMyopiaWarningLevel(BigDecimal sphere, BigDecimal cylinder, Integer age, BigDecimal nakedVision) {
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)) {
            return null;
        }

        if (ObjectsUtil.allNotNull(age, nakedVision)
                && ((age < 6 && BigDecimalUtil.lessThan(nakedVision, "4.9")) || (age >= 6 && BigDecimalUtil.lessThan(nakedVision, "5.0")))
                && BigDecimalUtil.lessThan(se, "-0.5")) {
            return MyopiaLevelEnum.SCREENING_MYOPIA;
        }

        if (BigDecimalUtil.isBetweenRight(se, MINUS_0_5, "0.75")) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_EARLY;
        }
        if (BigDecimalUtil.isBetweenLeft(se, MINUS_3, MINUS_0_5)) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT;
        }
        if (BigDecimalUtil.isBetweenLeft(se, MINUS_6, MINUS_3)) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE;
        }
        if (BigDecimalUtil.lessThan(se, MINUS_6)) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_HIGH;
        }

        return MyopiaLevelEnum.ZERO;

    }

    /**
     * 近视预警级别Level
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     * @param age         年龄
     * @param nakedVision 裸眼视力
     */
    public static Integer getMyopiaLevel(Float sphere, Float cylinder, Integer age, Float nakedVision) {
        if (ObjectsUtil.hasNull(sphere, cylinder, nakedVision)) {
            return null;
        }
        return getMyopiaLevel(sphere.toString(), cylinder.toString(), age, nakedVision.toString());
    }

    public static Integer getMyopiaLevel(String sphere, String cylinder, Integer age, String nakedVision) {
        if (ObjectsUtil.hasNull(sphere, cylinder, nakedVision)) {
            return null;
        }
        return getMyopiaLevel(new BigDecimal(sphere), new BigDecimal(cylinder), age, new BigDecimal(nakedVision));
    }

    public static Integer getMyopiaLevel(BigDecimal sphere, BigDecimal cylinder, Integer age, BigDecimal nakedVision) {
        MyopiaLevelEnum myopiaWarningLevel = getMyopiaWarningLevel(sphere, cylinder, age, nakedVision);
        if (Objects.nonNull(myopiaWarningLevel)) {
            return myopiaWarningLevel.code;
        }
        return null;
    }


    /**
     * 计算等效球镜 （球镜度+1/2柱镜度）
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static BigDecimal getSphericalEquivalent(BigDecimal sphere, BigDecimal cylinder) {
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
            return null;
        }
        return cylinder.divide(new BigDecimal(2)).add(sphere);
    }


    public static BigDecimal getSphericalEquivalent(String sphere, String cylinder) {
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
            return null;
        }
        return getSphericalEquivalent(new BigDecimal(sphere), new BigDecimal(cylinder));
    }


    //===============4、0-3级预警判断 （满足其一即可判断预警级别）

    /**
     * 裸眼视力数据
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static WarningLevel nakedVision(String nakedVision, Integer age) {
        if (StrUtil.isBlank(nakedVision) || age == null) {
            return null;
        }
        return nakedVision(new BigDecimal(nakedVision), age);
    }

    public static WarningLevel nakedVision(BigDecimal nakedVision, Integer age) {
        if (ObjectsUtil.hasNull(nakedVision, age) || age < 3) {
            return null;
        }
        age = age > 6 ? 7 : age;
        switch (age) {
            case 3:
                return nakedVision3(nakedVision);
            case 4:
                return nakedVision4(nakedVision);
            case 5:
                return nakedVision5(nakedVision);
            case 6:
            case 7:
                return nakedVisionMoreThanAndEqual6(nakedVision);
            default:
                return null;
        }
    }

    private static WarningLevel nakedVision3(BigDecimal nakedVision) {
        if (BigDecimalUtil.lessThan(nakedVision, "4.5")) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.6")) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.7")) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.8")) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    private static WarningLevel nakedVision4(BigDecimal nakedVision) {
        if (BigDecimalUtil.lessThan(nakedVision, "4.6")) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.7")) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.8")) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.9")) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    private static WarningLevel nakedVision5(BigDecimal nakedVision) {
        if (BigDecimalUtil.lessThan(nakedVision, "4.7")) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.8")) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.9")) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "5.0")) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    private static WarningLevel nakedVisionMoreThanAndEqual6(BigDecimal nakedVision) {
        if (BigDecimalUtil.lessThan(nakedVision, "4.7")) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "4.9")) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "5.0")) {
            return WarningLevel.ONE;
        }

        return null;
    }


    /**
     * 屈光数据
     * 1、近视：se 不能为空
     * 2、远视：se和age 不能为空
     * 3、散光：cyl 不能为空
     *
     * @param se   等效球镜
     * @param cyl  柱镜
     * @param age  年龄
     * @param type 类型 （近视-0、散光-1、远视-2）
     */
    public static WarningLevel refractiveData(BigDecimal se, BigDecimal cyl, Integer age, Integer type) {
        switch (type) {
            case 0:
                return refractiveDataMyopia(se);
            case 1:
                return refractiveDataAstigmatism(cyl);
            case 2:
                return refractiveDataFarsighted(se, age);
            default:
                return null;
        }
    }

    private static WarningLevel refractiveDataMyopia(BigDecimal se) {
        if (se != null) {
            if (BigDecimalUtil.isBetweenAll(se, MINUS_0_5, "-0.25")) {
                return WarningLevel.ZERO;
            }
            if (BigDecimalUtil.isBetweenLeft(se, MINUS_3, MINUS_0_5)) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenLeft(se, MINUS_6, MINUS_3)) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.lessThan(se, MINUS_6)) {
                return WarningLevel.THREE;
            }
        }
        return null;

    }

    private static WarningLevel refractiveDataFarsighted(BigDecimal se, Integer age) {
        if (age != null) {
            int key = age;
            if (age >= 8) {
                key = 8;
            }
            switch (key) {
                case 3:
                    return refractiveDataFarsighted3(se);
                case 4:
                case 5:
                    return refractiveDataFarsighted45(se);
                case 6:
                case 7:
                    return refractiveDataFarsighted67(se);
                case 8:
                    return refractiveDataFarsightedMoreThanAndEqual8(se);
                default:
                    return null;
            }
        }
        return null;
    }

    private static WarningLevel refractiveDataFarsighted3(BigDecimal se) {
        if (se != null) {
            if (BigDecimalUtil.isBetweenRight(se, "4.00", "6.00")) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenRight(se, "6.00", "9.00")) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.moreThan(se, "9.00")) {
                return WarningLevel.THREE;
            }
        }

        return WarningLevel.NORMAL;
    }

    private static WarningLevel refractiveDataFarsighted45(BigDecimal se) {
        if (se != null) {
            if (BigDecimalUtil.isBetweenRight(se, "2.00", "5.00")) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenRight(se, "5.00", "8.00")) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.moreThan(se, "8.00")) {
                return WarningLevel.THREE;
            }
        }
        return null;
    }

    private static WarningLevel refractiveDataFarsighted67(BigDecimal se) {
        if (se != null) {
            if (BigDecimalUtil.isBetweenRight(se, "1.50", "4.50")) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenRight(se, "4.50", "7.50")) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.moreThan(se, "7.50")) {
                return WarningLevel.THREE;
            }
        }
        return null;
    }

    private static WarningLevel refractiveDataFarsightedMoreThanAndEqual8(BigDecimal se) {
        if (se != null) {
            if (BigDecimalUtil.isBetweenRight(se, "0.50", "3.00")) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenRight(se, "3.00", "6.00")) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.moreThan(se, "6.00")) {
                return WarningLevel.THREE;
            }
        }
        return null;
    }

    private static WarningLevel refractiveDataAstigmatism(BigDecimal cyl) {
        if (cyl != null) {
            if (BigDecimalUtil.isBetweenLeft(cyl.abs(), "0.25", "0.50")) {
                return WarningLevel.ZERO;
            }
            if (BigDecimalUtil.isBetweenAll(cyl.abs(), "0.50", "2.00")) {
                return WarningLevel.ONE;
            }
            if (BigDecimalUtil.isBetweenRight(cyl.abs(), "2.00", "4.00")) {
                return WarningLevel.TWO;
            }
            if (BigDecimalUtil.moreThan(cyl.abs(), "4.00")) {
                return WarningLevel.THREE;
            }
        }
        return null;
    }

    /**
     * 远视储备不足（也纳入0级预警中）
     *
     * @param se 等效球镜
     */
    public static WarningLevel insufficientFarsightednessReserve(String se) {
        if (StrUtil.isNotBlank(se)) {
            return insufficientFarsightednessReserve(new BigDecimal(se));
        }
        return null;
    }

    public static WarningLevel insufficientFarsightednessReserve(BigDecimal se) {
        if (se != null && BigDecimalUtil.isBetweenAll(se, "0.00", "1.00")) {
            return WarningLevel.ZERO_SP;
        }
        return null;
    }


    //===============5、建议就诊

    /**
     * 满足幼儿园判断条件 （双眼视力相差2行及以上）
     *
     * @param leftNakedVision  左眼裸视
     * @param rightNakedVision 右眼裸视
     * @param age              年龄
     */
    public static Boolean kindergartenCondition1(BigDecimal leftNakedVision, BigDecimal rightNakedVision, Integer age) {
        return ObjectsUtil.allNotNull(leftNakedVision, rightNakedVision, age)
                && age < 7
                && BigDecimalUtil.moreThanAndEqual(leftNakedVision.abs().subtract(rightNakedVision.abs()).abs(), "0.2");
    }

    /**
     * 满足幼儿园判断条件 (裸眼视力在 *＜5岁  视力≤4.8 ; 5≤*＜7  视力≤4.9 )
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static Boolean kindergartenCondition2(BigDecimal nakedVision, Integer age) {
        return ObjectsUtil.allNotNull(nakedVision, age)
                && ((age < 5 && BigDecimalUtil.lessThanAndEqual(nakedVision, "4.8"))
                || ((age >= 5 && age < 7) && BigDecimalUtil.lessThanAndEqual(nakedVision, "4.9")));

    }

    /**
     * 建议就诊： 满足幼儿园判断 且 戴镜
     *
     * @param age             年龄
     * @param correctedVision 矫正视力
     */
    public static Integer kindergartenWearingGlasses(Integer age, BigDecimal correctedVision) {
        //欠矫者
        Boolean wearingGlassesCondition2 = age < 5 && BigDecimalUtil.lessThanAndEqual(correctedVision, "4.8");
        Boolean wearingGlassesCondition3 = age >= 5 && BigDecimalUtil.lessThanAndEqual(correctedVision, "4.9");

        //足矫者
        Boolean wearingGlassesCondition4 = age < 5 && BigDecimalUtil.moreThan(correctedVision, "4.8");
        Boolean wearingGlassesCondition5 = age >= 5 && BigDecimalUtil.moreThan(correctedVision, "4.9");


        if (wearingGlassesCondition2 || wearingGlassesCondition3) {
            return 1;
        }
        if (wearingGlassesCondition4 || wearingGlassesCondition5) {
            return 2;
        }

        return null;
    }

    /**
     * 建议就诊： 满足幼儿园判断 且 未戴镜
     *
     * @param se       等效球镜
     * @param cyl      柱镜
     * @param leftSph  左眼球镜度
     * @param rightSph 右眼球镜度
     */
    public static Integer kindergartenWithoutGlasses(BigDecimal se, BigDecimal cyl,
                                                     String leftSph, String rightSph) {

        boolean withoutGlassesCondition1 = se != null;
        boolean withoutGlassesCondition2 = cyl != null;

        if (withoutGlassesCondition1 && withoutGlassesCondition2
                && BigDecimalUtil.isBetweenLeft(se, "0.00", "2.00")
                && BigDecimalUtil.lessThanAndEqual(cyl.abs(), "1.50")) {
            return 3;
        }

        if (withoutGlassesCondition1 && BigDecimalUtil.lessThan(se, "0.00")) {
            return 4;
        }
        boolean withoutGlassesCondition3 = withoutGlassesCondition1 && (BigDecimalUtil.moreThan(se, "2.00") || BigDecimalUtil.moreThan(se, "0.00"));
        boolean withoutGlassesCondition4 = withoutGlassesCondition2 && BigDecimalUtil.moreThan(cyl.abs(), "1.50");
        if (withoutGlassesCondition3 || withoutGlassesCondition4 || isAnisometropiaVision(leftSph, rightSph)) {
            return 5;
        }

        return null;
    }

    /**
     * 建议就诊：满足小学及以上 且 裸眼视力<4.9 且戴眼镜
     *
     * @param nakedVision     裸眼视力
     * @param correctedVision 矫正视力
     */
    public static Boolean primarySchoolAndAboveWearingGlasses(BigDecimal nakedVision, BigDecimal correctedVision) {
        if (ObjectsUtil.allNotNull(nakedVision, correctedVision)) {
            Boolean wearingGlassesCondition1 = BigDecimalUtil.lessThan(nakedVision, "4.9");
            Boolean wearingGlassesCondition2 = BigDecimalUtil.lessThan(correctedVision, "4.9");
            Boolean wearingGlassesCondition3 = BigDecimalUtil.moreThanAndEqual(correctedVision, "4.9");
            if (wearingGlassesCondition1 && wearingGlassesCondition2) {
                return Boolean.TRUE;
            }
            if (wearingGlassesCondition1 && wearingGlassesCondition3) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 建议就诊：满足小学及以上 且 裸眼视力<4.9且 未戴眼镜
     *
     * @param nakedVision 裸眼视力
     * @param se          等效球镜
     * @param cyl         柱镜
     * @param schoolType  学校类型
     */
    public static Boolean primarySchoolAndAboveWithoutGlasses(BigDecimal nakedVision, BigDecimal se, BigDecimal cyl, Integer schoolType) {
        if (ObjectsUtil.allNull(nakedVision, schoolType, se, cyl)) {
            return Boolean.FALSE;
        }

        boolean condition = BigDecimalUtil.lessThan(nakedVision, "4.9");
        if (condition) {
            return getSchoolType(se, cyl, schoolType);
        } else {
            return Boolean.FALSE;
        }
    }

    private static Boolean getSchoolType(BigDecimal se, BigDecimal cyl, Integer schoolType) {
        if (SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
            Boolean withoutGlassesCondition1 = BigDecimalUtil.isBetweenLeft(se, "0.00", "2.00");
            Boolean withoutGlassesCondition2 = BigDecimalUtil.lessThan(cyl.abs(), "1.50");
            if (withoutGlassesCondition1 && withoutGlassesCondition2) {
                return Boolean.TRUE;
            }

            if (BigDecimalUtil.lessThan(se, "0.00")
                    || BigDecimalUtil.moreThanAndEqual(se, "2.00")
                    || BigDecimalUtil.moreThan(cyl.abs(), "1.50")) {
                return Boolean.TRUE;
            }
        } else {
            Boolean withoutGlassesCondition1 = BigDecimalUtil.isBetweenLeft(se, MINUS_0_5, "3.00");
            Boolean withoutGlassesCondition2 = BigDecimalUtil.lessThan(cyl.abs(), "1.5");
            if (withoutGlassesCondition1 && withoutGlassesCondition2) {
                return Boolean.TRUE;
            }
            if (BigDecimalUtil.lessThan(se, MINUS_0_5)
                    || BigDecimalUtil.moreThanAndEqual(se, "3.00")
                    || BigDecimalUtil.moreThan(cyl.abs(), "1.50")) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }


    /**
     * 建议就诊：满足小学及以上 且 裸眼视力>=4.9
     *
     * @param nakedVision 裸眼视力
     * @param se          等效球镜
     * @param age         年龄
     */
    public static Boolean primarySchoolAndAbove(BigDecimal nakedVision, BigDecimal se, Integer age) {
        if (ObjectsUtil.allNotNull(nakedVision, se) && BigDecimalUtil.lessThanAndEqual(nakedVision, "4.9")) {

            if (age != null && age >= 6) {
                return BigDecimalUtil.moreThanAndEqual(se, "2.00");
            }

            if (BigDecimalUtil.moreThanAndEqual(se, "0.00")) {
                return true;
            }
        }
        return false;
    }


    //================6、身高范围标准

    /**
     * 建议课桌椅高度的计算方式：
     *
     * @param height 身高
     */
    public static TwoTuple<Integer, Integer> calculateDeskAndChairHigh(Integer height) {
        BigDecimal desk = new BigDecimal(height.toString()).multiply(new BigDecimal("0.43"), new MathContext(0, RoundingMode.HALF_UP));
        BigDecimal chair = new BigDecimal(height.toString()).multiply(new BigDecimal("0.24"), new MathContext(0, RoundingMode.HALF_UP));
        return new TwoTuple<>(desk.intValue(), chair.intValue());
    }


    //=============7、复测

    /**
     * 复测：视力筛查 发生率
     *
     * @param itemErrorNumList                错误项次数集合
     * @param wearingGlassesIndicatorTotalNum 戴镜复测指标数的总和（包括左右眼裸眼视力、左右眼戴镜视力、左右眼等效球镜度数共6项）
     * @param wearingGlassesRetestNum         戴镜复测人数
     * @param withoutGlassesIndicatorTotalNum 非戴镜复测指标数的总和（包括左右眼裸眼视力、左右眼等效球镜度数共4项）
     * @param withoutGlassesRetestNum         非戴镜复测人数
     */
    public static BigDecimal visionScreeningRetest(List<Integer> itemErrorNumList,
                                                   Integer wearingGlassesIndicatorTotalNum, Integer wearingGlassesRetestNum,
                                                   Integer withoutGlassesIndicatorTotalNum, Integer withoutGlassesRetestNum) {

        int dividedSum = itemErrorNumList.stream().mapToInt(Integer::intValue).sum();
        int denominatorSum = wearingGlassesIndicatorTotalNum * wearingGlassesRetestNum + withoutGlassesIndicatorTotalNum * withoutGlassesRetestNum;
        return new BigDecimal(dividedSum).divide(new BigDecimal(denominatorSum), 2, RoundingMode.HALF_UP);
    }

    /**
     * 复测：常见病筛查 发生率
     *
     * @param itemErrorNumList                错误项次数集合
     * @param wearingGlassesIndicatorTotalNum 戴镜复测指标数的总和（包括身高、体重、左右眼裸眼视力、左右眼戴镜视力、左右眼等效球镜度数共8项）
     * @param wearingGlassesRetestNum         戴镜复测人数
     * @param withoutGlassesIndicatorTotalNum 非戴镜复测指标数的总和（包括身高、体重、左右眼裸眼视力、左右眼等效球镜度数共6项）
     * @param withoutGlassesRetestNum         非戴镜复测人数
     */
    public static BigDecimal commonDiseaseScreeningRetest(List<Integer> itemErrorNumList,
                                                          Integer wearingGlassesIndicatorTotalNum, Integer wearingGlassesRetestNum,
                                                          Integer withoutGlassesIndicatorTotalNum, Integer withoutGlassesRetestNum) {

        int molecularSum = itemErrorNumList.stream().mapToInt(Integer::intValue).sum();
        int denominatorSum = wearingGlassesIndicatorTotalNum * wearingGlassesRetestNum + withoutGlassesIndicatorTotalNum * withoutGlassesRetestNum;
        return new BigDecimal(molecularSum).divide(new BigDecimal(denominatorSum), 2, RoundingMode.HALF_UP);
    }


    //=================8、常见病相关指标【占比：保留2位小数点】


    /**
     * BMI = 体重/身高*身高
     *
     * @param weight 体重 kg
     * @param height 身高 m
     */
    public static BigDecimal bmi(BigDecimal weight, BigDecimal height) {
        BigDecimal heightSquare = height.multiply(height);
        return weight.divide(heightSquare, 1, RoundingMode.HALF_UP);
    }

    /**
     * 是否超重/是否肥胖
     *
     * @param weight 体重 kg
     * @param height 身高 m
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(String weight, String height, String age, Integer gender) {
        return isOverweightAndObesity(new BigDecimal(weight), new BigDecimal(height), age, gender);
    }

    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        BigDecimal bmi = bmi(weight, height);
        return isOverweightAndObesity(bmi,age,gender);
    }
    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(BigDecimal bmi,  String age, Integer gender) {
        StandardTableData.OverweightAndObesityData data = StandardTableData.getOverweightAndObesityData(age, gender);
        Boolean overweight=Boolean.FALSE;
        Boolean obesity=Boolean.FALSE;
        if(Objects.nonNull(data)){
            overweight = BigDecimalUtil.isBetweenLeft(bmi, data.getOverweight(), data.getObesity());
            obesity = BigDecimalUtil.moreThanAndEqual(bmi, data.getObesity());
        }
        return new TwoTuple<>(overweight, obesity);
    }

    /**
     * 是否生长迟缓
     *
     * @param gender 性别
     * @param age    年龄 （精确到半岁）
     * @param height 身高 cm
     */
    public static Boolean isStunting(Integer gender, String age, String height) {
        return isStunting(gender, age, new BigDecimal(height));
    }

    public static Boolean isStunting(Integer gender, String age, BigDecimal height) {
        StandardTableData.StuntingData stuntingData = StandardTableData.getStuntingData(age, gender);
        if(Objects.nonNull(stuntingData)){
            return BigDecimalUtil.lessThanAndEqual(height, stuntingData.getHeight());
        }
        return Boolean.FALSE;
    }

    /**
     * 是否消瘦
     *
     * @param weight 体重 kg
     * @param height 身高 m
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
    public static Boolean isWasting(String weight, String height, String age, Integer gender) {
        return isWasting(new BigDecimal(weight), new BigDecimal(height), age, gender);
    }

    public static Boolean isWasting(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        BigDecimal bmi = bmi(weight, height);
        return isWasting(bmi,age,gender);
    }
    public static Boolean isWasting(BigDecimal bmi, String age, Integer gender) {
        StandardTableData.WastingData wastingData = StandardTableData.getWastingData(age, gender);
        if(Objects.nonNull(wastingData)){
            Boolean mild = BigDecimalUtil.isBetweenAll(bmi, wastingData.getMild()[0], wastingData.getMild()[1]);
            Boolean moderateAndHigh = BigDecimalUtil.lessThanAndEqual(bmi, wastingData.getModerateAndHigh());
            return mild || moderateAndHigh;
        }
        return Boolean.FALSE;
    }


    /**
     * 是否血压偏高
     * 7～17岁男、女儿童青少年凡收缩压和（或）舒张压≥同性别、同年龄、同身高百分位血压P95者为血压偏高。
     * 18岁男女青少年参考成人标准，收缩压≥140 mmHg和（或）舒张压≥90 mmHg者为血压偏高。
     *
     * @param sbp    收缩压
     * @param dbp    舒张压
     * @param gender 性别
     * @param age    年龄
     */
    public static boolean isHighBloodPressure(Integer sbp, Integer dbp, Integer gender, Integer age) {
        if (age >= 7 && age <= 17) {
            StandardTableData.BloodPressureData bloodPressureData = StandardTableData.getBloodPressureData(age, gender);
            return sbp >= bloodPressureData.getSbp() && dbp >= bloodPressureData.getDbp();
        }
        if (age >= 18) {
            return sbp >= 140 && dbp >= 90;
        }

        return Boolean.FALSE;
    }

    /**
     * 获取年龄
     * 返回值：Integer:年龄的整数（四舍五入），String：精确到半岁
     *
     * @param birthday 生日
     * @return
     */
    public static TwoTuple<Integer, String> getAge(Date birthday) {
        LocalDate localDate = DateUtil.convertToLocalDate(birthday, ZoneId.systemDefault());
        Period between = Period.between(localDate, LocalDate.now());
        int years = between.getYears();
        int months = between.getMonths();
        String ageStr = "";
        if (months >= 6) {
            ageStr = years + ".5";
            years++;
        } else {
            ageStr = years + ".0";
        }
        return new TwoTuple<>(years, ageStr);
    }




    //======================= 旧 ：暂时不删，通过业务代码修改再去删除或者过期

    /**
     * 获取近视等级描述（严重的眼球）
     *
     * @param leftSpn          左眼球镜
     * @param leftCyl          左眼柱镜
     * @param rightSpn         右眼球镜
     * @param rightCyl         右眼球镜
     * @param age              年龄
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @return String
     */
    public String getMyopiaLevelDesc(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl,
                                     Integer age, BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        if (ObjectsUtil.allNull(leftSpn, leftCyl, rightCyl, rightSpn)) {
            return "";
        }
        Integer leftMyopiaLevel = null;
        Integer rightMyopiaLevel = null;
        if (ObjectsUtil.allNotNull(leftSpn, leftCyl, age, leftNakedVision)) {
            leftMyopiaLevel = getMyopiaLevel(leftSpn.floatValue(), leftCyl.floatValue(), age, leftNakedVision.floatValue());
        }
        if (ObjectsUtil.allNotNull(rightSpn, rightCyl, age, rightNakedVision)) {
            rightMyopiaLevel = getMyopiaLevel(rightSpn.floatValue(), rightCyl.floatValue(), age, rightNakedVision.floatValue());
        }
        if (!ObjectsUtil.allNull(leftMyopiaLevel, rightMyopiaLevel)) {
            Integer seriousLevel = getSeriousLevel(leftMyopiaLevel, rightMyopiaLevel);
            return MyopiaLevelEnum.getDesc(seriousLevel);
        }
        return "";
    }

    /**
     * 获取远视等级描述（严重的眼球）
     *
     * @param leftSpn  左眼球镜
     * @param leftCyl  左眼柱镜
     * @param rightSpn 右眼球镜
     * @param rightCyl 右眼球镜
     * @return String
     */
    public String getHyperopiaDesc(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl, Integer age) {
        if (ObjectsUtil.allNull(leftSpn, leftCyl, rightSpn, rightCyl)) {
            return "";
        }
        HyperopiaLevelEnum leftLevel = null;
        HyperopiaLevelEnum rightLevel = null;
        if (ObjectsUtil.allNotNull(leftSpn, leftCyl)) {
            leftLevel = getHyperopiaWarningLevel(leftSpn.floatValue(), leftCyl.floatValue(), age);
        }
        if (ObjectsUtil.allNotNull(rightSpn, rightCyl)) {
            rightLevel = getHyperopiaWarningLevel(rightSpn.floatValue(), rightCyl.floatValue(), age);
        }

        if (ObjectsUtil.allNull(leftLevel, rightLevel)) {
            return "";
        }
        Integer leftHyperopiaLevel = Objects.nonNull(leftLevel) ? leftLevel.code : null;
        Integer rightHyperopiaLevel = Objects.nonNull(rightLevel) ? rightLevel.code : null;
        if (!ObjectsUtil.allNull(leftHyperopiaLevel, rightHyperopiaLevel)) {
            Integer seriousLevel = getSeriousLevel(leftHyperopiaLevel, rightHyperopiaLevel);
            return HyperopiaLevelEnum.getDesc(seriousLevel);
        }
        return "";
    }

    /**
     * 获取散光描述
     *
     * @param leftCyl  左眼柱镜
     * @param rightCyl 右眼球镜
     * @return String
     */
    public String getAstigmatismDesc(BigDecimal leftCyl, BigDecimal rightCyl) {
        if (Objects.nonNull(leftCyl) && (leftCyl.abs().compareTo(new BigDecimal("0.5")) > 0)) {
            return "散光";
        }
        if (Objects.nonNull(rightCyl) && (rightCyl.abs().compareTo(new BigDecimal("0.5")) > 0)) {
            return "散光";
        }
        return "";
    }

    /**
     * 获取屈光描述
     *
     * @param leftSpn          左眼球镜
     * @param leftCyl          左眼柱镜
     * @param rightSpn         右眼球镜
     * @param rightCyl         右眼球镜
     * @param age              年龄
     * @param leftNakedVision
     * @param rightNakedVision
     * @return 描述
     */
    public String getRefractiveResult(BigDecimal leftSpn, BigDecimal leftCyl,
                                      BigDecimal rightSpn, BigDecimal rightCyl,
                                      Integer age, BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        return getMyopiaLevelDesc(leftSpn, leftCyl, rightSpn, rightCyl, age, leftNakedVision, rightNakedVision)
                + getHyperopiaDesc(leftSpn, leftCyl, rightSpn, rightCyl, age)
                + getAstigmatismDesc(leftCyl, rightCyl);
    }

    /**
     * 获取预警等级（两眼取严重的）
     *
     * @param leftCyl          左柱镜
     * @param leftSpn          左球镜
     * @param leftNakedVision  左裸眼视力
     * @param rightCyl         右柱镜
     * @param rightSpn         右球镜
     * @param rightNakedVision 右裸眼视力
     * @param age              年龄
     * @return {@link WarningLevel}
     */
    public Integer getWarningLevelInt(BigDecimal leftCyl, BigDecimal leftSpn, BigDecimal leftNakedVision,
                                      BigDecimal rightCyl, BigDecimal rightSpn, BigDecimal rightNakedVision,
                                      Integer age) {
        WarningLevel left = getWarningLevel(leftCyl, leftSpn, leftNakedVision, age);
        WarningLevel right = getWarningLevel(rightCyl, rightSpn, rightNakedVision, age);
        return getWarningSeriousLevel(Objects.nonNull(left) ? left.code : null, Objects.nonNull(right) ? right.code : null);
    }

    /**
     * 单眼获取预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    public WarningLevel getWarningLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision, Integer age) {

        if (ObjectsUtil.hasNull(cyl, spn, nakedVision, age)) {
            return null;
        }
        if (age >= 3 && age < 6) {
            return between3And5GetLevel(cyl, spn, nakedVision, age);
        }
        if (age >= 6 && age < 8) {
            return between6And7GetLevel(cyl, spn, nakedVision, age);
        }
        if (age >= 8) {
            return moreThan8GetLevel(cyl, spn, nakedVision);
        }
        return zeroSPWarningLevel(cyl, spn, age);
    }

    /**
     * 3到5岁预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel between3And5GetLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        BigDecimal absCyl = cyl.abs();
        if (Objects.nonNull(zeroSPWarningLevel(cyl, spn, age))) {
            return zeroSPWarningLevel(cyl, spn, age);
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (age < 4 && BigDecimalUtil.moreThan(se, "9")) || (age >= 4 && BigDecimalUtil.moreThan(se, "8")) || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "6", "9") || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "5", "8")) || twoAbsCyl(absCyl))) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.6", "4.7") || oneSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "3", "6")) || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "2", "5")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.7") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 6到7岁预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel between6And7GetLevel(BigDecimal cyl, BigDecimal spn,
                                              BigDecimal nakedVision, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        BigDecimal absCyl = cyl.abs();
        if (Objects.isNull(se)) {
            return null;
        }
        if (Objects.nonNull(zeroSPWarningLevel(cyl, spn, age))) {
            return zeroSPWarningLevel(cyl, spn, age);
        }
        if ((BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5")) || threeSE(se) || BigDecimalUtil.moreThan(se, "7.5") || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || BigDecimalUtil.isBetweenRight(se, "4.5", "7.5") || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.8") || oneSE(se) || BigDecimalUtil.isBetweenRight(se, "1.5", "4.5") || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.8") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 8岁以上预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @return {@link WarningLevel}
     */
    private WarningLevel moreThan8GetLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        BigDecimal absCyl = cyl.abs();
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (BigDecimalUtil.moreThan(se, "6") || threeAbsCyl(absCyl))) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.7") || twoSE(se) || (BigDecimalUtil.isBetweenRight(se, "3", "6")) || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.9") || oneSE(se) || (BigDecimalUtil.isBetweenRight(se, "0.5", "3")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.9") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 0级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean zeroSE(BigDecimal se) {
        return (BigDecimalUtil.isBetweenAll(se, "-0.5", "-0.25"));
    }

    /**
     * 1级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean oneSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenLeft(se, "-3", "-0.5");
    }

    /**
     * 2级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean twoSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenLeft(se, "-6", "-3");
    }

    /**
     * 3级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean threeSE(BigDecimal se) {
        return BigDecimalUtil.lessThan(se, "-6");
    }

    /**
     * 0级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean zeroAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenLeft(absCyl, "0.25", "0.5");
    }

    /**
     * 1级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean oneAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenAll(absCyl, "0.5", "2");
    }

    /**
     * 2级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean twoAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenRight(absCyl, "2", "4");
    }

    /**
     * 3级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean threeAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.moreThan(absCyl, "4");
    }

    /**
     * 取预警级别严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getWarningSeriousLevel(Integer leftLevel, Integer rightLevel) {
        // 排除远视储备不足
        if (Objects.isNull(leftLevel)) {
            return rightLevel;
        }
        if (Objects.isNull(rightLevel)) {
            return leftLevel;
        }
        if (leftLevel.equals(WarningLevel.ZERO_SP.code)) {
            if (rightLevel.equals(WarningLevel.ZERO.code) || rightLevel.equals(WarningLevel.NORMAL.code)) {
                return leftLevel;
            }
            return rightLevel;
        }
        if (rightLevel.equals(WarningLevel.ZERO_SP.code)) {
            if (leftLevel.equals(WarningLevel.ZERO.code) || leftLevel.equals(WarningLevel.NORMAL.code)) {
                return rightLevel;
            }
            return leftLevel;
        }
        return leftLevel > rightLevel ? leftLevel : rightLevel;
    }


    /**
     * 取严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getSeriousLevel(Integer leftLevel, Integer rightLevel) {
        if (Objects.isNull(leftLevel)) {
            return rightLevel;
        }
        if (Objects.isNull(rightLevel)) {
            return leftLevel;
        }
        return leftLevel > rightLevel ? leftLevel : rightLevel;
    }

    /**
     * 远视储备不足
     *
     * @param cyl 柱镜
     * @param spn 球镜
     * @param age 年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel zeroSPWarningLevel(BigDecimal cyl, BigDecimal spn, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        if (age >= 3 && age < 6 && BigDecimalUtil.isBetweenAll(se, "0", "1.5")) {
            return WarningLevel.ZERO_SP;
        }

        if (age >= 6 && age < 8 && BigDecimalUtil.isBetweenAll(se, "0", "1")) {
            return WarningLevel.ZERO_SP;
        }
        return null;
    }
}
