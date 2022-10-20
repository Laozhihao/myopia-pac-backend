package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 筛查结论计算工具 (产品说判断结论时没数据不给任何值)
 */
@UtilityClass
public class StatUtil {

    private static final String MINUS_0_5 = "0.50";
    private static final String MINUS_3 = "-3.00";
    private static final String MINUS_NEGATIVE_0_5 = "-0.50";
    private static final String MINUS_6 = "-6.00";
    private static final BigDecimal visionAndWeightRangeValue = new BigDecimal("0.1");
    private static final BigDecimal seAndHeightRangeValue = new BigDecimal("0.5");

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
     * 复测数据完整(纳入统计的数据)
     * @param visionScreeningResult 筛查结果数据
     */
    public static boolean rescreenCompletedData(VisionScreeningResult visionScreeningResult){

        if (Objects.equals(visionScreeningResult.getScreeningType(),ScreeningTypeEnum.VISION.getType())){
            //视力筛查
            VisionDataDO visionData = visionScreeningResult.getVisionData();
            ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
            if (ObjectsUtil.hasNull(visionData,computerOptometry)){
                return false;
            }
            return isCompletedData(visionData,computerOptometry);
        }else {
            //常见病筛查
            VisionDataDO visionData = visionScreeningResult.getVisionData();
            ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
            HeightAndWeightDataDO heightAndWeightData = visionScreeningResult.getHeightAndWeightData();
            if (ObjectsUtil.hasNull(visionData,computerOptometry,heightAndWeightData)){
                return false;
            }
            return heightAndWeightData.valid() && isCompletedData(visionData,computerOptometry);
        }
    }

    /**
     * 是否配合检查：0-配合、1-不配合
     * @param visionScreeningResult 筛查结果数据
     */
    public static Integer isCooperative(VisionScreeningResult visionScreeningResult) {
        if (Objects.isNull(visionScreeningResult)){
            return null;
        }

        Set<Integer> cooperativeSet = Sets.newHashSet();
        //视力
        Optional.ofNullable(visionScreeningResult.getVisionData()).ifPresent(visionData-> cooperativeSet.add(visionData.getIsCooperative()));

        //屈光
        Optional.ofNullable(visionScreeningResult.getComputerOptometry())
                .map(ComputerOptometryDO::getIsCooperative)
                .ifPresent(cooperativeSet::add);


        if (CollectionUtil.isNotEmpty(cooperativeSet)){
            if (cooperativeSet.size() == 1) {
                if (cooperativeSet.contains(0)){
                    return 0;
                }else {
                    return 1;
                }
            }
            if (cooperativeSet.size() == 2){
                //只要有一个不配合，这条数据都是不配合
                return 1;
            }
        }
        return 0;
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
     * 视力低下等级
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static LowVisionLevelEnum getLowVisionLevel(BigDecimal nakedVision, Integer age) {
        if (ObjectsUtil.hasNull(nakedVision,age) || age < 6) {
            return null;
        }

        if (BigDecimalUtil.decimalEqual(nakedVision, "4.9")) {
            return LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT;
        }
        if (BigDecimalUtil.isBetweenAll(nakedVision, "4.6", "4.8")) {
            return LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE;
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5")) {
            return LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH;
        }
        return null;
    }

    /**
     * 平均视力（左/右） (初筛数据完整才使用)
     * @param statConclusions
     */
    public static TwoTuple<BigDecimal,BigDecimal> calculateAverageVision(List<StatConclusion> statConclusions) {
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsValid())).collect(Collectors.toList());

        List<BigDecimal> visionLeftList = statConclusions.stream().map(StatConclusion::getVisionL).filter(Objects::nonNull).collect(Collectors.toList());
        int leftSumSize = visionLeftList.size();
        double sumVisionL = visionLeftList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        BigDecimal avgVisionL = BigDecimalUtil.divide(String.valueOf(sumVisionL), String.valueOf(leftSumSize),1);

        List<BigDecimal> visionRightList = statConclusions.stream().map(StatConclusion::getVisionR).filter(Objects::nonNull).collect(Collectors.toList());
        int rightSumSize = visionRightList.size();
        double sumVisionR = visionRightList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        BigDecimal avgVisionR = BigDecimalUtil.divide(String.valueOf(sumVisionR), String.valueOf(rightSumSize),1);

