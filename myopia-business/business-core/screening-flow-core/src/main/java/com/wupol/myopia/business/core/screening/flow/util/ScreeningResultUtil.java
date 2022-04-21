package com.wupol.myopia.business.core.screening.flow.util;

import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.domain.RefractoryResultItems;
import com.wupol.myopia.base.domain.VisionItems;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.CorrectedVisionDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.CylDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.NakedVisionDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SphDetails;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/22
 **/
@UtilityClass
public class ScreeningResultUtil {

    /**
     * 获取医生建议二
     *
     * @param result    筛查结果
     * @param gradeType 学龄段
     * @param age       年龄
     * @return 医生建议
     */
    public static String getDoctorAdviceDetail(VisionScreeningResult result, Integer gradeType, Integer age) {

        VisionDataDO visionData = result.getVisionData();

        if (null == visionData) {
            return null;
        }
        // 戴镜类型，取一只眼就行
        Integer glassesType = result.getVisionData().getLeftEyeData().getGlassesType();

        // 获取左右眼的裸眼视力
        BigDecimal leftNakedVision = visionData.getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = visionData.getRightEyeData().getNakedVision();
        if (ObjectsUtil.allNull(leftNakedVision, rightNakedVision)) {
            return "";
        }

        // 是否有其他眼病
        boolean otherEyeDiseasesNormal = Objects.nonNull(result.getOtherEyeDiseases()) && result.getOtherEyeDiseases().isNormal();

        // 获取左右眼的矫正视力
        BigDecimal leftCorrectedVision = visionData.getLeftEyeData().getCorrectedVision();
        BigDecimal rightCorrectedVision = visionData.getRightEyeData().getCorrectedVision();

        ComputerOptometryDO computerOptometry = result.getComputerOptometry();

        return getDoctorAdvice(leftNakedVision, rightNakedVision,
                leftCorrectedVision, rightCorrectedVision,
                glassesType, gradeType, age, otherEyeDiseasesNormal, computerOptometry).getAdvice();
    }

    /**
     * 视力检查结果
     *
     * @param date 数据
     * @param age  年龄
     * @return List<VisionItems>-视力检查结果
     */
    public static List<VisionItems> packageVisionResult(VisionDataDO date, Integer age) {
        List<VisionItems> itemsList = new ArrayList<>();

        // 裸眼视力
        VisionItems nakedVision = new VisionItems();
        nakedVision.setTitle("裸眼视力");

        // 矫正视力
        VisionItems correctedVision = new VisionItems();
        correctedVision.setTitle("矫正视力");

        if (null != date) {
            // 戴镜类型，取一只眼就行
            Integer glassesType = date.getLeftEyeData().getGlassesType();
            BigDecimal leftNakedVisionValue = date.getLeftEyeData().getNakedVision();
            BigDecimal rightNakedVisionValue = date.getRightEyeData().getNakedVision();
            BigDecimal leftCorrectedVisionValue = date.getLeftEyeData().getCorrectedVision();
            BigDecimal rightCorrectedVisionValue = date.getRightEyeData().getCorrectedVision();

            packageVisionDate(age, nakedVision, correctedVision, glassesType, leftNakedVisionValue, rightNakedVisionValue, leftCorrectedVisionValue, rightCorrectedVisionValue);
        }
        itemsList.add(nakedVision);
        itemsList.add(correctedVision);
        return itemsList;
    }

    /**
     * 视力检查结果
     *
     * @param age                       年龄
     * @param nakedVision               裸眼视力检查结果
     * @param correctedVision           矫正视力视力检查结果
     * @param glassesType               带镜类型
     * @param leftNakedVisionValue      左眼裸眼视力
     * @param rightNakedVisionValue     右眼裸眼视力
     * @param leftCorrectedVisionValue  左眼矫正视力
     * @param rightCorrectedVisionValue 右眼矫正视力
     */
    public static void packageVisionDate(Integer age, VisionItems nakedVision, VisionItems correctedVision,
                                         Integer glassesType, BigDecimal leftNakedVisionValue, BigDecimal rightNakedVisionValue,
                                         BigDecimal leftCorrectedVisionValue, BigDecimal rightCorrectedVisionValue) {
        // 左裸眼视力
        VisionItems.Item leftNakedVision = new VisionItems.Item();
        if (Objects.nonNull(leftNakedVisionValue)) {
            nakedVision.setOs(packageNakedVision(leftNakedVision, leftNakedVisionValue, age));
        }

        // 右裸眼视力
        VisionItems.Item rightNakedVision = new VisionItems.Item();
        if (Objects.nonNull(rightNakedVisionValue)) {
            nakedVision.setOd(packageNakedVision(rightNakedVision, rightNakedVisionValue, age));
        }

        // 左矫正视力
        VisionItems.Item leftCorrectedVision = new VisionItems.Item();
        if (Objects.nonNull(leftCorrectedVisionValue)) {
            correctedVision.setOs(packageCorrectedVision(leftCorrectedVision, leftCorrectedVisionValue,
                    leftNakedVisionValue, glassesType));
        }

        // 右矫正视力
        VisionItems.Item rightCorrectedVision = new VisionItems.Item();
        if (Objects.nonNull(rightCorrectedVisionValue)) {
            correctedVision.setOd(packageCorrectedVision(rightCorrectedVision, rightCorrectedVisionValue,
                    rightNakedVisionValue, glassesType));
        }
    }

    /**
     * 封装裸眼视力
     *
     * @param nakedVision      返回的实体
     * @param nakedVisionValue 裸眼视力值
     * @param age              年龄
     * @return VisionItems.Item
     */
    public static VisionItems.Item packageNakedVision(VisionItems.Item nakedVision, BigDecimal nakedVisionValue, Integer age) {
        nakedVision.setVision(nakedVisionValue);
        nakedVision.setDecimalVision(toDecimalVision(nakedVisionValue));
        nakedVision.setType(lowVisionType(nakedVisionValue, age));
        return nakedVision;
    }

    /**
     * 封装矫正视力
     *
     * @param correctedVision      返回的实体
     * @param correctedVisionValue 矫正视力值
     * @param nakedVisionValue     裸眼视力值
     * @param glassesType          戴镜类型
     * @return VisionItems.Item
     */
    public static VisionItems.Item packageCorrectedVision(VisionItems.Item correctedVision, BigDecimal correctedVisionValue,
                                                          BigDecimal nakedVisionValue, Integer glassesType) {
        correctedVision.setVision(correctedVisionValue);
        correctedVision.setDecimalVision(toDecimalVision(correctedVisionValue));
        if (Objects.nonNull(nakedVisionValue)) {
            correctedVision.setType(getCorrected2Type(nakedVisionValue, correctedVisionValue, glassesType));
        }
        return correctedVision;
    }

