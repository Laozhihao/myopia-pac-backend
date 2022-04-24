package com.wupol.myopia.business.core.screening.flow.domain.builder;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查数据结论
 */

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StatConclusionBuilder {
    private VisionScreeningResult currentVisionScreeningResult;
    private VisionScreeningResult anotherVisionScreeningResult;
    private ScreeningPlanSchoolStudent screeningPlanSchoolStudent;
    private StatConclusion statConclusion;
    private BasicData basicData;
    private boolean isUpdate;
    private final BigDecimal othersRangeValue = new BigDecimal("1.0");
    private final BigDecimal aveRangeValue = new BigDecimal("0.5");
    private String gradeCode;

    private StatConclusionBuilder() {

    }

    /**
     * 获取builder
     *
     * @return
     */
    public static StatConclusionBuilder getStatConclusionBuilder() {
        return new StatConclusionBuilder();
    }

    /**
     * 设置结果
     *
     * @param currentVisionScreeningResult
     * @param rescreeningVisionScreeningResult
     * @return
     */
    public StatConclusionBuilder setCurrentVisionScreeningResult(VisionScreeningResult currentVisionScreeningResult, VisionScreeningResult rescreeningVisionScreeningResult) {
        this.currentVisionScreeningResult = currentVisionScreeningResult;
        this.anotherVisionScreeningResult = rescreeningVisionScreeningResult;
        return this;
    }

    /**
     * 构建
     *
     * @return
     */
    public StatConclusion build() {
        if (!ObjectsUtil.allNotNull(currentVisionScreeningResult, screeningPlanSchoolStudent, statConclusion)) {
            throw new ManagementUncheckedException("StatConclusion构建失败，缺少关键参数");
        }
        // 基本数据的准备
        basicData = BasicData.getInstance(currentVisionScreeningResult, screeningPlanSchoolStudent);
        // 如果新增的话，设置基本的数据
        if (!isUpdate) {
            this.setBasicData();
        } else {
            statConclusion.setUpdateTime(new Date());
        }

        this.setValid();
        // 设置视力相关的数据
        this.setVisionRelatedData();
        //身高体重
        this.setHeightAndWeightData();
        //龋齿
        this.setSaprodontiaData();
        //脊柱
        this.setSpineData();
        //血压
        this.setBloodPressureData();
        //疾病史
        this.setDiseasesHistoryData();
        //隐私项
        this.setPrivacyData();
        this.setRefractiveError();
        this.setRecommendVisit();
        this.setMyopia();
        this.setLowVision();
        this.setWarningLevel();
        this.setRescreenErrorNum();
        this.setWarningVision();
        this.setMyopiaLevel();
        this.setHyperopiaLevel();
        this.setAstigmatismLevel();
        return statConclusion;
    }

    /**
     * 设置是否视力出现警告
     * 规则:
     * 视力大于5岁,视力小于5.0的时候
     */
    private void setWarningVision() {
        String keyParam = "4.9";
        boolean isLeftEyeVisionWarning = statConclusion.getVisionL() != null && BigDecimalUtil.lessThanAndEqual(statConclusion.getVisionL(),keyParam);
        boolean isRightEyeVisionWarning = statConclusion.getVisionR() != null && BigDecimalUtil.lessThanAndEqual(statConclusion.getVisionR(),keyParam);
        boolean isVisionWarning = (isLeftEyeVisionWarning || isRightEyeVisionWarning) && statConclusion.getAge() >= 6;
        statConclusion.setIsVisionWarning(isVisionWarning);
        statConclusion.setVisionWarningUpdateTime(new Date());
    }

    /**
     * 设置基础数据
     */
    private void setBasicData() {
        statConclusion.setScreeningPlanSchoolStudentId(screeningPlanSchoolStudent.getId());
        statConclusion.setStudentId(screeningPlanSchoolStudent.getStudentId());
        statConclusion.setResultId(currentVisionScreeningResult.getId());
        statConclusion.setScreeningOrgId(screeningPlanSchoolStudent.getScreeningOrgId());
        statConclusion.setSrcScreeningNoticeId(screeningPlanSchoolStudent.getSrcScreeningNoticeId());
        statConclusion.setTaskId(screeningPlanSchoolStudent.getScreeningTaskId());
        statConclusion.setPlanId(screeningPlanSchoolStudent.getScreeningPlanId());
        statConclusion.setCreateTime(new Date());
        statConclusion.setDistrictId(screeningPlanSchoolStudent.getSchoolDistrictId());
        statConclusion.setSchoolAge(screeningPlanSchoolStudent.getGradeType());
        statConclusion.setGender(screeningPlanSchoolStudent.getGender());
        statConclusion.setAge(screeningPlanSchoolStudent.getStudentAge());
        statConclusion.setIsRescreen(currentVisionScreeningResult.getIsDoubleScreen());
        statConclusion.setRescreenErrorNum(0);
        statConclusion.setSchoolId(screeningPlanSchoolStudent.getSchoolId());
        statConclusion.setSchoolClassName(screeningPlanSchoolStudent.getClassName());
        statConclusion.setSchoolGradeCode(gradeCode);
        statConclusion.setScreeningType(currentVisionScreeningResult.getScreeningType());
    }

    /**
     * 设置视力相关的数据
     */
    private void setVisionRelatedData() {
        VisionDataDO visionData = currentVisionScreeningResult.getVisionData();
        if(Objects.isNull(visionData)){return;}
        this.setHyperopia();
        this.setAstigmatism();
        this.setVisionOtherData();
        this.setNakedVisionWarningLevel();
        this.setMyopiaWarningLevel();
        this.setVisionCorrection();
        this.setAnisometropia();
    }


    /**
     * 屈光参差
     */
    private void setAnisometropia() {
        Boolean anisometropiaVision = StatUtil.isAnisometropiaVision(basicData.getLeftSph(), basicData.getRightSph());
        Boolean anisometropiaAstigmatism = StatUtil.isAnisometropiaAstigmatism(basicData.getLeftCyl(), basicData.getRightCyl());
        if (ObjectsUtil.hasNull(anisometropiaAstigmatism,anisometropiaAstigmatism)){
            return;
        }
        statConclusion.setIsAnisometropia( anisometropiaVision || anisometropiaAstigmatism);
    }

    /**
     * 设置视力矫正的情况
     */
    private void setVisionCorrection() {
        String keyParam = "4.9";
        if (ObjectsUtil.allNotNull(basicData.getRightNakedVision(), basicData.getLeftNakedVision())
                && BigDecimalUtil.moreThanAndEqual(basicData.getRightNakedVision(),keyParam)
                && BigDecimalUtil.moreThanAndEqual(basicData.getLeftNakedVision(),keyParam)) {
            statConclusion.setVisionCorrection(VisionCorrection.NORMAL.code);
        } else if (Objects.nonNull(basicData.getIsWearingGlasses()) && !basicData.getIsWearingGlasses()) {
            statConclusion.setVisionCorrection(VisionCorrection.UNCORRECTED.code);
        } else if (ObjectsUtil.allNotNull(basicData.getLeftCorrectVision(), basicData.getRightCorrectVision())
                && BigDecimalUtil.moreThan(basicData.getLeftCorrectVision(),keyParam)) {
            statConclusion.setVisionCorrection(VisionCorrection.ENOUGH_CORRECTED.code);
        } else {
            statConclusion.setVisionCorrection(VisionCorrection.UNDER_CORRECTED.code);
        }
    }

    /**
     * 设置近视预警级别
     */
    private void setMyopiaWarningLevel() {
        statConclusion.setMyopiaWarningLevel(basicData.getMyopiaWarningLevel());
    }

    /**
     * 设置裸眼视力预警级别
     */
    private void setNakedVisionWarningLevel() {
        statConclusion.setNakedVisionWarningLevel(basicData.getNakedVisionWarningLevel());
    }


    /**
     * 设置screeningPlanSchoolStudent的数据
     *
     * @param screeningPlanSchoolStudent
     * @return
     */
    public StatConclusionBuilder setScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        this.screeningPlanSchoolStudent = screeningPlanSchoolStudent;
        return this;
    }

    /**
     * 设置视力的其他数据
     */
    private void setVisionOtherData() {
        statConclusion.setIsWearingGlasses(basicData.getIsWearingGlasses());
        statConclusion.setGlassesType(basicData.getGlassesType());
        statConclusion.setVisionR(Optional.ofNullable(basicData.getRightNakedVision()).orElse(null));
        statConclusion.setVisionL(Optional.ofNullable(basicData.getLeftNakedVision()).orElse(null));
    }

    /**
     * 设置预警级别
     */
    private void setWarningLevel() {
        // 特殊处理 角膜塑型镜特殊处理
        if (basicData.getGlassesType() != null && basicData.getGlassesType().equals(GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setWarningLevel(WarningLevel.NORMAL.code);
            return;
        }
        Integer warningLevelInt = StatUtil.getWarningLevelInt(
                basicData.getLeftCyl(),basicData.getLeftSph(),basicData.getLeftNakedVision(),
                basicData.getRightCyl(),basicData.getRightSph(),basicData.getRightNakedVision(),
                basicData.getAge());
        statConclusion.setWarningLevel(warningLevelInt);
    }

    /**
     * 近视等级
     */
    private void setMyopiaLevel() {
        Integer left = StatUtil.getMyopiaLevel(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge(), basicData.getLeftNakedVision());
        Integer right = StatUtil.getMyopiaLevel(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge(), basicData.getRightNakedVision());
        statConclusion.setMyopiaLevel(StatUtil.getSeriousLevel(left, right));
    }

    /**
     * 远视等级
     */
    private void setHyperopiaLevel() {
        Integer left = StatUtil.getHyperopiaLevel(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge());
        Integer right = StatUtil.getHyperopiaLevel(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge());
        statConclusion.setHyperopiaLevel(StatUtil.getSeriousLevel(left, right));
    }

    /**
     * 散光等级
     */
    private void setAstigmatismLevel() {
        Integer left = StatUtil.getAstigmatismLevel(basicData.getLeftCyl());
        Integer right = StatUtil.getAstigmatismLevel(basicData.getRightCyl());
        statConclusion.setAstigmatismLevel(StatUtil.getSeriousLevel(left, right));
    }

    /**
     * 设置视力低下的数据
     */
    private void setLowVision() {
        Boolean isLeftResult = null;
        Boolean isrightResult = null;
        if (basicData.getLeftNakedVision() != null) {
            isLeftResult = StatUtil.isLowVision(basicData.getLeftNakedVision(), basicData.getAge());
        }
        if (basicData.getRightNakedVision() != null) {
            isrightResult = StatUtil.isLowVision(basicData.getRightNakedVision(), basicData.getAge());
        }
        if(ObjectsUtil.hasNull(isLeftResult,isrightResult)){
            return;
        }
        statConclusion.setIsLowVision(isLeftResult || isrightResult);
    }

    private void setRefractiveError() {
        Boolean leftRefractiveError = StatUtil.isRefractiveError(basicData.getLeftSph(),basicData.getLeftCyl(),basicData.getAge());
        Boolean rightRefractiveError = StatUtil.isRefractiveError(basicData.getRightSph(),basicData.getRightCyl(),basicData.getAge());
        if (ObjectsUtil.hasNull(leftRefractiveError,rightRefractiveError)){
            return;
        }
        statConclusion.setIsRefractiveError( leftRefractiveError || rightRefractiveError);
    }

    private void setMyopia() {
        Boolean isLeftMyopia = null;
        Boolean isrightMyopia = null;
        if (basicData.getLeftMyopiaWarningLevel() != null) {
            isLeftMyopia = StatUtil.isMyopia(basicData.getLeftMyopiaWarningLevel());
        }
        if (basicData.getRightMyopiaWarningLevel() != null) {
            isrightMyopia = StatUtil.isMyopia(basicData.getRightMyopiaWarningLevel());
        }
        if (ObjectsUtil.hasNull(isLeftMyopia,isrightMyopia)){
            return;
        }
        statConclusion.setIsMyopia(isLeftMyopia || isrightMyopia);
    }

    private void setHyperopia() {
        Boolean isHyperopia = null;
        if (ObjectsUtil.allNotNull(basicData.getLeftHyperopiaWarningLevel(), basicData.getRightHyperopiaWarningLevel())) {
            isHyperopia = StatUtil.isHyperopia(basicData.getLeftHyperopiaWarningLevel()) || StatUtil.isHyperopia(basicData.getRightHyperopiaWarningLevel());
        }
        statConclusion.setIsHyperopia(isHyperopia);
    }

    private void setAstigmatism() {
        statConclusion.setIsAstigmatism(basicData.getIsAstigmatism());
    }


    private void setRecommendVisit() {
        OtherEyeDiseasesDO otherEyeDiseases = currentVisionScreeningResult.getOtherEyeDiseases();
        Boolean otherEyeDiseasesNormal = Objects.nonNull(otherEyeDiseases)? otherEyeDiseases.isNormal():null;

        Boolean isRecommendVisit = ScreeningResultUtil.getDoctorAdvice(
                basicData.getLeftNakedVision(),basicData.getRightNakedVision(),
                basicData.getLeftCorrectVision(),basicData.getRightCorrectVision(),
                basicData.getGlassesType(), basicData.getSchoolAge(), basicData.getAge(), otherEyeDiseasesNormal,
                currentVisionScreeningResult.getComputerOptometry()).getIsRecommendVisit();
        statConclusion.setIsRecommendVisit(isRecommendVisit);
        statConclusion.setIsReview(isRecommendVisit);
    }

    private void setRescreenErrorNum() {
        if (anotherVisionScreeningResult != null) {
            statConclusion.setRescreenErrorNum(calculateErrorNum());
        } else {
            statConclusion.setRescreenErrorNum(0);
        }
    }

    /**
     * 计算错误次数
     *
     * @return
     */
    private int calculateErrorNum() {
        int errorNum = getNakedVisionErrorNum() + getCorrectedVisionErrorNum();
        if (Objects.nonNull(basicData.getIsWearingGlasses()) && basicData.getIsWearingGlasses()) {
            errorNum += getSeErrorNum();
        }
        return errorNum;
    }

    /**
     * 获取视力错误数
     *
     * @return
     */
    public int getNakedVisionErrorNum() {
        int errorNum = 0;
        VisionDataDO currentVisionData = currentVisionScreeningResult.getVisionData();
        VisionDataDO anotherVisionData = anotherVisionScreeningResult.getVisionData();
        if (currentVisionData != null && anotherVisionData != null) {
            errorNum += inRange(currentVisionData.getLeftEyeData().getNakedVision(), anotherVisionData.getLeftEyeData().getNakedVision(), othersRangeValue);
            errorNum += inRange(currentVisionData.getRightEyeData().getNakedVision(), anotherVisionData.getRightEyeData().getNakedVision(), othersRangeValue);
        }
        return errorNum;
    }

    /**
     * 获取矫正视力错误数
     *
     * @return
     */
    public int getCorrectedVisionErrorNum() {
        int errorNum = 0;
        VisionDataDO currentVisionData = currentVisionScreeningResult.getVisionData();
        VisionDataDO anotherVisionData = anotherVisionScreeningResult.getVisionData();
        if (currentVisionData != null && anotherVisionData != null) {
            errorNum += inRange(currentVisionData.getLeftEyeData().getCorrectedVision(), anotherVisionData.getLeftEyeData().getCorrectedVision(), othersRangeValue);
            errorNum += inRange(currentVisionData.getRightEyeData().getCorrectedVision(), anotherVisionData.getRightEyeData().getCorrectedVision(), othersRangeValue);
        }
        return errorNum;
    }


    /**
     * 获取等效球镜复测错误
     *
     * @return
     */
    public int getSeErrorNum() {
        int errorNum = 0;
        ComputerOptometryDO currentComputerOptometry = currentVisionScreeningResult.getComputerOptometry();
        ComputerOptometryDO anotherComputerOptometry = anotherVisionScreeningResult.getComputerOptometry();
        if (ObjectsUtil.allNotNull(currentComputerOptometry,anotherComputerOptometry)) {
            errorNum += inRange(currentComputerOptometry.getLeftEyeData().getSph().add(currentComputerOptometry.getLeftEyeData().getCyl().divide(new BigDecimal(2))), anotherComputerOptometry.getLeftEyeData().getSph().add(anotherComputerOptometry.getLeftEyeData().getCyl().divide(new BigDecimal(2))), othersRangeValue);
            errorNum += inRange(currentComputerOptometry.getRightEyeData().getSph().add(currentComputerOptometry.getRightEyeData().getCyl().divide(new BigDecimal(2))), anotherComputerOptometry.getRightEyeData().getSph().add(anotherComputerOptometry.getRightEyeData().getCyl().divide(new BigDecimal(2))), othersRangeValue);
        }
        return errorNum;
    }

    /**
     * 判断是否在范围内
     *
     * @param beforeValue
     * @param afterValue
     * @param rangeValue
     * @return
     */
    private int inRange(BigDecimal beforeValue, BigDecimal afterValue, BigDecimal rangeValue) {
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

    public StatConclusionBuilder setStatConclusion(StatConclusion statConclusion) {
        if (statConclusion == null || statConclusion.getId() == null) {
            isUpdate = false;
            this.statConclusion = new StatConclusion();
        } else {
            this.statConclusion = statConclusion;
            isUpdate = true;
        }
        return this;
    }

    private void setValid() {
        if (currentVisionScreeningResult.getVisionData() == null || currentVisionScreeningResult.getComputerOptometry() == null) {
            statConclusion.setIsValid(false);
        }
        statConclusion.setIsValid(StatUtil.isCompletedData(currentVisionScreeningResult.getVisionData(), currentVisionScreeningResult.getComputerOptometry()));
    }

    public StatConclusionBuilder setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
        return this;
    }

    /**
     * 处理身高体重相关的数据
     *
     */
    private void setHeightAndWeightData() {
        HeightAndWeightDataDO heightAndWeightData = currentVisionScreeningResult.getHeightAndWeightData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) || Objects.isNull(heightAndWeightData) ){
            return;
        }
        TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
        overweightAndObesity(heightAndWeightData.getBmi(),ageTuple.getSecond());
        malnutrition(heightAndWeightData.getBmi(),heightAndWeightData.getHeight(),ageTuple.getSecond());
    }



    /**
     * 超重/肥胖
     * @param bmi 身体质量指数值
     * @param age 年龄（精确到半岁）
     */
    private void overweightAndObesity(BigDecimal bmi,String age){
        TwoTuple<Boolean, Boolean> overweightAndObesity = StatUtil.isOverweightAndObesity(bmi, age, screeningPlanSchoolStudent.getGender());
        if (Objects.nonNull(overweightAndObesity)){
            statConclusion.setIsOverweight(overweightAndObesity.getFirst());
            statConclusion.setIsObesity(overweightAndObesity.getSecond());
        }

    }


    /**
     * 营养不良
     * @param bmi 身体质量指数值
     * @param height 身高
     * @param age 年龄（精确到半岁）
     */
    private void malnutrition(BigDecimal bmi,BigDecimal height,String age){
        Boolean wasting = StatUtil.isWasting(bmi, age, screeningPlanSchoolStudent.getGender());
        Boolean stunting = StatUtil.isStunting(screeningPlanSchoolStudent.getGender(), age, height);
        if (Objects.nonNull(wasting)){
            statConclusion.setIsStunting(stunting);
            if (Objects.nonNull(stunting) ){
                statConclusion.setIsMalnutrition(wasting && stunting);
            }
        }



    }

    /**
     * 处理龋齿相关的数据
     */
    private void setSaprodontiaData() {
        SaprodontiaDataDO saprodontiaData = currentVisionScreeningResult.getSaprodontiaData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) ||Objects.isNull(saprodontiaData)){
            return;
        }
        List<SaprodontiaDataDO.SaprodontiaItem> above = saprodontiaData.getAbove();
        List<SaprodontiaDataDO.SaprodontiaItem> underneath = saprodontiaData.getUnderneath();
        Set<SaprodontiaDataDO.SaprodontiaItem> saprodontias = getSaprodontia(getSaprodontiaItemList(above, underneath), Lists.newArrayList("d", "D"));
        Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaLoss = getSaprodontia(getSaprodontiaItemList(above, underneath), Lists.newArrayList("m", "M"));
        Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaRepair = getSaprodontia(getSaprodontiaItemList(above, underneath), Lists.newArrayList("f", "F"));
        statConclusion.setIsSaprodontia(CollectionUtil.isNotEmpty(saprodontias));
        statConclusion.setSaprodontiaTeeth(CollectionUtil.isNotEmpty(saprodontias)?saprodontias.size():0);
        statConclusion.setIsSaprodontiaLoss(CollectionUtil.isNotEmpty(saprodontiaLoss));
        statConclusion.setSaprodontiaLossTeeth(CollectionUtil.isNotEmpty(saprodontiaLoss)?saprodontias.size():0);
        statConclusion.setIsSaprodontiaRepair(CollectionUtil.isNotEmpty(saprodontiaRepair));
        statConclusion.setSaprodontiaRepairTeeth(CollectionUtil.isNotEmpty(saprodontiaRepair)?saprodontias.size():0);
    }

    private List<SaprodontiaDataDO.SaprodontiaItem> getSaprodontiaItemList(List<SaprodontiaDataDO.SaprodontiaItem> above,List<SaprodontiaDataDO.SaprodontiaItem> underneath){
        List<SaprodontiaDataDO.SaprodontiaItem> list=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(above)){
            list.addAll(above);
        }
        if (CollectionUtil.isNotEmpty(underneath)){
            list.addAll(underneath);
        }
        return list;
    }

    private Set<SaprodontiaDataDO.SaprodontiaItem> getSaprodontia(List<SaprodontiaDataDO.SaprodontiaItem> list,List<String> itemList) {
        if (CollectionUtil.isEmpty(list)) {
            return Sets.newHashSet();
        }

        return list.stream().filter(s -> itemList.contains(s.getDeciduous()) || itemList.contains(s.getPermanent())).collect(Collectors.toSet());
    }

    /**
     * 处理脊柱相关数据
     */
    private void setSpineData() {
        SpineDataDO spineData = currentVisionScreeningResult.getSpineData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) || Objects.isNull(spineData)){
            return;
        }
        statConclusion.setIsSpinalCurvature(spineData.isSpinalCurvature());
    }

    /**
     * 处理血压相关数据
     */
    private void setBloodPressureData() {
        BloodPressureDataDO bloodPressureData = currentVisionScreeningResult.getBloodPressureData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) || Objects.isNull(bloodPressureData)){
            return;
        }
        TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
        Integer age = ageTuple.getFirst();
        if (age < 7){
            age = 7;
        }
        boolean highBloodPressure = StatUtil.isHighBloodPressure(bloodPressureData.getSbp().intValue(), bloodPressureData.getDbp().intValue(), screeningPlanSchoolStudent.getGender(), age);
        statConclusion.setIsNormalBloodPressure(highBloodPressure);

    }

    /**
     * 处理个人隐私相关数据
     */
    private void setPrivacyData() {
        PrivacyDataDO privacyData = currentVisionScreeningResult.getPrivacyData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) ||Objects.isNull(privacyData)){
            return;
        }
        Integer gender = screeningPlanSchoolStudent.getGender();
        if (Objects.equals(0,gender) ){
            statConclusion.setIsNocturnalEmission(privacyData.getHasIncident());
        }else {
            statConclusion.setIsMenarche(privacyData.getHasIncident());
        }

    }

    /**
     * 处理疾病史相关数据
     */
    private void setDiseasesHistoryData() {
        DiseasesHistoryDO diseasesHistoryData = currentVisionScreeningResult.getDiseasesHistoryData();
        if (Objects.nonNull(diseasesHistoryData) && CollectionUtil.isNotEmpty(diseasesHistoryData.getDiseases())){
            statConclusion.setIsDiseasesHistory(Boolean.TRUE);
        }
    }




    /**
     * 基础数据
     */
    @Getter
    @Setter
    static class BasicData {
        private Boolean isWearingGlasses;
        private BigDecimal leftCyl;
        private BigDecimal rightCyl;
        private BigDecimal leftSph;
        private BigDecimal rightSph;
        private BigDecimal rightNakedVision;
        private BigDecimal leftNakedVision;
        private BigDecimal leftCorrectVision;
        private BigDecimal rightCorrectVision;
        private AstigmatismLevelEnum leftAstigmatismWarningLevel;
        private AstigmatismLevelEnum rightAstigmatismWarningLevel;
        private Boolean isAstigmatism;
        private WarningLevel leftNakedVisionWarningLevel;
        private WarningLevel rightNakedVisionWarningLevel;
        private HyperopiaLevelEnum leftHyperopiaWarningLevel;
        private HyperopiaLevelEnum rightHyperopiaWarningLevel;
        private MyopiaLevelEnum leftMyopiaWarningLevel;
        private MyopiaLevelEnum rightMyopiaWarningLevel;
        private Boolean isRescreen;
        private Integer age;
        private Integer schoolAge;
        private Integer nakedVisionWarningLevel;
        private Integer myopiaWarningLevel;
        private Integer glassesType;


        private BasicData() {

        }

        /**
         * 获取实例
         *
         * @param visionScreeningResult
         * @param screeningPlanSchoolStudent
         * @return
         */
        public static BasicData getInstance(VisionScreeningResult visionScreeningResult, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
            BasicData basicData = new BasicData();
            //01.处理基础的数据
            dealWithBasicData(screeningPlanSchoolStudent, basicData);

            //02.处理电脑验光的数据
            dealWithComputerOptometry(screeningPlanSchoolStudent, basicData, visionScreeningResult.getComputerOptometry());

            //03.处理视力相关的数据
            dealWithVisionData(basicData, visionScreeningResult.getVisionData());

            return basicData;
        }



        /**
         * 处理基础的数据
         *
         * @param screeningPlanSchoolStudent
         * @param basicData
         */
        private static void dealWithBasicData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, BasicData basicData) {
            basicData.age = screeningPlanSchoolStudent.getStudentAge();
            basicData.schoolAge = screeningPlanSchoolStudent.getGradeType();

        }

        /**
         * 处理电脑视光的数据
         *
         * @param screeningPlanSchoolStudent
         * @param basicData
         * @param computerOptometry
         */
        private static void dealWithComputerOptometry(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, BasicData basicData, ComputerOptometryDO computerOptometry) {
            if(Objects.isNull(computerOptometry)){
                return;
            }
            ComputerOptometryDO.ComputerOptometry leftData = computerOptometry.getLeftEyeData();
            ComputerOptometryDO.ComputerOptometry rightData = computerOptometry.getRightEyeData();
            basicData.leftCyl =  leftData.getCyl();
            basicData.rightCyl = rightData.getCyl();
            basicData.leftSph = leftData.getSph();
            basicData.rightSph = rightData.getSph();
            basicData.leftAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(basicData.getLeftCyl());
            basicData.rightAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(basicData.getRightCyl());
            if (basicData.getLeftAstigmatismWarningLevel() != null && basicData.getRightAstigmatismWarningLevel() != null) {
                basicData.isAstigmatism = StatUtil.isAstigmatism(basicData.getLeftAstigmatismWarningLevel()) || StatUtil.isAstigmatism(basicData.getRightAstigmatismWarningLevel());
            }

            basicData.leftHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(basicData.getLeftSph(), basicData.getLeftCyl(), screeningPlanSchoolStudent.getStudentAge());
            basicData.rightHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(basicData.getRightSph(), basicData.getRightCyl(), screeningPlanSchoolStudent.getStudentAge());
            basicData.leftMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge(), basicData.getLeftNakedVision());
            basicData.rightMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge(), basicData.getRightNakedVision());
        }

        /**
         * 处理视力相关的数据
         *
         * @param basicData
         * @param visionData
         */
        private static void dealWithVisionData(BasicData basicData, VisionDataDO visionData) {
            if(Objects.isNull(visionData)){
                return;
            }
            basicData.glassesType = visionData.getLeftEyeData().getGlassesType();
            basicData.isWearingGlasses = basicData.getGlassesType() > 0;
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            basicData.leftNakedVision = leftEyeData.getNakedVision();
            basicData.leftCorrectVision = leftEyeData.getCorrectedVision();
            if (basicData.leftNakedVision != null) {
                basicData.leftNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(basicData.getLeftNakedVision(), basicData.getAge());
            }
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
            basicData.rightNakedVision = rightEyeData.getNakedVision();
            basicData.rightCorrectVision = rightEyeData.getCorrectedVision();
            if (basicData.rightNakedVision != null) {
                basicData.rightNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(basicData.getRightNakedVision(), basicData.getAge());
            }
            setVisionWarningLevel(basicData);
            setMyopiaVisionWarningLevel(basicData);
        }

        /**
         * 视力预警等级
         *
         * @param basicData
         */
        private static void setVisionWarningLevel(BasicData basicData) {
            List<Integer> warningLevelList = new ArrayList<>();
            if (basicData.getLeftNakedVisionWarningLevel() != null) {
                warningLevelList.add(basicData.getLeftNakedVisionWarningLevel().code);
            }
            if (basicData.getRightNakedVisionWarningLevel() != null) {
                warningLevelList.add(basicData.getRightNakedVisionWarningLevel().code);
            }
            if (CollectionUtils.isNotEmpty(warningLevelList)) {
                basicData.nakedVisionWarningLevel = Collections.max(warningLevelList);
            }
        }


        /**
         * 近视预警级别
         *
         * @param basicData
         */
        private static void setMyopiaVisionWarningLevel(BasicData basicData) {
            List<Integer> warningLevelList = new ArrayList<>();
            if (basicData.getLeftMyopiaWarningLevel() != null) {
                warningLevelList.add(basicData.getLeftMyopiaWarningLevel().code);
            }
            if (basicData.getRightMyopiaWarningLevel() != null) {
                warningLevelList.add(basicData.getRightMyopiaWarningLevel().code);
            }
            if (CollectionUtils.isNotEmpty(warningLevelList)) {
                basicData.myopiaWarningLevel = Collections.max(warningLevelList);
            }
        }

    }
}
