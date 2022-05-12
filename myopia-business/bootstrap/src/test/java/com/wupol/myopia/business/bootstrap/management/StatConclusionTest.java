package com.wupol.myopia.business.bootstrap.management;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
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
    public void testInsert() {
        VisionScreeningResult query = new VisionScreeningResult();
        query.setTaskId(1);
        List<VisionScreeningResult> list = visionScreeningResultService.findByList(query);
        for (VisionScreeningResult result : list) {
            ComputerOptometryDO computerOptometryDo = result.getComputerOptometry();

            VisionDataDO visionData = result.getVisionData();

            ComputerOptometryDO.ComputerOptometry leftData = computerOptometryDo.getLeftEyeData();
            ComputerOptometryDO.ComputerOptometry rightData = computerOptometryDo.getRightEyeData();

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
            int glassesType = GlassesTypeEnum.CONTACT_LENS.code;
            // TODO: 需要获取视力矫正状态
            int visionCorrection = VisionCorrection.ENOUGH_CORRECTED.code;

            boolean isWearingGlasses = GlassesTypeEnum.NOT_WEARING.code == 0 ? false : true;

            BigDecimal leftCyl = leftData.getCyl();
            BigDecimal rightCyl = rightData.getCyl();

            BigDecimal leftSph = leftData.getSph();
            BigDecimal rightSph = rightData.getSph();

            AstigmatismLevelEnum leftAstigmatismWarningLevel = StatUtil.getAstigmatismLevel(leftCyl);
            AstigmatismLevelEnum rightAstigmatismWarningLevel = StatUtil.getAstigmatismLevel(rightCyl);

            HyperopiaLevelEnum leftHyperopiaWarningLevel =
                    StatUtil.getHyperopiaLevel(leftSph, leftCyl, age);
            HyperopiaLevelEnum rightHyperopiaWarningLevel =
                    StatUtil.getHyperopiaLevel(rightSph, rightCyl, age);

            BigDecimal leftNakedVision = visionData.getLeftEyeData().getNakedVision();
            BigDecimal rightNakedVision = visionData.getRightEyeData().getNakedVision();
            MyopiaLevelEnum leftMyopiaWarningLevel = StatUtil.getMyopiaLevel(leftSph, leftCyl, age, leftNakedVision);
            MyopiaLevelEnum rightMyopiaWarningLevel = StatUtil.getMyopiaLevel(rightSph, rightCyl, age, rightNakedVision);

            Integer myopiaWarningLevel = leftMyopiaWarningLevel.code > rightMyopiaWarningLevel.code
                    ? leftMyopiaWarningLevel.code
                    : rightMyopiaWarningLevel.code;

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
                    StatUtil.nakedVision(leftNakedVision, age);
            WarningLevel rightNakedVisionWarningLevel =
                    StatUtil.nakedVision(rightNakedVision, age);
            Integer nakedVisionWarningLevel =
                    leftNakedVisionWarningLevel.code > rightNakedVisionWarningLevel.code
                    ? leftNakedVisionWarningLevel.code
                    : rightNakedVisionWarningLevel.code;

            boolean isLowVision = StatUtil.isLowVision(visionData.getLeftEyeData().getNakedVision().floatValue(), age)
                    || StatUtil.isLowVision(visionData.getLeftEyeData().getNakedVision().floatValue(), age);

            boolean leftRefractiveError = StatUtil.isRefractiveError(leftSph, leftCyl, age,false);
            boolean rightRefractiveError = StatUtil.isRefractiveError(rightSph, rightCyl, age,false);
            boolean isRefractiveError = leftRefractiveError || rightRefractiveError;
            boolean isRecommendVisit = false;

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
            UpdateWrapper<StatConclusion> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("result_id", resultId);
            Assert.assertTrue(statConclusionService.saveOrUpdate(statConclusion, updateWrapper));
        }
    }
}
