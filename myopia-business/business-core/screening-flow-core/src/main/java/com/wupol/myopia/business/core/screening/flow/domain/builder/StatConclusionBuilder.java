package com.wupol.myopia.business.core.screening.flow.domain.builder;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

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
    private Boolean isUpdate;
    private final BigDecimal OTHERS_RANGE_VALUE = BigDecimal.valueOf(1.0);
    private final BigDecimal AVE_RANGE_VALUE = BigDecimal.valueOf(0.5);
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
        // 设置视力相关的数据
        if (currentVisionScreeningResult.getVisionData() != null) {
            this.setVisionRelatedData();
        }
        // 设置电脑验光的数据
        this.setRefractiveError();
        this.setRecommendVisit();
        this.setMyopia();
        this.setLowVision();
        this.setWarningLevel();
        this.setValid();
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
        boolean isLeftEyeVisionWarning = statConclusion.getVisionL() != null && statConclusion.getVisionL() <= 4.9;
        boolean isRightEyeVisionWarning = statConclusion.getVisionR() != null && statConclusion.getVisionR() <= 4.9;
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
        statConclusion.setSchoolAge(basicData.schoolAge);
        statConclusion.setGender(basicData.gender);
        statConclusion.setAge(basicData.age);
        statConclusion.setIsRescreen(basicData.isRescreen);
        statConclusion.setRescreenErrorNum(0);
        statConclusion.setSchoolId(screeningPlanSchoolStudent.getSchoolId());
        statConclusion.setSchoolClassName(screeningPlanSchoolStudent.getClassName());
        statConclusion.setSchoolGradeCode(gradeCode);
    }

    /**
     * 设置视力相关的数据
     */
    private void setVisionRelatedData() {
        this.setHyperopia();
        this.setAstigmatism();
        this.setVisionOtherData();
        this.setNakedVisionWarningLevel();
        this.setMyopiaWarningLevel();
        this.setVisionCorrection();
    }

    /**
     * 设置视力矫正的情况
     */
    private void setVisionCorrection() {
        double keyParam = 4.9D;
        if (ObjectsUtil.allNotNull(basicData.rightNakedVision, basicData.leftNakedVision) && basicData.rightNakedVision >= keyParam && basicData.leftNakedVision >= keyParam) {
            statConclusion.setVisionCorrection(VisionCorrection.NORMAL.code);
        } else if (!basicData.isWearingGlasses) {
            statConclusion.setVisionCorrection(VisionCorrection.UNCORRECTED.code);
        } else if (ObjectsUtil.allNotNull(basicData.leftCorrectVision, basicData.rightCorrectVision) && basicData.leftCorrectVision > keyParam) {
            statConclusion.setVisionCorrection(VisionCorrection.ENOUGH_CORRECTED.code);
        } else {
            statConclusion.setVisionCorrection(VisionCorrection.UNDER_CORRECTED.code);
        }
    }

    /**
     * 设置近视预警级别
     */
    private void setMyopiaWarningLevel() {
        statConclusion.setMyopiaWarningLevel(basicData.myopiaWarningLevel);
    }

    /**
     * 设置裸眼视力预警级别
     */
    private void setNakedVisionWarningLevel() {
        statConclusion.setNakedVisionWarningLevel(basicData.nakedVisionWarningLevel);
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
        statConclusion.setGlassesType(basicData.glassesType);
        statConclusion.setVisionR(basicData.rightNakedVision == null ? 0.0f : basicData.rightNakedVision);
        statConclusion.setVisionL(basicData.leftNakedVision == null ? 0.0f : basicData.leftNakedVision);
    }

    /**
     * 设置预警级别
     */
    private void setWarningLevel() {
        // 特殊处理 角膜塑型镜特殊处理
        if (basicData.glassesType != null && basicData.glassesType.equals(GlassesType.ORTHOKERATOLOGY.code)) {
            statConclusion.setWarningLevel(WarningLevel.NORMAL.code);
            return;
        }
        Integer warningLevelInt = StatUtil.getWarningLevelInt(
                Objects.nonNull(basicData.getLeftCyl()) ? new BigDecimal(basicData.getLeftCyl().toString()) : null,
                Objects.nonNull(basicData.getLeftSph()) ? new BigDecimal(basicData.getLeftSph().toString()) : null,
                Objects.nonNull(basicData.getLeftNakedVision()) ? new BigDecimal(basicData.getLeftNakedVision().toString()) : null,
                Objects.nonNull(basicData.getRightCyl()) ? new BigDecimal(basicData.getRightCyl().toString()) : null,
                Objects.nonNull(basicData.getRightSph()) ? new BigDecimal(basicData.getRightSph().toString()) : null,
                Objects.nonNull(basicData.getRightNakedVision()) ? new BigDecimal(basicData.getRightNakedVision().toString()) : null,
                basicData.getAge());
        statConclusion.setWarningLevel(warningLevelInt);
    }

    /**
     * 近视等级
     */
    private void setMyopiaLevel() {
        Integer left = StatUtil.getMyopiaLevel(basicData.getLeftSph(), basicData.getLeftCyl());
        Integer right = StatUtil.getMyopiaLevel(basicData.getRightSph(), basicData.getRightCyl());
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
        boolean isLeftResult = false;
        boolean isrightResult = false;
        if (basicData.leftNakedVision != null) {
            isLeftResult = StatUtil.isLowVision(basicData.leftNakedVision, basicData.age);
        }
        if (basicData.rightNakedVision != null) {
            isrightResult = StatUtil.isLowVision(basicData.rightNakedVision, basicData.age);
        }
        boolean isLowVision = isLeftResult || isrightResult;
        statConclusion.setIsLowVision(isLowVision);
    }

    private void setRefractiveError() {
        boolean isRefractiveError = StatUtil.isRefractiveError(basicData.isAstigmatism != null ? basicData.isAstigmatism : false, statConclusion.getIsMyopia() != null ? statConclusion.getIsMyopia() : false, statConclusion.getIsHyperopia() != null ? statConclusion.getIsHyperopia() : false);
        statConclusion.setIsRefractiveError(isRefractiveError);
    }

    private void setMyopia() {
        boolean isLeftMyopia = false;
        boolean isrightMyopia = false;
        if (basicData.leftMyopiaWarningLevel != null) {
            isLeftMyopia = StatUtil.isMyopia(basicData.leftMyopiaWarningLevel);
        }
        if (basicData.rightMyopiaWarningLevel != null) {
            isrightMyopia = StatUtil.isMyopia(basicData.rightMyopiaWarningLevel);
        }
        boolean isMyopia = isLeftMyopia || isrightMyopia;
        statConclusion.setIsMyopia(isMyopia);
    }

    private void setHyperopia() {
        Boolean isHyperopia = null;
        if (ObjectsUtil.allNotNull(basicData.leftHyperopiaWarningLevel, basicData.rightHyperopiaWarningLevel)) {
            isHyperopia = StatUtil.isHyperopia(basicData.leftHyperopiaWarningLevel)
                    || StatUtil.isHyperopia(basicData.rightHyperopiaWarningLevel);
        }
        statConclusion.setIsHyperopia(isHyperopia);
    }

    private void setAstigmatism() {
        statConclusion.setIsAstigmatism(basicData.isAstigmatism);
    }


    private void setRecommendVisit() {
        boolean isRecommendVisit = ScreeningResultUtil.getDoctorAdvice(
                Objects.nonNull(basicData.leftNakedVision) ? new BigDecimal(basicData.leftNakedVision.toString()) : null,
                Objects.nonNull(basicData.rightNakedVision) ? new BigDecimal(basicData.rightNakedVision.toString()) : null,
                Objects.nonNull(basicData.leftCorrectVision) ? new BigDecimal(basicData.leftCorrectVision.toString()) : null,
                Objects.nonNull(basicData.rightCorrectVision) ? new BigDecimal(basicData.rightCorrectVision.toString()) : null,
                basicData.glassesType, basicData.schoolAge, basicData.age, basicData.otherEyeDiseasesNormal,
                currentVisionScreeningResult.getComputerOptometry()).getIsRecommendVisit();
        statConclusion.setIsRecommendVisit(isRecommendVisit);
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
        if (basicData.isWearingGlasses) {
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
            errorNum += inRange(currentVisionData.getLeftEyeData().getNakedVision(), anotherVisionData.getLeftEyeData().getNakedVision(), OTHERS_RANGE_VALUE);
            errorNum += inRange(currentVisionData.getRightEyeData().getNakedVision(), anotherVisionData.getRightEyeData().getNakedVision(), OTHERS_RANGE_VALUE);
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
            errorNum += inRange(currentVisionData.getLeftEyeData().getCorrectedVision(), anotherVisionData.getLeftEyeData().getCorrectedVision(), OTHERS_RANGE_VALUE);
            errorNum += inRange(currentVisionData.getRightEyeData().getCorrectedVision(), anotherVisionData.getRightEyeData().getCorrectedVision(), OTHERS_RANGE_VALUE);
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
        if (currentComputerOptometry != null && anotherComputerOptometry != null) {
            errorNum += inRange(currentComputerOptometry.getLeftEyeData().getSph().add(currentComputerOptometry.getLeftEyeData().getCyl().divide(new BigDecimal(2))), anotherComputerOptometry.getLeftEyeData().getSph().add(anotherComputerOptometry.getLeftEyeData().getCyl().divide(new BigDecimal(2))), OTHERS_RANGE_VALUE);
            errorNum += inRange(currentComputerOptometry.getRightEyeData().getSph().add(currentComputerOptometry.getRightEyeData().getCyl().divide(new BigDecimal(2))), anotherComputerOptometry.getRightEyeData().getSph().add(anotherComputerOptometry.getRightEyeData().getCyl().divide(new BigDecimal(2))), OTHERS_RANGE_VALUE);
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
     * 基础数据
     */
    @Getter
    @Setter
    static class BasicData {
        public boolean isWearingGlasses;
        private Float leftCyl;
        private Float rightCyl;
        private Float leftSph;
        private Float rightSph;
        private Float rightNakedVision;
        private Float leftNakedVision;
        private Float leftCorrectVision;
        private Float rightCorrectVision;
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
        private Integer gender;
        private Integer schoolAge;
        private Integer noticeId;
        private Boolean isUpdate;
        private Integer nakedVisionWarningLevel;
        private Integer myopiaWarningLevel;
        private Integer glassesType;
        private Boolean otherEyeDiseasesNormal;

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
            dealWithBasicData(visionScreeningResult, screeningPlanSchoolStudent, basicData);
            //02.处理电脑验光的数据
            ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
            if (computerOptometry != null) {
                dealWithComputerOptometry(screeningPlanSchoolStudent, basicData, computerOptometry);
            }
            //03.处理视力相关的数据
            VisionDataDO visionData = visionScreeningResult.getVisionData();
            if (visionData != null) {
                dealWithVisionData(basicData, visionData);
            }
            setOtherEyeDiseases(basicData, visionScreeningResult.getOtherEyeDiseases());
            return basicData;
        }

        /**
         * 处理基础的数据
         *
         * @param visionScreeningResult
         * @param screeningPlanSchoolStudent
         * @param basicData
         */
        private static void dealWithBasicData(VisionScreeningResult visionScreeningResult, ScreeningPlanSchoolStudent screeningPlanSchoolStudent, BasicData basicData) {
            basicData.isRescreen = visionScreeningResult.getIsDoubleScreen();
            basicData.age = screeningPlanSchoolStudent.getStudentAge();
            basicData.gender = screeningPlanSchoolStudent.getGender();
            basicData.schoolAge = screeningPlanSchoolStudent.getGradeType();
            basicData.noticeId = screeningPlanSchoolStudent.getSrcScreeningNoticeId();
        }

        /**
         * 处理电脑视光的数据
         *
         * @param screeningPlanSchoolStudent
         * @param basicData
         * @param computerOptometry
         */
        private static void dealWithComputerOptometry(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, BasicData basicData, ComputerOptometryDO computerOptometry) {
            ComputerOptometryDO.ComputerOptometry leftData = computerOptometry.getLeftEyeData();
            ComputerOptometryDO.ComputerOptometry rightData = computerOptometry.getRightEyeData();
            basicData.leftCyl = leftData.getCyl() == null ? null : leftData.getCyl().floatValue();
            basicData.rightCyl = rightData.getCyl() == null ? null : rightData.getCyl().floatValue();
            basicData.leftSph = leftData.getSph() == null ? null : leftData.getSph().floatValue();
            basicData.rightSph = rightData.getSph() == null ? null : rightData.getSph().floatValue();
            basicData.leftAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(basicData.leftCyl);
            basicData.rightAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(basicData.rightCyl);
            if (basicData.leftAstigmatismWarningLevel != null && basicData.rightAstigmatismWarningLevel != null) {
                basicData.isAstigmatism = StatUtil.isAstigmatism(basicData.leftAstigmatismWarningLevel) || StatUtil.isAstigmatism(basicData.rightAstigmatismWarningLevel);
            }
            basicData.leftHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(basicData.leftSph, basicData.leftCyl, screeningPlanSchoolStudent.getStudentAge());
            basicData.rightHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(basicData.rightSph, basicData.rightCyl, screeningPlanSchoolStudent.getStudentAge());
            basicData.leftMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(basicData.leftSph, basicData.leftCyl);
            basicData.rightMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(basicData.rightSph, basicData.rightCyl);
        }

        /**
         * 处理视力相关的数据
         *
         * @param basicData
         * @param visionData
         */
        private static void dealWithVisionData(BasicData basicData, VisionDataDO visionData) {
            basicData.glassesType = visionData.getLeftEyeData().getGlassesType();
            basicData.isWearingGlasses = basicData.glassesType > 0;
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            basicData.leftNakedVision = leftEyeData.getNakedVision() == null ? null : leftEyeData.getNakedVision().floatValue();
            basicData.leftCorrectVision = leftEyeData.getCorrectedVision() == null ? null : leftEyeData.getCorrectedVision().floatValue();
            if (basicData.leftNakedVision != null) {
                basicData.leftNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(basicData.leftNakedVision, basicData.age);
            }
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
            basicData.rightNakedVision = rightEyeData.getNakedVision() == null ? null : rightEyeData.getNakedVision().floatValue();
            basicData.rightCorrectVision = rightEyeData.getCorrectedVision() == null ? null : rightEyeData.getCorrectedVision().floatValue();
            if (basicData.rightNakedVision != null) {
                basicData.rightNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(basicData.rightNakedVision, basicData.age);
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
            if (basicData.leftNakedVisionWarningLevel != null) {
                warningLevelList.add(basicData.leftNakedVisionWarningLevel.code);
            }
            if (basicData.rightNakedVisionWarningLevel != null) {
                warningLevelList.add(basicData.rightNakedVisionWarningLevel.code);
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
            if (basicData.leftMyopiaWarningLevel != null) {
                warningLevelList.add(basicData.leftMyopiaWarningLevel.code);
            }
            if (basicData.rightMyopiaWarningLevel != null) {
                warningLevelList.add(basicData.rightMyopiaWarningLevel.code);
            }
            if (CollectionUtils.isNotEmpty(warningLevelList)) {
                basicData.myopiaWarningLevel = Collections.max(warningLevelList);
            }
        }

        /**
         * 其他眼病
         *
         * @param basicData        基本数据
         * @param otherEyeDiseases 其他眼病
         */
        private static void setOtherEyeDiseases(BasicData basicData, OtherEyeDiseasesDO otherEyeDiseases) {
            if (Objects.nonNull(otherEyeDiseases)) {
                basicData.otherEyeDiseasesNormal = otherEyeDiseases.isNormal();
            }
        }
    }
}