    /**
     * 验光仪检查结果
     *
     * @param date 数据
     * @param age  年龄
     * @return TwoTuple<List < RefractoryResultItems>, Integer> left-验光仪检查数据 right-预警级别
     */
    public static TwoTuple<List<RefractoryResultItems>, Integer> packageRefractoryResult(ComputerOptometryDO date, Integer age, VisionDataDO visionData) {

        List<RefractoryResultItems> items = new ArrayList<>();
        Integer maxType = 0;

        RefractoryResultItems sphItems = new RefractoryResultItems();
        sphItems.setTitle("球镜SC");

        RefractoryResultItems cylItems = new RefractoryResultItems();
        cylItems.setTitle("柱镜DC");

        RefractoryResultItems axialItems = new RefractoryResultItems();
        axialItems.setTitle("轴位A");

        RefractoryResultItems seItems = new RefractoryResultItems();
        seItems.setTitle("等效球镜SE");

        BigDecimal leftNakedVision = null;
        BigDecimal rightNakedVision = null;
        if (Objects.nonNull(visionData)
                && ObjectsUtil.allNotNull(visionData.getLeftEyeData(), visionData.getRightEyeData())) {
            leftNakedVision = visionData.getLeftEyeData().getNakedVision();
            rightNakedVision = visionData.getRightEyeData().getNakedVision();
        }

        if (Objects.nonNull(date)) {
            // 左眼数据
            ComputerOptometryDO.ComputerOptometry leftEyeData = date.getLeftEyeData();
            BigDecimal leftSph = leftEyeData.getSph();
            BigDecimal leftCyl = leftEyeData.getCyl();

            BigDecimal rightSph = date.getRightEyeData().getSph();
            BigDecimal rightCyl = date.getRightEyeData().getCyl();

            BigDecimal leftAxial = leftEyeData.getAxial();
            BigDecimal rightAxial = date.getRightEyeData().getAxial();

            maxType = packageRefractoryResult(age, items, maxType, sphItems, cylItems, axialItems, seItems,
                    leftSph, leftCyl, rightSph, rightCyl, leftAxial, rightAxial, leftNakedVision, rightNakedVision);
            return new TwoTuple<>(items, maxType);
        }
        items.add(seItems);
        items.add(cylItems);
        items.add(axialItems);
        return new TwoTuple<>(items, maxType);
    }