        return TwoTuple.of(avgVisionL,avgVisionR);
    }

    /**
     * 平均视力 (初筛数据完整才使用)
     */
    public static BigDecimal averageVision(List<StatConclusion> statConclusions) {
        List<BigDecimal> visionList = statConclusions.stream().flatMap(sc->Lists.newArrayList(sc.getVisionL(),sc.getVisionR()).stream()).filter(Objects::nonNull).collect(Collectors.toList());
        double sumVision = visionList.stream().mapToDouble(BigDecimal::doubleValue).sum();
        return BigDecimalUtil.divide(String.valueOf(sumVision), String.valueOf(visionList.size()),1);
    }

    /**
     * 是否近视
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     * @param age         年龄
     * @param nakedVision 裸眼视力
     */
    public static Boolean isMyopia(BigDecimal sphere, BigDecimal cylinder, Integer age, BigDecimal nakedVision) {

        MyopiaLevelEnum screeningMyopia = getScreeningMyopia(sphere, cylinder, age, nakedVision);
        MyopiaLevelEnum myopiaLevel = getMyopiaLevel(sphere, cylinder);

        if (Objects.isNull(screeningMyopia)){
            if (Objects.nonNull(myopiaLevel) ){
                return !Objects.equals(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY, myopiaLevel);
            }
        }else {
            return Boolean.TRUE;
        }

        return null;
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

    /**
     * 是否屈光不正
     *
     * @param sphere 球镜
     * @param cyl    柱镜
     * @param age    年龄
     */
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

    /**
     * 是否屈光不正（0-6岁平台）
     *
     * @param sphere 球镜
     * @param cyl    柱镜
     * @param age    年龄
     */
    public static Boolean isRefractiveErrorByZeroToSixPlatform(BigDecimal sphere, BigDecimal cyl, Integer age) {
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
                return refractiveError2ByZeroToSixPlatform(se, cyl);
            case 3:
                return refractiveError3ByZeroToSixPlatform(se, cyl);
            case 4:
                return refractiveError4(se, cyl);
            case 5:
                return refractiveErrorOver5(se, cyl);
            default:
                return null;
        }
    }

    /**
     * 小于等于2岁判断
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveError2(BigDecimal se, BigDecimal cyl) {
        boolean b = Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, "-3.50") || BigDecimalUtil.moreThan(se, "4.50"));
        return b || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.00").abs()));

    }

    /**
     * 小于等于2岁判断（0-6岁平台使用）
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveError2ByZeroToSixPlatform(BigDecimal se, BigDecimal cyl) {
        boolean b = Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, "-3.50") || BigDecimalUtil.moreThan(se, "4.50"));
        return b || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("2.00").abs()));
    }

    /**
     * 3岁判断
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveError3(BigDecimal se, BigDecimal cyl) {
        boolean b = Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00"));
        return b || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.00").abs()));
    }

    /**
     *  3岁判断（0-6岁平台使用）
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveError3ByZeroToSixPlatform(BigDecimal se, BigDecimal cyl) {
        boolean b = Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00"));
        return b || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("2.00").abs()));
    }

    /**
     * 4岁判断
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveError4(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, MINUS_3) || BigDecimalUtil.moreThan(se, "4.00")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("2.00").abs()));
    }

    /**
     * 大于等于5岁判断
     * @param se 等效球镜
     * @param cyl 柱镜
     */
    private static Boolean refractiveErrorOver5(BigDecimal se, BigDecimal cyl) {
        return (Objects.nonNull(se) && (BigDecimalUtil.lessThan(se, "-1.50") || BigDecimalUtil.moreThan(se, "3.50")))
                || (Objects.nonNull(cyl) && BigDecimalUtil.moreThan(cyl.abs(), new BigDecimal("1.50").abs()));
    }





    //=============== 欠矫、足矫

    /**
     *  欠矫、足矫
     *
     * @param leftNakedVision  左眼裸视
     * @param rightNakedVision 右眼裸视
     * @param leftCorrectVision 左眼戴镜视力
     * @param rightCorrectVision 右眼戴镜视力
     * @param schoolType 学校类型
     * @param age 年龄
     * @param isWearGlasses 是否戴镜（true-戴镜，false-不戴镜）
     */
    public static Integer correction(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                     BigDecimal leftCorrectVision, BigDecimal rightCorrectVision,
                                     Integer schoolType,Integer age,Boolean isWearGlasses) {

        if (ObjectsUtil.hasNull(schoolType,age,isWearGlasses)){
            return null;
        }
        if (Objects.equals(SchoolEnum.TYPE_KINDERGARTEN.getType(),schoolType)){

            if(age < 5){
                String nakedVision = "4.8";
                return kindergartenCorrection(leftNakedVision,rightNakedVision,leftCorrectVision,rightCorrectVision,isWearGlasses,nakedVision);
            }

            if (age >= 5 && age < 7){
                String nakedVision = "4.9";
                return kindergartenCorrection(leftNakedVision,rightNakedVision,leftCorrectVision,rightCorrectVision,isWearGlasses,nakedVision);
            }

            return null;
        }else {
            String nakedVision = "4.9";
            return primarySchoolAboveCorrection(leftNakedVision,rightNakedVision,leftCorrectVision,rightCorrectVision,isWearGlasses,nakedVision);
        }
    }

    /**
     * 幼儿园
     * @param leftNakedVision 左裸视
     * @param rightNakedVision 右裸视
     * @param leftCorrectVision 左矫正视力
     * @param rightCorrectVision 右矫正视力
     * @param isWearGlasses 是否戴镜
     * @param nakedVision 裸视
     */
    private static Integer kindergartenCorrection(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                  BigDecimal leftCorrectVision, BigDecimal rightCorrectVision,
                                                  Boolean isWearGlasses,String nakedVision){
        if (ObjectsUtil.hasNull(leftNakedVision,rightNakedVision,isWearGlasses)){
            return null;
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,nakedVision)
                ||BigDecimalUtil.lessThanAndEqual(rightNakedVision,nakedVision)){

            return correctionWearGlasses(leftCorrectVision,rightCorrectVision,isWearGlasses,nakedVision);

        }
        return VisionCorrection.NORMAL.code;

    }

    /**
     * 小学及以上
     * @param leftNakedVision 左裸视
     * @param rightNakedVision 右裸视
     * @param leftCorrectVision 左矫正视力
     * @param rightCorrectVision 右矫正视力
     * @param isWearGlasses 是否戴镜
     * @param nakedVision 裸视
     */
    private static Integer primarySchoolAboveCorrection(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                        BigDecimal leftCorrectVision, BigDecimal rightCorrectVision,
                                                        Boolean isWearGlasses,String nakedVision ){
        if (ObjectsUtil.hasNull(leftNakedVision,rightNakedVision,isWearGlasses)){
            return null;
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision,nakedVision)
                ||BigDecimalUtil.lessThanAndEqual(rightNakedVision,nakedVision)){

            return correctionWearGlasses(leftCorrectVision,rightCorrectVision,isWearGlasses,nakedVision);
        }
        return VisionCorrection.NORMAL.code;
    }

    /**
     * 矫正戴镜
     * @param leftCorrectVision 左矫正视力
     * @param rightCorrectVision 右矫正视力
     * @param isWearGlasses 是否戴镜
     * @param nakedVision 裸视
     */
    private Integer correctionWearGlasses(BigDecimal leftCorrectVision, BigDecimal rightCorrectVision,
                                          Boolean isWearGlasses,String nakedVision){
        if(Objects.equals(isWearGlasses,Boolean.TRUE)){
            if (BigDecimalUtil.moreThan(leftCorrectVision,nakedVision) && BigDecimalUtil.moreThan(rightCorrectVision,nakedVision)){
                return VisionCorrection.ENOUGH_CORRECTED.code;
            }
            if (BigDecimalUtil.lessThanAndEqual(leftCorrectVision,nakedVision)
                    || BigDecimalUtil.lessThanAndEqual(rightCorrectVision,nakedVision)
                    || (BigDecimalUtil.lessThanAndEqual(leftCorrectVision,nakedVision) && BigDecimalUtil.lessThanAndEqual(rightCorrectVision,nakedVision))){
                return VisionCorrection.UNDER_CORRECTED.code;
            }
            return null;
        }else {
            return VisionCorrection.UNCORRECTED.code;
        }
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

    /**
     * 屈光参差：双眼球镜度（远视、近视）差值>1.50D
     *
     * @param leftSph  左眼球镜度
     * @param rightSph 右眼球镜度
     */
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

    /**
     * 屈光参差：双眼柱镜度（散光）差值>1.00D
     *
     * @param leftCyl  左眼柱镜度
     * @param rightCyl 右眼柱镜度
     */
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

    /**
     * 是否散光
     *
     * @param cyl 柱镜
     * @return
     */
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
    public static AstigmatismLevelEnum getAstigmatismLevel(Float cyl) {
        if(Objects.isNull(cyl)){
            return null;
        }
        return getAstigmatismLevel(cyl.toString());
    }

    /**
     * 散光等级
     *
     * @param cyl 柱镜
     */
    public static AstigmatismLevelEnum getAstigmatismLevel(String cyl) {
        if (StrUtil.isBlank(cyl)) {
            return null;
        }
        return getAstigmatismLevel(new BigDecimal(cyl));
    }

    /**
     * 散光等级
     *
     * @param cyl 柱镜
     */
    public static AstigmatismLevelEnum getAstigmatismLevel(BigDecimal cyl) {
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

    /**
     * 是否远视
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static Boolean isHyperopia(String sphere, String cylinder, Integer age) {
        if(StrUtil.isBlank(sphere) || StrUtil.isBlank(cylinder)){
            return null;
        }
        return isHyperopia(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    /**
     * 是否远视
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
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
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaLevel(sphere, cylinder, age);
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
     * 远视等级
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static HyperopiaLevelEnum getHyperopiaLevel(Float sphere, Float cylinder, Integer age) {
        if (ObjectsUtil.hasNull(sphere,cylinder,age)) {
            return null;
        }
        return getHyperopiaLevel(sphere.toString(), cylinder.toString(), age);
    }

    /**
     * 远视等级
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static HyperopiaLevelEnum getHyperopiaLevel(String sphere, String cylinder, Integer age) {
        if (StrUtil.isBlank(sphere) || StrUtil.isBlank(cylinder) || age == null) {
            return null;
        }
        return getHyperopiaLevel(new BigDecimal(sphere), new BigDecimal(cylinder), age);
    }

    /**
     * 远视等级
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static HyperopiaLevelEnum getHyperopiaLevel(BigDecimal sphere, BigDecimal cylinder, Integer age) {
        if (ObjectsUtil.hasNull(sphere,cylinder,age)) {
            return null;
        }
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)) {
            return null;
        }
        if (BigDecimalUtil.isBetweenAll(se,MINUS_NEGATIVE_0_5,MINUS_0_5)){
            return HyperopiaLevelEnum.ZERO;
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
     * 近视等级
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     */
    public static MyopiaLevelEnum getMyopiaLevel(Float sphere, Float cylinder) {
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
            return null;
        }
        return getMyopiaLevel(sphere.toString(), cylinder.toString());
    }

    /**
     * 近视等级
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     */
    public static MyopiaLevelEnum getMyopiaLevel(String sphere, String cylinder) {
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
            return null;
        }
        return getMyopiaLevel(new BigDecimal(sphere), new BigDecimal(cylinder));
    }

    /**
     * 近视等级
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     */
    public static MyopiaLevelEnum getMyopiaLevel(BigDecimal sphere, BigDecimal cylinder) {
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)) {
            return null;
        }

        if (BigDecimalUtil.isBetweenAll(se,MINUS_NEGATIVE_0_5,MINUS_0_5)){
            return MyopiaLevelEnum.ZERO;
        }

        if (BigDecimalUtil.isBetweenRight(se, MINUS_NEGATIVE_0_5, "0.75")) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_EARLY;
        }

        if (BigDecimalUtil.isBetweenRight(se, MINUS_6, MINUS_NEGATIVE_0_5)) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT;
        }

        if (BigDecimalUtil.lessThanAndEqual(se, MINUS_6)) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_HIGH;
        }

        return null;
    }

    /**
     * 筛查性近视
     */
    public static MyopiaLevelEnum getScreeningMyopia(BigDecimal sphere, BigDecimal cylinder, Integer age, BigDecimal nakedVision) {

        if (ObjectsUtil.allNotNull(age, nakedVision)
                && ((age < 6 && BigDecimalUtil.lessThan(nakedVision, "4.9")) || (age >= 6 && BigDecimalUtil.lessThan(nakedVision, "5.0")))) {

            BigDecimal se = getSphericalEquivalent(sphere, cylinder);
            if (Objects.isNull(se)) {
                return null;
            }

            if (BigDecimalUtil.lessThanAndEqual(se, MINUS_NEGATIVE_0_5)) {
                return MyopiaLevelEnum.SCREENING_MYOPIA;
            }
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


    /**
     * 计算等效球镜 （球镜度+1/2柱镜度）
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
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

    /**
     * 裸眼视力数据
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     */
    public static WarningLevel nakedVision(BigDecimal nakedVision, Integer age) {
        if (Objects.isNull(nakedVision) || Objects.isNull(age) || age < 3) {
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

    /**
     * 裸眼视力数据-3岁
     *
     * @param nakedVision 裸眼视力
     */
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

    /**
     * 裸眼视力数据-4岁
     *
     * @param nakedVision 裸眼视力
     */
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

    /**
     * 裸眼视力数据-大于等于5岁
     *
     * @param nakedVision 裸眼视力
     */
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
    public static WarningLevel warningLevel(BigDecimal sphere, BigDecimal cyl, Integer age, Integer type) {
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

    /**
     * 近视预警等级
     * @param se 等效球镜
     */
    private static WarningLevel refractiveDataMyopia(BigDecimal se) {
        if (Objects.nonNull(se)) {
            if (BigDecimalUtil.isBetweenAll(se, MINUS_NEGATIVE_0_5, "-0.25")) {
                return WarningLevel.ZERO;
            }
            if (BigDecimalUtil.isBetweenLeft(se, MINUS_3, MINUS_NEGATIVE_0_5)) {
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

    /**
     * 散光预警等级
     * @param cyl 柱镜
     */
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

    /**
     * 远视预警等级
     * @param se 等效球镜
     * @param age 年龄
     */
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

    /**
     * 远视预警等级-3岁
     *
     * @param se 等效球镜
     */
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

    /**
     * 远视预警等级-4或5岁
     *
     * @param se 等效球镜
     */
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

    /**
     * 远视预警等级-6或7岁
     *
     * @param se 等效球镜
     */
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

    /**
     * 远视预警等级-大于等于8岁
     *
     * @param se 等效球镜
     */
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


    //===============5、建议就诊在ScreeningResultUtil.getDoctorAdvice中



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

    /**
     * 计算错误次数
     *
     * 身高误差超过0.5cm
     *
     * 体重误差超过0.1kg
     *
     * 裸眼和戴镜视力误差超过±1行（1行 0.1）
     *
     * 等效球镜度数误差超过±0.50D
     *
     * @param currentVisionScreeningResult 当前筛查结果数据
     * @param anotherVisionScreeningResult 上一次筛查结果数据
     * @param isWearingGlasses 是否戴镜
     */
    public static int calculateErrorNum(VisionScreeningResult currentVisionScreeningResult,VisionScreeningResult anotherVisionScreeningResult,Boolean isWearingGlasses) {
        int errorNum = getNakedVisionErrorNum(currentVisionScreeningResult,anotherVisionScreeningResult) + getSeErrorNum(currentVisionScreeningResult,anotherVisionScreeningResult);
        if (Objects.nonNull(isWearingGlasses) && Objects.equals(isWearingGlasses,Boolean.TRUE)) {
            errorNum += getCorrectedVisionErrorNum(currentVisionScreeningResult,anotherVisionScreeningResult);
        }
        if (Objects.equals(ScreeningTypeEnum.COMMON_DISEASE.getType(),currentVisionScreeningResult.getScreeningType())){
            errorNum += getHeightAndWeight(currentVisionScreeningResult,anotherVisionScreeningResult);
        }
        return errorNum;
    }

    /**
     * 获取身高体重错误数
     * @param currentVisionScreeningResult 当前筛查结果数据
     * @param anotherVisionScreeningResult 上一次筛查结果数据
     */
    public int getHeightAndWeight(VisionScreeningResult currentVisionScreeningResult,VisionScreeningResult anotherVisionScreeningResult) {
        int errorNum = 0;
        HeightAndWeightDataDO currentHeightAndWeightData = currentVisionScreeningResult.getHeightAndWeightData();
        HeightAndWeightDataDO anotherHeightAndWeightData = anotherVisionScreeningResult.getHeightAndWeightData();
        if (ObjectsUtil.allNotNull(currentHeightAndWeightData,anotherHeightAndWeightData)){
            errorNum += inRange(currentHeightAndWeightData.getHeight(),anotherHeightAndWeightData.getHeight(),seAndHeightRangeValue);
            errorNum += inRange(currentHeightAndWeightData.getWeight(),anotherHeightAndWeightData.getWeight(),visionAndWeightRangeValue);
        }

        return errorNum;
    }

    /**
     * 获取视力错误数
     * @param currentVisionScreeningResult 当前筛查结果数据
     * @param anotherVisionScreeningResult 上一次筛查结果数据
     */
    public static int getNakedVisionErrorNum(VisionScreeningResult currentVisionScreeningResult,VisionScreeningResult anotherVisionScreeningResult) {
        int errorNum = 0;
        VisionDataDO currentVisionData = currentVisionScreeningResult.getVisionData();
        VisionDataDO anotherVisionData = anotherVisionScreeningResult.getVisionData();
        if (currentVisionData != null && anotherVisionData != null) {
            errorNum += inRange(currentVisionData.getLeftEyeData().getNakedVision(), anotherVisionData.getLeftEyeData().getNakedVision(), visionAndWeightRangeValue);
            errorNum += inRange(currentVisionData.getRightEyeData().getNakedVision(), anotherVisionData.getRightEyeData().getNakedVision(), visionAndWeightRangeValue);
        }
        return errorNum;
    }

    /**
     * 获取矫正视力错误数
     * @param currentVisionScreeningResult 当前筛查结果数据
     * @param anotherVisionScreeningResult 上一次筛查结果数据
     */
    public static int getCorrectedVisionErrorNum(VisionScreeningResult currentVisionScreeningResult,VisionScreeningResult anotherVisionScreeningResult) {
        int errorNum = 0;
        VisionDataDO currentVisionData = currentVisionScreeningResult.getVisionData();
        VisionDataDO anotherVisionData = anotherVisionScreeningResult.getVisionData();
        if (ObjectsUtil.allNotNull(currentVisionData,anotherVisionData)) {
            errorNum += inRange(currentVisionData.getLeftEyeData().getCorrectedVision(), anotherVisionData.getLeftEyeData().getCorrectedVision(), visionAndWeightRangeValue);
            errorNum += inRange(currentVisionData.getRightEyeData().getCorrectedVision(), anotherVisionData.getRightEyeData().getCorrectedVision(), visionAndWeightRangeValue);
        }
        return errorNum;
    }


    /**
     * 获取等效球镜复测错误
     * @param currentVisionScreeningResult 当前筛查结果数据
     * @param anotherVisionScreeningResult 上一次筛查结果数据
     */
    public static int getSeErrorNum(VisionScreeningResult currentVisionScreeningResult,VisionScreeningResult anotherVisionScreeningResult) {
        int errorNum = 0;
        ComputerOptometryDO currentComputerOptometry = currentVisionScreeningResult.getComputerOptometry();
        ComputerOptometryDO anotherComputerOptometry = anotherVisionScreeningResult.getComputerOptometry();
        if (ObjectsUtil.allNotNull(currentComputerOptometry,anotherComputerOptometry)) {
            BigDecimal currentLeftSe = StatUtil.getSphericalEquivalent(currentComputerOptometry.getLeftEyeData().getSph(), currentComputerOptometry.getLeftEyeData().getCyl());
            BigDecimal currentRightSe = StatUtil.getSphericalEquivalent(currentComputerOptometry.getRightEyeData().getSph(), currentComputerOptometry.getRightEyeData().getCyl());
            BigDecimal anotherLeftSe = StatUtil.getSphericalEquivalent(anotherComputerOptometry.getLeftEyeData().getSph(), anotherComputerOptometry.getLeftEyeData().getCyl());
            BigDecimal anotherRightSe = StatUtil.getSphericalEquivalent(anotherComputerOptometry.getRightEyeData().getSph(), anotherComputerOptometry.getRightEyeData().getCyl());
            errorNum += inRange(currentLeftSe,anotherLeftSe, seAndHeightRangeValue);
            errorNum += inRange(currentRightSe,anotherRightSe, seAndHeightRangeValue);
        }
        return errorNum;
    }

    /**
     * 判断是否在范围内
     *
     * @param beforeValue 前值
     * @param afterValue 后值
     * @param rangeValue 范围值
     * @return
     */
    public static int inRange(BigDecimal beforeValue, BigDecimal afterValue, BigDecimal rangeValue) {
        int errorNum = 0;
        if (beforeValue == null || afterValue == null || rangeValue == null) {
            return errorNum;
        }
        //属于误差范围内
        if (beforeValue.subtract(afterValue).abs().compareTo(rangeValue) > 0) {
            errorNum++;
        }
        return errorNum;
    }

    /**
     * 计算复测项次
     *  左右眼裸眼视力、左右眼等效球镜度数
     *  左右眼戴镜视力 （戴镜）
     *  身高、体重 （常见病）
     *
     * @param currentVisionScreeningResult 当前筛查结果数据
     */
    public static int calculateItemNum(VisionScreeningResult currentVisionScreeningResult) {
        int itemCount = 0;
        VisionDataDO visionData = currentVisionScreeningResult.getVisionData();
        if(Objects.nonNull(visionData)){
            if (visionData.validNakedVision()){
                itemCount += 2;
            }
            if (visionData.validCorrectedVision()){
                itemCount += 2;
            }
        }
        ComputerOptometryDO computerOptometry = currentVisionScreeningResult.getComputerOptometry();
        if (Objects.nonNull(computerOptometry) && computerOptometry.valid()){
            itemCount += 2;
        }

        HeightAndWeightDataDO heightAndWeightData = currentVisionScreeningResult.getHeightAndWeightData();
        if (Objects.nonNull(heightAndWeightData)){
            itemCount += count(heightAndWeightData.getHeight());
            itemCount += count(heightAndWeightData.getWeight());
        }
        return itemCount;
    }

    public static int count(BigDecimal value){
        int itemCount=0;
        if (Objects.nonNull(value)){
            itemCount++;
        }
        return itemCount;
    }


    //=================8、常见病相关指标【占比：保留2位小数点】

    /**
     * 龋齿相关
     * @param saprodontiaData 龋齿数据
     * @param itemList 相关类型
     */
    public static List<SaprodontiaDataDO.SaprodontiaItem> getSaprodontia(SaprodontiaDataDO saprodontiaData, List<String> itemList){
        List<SaprodontiaDataDO.SaprodontiaItem> above = saprodontiaData.getAbove();
        List<SaprodontiaDataDO.SaprodontiaItem> underneath = saprodontiaData.getUnderneath();
        List<SaprodontiaDataDO.SaprodontiaItem> saprodontiaItemList=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(above)){
            saprodontiaItemList.addAll(above);
        }
        if (CollectionUtil.isNotEmpty(underneath)){
            saprodontiaItemList.addAll(underneath);
        }
        return itemList.stream().flatMap(item -> saprodontiaItemList.stream().filter(s -> Objects.equals(item,s.getDeciduous()) || Objects.equals(item,s.getPermanent()))).collect(Collectors.toList());
    }


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

    /**
     * 是否超重/是否肥胖
     *
     * @param weight 体重 kg
     * @param height 身高 m
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
    public static TwoTuple<Boolean, Boolean> isOverweightAndObesity(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        if (ObjectsUtil.hasNull(weight,height,gender) || StrUtil.isBlank(age)){
            return null;
        }
        BigDecimal bmi = bmi(weight, height);
        return isOverweightAndObesity(bmi,age,gender);
    }

    /**
     * 是否超重/是否肥胖
     *
     * @param bmi    指标
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
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

    /**
     * 是否生长迟缓
     *
     * @param gender 性别
     * @param age    年龄 （精确到半岁）
     * @param height 身高 cm
     */
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

    /**
     * 是否消瘦
     *
     * @param weight 体重 kg
     * @param height 身高 m
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
    public static Boolean isWasting(BigDecimal weight, BigDecimal height, String age, Integer gender) {
        if (ObjectsUtil.hasNull(weight,height)){
            return null;
        }
        BigDecimal bmi = bmi(weight, height);
        return isWasting(bmi,age,gender);
    }

    /**
     * 是否消瘦
     *
     * @param bmi 指标
     * @param age    年龄 （精确到半岁）
     * @param gender 性别
     */
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
    public static boolean isHighBloodPressure(Integer sbp, Integer dbp, Integer gender, Integer age,BigDecimal height) {
        if (age >= 7 && age <= 17) {
            StandardTableData.BloodPressureData bloodPressureData = StandardTableData.getBloodPressureData(age, gender,height);
            return sbp >= bloodPressureData.getSbp() || dbp >= bloodPressureData.getDbp();
        }
        if (age >= 18) {
            return sbp >= 140 || dbp >= 90;
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
        return getSeriousLevel(left, right);
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

        if (Objects.equals(schoolType,SchoolAge.KINDERGARTEN.code)){

            WarningLevel warningLevel = StatUtil.myopiaLevelInsufficient(spn, cyl);
            if (Objects.isNull(warningLevel)){
                //裸眼视力
                return StatUtil.nakedVision(nakedVision, age);
            }
            //0级预警（远视储备不足）优先级大于 0级预警
            WarningLevel nakedVisionWarningLevel = StatUtil.nakedVision(nakedVision, age);
            if (Objects.isNull(nakedVisionWarningLevel) || Objects.equals(nakedVisionWarningLevel,WarningLevel.ZERO)){
                return warningLevel;
            }
            return nakedVisionWarningLevel;

        }

        List<WarningLevel> warningLevelList = Lists.newArrayList();
        if (!Objects.equals(schoolType,SchoolAge.KINDERGARTEN.code)){
            //裸眼视力
            warningLevelList.add(StatUtil.nakedVision(nakedVision, age));
            //近视
            warningLevelList.add(StatUtil.warningLevel(spn, cyl, age, 0));
            //散光
            warningLevelList.add(StatUtil.warningLevel(spn, cyl, age, 1));
            //远视
            warningLevelList.add(StatUtil.warningLevel(spn, cyl, age, 2));
        }
        return warningLevelList.stream().filter(Objects::nonNull).max(Comparator.comparing(WarningLevel::getCode)).orElse(null);

    }

    /**
     * 是否复查
     */
    public static Boolean isReview(Boolean isLowVision,Boolean isMyopia,Boolean isHyperopia,
                                   Boolean isAstigmatism,Boolean isObesity,Boolean isOverweight,
                                   Boolean isMalnutrition,Boolean isStunting,Boolean isSpinalCurvature) {
        List<Boolean> isReviewList =Lists.newArrayList();
        Consumer<Boolean> consumerTrue = flag -> isReviewList.add(Objects.equals(Boolean.TRUE, flag));

        Optional.ofNullable(isLowVision).ifPresent(consumerTrue);
        Optional.ofNullable(isMyopia).ifPresent(consumerTrue);
        Optional.ofNullable(isHyperopia).ifPresent(consumerTrue);
        Optional.ofNullable(isAstigmatism).ifPresent(consumerTrue);
        Optional.ofNullable(isObesity).ifPresent(consumerTrue);
        Optional.ofNullable(isOverweight).ifPresent(consumerTrue);
        Optional.ofNullable(isMalnutrition).ifPresent(consumerTrue);
        Optional.ofNullable(isStunting).ifPresent(consumerTrue);
        Optional.ofNullable(isSpinalCurvature).ifPresent(consumerTrue);

        if (CollectionUtil.isNotEmpty(isReviewList)){
            return isReviewList.stream().filter(Objects::nonNull).anyMatch(Boolean::booleanValue);
        }
        return null;
    }


    /**
     * 取严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getSeriousLevel(WarningLevel leftLevel, WarningLevel rightLevel) {
        Integer left = Optional.ofNullable(leftLevel).map(l -> l.code).orElse(null);
        Integer right = Optional.ofNullable(rightLevel).map(l -> l.code).orElse(null);
        return getSeriousLevel(left,right);
    }
    public Integer getSeriousLevel(LowVisionLevelEnum leftLevel, LowVisionLevelEnum rightLevel) {
        Integer left = Optional.ofNullable(leftLevel).map(l -> l.code).orElse(null);
        Integer right = Optional.ofNullable(rightLevel).map(l -> l.code).orElse(null);
        return getSeriousLevel(left,right);
    }
    public Integer getSeriousLevel(AstigmatismLevelEnum leftLevel, AstigmatismLevelEnum rightLevel) {
        Integer left = Optional.ofNullable(leftLevel).map(l -> l.code).orElse(null);
        Integer right = Optional.ofNullable(rightLevel).map(l -> l.code).orElse(null);
        return getSeriousLevel(left,right);
    }
    public Integer getSeriousLevel(HyperopiaLevelEnum leftLevel, HyperopiaLevelEnum rightLevel) {
        Integer left = Optional.ofNullable(leftLevel).map(l -> l.code).orElse(null);
        Integer right = Optional.ofNullable(rightLevel).map(l -> l.code).orElse(null);
        return getSeriousLevel(left,right);
    }
    public Integer getSeriousLevel(MyopiaLevelEnum leftLevel, MyopiaLevelEnum rightLevel) {
        Integer left = Optional.ofNullable(leftLevel).map(l -> l.code).orElse(null);
        Integer right = Optional.ofNullable(rightLevel).map(l -> l.code).orElse(null);
        return getSeriousLevel(left,right);
    }

    /**
     * 取严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     */
    public Integer getSeriousLevel(Integer leftLevel, Integer rightLevel) {
        List<Integer> codeList = Lists.newArrayList();
        if (Objects.nonNull(leftLevel)) {
            codeList.add(leftLevel);
        }
        if (Objects.nonNull(rightLevel)) {
            codeList.add(rightLevel);
        }
        if (CollectionUtils.isNotEmpty(codeList)) {
            return Collections.max(codeList);
        }
        return null;
    }

    /**
     * 是否存在
     *
     * @param left  左眼
     * @param right 右眼
     */
    public Boolean getIsExist(Boolean left,Boolean right){
        if (ObjectsUtil.allNull(left,right)){
            return null;
        }
        //两眼都存在
        if (Objects.nonNull(left) && Objects.nonNull(right)){
            return  left || right;
        }
        //左眼不存
        if (Objects.isNull(left)){
            return right;
        }
        //右眼
        return  left;
    }

    /**
     * 是否正常
     * @param sphere
     * @param cylinder
     */
    public Boolean isNormal(BigDecimal sphere, BigDecimal cylinder) {
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        if (Objects.isNull(se)){
            return null;
        }
        return BigDecimalUtil.isBetweenAll(se,MINUS_NEGATIVE_0_5,MINUS_0_5);
    }

}
