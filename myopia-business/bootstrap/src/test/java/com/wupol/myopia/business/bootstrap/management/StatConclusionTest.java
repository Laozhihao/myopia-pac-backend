package com.wupol.myopia.business.bootstrap.management;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.constant.GlassesType;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.VisionCorrection;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO.ComputerOptometry;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.service.*;
import com.wupol.myopia.business.management.util.StatUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatConclusionTest {
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Autowired
    private ScreeningPlanSchoolStudentService planStudentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Test
    public void testInsert() throws IOException {
        VisionScreeningResult query = new VisionScreeningResult();
        query.setTaskId(1);
        List<VisionScreeningResult> list = visionScreeningResultService.findByList(query);
        for (VisionScreeningResult result : list) {
            ComputerOptometryDO computerOptometryDo = result.getComputerOptometry();

            VisionDataDO visionData = result.getVisionData();

            ComputerOptometry leftData = computerOptometryDo.getLeftEyeData();
            ComputerOptometry rightData = computerOptometryDo.getRightEyeData();

            int screeningPlanSchoolStudentId = result.getScreeningPlanSchoolStudentId();
            ScreeningPlanSchoolStudent planStudent =
                    planStudentService.getById(screeningPlanSchoolStudentId);

            Student student = studentService.getById(result.getStudentId());

            int schoolId = planStudent.getSchoolId();
            String schoolClassName = planStudent.getClassName();
            int gradeId = planStudent.getGradeId();
            SchoolGrade schoolGrade = schoolGradeService.getById(gradeId);
            String schoolGradeCode = schoolGrade.getGradeCode();
            int age = planStudent.getStudentAge();
            int districtId = result.getDistrictId();
            int gender = student.getGender();
            int planId = result.getPlanId();
            int taskId = result.getTaskId();
            int resultId = result.getId();
            boolean isRescreen = result.getIsDoubleScreen();
            // TODO: 需要获取noticeId
            int srcScreeningNoticeId = 1;
            // TODO: 如何获取学龄
            int schoolAge = SchoolAge.HIGH.code;
            // TODO: 需要增加合法数据判断
            boolean isValid = true;
            // TODO: 需要获取戴镜类型
            int glassesType = GlassesType.CONTACT_LENS.code;
            // TODO: 需要获取视力矫正状态
            int visionCorrection = VisionCorrection.ENOUGH_CORRECTED.code;

            boolean isWearingGlasses = GlassesType.NOT_WEARING.code == 0 ? false : true;

            float leftCyl = leftData.getCyl().floatValue();
            float rightCyl = rightData.getCyl().floatValue();

            float leftSph = leftData.getSph().floatValue();
            float rightSph = rightData.getSph().floatValue();

            WarningLevel leftAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(leftCyl);
            WarningLevel rightAstigmatismWarningLevel =
                    StatUtil.getAstigmatismWarningLevel(rightCyl);

            WarningLevel leftHyperopiaWarningLevel =
                    StatUtil.getHyperopiaWarningLevel(leftSph, leftCyl, age);
            WarningLevel rightHyperopiaWarningLevel =
                    StatUtil.getHyperopiaWarningLevel(rightSph, rightCyl, age);

            WarningLevel leftMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(leftSph, leftCyl);
            WarningLevel rightMyopiaWarningLevel =
                    StatUtil.getMyopiaWarningLevel(rightSph, rightCyl);

            Integer myopiaWarningLevel = leftMyopiaWarningLevel.code > rightMyopiaWarningLevel.code
                    ? leftMyopiaWarningLevel.code
                    : rightMyopiaWarningLevel.code;

            float leftNakedVision = visionData.getLeftEyeData().getNakedVision().floatValue();
            float rightNakedVision = visionData.getRightEyeData().getNakedVision().floatValue();

            float leftCorrectVision = visionData.getLeftEyeData().getCorrectedVision().floatValue();
            float rightCorrectVision =
                    visionData.getRightEyeData().getCorrectedVision().floatValue();

            boolean isHyperopia = StatUtil.isHyperopia(leftHyperopiaWarningLevel)
                    || StatUtil.isHyperopia(rightHyperopiaWarningLevel);
            boolean isAstigmatism = StatUtil.isAstigmatism(leftAstigmatismWarningLevel)
                    || StatUtil.isAstigmatism(rightAstigmatismWarningLevel);
            boolean isMyopia = StatUtil.isMyopia(leftMyopiaWarningLevel)
                    || StatUtil.isMyopia(rightMyopiaWarningLevel);

            WarningLevel leftNakedVisionWarningLevel =
                    StatUtil.getNakedVisionWarningLevel(leftNakedVision, age);
            WarningLevel rightNakedVisionWarningLevel =
                    StatUtil.getNakedVisionWarningLevel(rightNakedVision, age);
            Integer nakedVisionWarningLevel =
                    leftNakedVisionWarningLevel.code > rightNakedVisionWarningLevel.code
                    ? leftNakedVisionWarningLevel.code
                    : rightNakedVisionWarningLevel.code;

            boolean isLowVision = StatUtil.isLowVision(visionData.getLeftEyeData().getNakedVision().floatValue(), age)
                    || StatUtil.isLowVision(visionData.getLeftEyeData().getNakedVision().floatValue(), age);

            boolean isRefractiveError =
                    StatUtil.isRefractiveError(isAstigmatism, isMyopia, isHyperopia);

            boolean isRecommendVisit =
                    StatUtil.isRecommendVisit(leftNakedVision, leftSph, leftCyl, isWearingGlasses,
                            leftCorrectVision, age, SchoolAge.get(schoolAge))
                    || StatUtil.isRecommendVisit(rightNakedVision, rightSph, rightCyl,
                            isWearingGlasses, rightCorrectVision, age, SchoolAge.get(schoolAge));

            List<Integer> warningLevelList = new ArrayList() {
                {
                    add(leftAstigmatismWarningLevel.code);
                    add(rightAstigmatismWarningLevel.code);

                    add(leftHyperopiaWarningLevel.code);
                    add(rightHyperopiaWarningLevel.code);

                    add(leftMyopiaWarningLevel.code);
                    add(rightMyopiaWarningLevel.code);

                    add(leftNakedVisionWarningLevel.code);
                    add(rightNakedVisionWarningLevel.code);
                }
            };

            Integer warningLevel = Collections.max(warningLevelList);

            StatConclusion statConclusion = new StatConclusion();
            statConclusion.setResultId(resultId);
            statConclusion.setSrcScreeningNoticeId(srcScreeningNoticeId);
            statConclusion.setTaskId(taskId);
            statConclusion.setPlanId(planId);
            statConclusion.setDistrictId(districtId);
            statConclusion.setSchoolAge(schoolAge);
            statConclusion.setGender(gender);
            statConclusion.setWarningLevel(warningLevel);
            statConclusion.setVisionL(leftNakedVision);
            statConclusion.setVisionR(rightNakedVision);
            statConclusion.setIsLowVision(isLowVision);
            statConclusion.setIsRefractiveError(isRefractiveError);
            statConclusion.setIsMyopia(isMyopia);
            statConclusion.setIsHyperopia(isHyperopia);
            statConclusion.setIsAstigmatism(isAstigmatism);
            // statConclusion.setIsWearingGlasses(isWearingGlasses);
            statConclusion.setGlassesType(glassesType);
            statConclusion.setIsRecommendVisit(isRecommendVisit);
            statConclusion.setIsRescreen(isRescreen);
            statConclusion.setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId);
            statConclusion.setSchoolId(schoolId);
            statConclusion.setSchoolClassName(schoolClassName);
            statConclusion.setSchoolGradeCode(schoolGradeCode);
            statConclusion.setVisionCorrection(visionCorrection);
            statConclusion.setNakedVisionWarningLevel(nakedVisionWarningLevel);
            statConclusion.setMyopiaWarningLevel(myopiaWarningLevel);
            if (isRescreen) {
                // TODO: 需要与初筛数据对比获取错误项次
                int rescreenErrorNum = 4;
                statConclusion.setRescreenErrorNum(rescreenErrorNum);
            } else {
                statConclusion.setRescreenErrorNum(0);
            }
            statConclusion.setIsValid(isValid);

            // StatConclusion wrapper = new StatConclusion();
            // wrapper.setResultId(resultId);
            UpdateWrapper<StatConclusion> updateWrapper = new UpdateWrapper();
            updateWrapper.eq("result_id", resultId);
            Assert.assertTrue(statConclusionService.saveOrUpdate(statConclusion, updateWrapper));
        }
    }
}
