package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.constant.CorrectionEnum;
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
import java.util.*;
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
        if (ObjectsUtil.hasNull(nakedVision,age) || age < 6) {
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
        if (ObjectsUtil.hasNull(nakedVision,age) || age < 6) {
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
            if (age < 6 && Objects.nonNull(nakedVision) && BigDecimalUtil.lessThan(BigDecimal.valueOf(nakedVision),"4.9")) {
                return true;
            }
            if (Objects.nonNull(nakedVision) && age >= 6 && BigDecimalUtil.lessThan(BigDecimal.valueOf(nakedVision),"5.0")) {
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
     *      【 * ＜3】2岁儿童 等效球镜（SE）> +4.50D或SE<-3.50D或柱镜>|1.00|D
     *      【3≤ * ＜4】3岁儿童 等效球镜（SE）> +4.00D或SE<-3.00D或柱镜>|1.00|D
     *      【4≤ * ＜5】4岁儿童 等效球镜（SE）> +4.00D或 SE<-3.00D或柱镜>|2.00|D
     *      【* ≥5】5岁及以上儿童 等效球镜（SE）> +3.50D或 SE<-1.50D或柱镜>|1.50|D
     *
     *
     *       针对0-6岁眼保健平台
     *         24月龄。柱镜（散光） >|2.00|D，等效球镜（SE） >+4.50D，等效球镜（SE） <-3.50D
     *         36月龄。屈光不正：柱镜（散光） >|2.00|D，等效球镜（SE） >+4.00D，等效球镜（SE） <-3.00D
     *
     *         屈光不正学生数，占比：屈光不正数 / 纳入统计的学生数 * 100%
     */

    /**
     * 是否屈光不正
     *
     * @param sphere 球镜
     * @param cyl    柱镜
     * @param age    年龄
     */
    public static Boolean isRefractiveError(String sphere, String cyl, Integer age) {
        if (StrUtil.isBlank(sphere) || StrUtil.isBlank(cyl)){
            return null;
        }
        return isRefractiveError(new BigDecimal(sphere), new BigDecimal(cyl), age);
    }

    public static Boolean isRefractiveError(BigDecimal sphere, BigDecimal cyl, Integer age) {
        if (ObjectsUtil.hasNull(sphere,cyl,age)) {
            return null;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cyl);
        if (age>5){
            age =5;
        }
        if (age < 2){
            age=2;
        }
        switch (age) {
            case 2:
                return refractiveError2(se, cyl);
            case 3:
                return refractiveError3(se, cyl);
            case 4:
                return refractiveError4(se, cyl);
            case 5:
                return refractiveErrorOver5(se, cyl);
            default:
                return null;
        }
    }
    private static Boolean refractiveError2(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, "-3.50") || BigDecimalUtil.moreThan(se, "4.50")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.00").abs()));
    }

    private static Boolean refractiveError3(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.00").abs()));
    }
    private static Boolean refractiveError4(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("2.00").abs()));
    }

    private static Boolean refractiveErrorOver5(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, "-1.50") || BigDecimalUtil.moreThan(se, "3.50")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.50").abs()));
    }



    //=============== 欠矫、足矫


    /**
     *  矫正
     *
     * @param leftNakedVision  左眼裸视
     * @param rightNakedVision 右眼裸视
     * @param schoolType 学校类型
     * @param age 年龄
     * @param isWearGlasses 是否戴镜（true-戴镜，false-不戴镜）
     */
    public static Integer correction(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                     Integer schoolType,Integer age,Boolean isWearGlasses) {

        if (ObjectsUtil.hasNull(schoolType,age,isWearGlasses)){
            return null;
        }
        if (Objects.equals(SchoolEnum.TYPE_KINDERGARTEN.getType(),schoolType)){

            if(age < 5){
                return kindergartenCorrection5(leftNakedVision,rightNakedVision,isWearGlasses);
            }

            if (age >= 5 && age < 7){
                return kindergartenCorrection7(leftNakedVision,rightNakedVision,isWearGlasses);
            }

            return null;
        }else {
            return primarySchoolAboveCorrection(leftNakedVision,rightNakedVision,isWearGlasses);
        }
    }

    private static Integer kindergartenCorrection5(BigDecimal leftNakedVision, BigDecimal rightNakedVision,Boolean isWearGlasses){
        if (ObjectsUtil.hasNull(leftNakedVision,rightNakedVision,isWearGlasses)){
            return null;
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,"4.8")
                ||BigDecimalUtil.lessThanAndEqual(rightNakedVision,"4.8")){

            if(isWearGlasses){
                if (BigDecimalUtil.moreThan(leftNakedVision,"4.8") && BigDecimalUtil.moreThan(rightNakedVision,"4.8")){
                    return CorrectionEnum.ABOVE_CORRECTION.getCode();
                }
                if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,"4.9") && BigDecimalUtil.lessThanAndEqual(rightNakedVision,"4.9")){
                    return CorrectionEnum.UNDER_CORRECTION.getCode();
                }
            }else {
                return CorrectionEnum.NORMAL_CORRECTION.getCode();
            }
        }
        return null;

    }
    private static Integer kindergartenCorrection7(BigDecimal leftNakedVision, BigDecimal rightNakedVision,Boolean isWearGlasses){
        if (ObjectsUtil.hasNull(leftNakedVision,rightNakedVision,isWearGlasses)){
            return null;
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,"4.9")
                ||BigDecimalUtil.lessThanAndEqual(rightNakedVision,"4.9")){

            if(isWearGlasses){
                if (BigDecimalUtil.moreThan(leftNakedVision,"4.9") && BigDecimalUtil.moreThan(rightNakedVision,"4.9")){
                    return CorrectionEnum.ABOVE_CORRECTION.getCode();
                }
                if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,"4.9") && BigDecimalUtil.lessThanAndEqual(rightNakedVision,"4.9")){
                    return CorrectionEnum.UNDER_CORRECTION.getCode();
                }
            }else {
                return CorrectionEnum.NORMAL_CORRECTION.getCode();
            }
        }
        return null;
    }

    private static Integer primarySchoolAboveCorrection(BigDecimal leftNakedVision, BigDecimal rightNakedVision,Boolean isWearGlasses){
        if (ObjectsUtil.hasNull(leftNakedVision,rightNakedVision,isWearGlasses)){
            return null;
        }
        if (BigDecimalUtil.lessThan(leftNakedVision,"4.9")
                ||BigDecimalUtil.lessThan(rightNakedVision,"4.9")){

            if(isWearGlasses){
                if (BigDecimalUtil.moreThan(leftNakedVision,"4.9") && BigDecimalUtil.moreThan(rightNakedVision,"4.9")){
                    return CorrectionEnum.ABOVE_CORRECTION.getCode();
                }
                if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,"4.9") && BigDecimalUtil.lessThanAndEqual(rightNakedVision,"4.9")){
                    return CorrectionEnum.UNDER_CORRECTION.getCode();
                }
            }else {
                return CorrectionEnum.NORMAL_CORRECTION.getCode();
            }
        }
        return null;
    }


    /**
     * 屈光参差：双眼球镜度（远视、近视）差值>1.50D
     *
     * @param leftSph  左眼球镜度
     * @param rightSph 右眼球镜度
     */
    public static Boolean isAnisometropiaVision(String leftSph, String rightSph) {
        if (StrUtil.isNotBlank(leftSph) && StrUtil.isNotBlank(rightSph)) {
            return isAnisometropiaVision(new BigDecimal(leftSph),new BigDecimal(rightSph));
        }
        return null;
    }

    public static Boolean isAnisometropiaVision(BigDecimal leftSph, BigDecimal rightSph){
        if (ObjectsUtil.allNotNull(leftSph,rightSph)) {
            BigDecimal diffSph = leftSph.subtract(rightSph).abs();
            return BigDecimalUtil.moreThan(diffSph, "1.50");
        }
        return null;
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
        return null;
    }
    public static Boolean isAnisometropiaAstigmatism(BigDecimal leftCyl, BigDecimal rightCyl) {
        if (ObjectsUtil.allNotNull(leftCyl,rightCyl)) {
            BigDecimal diffCyl = leftCyl.subtract(rightCyl).abs();
            return BigDecimalUtil.moreThan(diffCyl, "1.00");
        }
        return null;
    }

    /**
     * 是否散光
     *
     * @param cyl 柱镜
     * @return
     */
    public Boolean isAstigmatism(String cyl) {
        if (StrUtil.isBlank(cyl)){
            return null;
        }
        return isAstigmatism(new BigDecimal(cyl));
    }
    public Boolean isAstigmatism(BigDecimal cyl) {
        if(Objects.isNull(cyl)){
            return null;
        }
        return BigDecimalUtil.moreThanAndEqual(cyl.abs(), "0.50");
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
            return null;
        }
        return getAstigmatismWarningLevel(cyl.toString());
    }

    public static AstigmatismLevelEnum getAstigmatismWarningLevel(String cyl) {
        if (StrUtil.isBlank(cyl)) {
            return null;
        }
        return getAstigmatismWarningLevel(new BigDecimal(cyl));
    }

    public static AstigmatismLevelEnum getAstigmatismWarningLevel(BigDecimal cyl) {
        if (Objects.isNull(cyl)) {
            return null;
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
        return null;
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
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
           return null;
        }
        return isHyperopia(sphere.toString(), cylinder.toString(), age);
    }

    public static Boolean isHyperopia(String sphere, String cylinder, Integer age) {
        if(StrUtil.isBlank(sphere) || StrUtil.isBlank(cylinder)){
            return null;
        }
        return isHyperopia(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    public static Boolean isHyperopia(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (Objects.isNull(age)) {
            return null;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)) {
            return null;
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
        if (ObjectsUtil.hasNull(sphere,cylinder,age)) {
            return null;
        }
        return getHyperopiaWarningLevel(sphere.toString(), cylinder.toString(), age);
    }

    public static HyperopiaLevelEnum getHyperopiaWarningLevel(String sphere, String cylinder, Integer age) {
        if (StrUtil.isBlank(sphere) || StrUtil.isBlank(cylinder) || age == null) {
            return null;
        }
        return getHyperopiaWarningLevel(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    public static HyperopiaLevelEnum getHyperopiaWarningLevel(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (ObjectsUtil.hasNull(sphere,cylinder,age)) {
            return null;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)) {
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
        if (ObjectsUtil.hasNull(sphere,cylinder,age)) {
            return null;
        }
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        if (Objects.nonNull(hyperopiaWarningLevel)) {
            return hyperopiaWarningLevel.code;
        }
        return null;
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
                && ((age < 6 && BigDecimalUtil.lessThan(nakedVision, "4.9")) || (age >= 6 && BigDecimalUtil.lessThan(nakedVision, "5.0")))) {

            if (BigDecimalUtil.isBetweenLeft(se, MINUS_3, MINUS_0_5)) {
                return MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT;
            }
            if (BigDecimalUtil.isBetweenLeft(se, MINUS_6, MINUS_3)) {
                return MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE;
            }
            if (BigDecimalUtil.lessThan(se, MINUS_6)) {
                return MyopiaLevelEnum.MYOPIA_LEVEL_HIGH;
            }
        }

        if (BigDecimalUtil.isBetweenRight(se, MINUS_0_5, "0.75")) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_EARLY;
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
        if (StrUtil.isBlank(nakedVision)) {
            return null;
        }
        return nakedVision(new BigDecimal(nakedVision), age);
    }

    public static WarningLevel nakedVision(BigDecimal nakedVision, Integer age) {
        if (ObjectsUtil.hasNull(nakedVision, age) || (Objects.nonNull(age) && age < 3)) {
            return null;
        }
        if (age >= 6){
            age = 5;
        }
        switch (age) {
            case 3:
                return nakedVision3(nakedVision);
            case 4:
                return nakedVision4(nakedVision);
            case 5:
                return nakedVisionMoreThanAndEqual5(nakedVision);
            default:
                return null;
        }
    }

    private static WarningLevel nakedVision3(BigDecimal nakedVision) {
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

    private static WarningLevel nakedVision4(BigDecimal nakedVision) {
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

    private static WarningLevel nakedVisionMoreThanAndEqual5(BigDecimal nakedVision) {
        if (BigDecimalUtil.lessThan(nakedVision, "4.8")) {
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
     * 屈光数据 (小学及以上)
     * 1、近视：se 不能为空
     * 2、远视：se和age 不能为空
     * 3、散光：cyl 不能为空
     *
     * @param sphere   球镜
     * @param cyl  柱镜
     * @param age  年龄
     * @param type 类型 （近视-0、散光-1、远视-2）
     */
    public static WarningLevel refractiveData(BigDecimal sphere, BigDecimal cyl, Integer age, Integer type) {
        BigDecimal se = getSphericalEquivalent(sphere, cyl);
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
        if (Objects.nonNull(se)) {
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

    private static WarningLevel refractiveDataAstigmatism(BigDecimal cyl) {
        if (Objects.nonNull(cyl)) {
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

    private static WarningLevel refractiveDataFarsighted(BigDecimal se, Integer age) {
        if (age != null) {
            int key = age;
            if (age > 8) {
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
        return null;
    }

    private static WarningLevel refractiveDataFarsighted45(BigDecimal se) {
        if (Objects.nonNull(se)) {
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
        if (Objects.nonNull(se)) {
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



    /**
     * 远视储备不足（也纳入0级预警中）
     *
     * @param sphere 球镜
     * @param cylinder 柱镜
     */
    public static WarningLevel myopiaLevelInsufficient(String sphere,String cylinder) {
        if (StrUtil.isNotBlank(sphere) && StrUtil.isNotBlank(cylinder)) {
            return myopiaLevelInsufficient(new BigDecimal(sphere),new BigDecimal(cylinder));
        }
        return null;
    }

    public static WarningLevel myopiaLevelInsufficient(BigDecimal sphere, BigDecimal cylinder) {
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)){
            return null;
        }
        if (BigDecimalUtil.lessThan(se, "0.00")) {
            return WarningLevel.ZERO_SP;
        }
        return null;
    }


    //===============5、建议就诊 ScreeningResultUtil.getDoctorAdvice



    //================6、身高范围标准

    /**
     * 建议课桌椅高度的计算方式：
     *
     * @param height 身高
     */
    public static TwoTuple<Integer, Integer> calculateDeskAndChairHigh(BigDecimal height) {
        if (Objects.isNull(height)){
            return null;
        }
        BigDecimal desk = height.multiply(new BigDecimal("0.43"), new MathContext(0, RoundingMode.HALF_UP));
        BigDecimal chair = height.multiply(new BigDecimal("0.24"), new MathContext(0, RoundingMode.HALF_UP));
        return new TwoTuple<>(desk.intValue(), chair.intValue());
    }


    //=============7、复测


    //=================8、常见病相关指标【占比：保留2位小数点】


    /**
     * BMI = 体重/身高*身高
     *
     * @param weight 体重 kg
     * @param height 身高 m
     */
    public static BigDecimal bmi(BigDecimal weight, BigDecimal height) {
        if (ObjectsUtil.hasNull(weight, height)) {
            return null;
        }
        BigDecimal heightSquare = height.multiply(height).divide(new BigDecimal("10000"));
        if (heightSquare.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
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
        if (StrUtil.isBlank(weight) || StrUtil.isBlank(height) || StrUtil.isBlank(age) || Objects.isNull(gender)){
            return null;
        }
        return isOverweightAndObesity(new BigDecimal(weight), new BigDecimal(height), age, gender);
    }

    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        if (ObjectsUtil.hasNull(weight,height,gender) || StrUtil.isBlank(age)){
            return null;
        }
        BigDecimal bmi = bmi(weight, height);
        return isOverweightAndObesity(bmi,age,gender);
    }
    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(BigDecimal bmi,  String age, Integer gender) {
        if (ObjectsUtil.hasNull(bmi,gender) || StrUtil.isBlank(age)){
            return null;
        }
        StandardTableData.OverweightAndObesityData data = StandardTableData.getOverweightAndObesityData(age, gender);
        if(Objects.isNull(data)){
            return null;
        }
        Boolean overweight = BigDecimalUtil.isBetweenLeft(bmi, data.getOverweight(), data.getObesity());
        Boolean obesity = BigDecimalUtil.moreThanAndEqual(bmi, data.getObesity());
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
        if (StrUtil.isBlank(height)){
            return null;
        }
        return isStunting(gender, age, new BigDecimal(height));
    }

    public static Boolean isStunting(Integer gender, String age, BigDecimal height) {
        if (ObjectsUtil.hasNull(gender,height) || StrUtil.isBlank(age)){
            return null;
        }
        StandardTableData.StuntingData stuntingData = StandardTableData.getStuntingData(age, gender);
        if(Objects.isNull(stuntingData)){
            return null;
        }
        return BigDecimalUtil.lessThanAndEqual(height, stuntingData.getHeight());
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
        if (StrUtil.isBlank(weight) || StrUtil.isBlank(height) ){
            return null;
        }
        return isWasting(new BigDecimal(weight), new BigDecimal(height), age, gender);
    }

    public static Boolean isWasting(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        if (ObjectsUtil.hasNull(weight,height)){
            return null;
        }
        BigDecimal bmi = bmi(weight, height);
        return isWasting(bmi,age,gender);
    }
    public static Boolean isWasting(BigDecimal bmi, String age, Integer gender) {
        if (ObjectsUtil.hasNull(bmi,gender) ||StrUtil.isBlank(age)){
            return null;
        }
        StandardTableData.WastingData wastingData = StandardTableData.getWastingData(age, gender);
        if (Objects.isNull(wastingData)){
            return null;
        }
        Boolean mild = BigDecimalUtil.isBetweenAll(bmi, wastingData.getMild()[0], wastingData.getMild()[1]);
        Boolean moderateAndHigh = BigDecimalUtil.lessThanAndEqual(bmi, wastingData.getModerateAndHigh());
        return mild || moderateAndHigh;
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
        String ageStr;
        if (months >= 6) {
            ageStr = years + ".5";
            years++;
        } else {
            ageStr = years + ".0";
        }
        return new TwoTuple<>(years, ageStr);
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
                                      Integer age,Integer schoolType) {
        WarningLevel left = getWarningLevel(leftCyl, leftSpn, leftNakedVision, age,schoolType);
        WarningLevel right = getWarningLevel(rightCyl, rightSpn, rightNakedVision, age,schoolType);
        return getSeriousLevel(Optional.ofNullable(left).map(wl->wl.code).orElse(null), Optional.ofNullable(right).map(wl->wl.code).orElse(null));
    }

    /**
     * 获取预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    public WarningLevel getWarningLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision, Integer age,Integer schoolType) {

        if (ObjectsUtil.hasNull(cyl, spn, nakedVision, age)) {
            return null;
        }

        List<WarningLevel> warningLevelList = Lists.newArrayList();

        if (Objects.equals(schoolType,SchoolAge.KINDERGARTEN.code)){
            //裸眼视力
            warningLevelList.add(StatUtil.nakedVision(nakedVision, age));
            warningLevelList.add(StatUtil.myopiaLevelInsufficient(spn, cyl));

        }else {
            //裸眼视力
            warningLevelList.add(StatUtil.nakedVision(nakedVision, age));
            //近视
            warningLevelList.add(StatUtil.refractiveData(spn, cyl, age, 0));
            //散光
            warningLevelList.add(StatUtil.refractiveData(spn, cyl, age, 1));
            //远视
            warningLevelList.add(StatUtil.refractiveData(spn, cyl, age, 2));

        }

        return warningLevelList.stream().filter(Objects::nonNull).max(Comparator.comparing(WarningLevel::getCode)).orElse(null);

    }


    /**
     * 取严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getSeriousLevel(Integer leftLevel, Integer rightLevel) {
        if (ObjectsUtil.allNull(leftLevel,rightLevel)){
            return null;
        }
        if (Objects.isNull(leftLevel)) {
            return rightLevel;
        }
        if (Objects.isNull(rightLevel)) {
            return leftLevel;
        }
        return leftLevel > rightLevel ? leftLevel : rightLevel;
    }

}