    /**
     * 验光仪检查结果
     *
     * @param age              年龄
     * @param items            验光仪检查结果对象
     * @param maxType          最严重的级别
     * @param sphItems         球镜对象
     * @param cylItems         柱镜对象
     * @param axialItems       轴位对象
     * @param seItems          等效球镜对象
     * @param leftSph          左眼球镜
     * @param leftCyl          左眼柱镜
     * @param rightSph         右眼球镜
     * @param rightCyl         右眼柱镜
     * @param leftAxial        左眼轴位
     * @param rightAxial       右眼轴位
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @return 最严重的级别
     */
    public static Integer packageRefractoryResult(Integer age, List<RefractoryResultItems> items, Integer maxType,
                                                  RefractoryResultItems sphItems, RefractoryResultItems cylItems, RefractoryResultItems axialItems,
                                                  RefractoryResultItems seItems, BigDecimal leftSph, BigDecimal leftCyl, BigDecimal rightSph, BigDecimal rightCyl,
                                                  BigDecimal leftAxial, BigDecimal rightAxial, BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        // 左眼球镜
        if (Objects.nonNull(leftSph)) {
            sphItems.setOs(packageSpnItem(leftSph));
        }
        // 右眼球镜
        if (Objects.nonNull(rightSph)) {
            sphItems.setOd(packageSpnItem(rightSph));
        }
        items.add(sphItems);

        // 左眼柱镜DC
        if (Objects.nonNull(leftCyl)) {
            TwoTuple<Integer, RefractoryResultItems.Item> result = packageCylItem(leftCyl, maxType);
            maxType = result.getFirst();
            cylItems.setOs(result.getSecond());
        }
        // 右眼柱镜DC
        if (Objects.nonNull(rightCyl)) {
            TwoTuple<Integer, RefractoryResultItems.Item> result = packageCylItem(rightCyl, maxType);
            maxType = result.getFirst();
            cylItems.setOd(result.getSecond());
        }
        items.add(cylItems);

        // 左眼轴位A
        if (Objects.nonNull(leftAxial)) {
            axialItems.setOs(packageAxialItem(leftAxial));
        }
        // 右眼轴位A
        if (Objects.nonNull(rightAxial)) {
            axialItems.setOd(packageAxialItem(rightAxial));
        }
        items.add(axialItems);

        if (ObjectsUtil.allNotNull(leftNakedVision, rightNakedVision)) {
            // 左眼等效球镜SE
            if (Objects.nonNull(leftSph) && Objects.nonNull(leftCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageSeItem(leftSph, leftCyl, age, maxType, leftNakedVision);
                maxType = result.getFirst();
                seItems.setOs(result.getSecond());
            }
            // 右眼等效球镜SE
            if (Objects.nonNull(rightSph) && Objects.nonNull(rightCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageSeItem(rightSph, rightCyl, age, maxType, rightNakedVision);
                maxType = result.getFirst();
                seItems.setOd(result.getSecond());
            }
        }
        items.add(seItems);
        return maxType;
    }

    /**
     * 封装等效球镜SE
     *
     * @param spn     球镜
     * @param cyl     柱镜
     * @param age     年龄
     * @param maxType 最大类型
     * @return TwoTuple<Integer, RefractoryResultItems.Item>
     */
    public static TwoTuple<Integer, RefractoryResultItems.Item> packageSeItem(BigDecimal spn, BigDecimal cyl,
                                                                              Integer age, Integer maxType, BigDecimal nakedVision) {
        RefractoryResultItems.Item sphItems = new RefractoryResultItems.Item();
        // 等效球镜SE
        sphItems.setVision(calculationSE(spn, cyl));
        TwoTuple<String, Integer> leftSphType = getSphTypeName(spn, cyl, age, nakedVision);
        sphItems.setTypeName(leftSphType.getFirst());
        Integer type = leftSphType.getSecond();
        // 取最大的type
        if (Objects.nonNull(type)) {
            maxType = maxType > type ? maxType : type;
            sphItems.setType(type);
        }
        return new TwoTuple<>(maxType, sphItems);
    }

    /**
     * 封装柱镜DC
     *
     * @param cyl     柱镜
     * @param maxType 最大类型
     * @return TwoTuple<Integer, RefractoryResultItems.Item>
     */
    public static TwoTuple<Integer, RefractoryResultItems.Item> packageCylItem(BigDecimal cyl, Integer maxType) {
        RefractoryResultItems.Item cylItems = new RefractoryResultItems.Item();
        cylItems.setVision(cyl);
        TwoTuple<String, Integer> leftCylType = getCylTypeName(cyl);
        Integer type = leftCylType.getSecond();
        cylItems.setType(type);
        // 取最大的type
        maxType = maxType > type ? maxType : type;
        cylItems.setTypeName(leftCylType.getFirst());
        return new TwoTuple<>(maxType, cylItems);
    }

    /**
     * 封装轴位
     *
     * @param axial 轴位
     * @return RefractoryResultItems.Item {@link RefractoryResultItems.Item}
     */
    public static RefractoryResultItems.Item packageAxialItem(BigDecimal axial) {
        RefractoryResultItems.Item axialItems = new RefractoryResultItems.Item();
        axialItems.setVision(axial);
        axialItems.setTypeName(getAxialTypeName(axial));
        return axialItems;
    }

    /**
     * 生物测量
     *
     * @param date       数据
     * @param diseasesDO 其他眼病
     * @return List<BiometricItems> 生物测量
     */
    public static List<BiometricItems> packageBiometricResult(BiometricDataDO date, OtherEyeDiseasesDO diseasesDO) {
        List<BiometricItems> items = new ArrayList<>();
        // 房水深度AD
        BiometricItems ADItems = packageADItem(date);
        items.add(ADItems);

        // 眼轴AL
        BiometricItems ALItems = packageALItem(date);
        items.add(ALItems);

        // 角膜中央厚度CCT
        BiometricItems CCTItems = packageCCTItem(date);
        items.add(CCTItems);

        // 状体厚度LT
        BiometricItems LTItems = packageLTItem(date);
        items.add(LTItems);

        // 角膜白到白距离WTW
        BiometricItems WTWItems = packageWTWItem(date);
        items.add(WTWItems);

        items.add(packageEyeDiseases(diseasesDO));
        return items;
    }

    /**
     * 房水深度AD
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    public static BiometricItems packageADItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("房水深度AD");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getAd());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getAd());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 眼轴AL
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    public static BiometricItems packageALItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("眼轴AL");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getAl());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getAl());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 角膜中央厚度CCT
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    public static BiometricItems packageCCTItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("角膜中央厚度CCT");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getCct());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getCct());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 状体厚度LT
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    public static BiometricItems packageLTItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("状体厚度LT");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getLt());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getLt());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 角膜白到白距离WTW
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    public static BiometricItems packageWTWItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("角膜白到白距离WTW");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getWtw());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getWtw());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 其他眼疾
     *
     * @param diseasesDO 眼疾
     * @return BiometricItems
     */
    public static BiometricItems packageEyeDiseases(OtherEyeDiseasesDO diseasesDO) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("其他眼病");
        if (null != diseasesDO) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setEyeDiseases(diseasesDO.getLeftEyeData().getEyeDiseases());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setEyeDiseases(diseasesDO.getRightEyeData().getEyeDiseases());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 矫正视力详情
     *
     * @param results 筛查结果
     * @return List<CorrectedVisionDetails> 矫正视力详情
     */
    public static List<CorrectedVisionDetails> packageVisionTrendsByCorrected(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CorrectedVisionDetails details = new CorrectedVisionDetails();

            CorrectedVisionDetails.Item left = new CorrectedVisionDetails.Item();
            CorrectedVisionDetails.Item right = new CorrectedVisionDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            VisionDataDO visionData = result.getVisionData();
            if (Objects.nonNull(visionData)) {
                // 左眼
                left.setVision(visionData.getLeftEyeData().getCorrectedVision());
                // 右眼
                right.setVision(visionData.getRightEyeData().getCorrectedVision());
            }
            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 柱镜详情
     *
     * @param results 筛查结果
     * @return List<CylDetails> 柱镜详情
     */
    public static List<CylDetails> packageVisionTrendsByCyl(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CylDetails details = new CylDetails();

            CylDetails.Item left = new CylDetails.Item();
            CylDetails.Item right = new CylDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.nonNull(computerOptometry)) {
                // 左眼
                left.setVision(computerOptometry.getLeftEyeData().getCyl().multiply(new BigDecimal("100")));
                // 右眼
                right.setVision(computerOptometry.getRightEyeData().getCyl().multiply(new BigDecimal("100")));
            }
            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 球镜详情
     *
     * @param results 筛查结果
     * @return List<SphDetails> 球镜详情
     */
    public static List<SphDetails> packageVisionTrendsBySph(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            SphDetails details = new SphDetails();

            SphDetails.Item left = new SphDetails.Item();
            SphDetails.Item right = new SphDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.nonNull(computerOptometry)) {
                // 左眼
                left.setVision(calculationSE(computerOptometry.getLeftEyeData().getSph(),
                        computerOptometry.getLeftEyeData().getCyl()));
                // 右眼
                right.setVision(calculationSE(computerOptometry.getRightEyeData().getSph(),
                        computerOptometry.getRightEyeData().getCyl()));
            }
            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 裸眼视力详情
     *
     * @param results 筛查结果
     * @return List<NakedVisionDetails> 裸眼视力详情
     */
    public static List<NakedVisionDetails> packageVisionTrendsByNakedVision(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            NakedVisionDetails details = new NakedVisionDetails();

            NakedVisionDetails.Item left = new NakedVisionDetails.Item();
            NakedVisionDetails.Item right = new NakedVisionDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            VisionDataDO visionData = result.getVisionData();
            if (Objects.nonNull(visionData)) {
                // 左眼
                left.setVision(visionData.getLeftEyeData().getNakedVision());
                // 右眼
                right.setVision(visionData.getRightEyeData().getNakedVision());
            }
            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 取视力值低的眼球
     *
     * @param left  左眼
     * @param right 右眼
     * @return TwoTuple<BigDecimal, Integer> left-视力 right-左右眼
     */
    public static TwoTuple<BigDecimal, Integer> getResultVision(BigDecimal left, BigDecimal right) {
        if (Objects.isNull(left) || Objects.isNull(right)) {
            // 左眼为空取右眼
            if (Objects.isNull(left)) {
                return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
            }
            // 右眼为空取左眼
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        if (left.compareTo(right) == 0) {
            return new TwoTuple<>(left, CommonConst.SAME_EYE);
        }
        if (left.compareTo(right) < 0) {
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
    }

    /**
     * 计算 等效球镜
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return 等效球镜
     */
    public static BigDecimal calculationSE(BigDecimal sph, BigDecimal cyl) {
        return EyeDataUtil.calculationSE(sph,cyl);
    }

    /**
     * 获取散光轴位
     *
     * @param axial 轴位
     * @return String 中文散光轴位
     */
    public static String getAxialTypeName(BigDecimal axial) {
        return "散光轴位" + axial.abs() + "°";
    }

    /**
     * 获取球镜typeName
     *
     * @param sph         球镜
     * @param cyl         柱镜
     * @param age         年龄
     * @param nakedVision 裸眼视力
     * @return TwoTuple<> left-球镜中文 right-预警级别(重新封装的一层)
     */
    public static TwoTuple<String, Integer> getSphTypeName(BigDecimal sph, BigDecimal cyl, Integer age, BigDecimal nakedVision) {
        BigDecimal se = calculationSE(sph, cyl);
        if (Objects.isNull(se)) {
            return new TwoTuple<>();
        }
        BigDecimal seVal = se.abs().multiply(new BigDecimal("100")).setScale(0, RoundingMode.DOWN);
        if (se.compareTo(new BigDecimal("0.00")) <= 0) {
            // 近视
            MyopiaLevelEnum myopiaWarningLevel = StatUtil.getMyopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age, nakedVision.floatValue());
            String str;
            if (se.compareTo(new BigDecimal("-0.50")) < 0) {
                str = "近视" + seVal + "度";
            } else {
                str = seVal + "度";
            }
            return new TwoTuple<>(str, myopiaLevel2Type(myopiaWarningLevel));
        } else {
            // 远视
            HyperopiaLevelEnum hyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age);
            String str;
            if (StatUtil.isHyperopia(sph.floatValue(), cyl.floatValue(), age)) {
                str = "远视" + seVal + "度";
            } else {
                str = seVal + "度";
            }
            return new TwoTuple<>(str, hyperopiaLevelLevel2Type(hyperopiaWarningLevel, se));
        }
    }

    /**
     * 获取散光TypeName
     *
     * @param cyl 柱镜
     * @return String 散光中文名
     */
    public static TwoTuple<String, Integer> getCylTypeName(BigDecimal cyl) {
        AstigmatismLevelEnum astigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(cyl.floatValue());
        BigDecimal cylVal = cyl.abs().multiply(new BigDecimal("100")).setScale(0, RoundingMode.DOWN);
        if (BigDecimalUtil.isBetweenAll(cyl, new BigDecimal("-0.5"), new BigDecimal("0.5"))) {
            return new TwoTuple<>(cylVal + "度", astigmatismLevelLevel2Type(astigmatismWarningLevel));
        }
        return new TwoTuple<>("散光" + cylVal + "度", astigmatismLevelLevel2Type(astigmatismWarningLevel));
    }

    /**
     * 获取裸眼视力类型
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return Integer {@link ParentReportConst}
     */
    public static Integer lowVisionType(BigDecimal nakedVision, Integer age) {
        boolean lowVision = StatUtil.isLowVision(nakedVision.floatValue(), age);
        if (lowVision) {
            return ParentReportConst.NAKED_LOW;
        }
        return ParentReportConst.NAKED_NORMAL;
    }

    /**
     * 近视级别转换成type
     * <p>预警级别 {@link MyopiaLevelEnum}</p>
     *
     * @param warningLevel 预警级别
     * @return Integer {@link ParentReportConst}
     */
    public static Integer myopiaLevel2Type(MyopiaLevelEnum warningLevel) {
        if (null == warningLevel) {
            return ParentReportConst.LABEL_NORMAL;
        }
        // 预警-1或0则是正常
        if (warningLevel.code.equals(MyopiaLevelEnum.ZERO.code) || warningLevel.code.equals(MyopiaLevelEnum.SCREENING_MYOPIA.code)) {
            return ParentReportConst.LABEL_NORMAL;
        }

        if (warningLevel.code.equals(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code)) {
            return ParentReportConst.LABEL_EARLY;
        }

        if (warningLevel.code.equals(MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)) {
            return ParentReportConst.LABEL_MILD;
        }

        if (warningLevel.code.equals(MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE.code)) {
            return ParentReportConst.LABEL_MODERATE;
        }

        if (warningLevel.code.equals(MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code)) {
            return ParentReportConst.LABEL_SEVERE;
        }
        // 未知返回正常
        return ParentReportConst.LABEL_NORMAL;
    }

    /**
     * 远视级别转换成type
     * <p>预警级别 {@link HyperopiaLevelEnum}</p>
     *
     * @param hyperopiaLevelEnum 预警级别
     * @param se                 等效球镜
     * @return Integer {@link ParentReportConst}
     */
    public static Integer hyperopiaLevelLevel2Type(HyperopiaLevelEnum hyperopiaLevelEnum, BigDecimal se) {

        if (BigDecimalUtil.isBetweenAll(se, "0", "0.75")) {
            return ParentReportConst.LABEL_EARLY;
        }

        if (null == hyperopiaLevelEnum) {
            return null;
        }
        // 预警-1或0则是正常
        if (hyperopiaLevelEnum.code.equals(HyperopiaLevelEnum.ZERO.code)) {
            return ParentReportConst.LABEL_NORMAL;
        }

        if (hyperopiaLevelEnum.code.equals(HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.code)) {
            return ParentReportConst.LABEL_MILD;
        }

        if (hyperopiaLevelEnum.code.equals(HyperopiaLevelEnum.HYPEROPIA_LEVEL_MIDDLE.code)) {
            return ParentReportConst.LABEL_MODERATE;
        }

        if (hyperopiaLevelEnum.code.equals(HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH.code)) {
            return ParentReportConst.LABEL_SEVERE;
        }
        // 未知返回正常
        return ParentReportConst.LABEL_NORMAL;
    }

    /**
     * 散光级别转换成type
     * <p>预警级别 {@link AstigmatismLevelEnum}</p>
     *
     * @param hyperopiaLevelEnum 预警级别
     * @return Integer {@link ParentReportConst}
     */
    public static Integer astigmatismLevelLevel2Type(AstigmatismLevelEnum hyperopiaLevelEnum) {
        if (null == hyperopiaLevelEnum) {
            return ParentReportConst.LABEL_NORMAL;
        }
        // 预警-1或0则是正常
        if (hyperopiaLevelEnum.code.equals(AstigmatismLevelEnum.ZERO.code)) {
            return ParentReportConst.LABEL_NORMAL;
        }

        if (hyperopiaLevelEnum.code.equals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT.code)) {
            return ParentReportConst.LABEL_MILD;
        }

        if (hyperopiaLevelEnum.code.equals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE.code)) {
            return ParentReportConst.LABEL_MODERATE;
        }

        if (hyperopiaLevelEnum.code.equals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH.code)) {
            return ParentReportConst.LABEL_SEVERE;
        }
        // 未知返回正常
        return ParentReportConst.LABEL_NORMAL;
    }

    /**
     * 矫正状态
     *
     * @param nakedVision     裸眼视力
     * @param correctedVision 矫正视力
     * @param glassesType     戴镜类型
     * @return Integer {@link ParentReportConst}
     */
    public static Integer getCorrected2Type(BigDecimal nakedVision, BigDecimal correctedVision, Integer glassesType) {
        // 凡单眼裸眼视力＜4.9时，计入矫正人数
        if (nakedVision.compareTo(new BigDecimal("4.9")) < 0) {
            // 有问题但是没有佩戴眼镜,为未矫
            if (glassesType.equals(GlassesTypeEnum.NOT_WEARING.code)) {
                return ParentReportConst.CORRECTED_NOT;
            } else {
                if (correctedVision.compareTo(new BigDecimal("4.9")) > 0) {
                    // 矫正视力都＞4.9，正常
                    return ParentReportConst.CORRECTED_NORMAL;
                } else {
                    // 矫正视力都<=4.9，欠矫
                    return ParentReportConst.CORRECTED_OWE;
                }
            }
        }
        return ParentReportConst.CORRECTED_NORMAL;
    }

    /**
     * 获取医生建议
     *
     * @param leftNakedVision        左-裸眼
     * @param rightNakedVision       右-裸眼
     * @param leftCorrectedVision    左-矫正视力
     * @param rightCorrectedVision   右-矫正视力
     * @param glassesType            戴镜类型
     * @param schoolAge              学龄段
     * @param age                    年龄
     * @param otherEyeDiseasesNormal 是否有其他眼病
     * @param computerOptometry      电脑验光数据
     * @return 医生建议
     */
    public static RecommendVisitEnum getDoctorAdvice(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                     BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                                     Integer glassesType, Integer schoolAge, Integer age, Boolean otherEyeDiseasesNormal,
                                                     ComputerOptometryDO computerOptometry) {

        // 幼儿园、7岁以下
        if (SchoolAge.KINDERGARTEN.code.equals(schoolAge) && age < 7) {
            return kindergartenAdviceResult(leftNakedVision, rightNakedVision, leftCorrectedVision, rightCorrectedVision,
                    glassesType, age, otherEyeDiseasesNormal, computerOptometry);
        }
        // 中小学
        return middleAdviceResult(leftNakedVision, rightNakedVision, leftCorrectedVision, rightCorrectedVision,
                glassesType, age, schoolAge, computerOptometry);

    }

    /**
     * 中小学医生建议
     *
     * @param leftNakedVision      左-裸眼
     * @param rightNakedVision     右-裸眼
     * @param leftCorrectedVision  左-矫正视力
     * @param rightCorrectedVision 右-矫正视力
     * @param glassesType          戴镜类型
     * @param age                  年龄
     * @param schoolAge            学龄段
     * @param computerOptometry    电脑验光数据
     * @return 医生建议
     */
    public RecommendVisitEnum middleAdviceResult(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                 BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                                 Integer glassesType, Integer age, Integer schoolAge, ComputerOptometryDO computerOptometry) {
        TwoTuple<BigDecimal, Integer> nakedVisionResult = getResultVision(leftNakedVision, rightNakedVision);

        BigDecimal leftSph = Objects.nonNull(computerOptometry) ? computerOptometry.getLeftEyeData().getSph() : null;
        BigDecimal leftCyl = Objects.nonNull(computerOptometry) ? computerOptometry.getLeftEyeData().getCyl() : null;
        BigDecimal rightSph = Objects.nonNull(computerOptometry) ? computerOptometry.getRightEyeData().getSph() : null;
        BigDecimal rightCyl = Objects.nonNull(computerOptometry) ? computerOptometry.getRightEyeData().getCyl() : null;

        BigDecimal leftSe = calculationSE(leftSph, leftCyl);
        BigDecimal rightSe = calculationSE(rightSph, rightCyl);

        if (Objects.isNull(nakedVisionResult.getFirst())) {
            return RecommendVisitEnum.EMPTY;
        }
        // 裸眼视力是否小于4.9
        if (BigDecimalUtil.lessThan(nakedVisionResult.getFirst(), "4.9")) {
            // 是否佩戴眼镜
            if (glassesType >= 1) {
                return getIsWearingGlasses(leftCorrectedVision, rightCorrectedVision,
                        leftNakedVision, rightNakedVision, nakedVisionResult);
            } else {
                // 获取两只眼的结论
                TwoTuple<Integer, RecommendVisitEnum> left = getNotWearingGlasses(leftCyl, leftSe, schoolAge, age, leftNakedVision);
                TwoTuple<Integer, RecommendVisitEnum> right = getNotWearingGlasses(rightCyl, rightSe, schoolAge, age, rightNakedVision);

                // 取结论严重的那只眼
                if (left.getFirst() >= right.getFirst()) {
                    return left.getSecond();
                } else {
                    return right.getSecond();
                }
            }
        } else {
            // 裸眼视力>=4.9
            return nakedVisionNormal(leftNakedVision, rightNakedVision, leftSe, rightSe, nakedVisionResult, age);
        }
    }

    /**
     * 获取屈光参差
     *
     * @param leftSe  左眼等效球镜
     * @param rightSe 右眼等效球镜
     * @return 屈光参差
     */
    private BigDecimal getAnisometropia(BigDecimal leftSe, BigDecimal rightSe) {
        if (ObjectsUtil.hasNull(leftSe, rightSe)) {
            return null;
        }
        // 同侧相减
        if (BigDecimalUtil.isSameSide(leftSe, rightSe, "0")) {
            return leftSe.abs().subtract(rightSe).abs();
        }
        // 不同侧相加
        return leftSe.abs().add(rightSe).abs();
    }


    /**
     * 幼儿园、0-6岁获取医生建议
     *
     * @param leftNakedVision        左-裸眼
     * @param rightNakedVision       右-裸眼
     * @param leftCorrectedVision    左-矫正视力
     * @param rightCorrectedVision   右-矫正视力
     * @param glassesType            戴镜类型
     * @param age                    年龄
     * @param otherEyeDiseasesNormal 是否有其他眼病
     * @param computerOptometry      电脑验光数据
     * @return 医生建议
     */
    public RecommendVisitEnum kindergartenAdviceResult(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                       BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                                       Integer glassesType, Integer age, Boolean otherEyeDiseasesNormal, ComputerOptometryDO computerOptometry) {
        BigDecimal correctedVision;


        if (Objects.isNull(glassesType)) {
            return RecommendVisitEnum.EMPTY;
        }
        // 佩戴眼镜
        if (glassesType >= 1) {
            // 5岁以下
            correctedVision = kindergartenHaveGlassesResult(leftNakedVision, rightNakedVision, leftCorrectedVision, rightCorrectedVision, age, "4.8");
            if (!haveGlassesKindergartenIsMatch(age, correctedVision).equals(RecommendVisitEnum.EMPTY)) {
                return haveGlassesKindergartenIsMatch(age, correctedVision);
            }
            // 5-7岁
            correctedVision = kindergartenHaveGlassesResult(leftNakedVision, rightNakedVision, leftCorrectedVision, rightCorrectedVision, age, "4.9");
            if (!haveGlassesKindergartenIsMatch(age, correctedVision).equals(RecommendVisitEnum.EMPTY)) {
                return haveGlassesKindergartenIsMatch(age, correctedVision);
            }
        } else {
            if (Objects.isNull(computerOptometry) || !computerOptometry.valid()) {
                return RecommendVisitEnum.EMPTY;
            }
            BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
            BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
            BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
            BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();
            if (ObjectsUtil.hasNull(leftSph, leftCyl, rightSph, rightCyl)) {
                return RecommendVisitEnum.EMPTY;
            }
            // 5岁以下
            RecommendVisitEnum recommendVisitEnum = noGlassesKindergartenIsMatch(kindergartenNoGlassesResult(leftNakedVision, rightNakedVision, leftSph, rightSph, leftCyl, rightCyl, age, "4.8"), otherEyeDiseasesNormal);
            if (!recommendVisitEnum.equals(RecommendVisitEnum.EMPTY)) {
                return recommendVisitEnum;
            }

            // 5-7岁
            RecommendVisitEnum recommendVisitEnum1 = noGlassesKindergartenIsMatch(kindergartenNoGlassesResult(leftNakedVision, rightNakedVision, leftSph, rightSph, leftCyl, rightCyl, age, "4.9"), otherEyeDiseasesNormal);
            if (!recommendVisitEnum1.equals(RecommendVisitEnum.EMPTY)) {
                return recommendVisitEnum1;
            }
        }
        return RecommendVisitEnum.EMPTY;
    }

    /**
     * 学龄前儿童获取满足条件的矫正视力
     *
     * @param leftNakedVision      左-裸眼
     * @param rightNakedVision     右-裸眼
     * @param leftCorrectedVision  左-矫正视力
     * @param rightCorrectedVision 右-矫正视力
     * @param age                  年龄
     * @param targetVision         目标视力
     * @return 矫正视力
     */
    private BigDecimal kindergartenHaveGlassesResult(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                     BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                                     Integer age, String targetVision) {
        Boolean differenceTwoLines = isDifferenceTwoLines(leftNakedVision, rightNakedVision);
        if (ObjectsUtil.allNotNull(leftCorrectedVision, rightCorrectedVision) && BigDecimalUtil.isAllLessThanAndEqual(leftNakedVision, rightNakedVision, targetVision)) {
            if (BigDecimalUtil.lessThanAndEqual(leftCorrectedVision, rightCorrectedVision)) {
                if (checkAgeAndNakedVision(age, leftNakedVision, differenceTwoLines)) {
                    return leftCorrectedVision;
                }
            } else {
                if (checkAgeAndNakedVision(age, rightNakedVision, differenceTwoLines)) {
                    return rightCorrectedVision;
                }
            }
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision, rightNakedVision)) {
            if (BigDecimalUtil.lessThanAndEqual(leftNakedVision, targetVision)
                    && checkAgeAndNakedVision(age, leftNakedVision, differenceTwoLines)) {
                return leftCorrectedVision;
            }
        } else {
            if (BigDecimalUtil.lessThanAndEqual(rightNakedVision, targetVision)
                    && checkAgeAndNakedVision(age, rightNakedVision, differenceTwoLines)) {
                return rightCorrectedVision;
            }
        }
        return null;
    }

