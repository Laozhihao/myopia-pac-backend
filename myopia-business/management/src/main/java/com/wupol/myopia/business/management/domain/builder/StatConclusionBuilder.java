package com.wupol.myopia.business.management.domain.builder;

import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.util.StatUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 筛查数据结论
 */

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StatConclusionBuilder {
    private VisionScreeningResult visionScreeningResult;
    private ScreeningPlanSchoolStudent screeningPlanSchoolStudent;
    private StatConclusion statConclusion;
    private BasicData basicData;
    private Boolean isUpdate;


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
     * @param visionScreeningResult
     * @return
     */
    public StatConclusionBuilder setVisionScreeningResult(VisionScreeningResult visionScreeningResult) {
        this.visionScreeningResult = visionScreeningResult;
        return this;
    }

    /**
     * 构建
     *
     * @return
     */
    public StatConclusion build() {
        if (!ObjectsUtil.allNotNull(visionScreeningResult, screeningPlanSchoolStudent, statConclusion)) {
            throw new ManagementUncheckedException("StatConclusion构建失败，缺少关键参数");
        }
        // 基本数据的准备
        basicData = BasicData.getInstance(visionScreeningResult, screeningPlanSchoolStudent);
        // 如果新增的话，设置基本的数据
        if (!isUpdate) {
            this.setBasicData();
        }
        // 设置视力相关的数据
        if (visionScreeningResult.getVisionData() != null) {
            this.setVisionRelatedData();
        }
        // 设置电脑验光的数据
        if (visionScreeningResult.getComputerOptometry() != null) {
            this.setComputerRelatedData();
        }
        // 设置通用的数据
        if (visionScreeningResult.getVisionData() != null && visionScreeningResult.getComputerOptometry() != null) {
            this.setRecommendVisit();
            this.setMyopia();
            this.setWarningLevel();
            this.setLowVision();
        }
        this.setValid();
        return statConclusion;
    }

    /**
     * 设置基础数据
     */
    private void setBasicData() {
        statConclusion.setScreeningPlanSchoolStudentId(screeningPlanSchoolStudent.getId());
        statConclusion.setResultId(visionScreeningResult.getId());
        statConclusion.setSrcScreeningNoticeId(screeningPlanSchoolStudent.getSrcScreeningNoticeId());
        statConclusion.setTaskId(screeningPlanSchoolStudent.getScreeningTaskId());
        statConclusion.setPlanId(screeningPlanSchoolStudent.getScreeningPlanId());
        statConclusion.setCreateTime(new Date());
        statConclusion.setDistrictId(screeningPlanSchoolStudent.getDistrictId());
        statConclusion.setSchoolAge(basicData.schoolAge);
        statConclusion.setGender(basicData.gender);
        statConclusion.setIsRescreen(basicData.isRescreen);
        statConclusion.setRescreenErrorNum(0);
    }

    private void setVisionRelatedData() {
        this.setHyperopia();
        this.setAstigmatism();
        this.setVisionOtherData();
    }


    private void setComputerRelatedData() {
        this.setRefractiveError();
        this.setRecommendVisit();
        this.setRescreenErrorNum();
        this.setComputerOtherData();
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

    private void setVisionOtherData() {
        statConclusion.setIsWearingGlasses(basicData.isWearingGlasses);
        statConclusion.setVisionR(basicData.rightNakedVision == null ? 0.0f : basicData.rightNakedVision);
        statConclusion.setVisionL(basicData.leftNakedVision == null ? 0.0f : basicData.leftNakedVision);
    }


    /**
     * 电脑的其他数据
     */
    private void setComputerOtherData() {
        statConclusion.setDistrictId(visionScreeningResult.getDistrictId());
        statConclusion.setTaskId(visionScreeningResult.getTaskId());
        statConclusion.setPlanId(visionScreeningResult.getPlanId());
        statConclusion.setSchoolAge(basicData.schoolAge);
        statConclusion.setGender(basicData.gender);
        statConclusion.setSrcScreeningNoticeId(basicData.noticeId);
        statConclusion.setResultId(visionScreeningResult.getId());
        statConclusion.setIsRescreen(basicData.isRescreen);
    }


    /**
     * 计算基础数据
     */
/*    private void calculateBasicData() {
        ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
        VisionDataDO visionData = visionScreeningResult.getVisionData();
        isWearingGlasses = true;
        age = screeningPlanSchoolStudent.getStudentAge();
        gender = screeningPlanSchoolStudent.getGender();
        schoolAge = screeningPlanSchoolStudent.getSchoolAge();
        noticeId = screeningPlanSchoolStudent.getSrcScreeningNoticeId();
        if (computerOptometry != null) {
            ComputerOptometryDO.ComputerOptometry leftData = computerOptometry.getLeftEyeData();
            ComputerOptometryDO.ComputerOptometry rightData = computerOptometry.getRightEyeData();
            leftCyl = leftData.getCyl() == null ? null : leftData.getCyl().floatValue();
            rightCyl = rightData.getCyl() == null ? null : rightData.getCyl().floatValue();
            leftSph = leftData.getSph() == null ? null : leftData.getSph().floatValue();
            rightSph = rightData.getSph() == null ? null : rightData.getSph().floatValue();
            leftAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(leftCyl);
            rightAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(rightCyl);
            isAstigmatism = StatUtil.isAstigmatism(leftAstigmatismWarningLevel) || StatUtil.isAstigmatism(rightAstigmatismWarningLevel);
            leftHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(leftSph, leftCyl, screeningPlanSchoolStudent.getStudentAge());
            rightHyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(rightSph, rightCyl, screeningPlanSchoolStudent.getStudentAge());
            leftMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(leftSph, leftCyl);
            rightMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(rightSph, rightCyl);
        }

        if (visionData != null) {
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            leftNakedVision = leftEyeData.getNakedVision() == null ? null : leftEyeData.getNakedVision().floatValue();
            leftCorrectVision = leftEyeData.getCorrectedVision() == null ? null : leftEyeData.getCorrectedVision().floatValue();
            if (leftNakedVision != null && leftCorrectVision != null) {
                leftNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(leftEyeData.getNakedVision().floatValue(), age);
            }
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
            rightNakedVision = rightEyeData.getNakedVision() == null ? null : rightEyeData.getNakedVision().floatValue();
            rightCorrectVision = rightEyeData.getCorrectedVision() == null ? null : rightEyeData.getCorrectedVision().floatValue();
            if (rightNakedVision != null && rightCorrectVision != null) {
                rightNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(rightEyeData.getNakedVision().floatValue(), age);
            }
        }
    }*/
    private void setWarningLevel() {
        if (!ObjectsUtil.allNotNull(basicData.leftAstigmatismWarningLevel,basicData.leftHyperopiaWarningLevel,basicData.rightAstigmatismWarningLevel
        ,basicData.rightHyperopiaWarningLevel,basicData.leftMyopiaWarningLevel,basicData.rightMyopiaWarningLevel
                ,basicData.leftNakedVisionWarningLevel,basicData.rightNakedVisionWarningLevel)) {
            return;
        }

        List<Integer> warningLevelList = new ArrayList() {
            {
                add(basicData.leftAstigmatismWarningLevel.code);
                add(basicData.rightAstigmatismWarningLevel.code);

                add(basicData.leftHyperopiaWarningLevel.code);
                add(basicData.rightHyperopiaWarningLevel.code);

                add(basicData.leftMyopiaWarningLevel.code);
                add(basicData.rightMyopiaWarningLevel.code);

                add(basicData.leftNakedVisionWarningLevel.code);
                add(basicData.rightNakedVisionWarningLevel.code);
            }
        };
        Integer warningLevel = Collections.max(warningLevelList);
        statConclusion.setWarningLevel(warningLevel);
    }


    private void setLowVision() {
        Boolean isLowVision = null;
        if (ObjectsUtil.allNotNull(basicData.leftNakedVisionWarningLevel, basicData.rightNakedVisionWarningLevel)) {
            isLowVision = StatUtil.isLowVision(basicData.leftNakedVisionWarningLevel)
                    || StatUtil.isLowVision(basicData.rightNakedVisionWarningLevel);
        }
        statConclusion.setIsLowVision(isLowVision);
    }

    private void setRefractiveError() {
        Boolean isRefractiveError = null;
        if (ObjectsUtil.allNotNull(basicData.isAstigmatism, statConclusion.getIsMyopia(), statConclusion.getIsHyperopia())) {
            isRefractiveError = StatUtil.isRefractiveError(basicData.isAstigmatism, statConclusion.getIsMyopia(), statConclusion.getIsHyperopia());
        }
        statConclusion.setIsRefractiveError(isRefractiveError);
    }

    private void setMyopia() {
        Boolean isMyopia = null;
        if (ObjectsUtil.allNotNull(basicData.leftMyopiaWarningLevel, basicData.rightMyopiaWarningLevel)) {
            isMyopia = StatUtil.isMyopia(basicData.leftMyopiaWarningLevel) || StatUtil.isMyopia(basicData.rightMyopiaWarningLevel);
        }
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
        if (!ObjectsUtil.allNotNull(basicData.leftNakedVision, basicData.leftSph, basicData.leftCyl, basicData.isWearingGlasses,
                basicData.leftCorrectVision, basicData.age, basicData.schoolAge, basicData.rightNakedVision, basicData.rightSph, basicData.rightCyl,
                basicData.rightCorrectVision)) {
            return;
        }
        boolean isRecommendVisit =
                StatUtil.isRecommendVisit(basicData.leftNakedVision, basicData.leftSph, basicData.leftCyl, basicData.isWearingGlasses,
                        basicData.leftCorrectVision, basicData.age, SchoolAge.get(basicData.schoolAge))
                        || StatUtil.isRecommendVisit(basicData.rightNakedVision, basicData.rightSph, basicData.rightCyl,
                        basicData.isWearingGlasses, basicData.rightCorrectVision, basicData.age, SchoolAge.get(basicData.schoolAge));
        statConclusion.setIsRecommendVisit(isRecommendVisit);
    }

    private void setRescreenErrorNum() {
        if (basicData.isRescreen) {
            // TODO: 需要与初筛数据对比获取错误项次
            int rescreenErrorNum = 4;
            statConclusion.setRescreenErrorNum(rescreenErrorNum);
        } else {
            statConclusion.setRescreenErrorNum(0);
        }
    }


    public StatConclusionBuilder setStatConclusion(StatConclusion statConclusion) {
        if (statConclusion == null) {
            isUpdate = false;
            this.statConclusion = new StatConclusion();
        } else if (statConclusion.getId() == null) {
            isUpdate = false;
        } else {
            isUpdate = true;
            this.statConclusion = statConclusion;
        }
        return this;
    }

    private void setValid() {
        if (visionScreeningResult.getVisionData() == null || visionScreeningResult.getComputerOptometry() == null) {
            statConclusion.setIsValid(false);
        }
        statConclusion.setIsValid(StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry()));
    }

    /**
     * 基础数据
     */
    @Getter
    @Setter
    static class BasicData {
        private Float leftCyl;
        private Float rightCyl;
        private Float leftSph;
        private Float rightSph;
        private Float rightNakedVision;
        private Float leftNakedVision;
        private Float leftCorrectVision;
        private Float rightCorrectVision;
        private WarningLevel leftAstigmatismWarningLevel;
        private WarningLevel rightAstigmatismWarningLevel;
        private Boolean isAstigmatism;
        private WarningLevel leftNakedVisionWarningLevel;
        private WarningLevel rightNakedVisionWarningLevel;
        private WarningLevel leftHyperopiaWarningLevel;
        private WarningLevel rightHyperopiaWarningLevel;
        private WarningLevel leftMyopiaWarningLevel;
        private WarningLevel rightMyopiaWarningLevel;
        private Boolean isRescreen;
        private Boolean isWearingGlasses;
        private Integer age;
        private Integer gender;
        private Integer schoolAge;
        private Integer noticeId;
        private Boolean isUpdate;

        private BasicData() {

        }

        public static BasicData getInstance(VisionScreeningResult visionScreeningResult, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
            BasicData basicData = new BasicData();
            basicData.isRescreen = visionScreeningResult.getIsDoubleScreen();
            ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
            VisionDataDO visionData = visionScreeningResult.getVisionData();
            basicData.isWearingGlasses = true;
            basicData.age = screeningPlanSchoolStudent.getStudentAge();
            basicData.gender = screeningPlanSchoolStudent.getGender();
            basicData.schoolAge = screeningPlanSchoolStudent.getSchoolAge();
            basicData.noticeId = screeningPlanSchoolStudent.getSrcScreeningNoticeId();
            if (computerOptometry != null) {
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

            if (visionData != null) {
                VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
                basicData.leftNakedVision = leftEyeData.getNakedVision() == null ? null : leftEyeData.getNakedVision().floatValue();
                basicData.leftCorrectVision = leftEyeData.getCorrectedVision() == null ? null : leftEyeData.getCorrectedVision().floatValue();
                if (basicData.leftNakedVision != null && basicData.leftCorrectVision != null) {
                    basicData.leftNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(leftEyeData.getNakedVision().floatValue(), basicData.age);
                }
                VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
                basicData.rightNakedVision = rightEyeData.getNakedVision() == null ? null : rightEyeData.getNakedVision().floatValue();
                basicData.rightCorrectVision = rightEyeData.getCorrectedVision() == null ? null : rightEyeData.getCorrectedVision().floatValue();
                if (basicData.rightNakedVision != null && basicData.rightCorrectVision != null) {
                    basicData.rightNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(rightEyeData.getNakedVision().floatValue(), basicData.age);
                }
            }
            return basicData;
        }
    }
}
