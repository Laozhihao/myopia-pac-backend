package com.wupol.myopia.business.core.screening.flow.domain.builder;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

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
    private String gradeCode;
    private SchoolClass schoolClass;

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
        // 设置基本的数据(数据实时更新)
        this.setBasicData();
        this.setValid();
        if (basicData.getIsValid()){
            // 设置视力相关的数据
            this.setVisionRelatedData();
            this.setRecommendVisit();
            //预警等级
            this.setWarningLevel();
        }
        this.setWarningVision();

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

        //复测错误项次
        this.setRescreenErrorNum();
        //复测项次
        this.setRescreenItemNum();

        this.setReview();
        this.setCooperative();
        this.setPhysiqueRescreenErrorNum();
        return statConclusion;
    }

    /**
     * 设置是否配合检查
     */
    private void setCooperative() {
        Optional.ofNullable(StatUtil.isCooperative(currentVisionScreeningResult)).ifPresent(cooperative->statConclusion.setIsCooperative(cooperative));
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
        statConclusion.setDistrictId(screeningPlanSchoolStudent.getSchoolDistrictId());
        statConclusion.setSchoolAge(screeningPlanSchoolStudent.getGradeType());
        statConclusion.setGender(screeningPlanSchoolStudent.getGender());
        statConclusion.setAge(screeningPlanSchoolStudent.getStudentAge());
        statConclusion.setIsRescreen(currentVisionScreeningResult.getIsDoubleScreen());
        statConclusion.setSchoolId(screeningPlanSchoolStudent.getSchoolId());
        statConclusion.setSchoolClassName(schoolClass.getName());
        statConclusion.setSchoolGradeCode(gradeCode);
        statConclusion.setScreeningType(currentVisionScreeningResult.getScreeningType());
        if (!isUpdate) {
            statConclusion.setCreateTime(new Date());
            statConclusion.setRescreenErrorNum(0);
        } else {
            statConclusion.setUpdateTime(new Date());
        }
    }

    /**
     * 设置视力相关的数据
     */
    private void setVisionRelatedData() {

        VisionDataDO visionData = currentVisionScreeningResult.getVisionData();
        if(Objects.isNull(visionData)){return;}
        this.setVisionOtherData();
        this.setNakedVisionWarningLevel();

        this.setLowVision();
        this.setLowVisionLevel();

        this.setVisionCorrection();


        ComputerOptometryDO computerOptometry = currentVisionScreeningResult.getComputerOptometry();
        if (Objects.isNull(computerOptometry)){return;}
        this.setHyperopia();
        this.setHyperopiaLevel();

        this.setAstigmatism();
        this.setAstigmatismLevel();

        this.setMyopia();
        this.setMyopiaLevel();
        this.setMyopiaWarningLevel();
        this.setScreeningMyopia();

        this.setAnisometropia();
        this.setRefractiveError();

    }


    /**
     * 屈光参差
     */
    private void setAnisometropia() {
        Boolean anisometropiaVision = StatUtil.isAnisometropiaVision(basicData.getLeftSph(), basicData.getRightSph());
        Boolean anisometropiaAstigmatism = StatUtil.isAnisometropiaAstigmatism(basicData.getLeftCyl(), basicData.getRightCyl());
        statConclusion.setIsAnisometropia(StatUtil.getIsExist(anisometropiaVision,anisometropiaAstigmatism));
    }

    /**
     * 设置视力矫正的情况
     */
    private void setVisionCorrection() {
        Integer schoolType = Optional.ofNullable(gradeCode).map(code->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(code);
            if (Objects.equals(SchoolAge.KINDERGARTEN.code,gradeCodeEnum.getType())){
                return SchoolEnum.TYPE_KINDERGARTEN.getType();
            }else {
                return SchoolEnum.TYPE_PRIMARY.getType();
            }
        }).orElse(null);
        Integer correction = StatUtil.correction(basicData.getLeftNakedVision(), basicData.getRightNakedVision(),
                basicData.getLeftCorrectVision(),basicData.getRightCorrectVision(),
                schoolType, basicData.getAge(), basicData.getIsWearingGlasses());
        statConclusion.setVisionCorrection(correction);
    }

    /**
     * 设置近视预警级别
     */
    private void setMyopiaWarningLevel() {
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setMyopiaWarningLevel(null);
            return;
        }
        WarningLevel leftLevel = StatUtil.warningLevel(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge(), 0);
        WarningLevel rightLevel = StatUtil.warningLevel(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge(), 0);
        statConclusion.setMyopiaWarningLevel(StatUtil.getSeriousLevel(leftLevel,rightLevel));
    }

    /**
     * 设置筛查性近视
     */
    private void setScreeningMyopia() {
        MyopiaLevelEnum leftScreeningMyopia = StatUtil.getScreeningMyopia(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge(), basicData.getLeftNakedVision());
        MyopiaLevelEnum rightScreeningMyopia = StatUtil.getScreeningMyopia(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge(), basicData.getRightNakedVision());
        statConclusion.setScreeningMyopia(StatUtil.getSeriousLevel(leftScreeningMyopia,rightScreeningMyopia));
    }
    /**
     * 设置裸眼视力预警级别
     */
    private void setNakedVisionWarningLevel() {
        WarningLevel leftLevel = StatUtil.nakedVision(basicData.getLeftNakedVision(), basicData.getAge());
        WarningLevel rightLevel = StatUtil.nakedVision(basicData.getRightNakedVision(), basicData.getAge());
        statConclusion.setNakedVisionWarningLevel(StatUtil.getSeriousLevel(leftLevel,rightLevel));
    }

    /**
     * 视力低下等级
     */
    private void setLowVisionLevel(){
        if (basicData.getAge() < 6 && Objects.equals(statConclusion.getIsLowVision(), Boolean.TRUE)) {
            statConclusion.setLowVisionLevel(LowVisionLevelEnum.LOW_VISION.code);
            return;
        }
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setLowVisionLevel(null);
            return;
        }
        LowVisionLevelEnum leftLevel = StatUtil.getLowVisionLevel(basicData.getLeftNakedVision(), basicData.getAge());
        LowVisionLevelEnum rightLevel = StatUtil.getLowVisionLevel(basicData.getRightNakedVision(), basicData.getAge());
        statConclusion.setLowVisionLevel(StatUtil.getSeriousLevel(leftLevel,rightLevel));
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
        statConclusion.setVisionR(basicData.getRightNakedVision());
        statConclusion.setVisionL(basicData.getLeftNakedVision());
    }

    /**
     * 设置预警级别
     */
    private void setWarningLevel() {
        // 特殊处理 角膜塑型镜特殊处理
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setWarningLevel(WarningLevel.NORMAL.code);
            return;
        }
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
        Integer warningLevelInt = StatUtil.getWarningLevelInt(
                basicData.getLeftCyl(),basicData.getLeftSph(),basicData.getLeftNakedVision(),
                basicData.getRightCyl(),basicData.getRightSph(),basicData.getRightNakedVision(),
                basicData.getAge(),gradeCodeEnum.getType());
        statConclusion.setWarningLevel(warningLevelInt);
    }

    /**
     * 近视等级
     */
    private void setMyopiaLevel() {
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setMyopiaLevel(null);
            return;
        }
        MyopiaLevelEnum leftLevel = StatUtil.getMyopiaLevel(basicData.getLeftSph(), basicData.getLeftCyl());
        MyopiaLevelEnum rightLevel = StatUtil.getMyopiaLevel(basicData.getRightSph(), basicData.getRightCyl());
        statConclusion.setMyopiaLevel(StatUtil.getSeriousLevel(leftLevel, rightLevel));
    }

    /**
     * 远视等级
     */
    private void setHyperopiaLevel() {
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setHyperopiaLevel(null);
            return;
        }
        HyperopiaLevelEnum leftLevel = StatUtil.getHyperopiaLevel(basicData.getLeftSph(), basicData.getLeftCyl(), basicData.getAge());
        HyperopiaLevelEnum rightLevel = StatUtil.getHyperopiaLevel(basicData.getRightSph(), basicData.getRightCyl(), basicData.getAge());
        statConclusion.setHyperopiaLevel(StatUtil.getSeriousLevel(leftLevel, rightLevel));
    }

    /**
     * 散光等级
     */
    private void setAstigmatismLevel() {
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setAstigmatismLevel(null);
            return;
        }
        AstigmatismLevelEnum leftLevel = StatUtil.getAstigmatismLevel(basicData.getLeftCyl());
        AstigmatismLevelEnum rightLevel = StatUtil.getAstigmatismLevel(basicData.getRightCyl());
        statConclusion.setAstigmatismLevel(StatUtil.getSeriousLevel(leftLevel, rightLevel));
    }

    /**
     * 设置视力低下的数据
     */
    private void setLowVision() {
        Boolean isLeftResult = StatUtil.isLowVision(basicData.getLeftNakedVision(), basicData.getAge());
        Boolean isRightResult = StatUtil.isLowVision(basicData.getRightNakedVision(), basicData.getAge());
        statConclusion.setIsLowVision(StatUtil.getIsExist(isLeftResult,isRightResult));
    }

    /**
     * 屈光不正
     */
    private void setRefractiveError() {
        Boolean leftRefractiveError = StatUtil.isRefractiveError(basicData.getLeftSph(),basicData.getLeftCyl(),basicData.getAge());
        Boolean rightRefractiveError = StatUtil.isRefractiveError(basicData.getRightSph(),basicData.getRightCyl(),basicData.getAge());
        statConclusion.setIsRefractiveError(StatUtil.getIsExist(leftRefractiveError,rightRefractiveError));
    }

    /**
     * 近视
     */
    private void setMyopia() {
        if (Objects.equals(basicData.getGlassesType(),GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            statConclusion.setIsMyopia(Boolean.TRUE);
            return;
        }
        Boolean isLeftMyopia = StatUtil.isMyopia(basicData.getLeftSph(),basicData.getLeftCyl(), basicData.getLeftNakedVision());
        Boolean isRightMyopia = StatUtil.isMyopia(basicData.getRightSph(),basicData.getRightCyl(), basicData.getRightNakedVision());
        statConclusion.setIsMyopia(StatUtil.getIsExist(isLeftMyopia,isRightMyopia));
    }

    /**
     * 远视
     */
    private void setHyperopia() {
        Boolean isLeftHyperopia = StatUtil.isHyperopia(basicData.getLeftSph(),basicData.getLeftCyl(),basicData.getAge());
        Boolean isRightHyperopia = StatUtil.isHyperopia(basicData.getRightSph(),basicData.getRightCyl(),basicData.getAge());
        statConclusion.setIsHyperopia(StatUtil.getIsExist(isLeftHyperopia,isRightHyperopia));
    }

    /**
     * 散光
     */
    private void setAstigmatism() {
        Boolean leftAstigmatism = StatUtil.isAstigmatism(basicData.getLeftCyl());
        Boolean rightAstigmatism = StatUtil.isAstigmatism(basicData.getRightCyl());
        statConclusion.setIsAstigmatism(StatUtil.getIsExist(leftAstigmatism,rightAstigmatism));
    }

    /**
     * 建议就诊
     */
    private void setRecommendVisit() {
        OtherEyeDiseasesDO otherEyeDiseases = currentVisionScreeningResult.getOtherEyeDiseases();
        Boolean otherEyeDiseasesNormal = Optional.ofNullable(otherEyeDiseases).map(OtherEyeDiseasesDO::isNormal).orElse(null);

        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
        Boolean isRecommendVisit = ScreeningResultUtil.getDoctorAdvice(
                basicData.getLeftNakedVision(),basicData.getRightNakedVision(),
                basicData.getLeftCorrectVision(),basicData.getRightCorrectVision(),
                basicData.getGlassesType(), gradeCodeEnum.getType(), basicData.getAge(), otherEyeDiseasesNormal,
                currentVisionScreeningResult.getComputerOptometry()).getIsRecommendVisit();
        statConclusion.setIsRecommendVisit(isRecommendVisit);
    }

    /**
     * 复查
     */
    private void setReview() {
        List<Boolean> isReviewList =Lists.newArrayList();
        Consumer<Boolean> consumerTrue = flag -> isReviewList.add(Objects.equals(Boolean.TRUE, flag));
        Consumer<Boolean> consumerFalse = flag -> isReviewList.add(Objects.equals(Boolean.FALSE, flag));

        Optional.ofNullable(statConclusion.getIsLowVision()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsMyopia()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsHyperopia()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsAstigmatism()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsObesity()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsOverweight()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsMalnutrition()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsStunting()).ifPresent(consumerTrue);
        Optional.ofNullable(statConclusion.getIsSpinalCurvature()).ifPresent(consumerFalse);

        if (CollectionUtil.isNotEmpty(isReviewList)){
           boolean isReview  = isReviewList.stream().filter(Objects::nonNull).anyMatch(Boolean::booleanValue);
           statConclusion.setIsReview(isReview);
        }

    }

    /**
     * 复测错误项次
     */
    private void setRescreenErrorNum() {
        if (anotherVisionScreeningResult != null) {
            statConclusion.setRescreenErrorNum(StatUtil.calculateErrorNum(currentVisionScreeningResult,anotherVisionScreeningResult,basicData.getIsWearingGlasses()));
        } else {
            statConclusion.setRescreenErrorNum(0);
        }
    }

    /**
     * 复测项次
     */
    private void setRescreenItemNum() {
        if (Objects.nonNull(currentVisionScreeningResult)){
            statConclusion.setRescreenItemNum(StatUtil.calculateItemNum(currentVisionScreeningResult));
        }
    }

    /**
     * 初始化筛查结论数据
     * @param statConclusion 筛查结论数据
     */
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

    /**
     * 数据有效性
     */
    private void setValid() {
        statConclusion.setIsValid(basicData.getIsValid());
    }

    /**
     * 设置年级编码
     * @param gradeCode 年级编码
     */
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
        if (heightAndWeightData.valid()){
            overweightAndObesity(heightAndWeightData.getBmi(),ageTuple.getSecond());
            malnutrition(heightAndWeightData.getBmi(),heightAndWeightData.getHeight(),ageTuple.getSecond());
        }
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
     * 营养不良 和 生长迟缓
     * @param bmi 身体质量指数值
     * @param height 身高
     * @param age 年龄（精确到半岁）
     */
    private void malnutrition(BigDecimal bmi,BigDecimal height,String age){
        Boolean wasting = StatUtil.isWasting(bmi, age, screeningPlanSchoolStudent.getGender());
        if (Objects.nonNull(wasting) ){
            statConclusion.setIsMalnutrition(wasting);
        }
        Boolean stunting = StatUtil.isStunting(screeningPlanSchoolStudent.getGender(), age, height);
        if (Objects.nonNull(stunting)){
            statConclusion.setIsStunting(stunting);
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
        List<SaprodontiaDataDO.SaprodontiaItem> saprodontias = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("d", "D"));
        List<SaprodontiaDataDO.SaprodontiaItem> saprodontiaLoss = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("m", "M"));
        List<SaprodontiaDataDO.SaprodontiaItem> saprodontiaRepair = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("f", "F"));
        statConclusion.setIsSaprodontia(CollectionUtil.isNotEmpty(saprodontias));
        statConclusion.setSaprodontiaTeeth(CollectionUtil.isNotEmpty(saprodontias)?saprodontias.size():0);
        statConclusion.setIsSaprodontiaLoss(CollectionUtil.isNotEmpty(saprodontiaLoss));
        statConclusion.setSaprodontiaLossTeeth(CollectionUtil.isNotEmpty(saprodontiaLoss)?saprodontiaLoss.size():0);
        statConclusion.setIsSaprodontiaRepair(CollectionUtil.isNotEmpty(saprodontiaRepair));
        statConclusion.setSaprodontiaRepairTeeth(CollectionUtil.isNotEmpty(saprodontiaRepair)?saprodontiaRepair.size():0);
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
        HeightAndWeightDataDO heightAndWeightData = currentVisionScreeningResult.getHeightAndWeightData();
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,screeningPlanSchoolStudent.getGradeType()) || Objects.isNull(bloodPressureData)){
            return;
        }
        if (Objects.isNull(heightAndWeightData) || Objects.isNull(heightAndWeightData.getHeight())){
            return;
        }
        TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
        Integer age = ageTuple.getFirst();
        if (age < 7){
            age = 7;
        }
        boolean highBloodPressure = StatUtil.isHighBloodPressure(bloodPressureData.getSbp().intValue(), bloodPressureData.getDbp().intValue(), screeningPlanSchoolStudent.getGender(), age,heightAndWeightData.getHeight());
        statConclusion.setIsNormalBloodPressure(!highBloodPressure);

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
            setDiseaseNumInfo(diseasesHistoryData.getDiseases());
        }
    }

    private void setDiseaseNumInfo(List<String> diseases){
        if (CollectionUtil.isEmpty(diseases)){
            return;
        }
        DiseaseNumDO diseaseNumDO= new DiseaseNumDO();
        for (String disease : diseases) {
            switch (disease){
                case "肝炎":
                    diseaseNumDO.setHepatitis(1);
                    break;
                case "肾炎":
                    diseaseNumDO.setNephritis(1);
                    break;
                case "心脏病":
                    diseaseNumDO.setHeartDisease(1);
                    break;
                case "贫血":
                    diseaseNumDO.setAnemia(1);
                    break;
                case "高血压":
                    diseaseNumDO.setHypertension(1);
                    break;
                case "糖尿病":
                    diseaseNumDO.setDiabetes(1);
                    break;
                case "过敏性哮喘":
                    diseaseNumDO.setAllergicAsthma(1);
                    break;
                case "身体残疾":
                    diseaseNumDO.setPhysicalDisability(1);
                    break;
                default:
                    break;
            }
        }
        statConclusion.setDiseaseNum(diseaseNumDO);

    }



    /**
     * 基础数据
     */
    @Getter
    @Setter
    static class BasicData {
        private Boolean isValid;
        private Boolean isWearingGlasses;
        private BigDecimal leftCyl;
        private BigDecimal rightCyl;
        private BigDecimal leftSph;
        private BigDecimal rightSph;
        private BigDecimal rightNakedVision;
        private BigDecimal leftNakedVision;
        private BigDecimal leftCorrectVision;
        private BigDecimal rightCorrectVision;

        private AstigmatismLevelEnum leftAstigmatismLevel;
        private AstigmatismLevelEnum rightAstigmatismLevel;

        private WarningLevel leftNakedVisionWarningLevel;
        private WarningLevel rightNakedVisionWarningLevel;
        private Integer nakedVisionWarningLevel;

        private LowVisionLevelEnum leftLowVisionLevel;
        private LowVisionLevelEnum rightLowVisionLevel;
        private Integer lowVisionLevel;

        private HyperopiaLevelEnum leftHyperopiaLevel;
        private HyperopiaLevelEnum rightHyperopiaLevel;

        private MyopiaLevelEnum leftMyopiaLevel;
        private MyopiaLevelEnum rightMyopiaLevel;

        private WarningLevel leftMyopiaWarningLevel;
        private WarningLevel rightMyopiaWarningLevel;
        private Integer myopiaWarningLevel;

        private Boolean isRescreen;
        private Integer age;
        private Integer schoolAge;
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

            //处理基础的数据
            dealWithBasicData(screeningPlanSchoolStudent, basicData);

            //视力相关数据的有效性
            dealWithVaild(visionScreeningResult,basicData);

            //处理电脑验光的数据
            dealWithComputerOptometry(basicData, visionScreeningResult.getComputerOptometry());

            //处理视力相关的数据
            dealWithVisionData(basicData, visionScreeningResult.getVisionData());

            return basicData;
        }

        /**
         * 处理数据有效性
         * @param visionScreeningResult 筛查结果数据
         * @param basicData 流转基础数据
         */
        private static void dealWithVaild(VisionScreeningResult visionScreeningResult, BasicData basicData) {
            if (Objects.equals(Boolean.FALSE,visionScreeningResult.getIsDoubleScreen())){
                VisionDataDO visionData = visionScreeningResult.getVisionData();
                ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
                if (ObjectsUtil.hasNull(visionData,computerOptometry)) {
                    basicData.isValid=Boolean.FALSE;
                    return;
                }
                basicData.isValid=StatUtil.isCompletedData(visionData, computerOptometry);
            }else {
                basicData.isValid= StatUtil.rescreenCompletedData(visionScreeningResult);
            }
        }


        /**
         * 处理基础的数据
         *
         * @param screeningPlanSchoolStudent 筛查计划学生信息
         * @param basicData 流转基础数据
         */
        private static void dealWithBasicData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, BasicData basicData) {
            basicData.age = screeningPlanSchoolStudent.getStudentAge();
            basicData.schoolAge = screeningPlanSchoolStudent.getGradeType();

        }

        /**
         * 处理电脑视光的数据
         *
         * @param basicData 流转基础数据
         * @param computerOptometry 电脑验光数据
         */
        private static void dealWithComputerOptometry(BasicData basicData, ComputerOptometryDO computerOptometry) {
            Optional<ComputerOptometryDO> optional = Optional.ofNullable(computerOptometry);
            basicData.leftCyl =  optional.map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).orElse(null);
            basicData.rightCyl = optional.map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).orElse(null);
            basicData.leftSph = optional.map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).orElse(null);
            basicData.rightSph = optional.map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).orElse(null);

        }

        /**
         * 处理视力相关的数据
         *
         * @param basicData 流转基础数据
         * @param visionData 视力数据
         */
        private static void dealWithVisionData(BasicData basicData, VisionDataDO visionData) {
            Optional<VisionDataDO> optional = Optional.ofNullable(visionData);

            basicData.glassesType = optional.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
            basicData.isWearingGlasses = Optional.ofNullable(basicData.getGlassesType()).map(g->g>0).orElse(null);
            basicData.leftNakedVision = optional.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getNakedVision).orElse(null);
            basicData.leftCorrectVision = optional.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getCorrectedVision).orElse(null);
            basicData.rightNakedVision = optional.map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getNakedVision).orElse(null);
            basicData.rightCorrectVision = optional.map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getCorrectedVision).orElse(null);
        }
    }

    private void setPhysiqueRescreenErrorNum() {
        statConclusion.setPhysiqueRescreenErrorNum(Objects.nonNull(anotherVisionScreeningResult) ? calculatePhysiqueRescreenErrorNum() : 0);
    }

    /**
     * 身高体重错误项计算
     *
     * @return 身高体重错误项
     */
    private int calculatePhysiqueRescreenErrorNum() {
        HeightAndWeightDataDO current = currentVisionScreeningResult.getHeightAndWeightData();
        HeightAndWeightDataDO another = anotherVisionScreeningResult.getHeightAndWeightData();
        if (ObjectsUtil.hasNull(current, another)) {
            return 0;
        }
        return StatUtil.inRange(current.getHeight(), another.getHeight(), new BigDecimal("0.5"))
                + StatUtil.inRange(current.getWeight(), another.getWeight(), new BigDecimal("0.1"));
    }

    public StatConclusionBuilder setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
        return this;
    }
}