    /**
     * 戴镜学龄前儿童获取结论
     *
     * @param age             年龄
     * @param correctedVision 矫正视力
     * @return 结论
     */
    private RecommendVisitEnum haveGlassesKindergartenIsMatch(Integer age, BigDecimal correctedVision) {
        if (Objects.isNull(correctedVision)) {
            return RecommendVisitEnum.EMPTY;
        }
        if ((age < 5 && BigDecimalUtil.lessThanAndEqual(correctedVision, "4.8"))
                || (age >= 5 && age < 7 && BigDecimalUtil.lessThanAndEqual(correctedVision, "4.9"))) {
            return RecommendVisitEnum.KINDERGARTEN_RESULT_1;
        }
        if (age < 5 && BigDecimalUtil.moreThan(correctedVision, "4.8")
                || (age >= 5 && age < 7 && BigDecimalUtil.moreThan(correctedVision, "4.9"))) {
            return RecommendVisitEnum.KINDERGARTEN_RESULT_2;
        }
        return RecommendVisitEnum.EMPTY;
    }

    /**
     * 不戴镜学龄前儿童获取结论
     *
     * @param threeTuple             右-球镜
     * @param otherEyeDiseasesNormal 目标视力
     * @return 结论
     */
    private RecommendVisitEnum noGlassesKindergartenIsMatch(ThreeTuple<BigDecimal, BigDecimal, BigDecimal> threeTuple, Boolean otherEyeDiseasesNormal) {

        if (Objects.isNull(threeTuple)) {
            return RecommendVisitEnum.EMPTY;
        }
        BigDecimal seBigDecimal = threeTuple.getFirst();
        BigDecimal cyl = threeTuple.getSecond();
        BigDecimal anisometropia = threeTuple.getThird();

        if (BigDecimalUtil.isBetweenLeft(seBigDecimal, "0", "2")
                && BigDecimalUtil.lessThanAndEqual(cyl.abs(), "1.5")) {
            return RecommendVisitEnum.KINDERGARTEN_RESULT_3;
        }
        if (BigDecimalUtil.lessThan(seBigDecimal, "0")) {
            return RecommendVisitEnum.KINDERGARTEN_RESULT_5;
        }
        if (Objects.isNull(otherEyeDiseasesNormal)) {
            return RecommendVisitEnum.EMPTY;
        }

        if ((BigDecimalUtil.moreThanAndEqual(seBigDecimal, "2") || BigDecimalUtil.moreThan(cyl.abs(), "1.5"))
                || (Objects.nonNull(anisometropia) && BigDecimalUtil.moreThan(anisometropia, "1.5"))
                || !otherEyeDiseasesNormal) {
            return RecommendVisitEnum.KINDERGARTEN_RESULT_4;
        }
        return RecommendVisitEnum.EMPTY;
    }

    private ThreeTuple<BigDecimal, BigDecimal, BigDecimal> kindergartenNoGlassesResult(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                                                       BigDecimal leftSph, BigDecimal rightSph,
                                                                                       BigDecimal leftCyl, BigDecimal rightCyl,
                                                                                       Integer age, String targetVision) {
        BigDecimal leftSe = StatUtil.getSphericalEquivalent(leftSph, leftCyl);
        BigDecimal rightSe = StatUtil.getSphericalEquivalent(rightSph, rightCyl);
        BigDecimal anisometropia = getAnisometropia(leftSe, rightSe);
        Boolean differenceTwoLines = isDifferenceTwoLines(leftNakedVision, rightNakedVision);
        if (Objects.isNull(anisometropia) || ObjectsUtil.hasNull(leftNakedVision, rightNakedVision)) {
            return null;
        }
        // 如果都满足，则取等效球镜低的一个
        if (BigDecimalUtil.isAllLessThanAndEqual(leftNakedVision, rightNakedVision, targetVision)) {
            if (Objects.nonNull(leftSe) && Objects.nonNull(rightSe) && BigDecimalUtil.moreThan(leftSe.abs(), rightSe.abs())) {
                return new ThreeTuple<>(leftSe, leftCyl, anisometropia);
            }
            return new ThreeTuple<>(rightSe, rightCyl, anisometropia);
        }
        if (BigDecimalUtil.lessThanAndEqual(leftNakedVision, rightNakedVision) && BigDecimalUtil.lessThanAndEqual(leftNakedVision, targetVision)) {
            if (checkAgeAndNakedVision(age, leftNakedVision, differenceTwoLines)) {
                return new ThreeTuple<>(leftSe, leftCyl, anisometropia);
            }
        } else {
            if (BigDecimalUtil.lessThanAndEqual(rightNakedVision, targetVision) && checkAgeAndNakedVision(age, rightNakedVision, differenceTwoLines)) {
                return new ThreeTuple<>(rightSe, rightCyl, anisometropia);
            }
        }
        return null;
    }

    /**
     * 检查年龄和裸眼视力是否匹配
     *
     * @param age                年龄
     * @param nakedVision        裸眼视力
     * @param differenceTwoLines 是否标准视力相差2行及以上
     * @return 是否匹配
     */
    private boolean checkAgeAndNakedVision(Integer age, BigDecimal nakedVision, Boolean differenceTwoLines) {
        return (age < 5 && BigDecimalUtil.lessThanAndEqual(nakedVision, "4.8"))
                || (age >= 5 && age < 7 && BigDecimalUtil.lessThanAndEqual(nakedVision, "4.9")
                || differenceTwoLines);
    }

    /**
     * 是否标准视力相差2行及以上
     *
     * @param left  左眼裸眼视力
     * @param right 右眼裸眼视力
     * @return 结果
     */
    private Boolean isDifferenceTwoLines(BigDecimal left, BigDecimal right) {
        if (ObjectsUtil.allNotNull(left, right)) {
            return BigDecimalUtil.moreThanAndEqual(BigDecimalUtil.subtract(left.abs(), right.abs()).abs(), "0.2");
        }
        return false;
    }


    /**
     * 两眼的值是否都在4.9的同侧
     *
     * @param leftNakedVision  左裸眼视力
     * @param rightNakedVision 右裸眼数据
     * @return boolean
     */
    public static boolean isNakedVisionMatch(BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        if (Objects.isNull(leftNakedVision) || Objects.isNull(rightNakedVision)) {
            return true;
        }
        return ((leftNakedVision.compareTo(new BigDecimal("4.9")) < 0) &&
                (rightNakedVision.compareTo(new BigDecimal("4.9")) < 0))
                ||
                ((leftNakedVision.compareTo(new BigDecimal("4.9")) >= 0) &&
                        (rightNakedVision.compareTo(new BigDecimal("4.9")) >= 0));
    }

    /**
     * 戴镜获取结论
     *
     * @param leftCorrectedVision  左眼矫正视力
     * @param rightCorrectedVision 右眼矫正视力
     * @param leftNakedVision      左眼裸眼视力
     * @param rightNakedVision     右眼裸眼视力
     * @param nakedVisionResult    取视力值低的眼球
     * @return 结论
     */
    public static RecommendVisitEnum getIsWearingGlasses(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                                         BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                         TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        if (Objects.isNull(leftCorrectedVision) || Objects.isNull(rightCorrectedVision)) {
            return RecommendVisitEnum.EMPTY;
        }
        BigDecimal visionVal = getResultVision(leftCorrectedVision, rightCorrectedVision,
                leftNakedVision, rightNakedVision, nakedVisionResult);
        if (BigDecimalUtil.lessThan(visionVal, "4.9")) {
            return RecommendVisitEnum.MIDDLE_RESULT_1;
        } else {
            return RecommendVisitEnum.MIDDLE_RESULT_2;
        }
    }

    /**
     * 获取矫正视力
     *
     * @param leftCorrectedVision  左眼矫正视力
     * @param rightCorrectedVision 右眼矫正视力
     * @param leftNakedVision      左眼裸眼视力
     * @param rightNakedVision     右眼裸眼视力
     * @param nakedVisionResult    取视力值低的眼球
     * @return 矫正视力
     */
    public static BigDecimal getResultVision(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                             BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                             TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        // 判断两只眼睛的裸眼视力是否都小于4.9或大于等于4.9
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            // 获取矫正视力低的眼球
            return getResultVision(leftCorrectedVision, rightCorrectedVision).getFirst();
        }
        return nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE) ? leftCorrectedVision : rightCorrectedVision;
    }

    /**
     * 没有戴镜情况下获取结论
     *
     * @param cyl         柱镜
     * @param se          等效球镜
     * @param schoolAge   学龄段
     * @param age
     * @param nakedVision 裸眼视力
     * @return TwoTuple<Integer, String>
     */
    public static TwoTuple<Integer, RecommendVisitEnum> getNotWearingGlasses(BigDecimal cyl, BigDecimal se, Integer schoolAge,
                                                                             Integer age, BigDecimal nakedVision) {
        // 是否大于4.9，大于4.9直接返回
        if (ObjectsUtil.hasNull(nakedVision, schoolAge, se)
                || BigDecimalUtil.moreThanAndEqual(nakedVision, "4.9")) {
            return new TwoTuple<>(0, RecommendVisitEnum.EMPTY);
        }
        boolean checkCyl = BigDecimalUtil.lessThanAndEqual(cyl, "1.5");

        if ((SchoolAge.isPrimaryAndKindergarten(schoolAge) && BigDecimalUtil.isBetweenLeft(se, "0", "2") && checkCyl)
                || (SchoolAge.isMiddleSchool(schoolAge) && BigDecimalUtil.isBetweenLeft(se, "-0.5", "3") && BigDecimalUtil.lessThan(cyl, "1.5"))) {
            return new TwoTuple<>(1, RecommendVisitEnum.MIDDLE_RESULT_3);
        }

        if ((SchoolAge.isPrimaryAndKindergarten(schoolAge) && !BigDecimalUtil.isBetweenLeft(se, "0", "2"))
                || (SchoolAge.isMiddleSchool(schoolAge) && !BigDecimalUtil.isBetweenLeft(se, "-0.5", "3") || BigDecimalUtil.moreThan(cyl, "1.5"))) {
            return new TwoTuple<>(2, RecommendVisitEnum.MIDDLE_RESULT_4);
        }
        return new TwoTuple<>(0, RecommendVisitEnum.EMPTY);
    }

    /**
     * 正常裸眼视力获取结论
     *
     * @param leftNakedVision   左眼裸眼视力
     * @param rightNakedVision  右眼裸眼视力
     * @param leftSe            左眼等效球镜
     * @param rightSe           右眼等效球镜
     * @param nakedVisionResult 取视力值低的眼球
     * @param age               年龄
     * @return 结论
     */
    public static RecommendVisitEnum nakedVisionNormal(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                       BigDecimal leftSe, BigDecimal rightSe,
                                                       TwoTuple<BigDecimal, Integer> nakedVisionResult, Integer age) {
        if (ObjectsUtil.hasNull(leftSe, rightSe)) {
            return RecommendVisitEnum.EMPTY;
        }
        BigDecimal se = getNakedVisionNormalSE(leftNakedVision, rightNakedVision, leftSe, rightSe, nakedVisionResult);
        if (BigDecimalUtil.moreThanAndEqual(se, "0")) {
            if (age >= 6 && BigDecimalUtil.moreThanAndEqual(se, "2")) {
                return RecommendVisitEnum.MIDDLE_RESULT_6;
            }
            return RecommendVisitEnum.MIDDLE_RESULT_5;
        }
        return RecommendVisitEnum.MIDDLE_RESULT_7;
    }

    /**
     * 获取等效球镜
     *
     * @param leftNakedVision   左眼裸眼视力
     * @param rightNakedVision  右眼裸眼视力
     * @param leftSe            左眼等效球镜
     * @param rightSe           右眼等效球镜
     * @param nakedVisionResult 裸眼视力结果
     * @return 等效球镜
     */
    public static BigDecimal getNakedVisionNormalSE(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                                    BigDecimal leftSe, BigDecimal rightSe,
                                                    TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        BigDecimal se;
        // 判断两只眼睛的裸眼视力是否都在4.9的同侧
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            if (Objects.nonNull(leftSe) && Objects.nonNull(rightSe) && BigDecimalUtil.moreThan(leftSe.abs(), rightSe.abs())) {
                se = leftSe;
            } else {
                se = rightSe;
            }
        } else {
            // 裸眼视力不同，取视力低的眼别
            se = nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE) ? leftSe : rightSe;
        }
        return se;
    }

    /**
     * 五分视力转换成一分视力
     *
     * @param vision 视力
     * @return 视力
     */
    private String toDecimalVision(BigDecimal vision) {
        if (Objects.isNull(vision)) {
            return null;
        }
        String strVision = vision.toString();
        switch (strVision) {
            case "4":
                return "0.1";
            case "4.1":
                return "0.12";
            case "4.2":
                return "0.15";
            case "4.3":
                return "0.2";
            case "4.4":
                return "0.25";
            case "4.5":
                return "0.3";
            case "4.6":
                return "0.4";
            case "4.7":
                return "0.5";
            case "4.8":
                return "0.6";
            case "4.9":
                return "0.8";
            case "5":
                return "1.0";
            case "5.1":
                return "1.2";
            case "5.2":
                return "1.5";
            case "5.3":
                return "2.0";
            default:
                return null;
        }
    }

    /**
     * 是否在正常视力范围
     *
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @param leftSph          左-柱镜
     * @param rightSph         右-柱镜
     * @param leftCyl          左-球镜
     * @param rightCyl         右-球镜
     * @param age              年龄
     * @return 是否正常
     */
    private boolean checkIsNormal(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                  BigDecimal leftSph, BigDecimal rightSph,
                                  BigDecimal leftCyl, BigDecimal rightCyl,
                                  Integer age) {

        if (Objects.isNull(leftSph) && Objects.isNull(rightSph) &&
                Objects.isNull(leftCyl) && Objects.isNull(rightCyl)) {
            return checkNakedVisionIsNormal(getSeriousVision(leftNakedVision, rightNakedVision), age);
        } else {
            return checkNakedVisionIsNormal(getSeriousVision(leftNakedVision, rightNakedVision), age) &&
                    (checkSEIsNormal(leftSph, rightSph, leftCyl, rightCyl) ||
                            checkSEIsNormalWithAge(leftSph, rightSph, leftCyl, rightCyl, age));
        }
    }

    /**
     * 取严重的眼别
     *
     * @param leftVision  左眼视力
     * @param rightVision 右眼视力
     * @return 视力
     */
    private BigDecimal getSeriousVision(BigDecimal leftVision, BigDecimal rightVision) {
        return Objects.isNull(leftVision) ?
                rightVision : Objects.isNull(rightVision) ?
                leftVision : leftVision.compareTo(rightVision) >= 0 ?
                rightVision : leftVision;
    }

    /**
     * 检查裸眼视力是否正常水平
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return 是否正常水平
     */
    private boolean checkNakedVisionIsNormal(BigDecimal nakedVision, Integer age) {

        if (age < 3 && nakedVision.compareTo(new BigDecimal("4.6")) >= 0) {
            return true;
        }
        if (age < 4 && nakedVision.compareTo(new BigDecimal("4.7")) >= 0) {
            return true;
        }
        if (age < 6 && nakedVision.compareTo(new BigDecimal("4.9")) >= 0) {
            return true;
        }
        return age > 6 && nakedVision.compareTo(new BigDecimal("5.0")) >= 0;
    }

    /**
     * 等效球镜是否属于正常范围
     *
     * @param leftSph  左-柱镜
     * @param rightSph 右-柱镜
     * @param leftCyl  左-球镜
     * @param rightCyl 右-球镜
     * @return 是否属于正常范围
     */
    private boolean checkSEIsNormal(BigDecimal leftSph, BigDecimal rightSph,
                                    BigDecimal leftCyl, BigDecimal rightCyl) {

        TwoTuple<BigDecimal, BigDecimal> normalSE = getNormalSE(leftSph, rightSph, leftCyl, rightCyl);
        BigDecimal leftSE = normalSE.getFirst();
        BigDecimal rightSE = normalSE.getSecond();
        if (Objects.isNull(leftSE) && Objects.isNull(rightSE)) {
            return false;
        }
        return BigDecimalUtil.isBetweenLeft(getSeriousVision(leftSE, rightSE), "-0.5", "0.0");
    }

    /**
     * 年龄段内等效球镜是否属于正常范围
     *
     * @param leftSph  左-柱镜
     * @param rightSph 右-柱镜
     * @param leftCyl  左-球镜
     * @param rightCyl 右-球镜
     * @return 是否属于正常范围
     */
    private boolean checkSEIsNormalWithAge(BigDecimal leftSph, BigDecimal rightSph,
                                           BigDecimal leftCyl, BigDecimal rightCyl, Integer age) {
        TwoTuple<BigDecimal, BigDecimal> normalSE = getNormalSE(leftSph, rightSph, leftCyl, rightCyl);
        BigDecimal leftSE = normalSE.getFirst();
        BigDecimal rightSE = normalSE.getSecond();
        if (Objects.isNull(leftSE) && Objects.isNull(rightSE)) {
            return false;
        }
        if (age < 3 && isMatchSEWithVision(leftSE, rightSE, "3.0")) {
            return true;
        }

        if (age >= 4 && age <= 5 && isMatchSEWithVision(leftSE, rightSE, "2.0")) {
            return true;
        }

        if (age >= 6 && age <= 7 && isMatchSEWithVision(leftSE, rightSE, "1.5")) {
            return true;
        }

        if (age == 8 && isMatchSEWithVision(leftSE, rightSE, "1.0")) {
            return true;
        }

        if (age == 9 && isMatchSEWithVision(leftSE, rightSE, "0.75")) {
            return true;
        }

        if (age >= 10 && age <= 12 && isMatchSEWithVision(leftSE, rightSE, "0.5")) {
            return true;
        }
        return age > 12 && new BigDecimal("0.5").compareTo(getSeriousVision(leftSE, rightSE)) >= 0;
    }

    /**
     * 是否满足指定视力区间
     *
     * @param leftSE            左眼等效球镜
     * @param rightSE           右眼等效球镜
     * @param rightTargetVision 右区间
     * @return 是否满足指定视力区间
     */
    private boolean isMatchSEWithVision(BigDecimal leftSE, BigDecimal rightSE,
                                        String rightTargetVision) {
        return BigDecimalUtil.isBetweenAll(getSeriousVision(leftSE, rightSE), new BigDecimal("0.0"), new BigDecimal(rightTargetVision));
    }

    /**
     * 获取正常的等效球镜
     *
     * @param leftSph  左-柱镜
     * @param rightSph 右-柱镜
     * @param leftCyl  左-球镜
     * @param rightCyl 右-球镜
     * @return TwoTuple<BigDecimal, BigDecimal>
     */
    private TwoTuple<BigDecimal, BigDecimal> getNormalSE(BigDecimal leftSph, BigDecimal rightSph,
                                                         BigDecimal leftCyl, BigDecimal rightCyl) {
        BigDecimal leftSE;
        BigDecimal rightSE;
        if (Objects.isNull(leftSph) || Objects.isNull(leftCyl)) {
            leftSE = null;
        } else {
            leftSE = calculationSE(leftSph, leftCyl);
        }
        if (Objects.isNull(rightSph) || Objects.isNull(rightCyl)) {
            rightSE = null;
        } else {
            rightSE = calculationSE(rightSph, rightCyl);
        }
        return new TwoTuple<>(leftSE, rightSE);
    }

    /**
     * 设置球镜
     *
     * @param spn 球镜
     * @return RefractoryResultItems.Item
     */
    private RefractoryResultItems.Item packageSpnItem(BigDecimal spn) {
        RefractoryResultItems.Item spnItems = new RefractoryResultItems.Item();
        spnItems.setVision(spn);
        spnItems.setTypeName(spn.abs().multiply(new BigDecimal("100")).intValue() + "度");
        return spnItems;
    }
}
