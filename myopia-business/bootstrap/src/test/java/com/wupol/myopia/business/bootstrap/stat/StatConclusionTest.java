package com.wupol.myopia.business.bootstrap.stat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO.ComputerOptometry;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO.VisionData;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import com.wupol.myopia.business.management.util.StatUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatConclusionTest {
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Autowired
    private ScreeningPlanSchoolStudentService planStudentService;

    @Autowired
    private StudentService studentService;

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

            ScreeningPlanSchoolStudent planStudent =
                    planStudentService.getById(result.getScreeningPlanSchoolStudentId());

            Student student = studentService.getById(result.getStudentId());

            int age = planStudent.getStudentAge();
            // TODO: 如何获取学龄
            int schoolAge = 0;
            int districtId = result.getDistrictId();
            int gender = student.getGender();

            float leftCyl = leftData.getCyl().floatValue();
            float rightCyl = rightData.getCyl().floatValue();

            float leftSph = leftData.getSph().floatValue();
            float rightSph = rightData.getSph().floatValue();

            int leftAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(leftCyl);
            int rightAstigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(rightCyl);

            int leftHyperopiaWarningLevel =
                    StatUtil.getHyperopiaWarningLevel(leftSph, leftCyl, age);
            int rightHyperopiaWarningLevel =
                    StatUtil.getHyperopiaWarningLevel(rightSph, rightCyl, age);

            int leftMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(leftSph, leftCyl);
            int rightMyopiaWarningLevel = StatUtil.getMyopiaWarningLevel(rightSph, rightCyl);

            float leftNakedVision = visionData.getLeftEyeData().getNakedVision().floatValue();
            float rightNakedVision = visionData.getRightEyeData().getNakedVision().floatValue();

            float leftCorrectVision = visionData.getLeftEyeData().getCorrectedVision().floatValue();
            float rightCorrectVision =
                    visionData.getRightEyeData().getCorrectedVision().floatValue();

            boolean isHyperopia = StatUtil.isHyperopia(WarningLevel.get(leftHyperopiaWarningLevel))
                    || StatUtil.isHyperopia(WarningLevel.get(rightHyperopiaWarningLevel));
            boolean isAstigmatism =
                    StatUtil.isAstigmatism(WarningLevel.get(leftAstigmatismWarningLevel))
                    || StatUtil.isAstigmatism(WarningLevel.get(rightAstigmatismWarningLevel));
            boolean isMyopia = StatUtil.isMyopia(WarningLevel.get(leftMyopiaWarningLevel))
                    || StatUtil.isMyopia(WarningLevel.get(rightMyopiaWarningLevel));

            int leftNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(
                    visionData.getLeftEyeData().getNakedVision().floatValue(), age);
            int rightNakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(
                    visionData.getLeftEyeData().getNakedVision().floatValue(), age);

            boolean isLowVision =
                    StatUtil.isLowVision(WarningLevel.get(leftNakedVisionWarningLevel))
                    || StatUtil.isLowVision(WarningLevel.get(rightNakedVisionWarningLevel));

            boolean isRefractiveError =
                    StatUtil.isRefractiveError(isAstigmatism, isMyopia, isHyperopia);

            boolean isRecommendVisit = StatUtil.isRecommendVisit(leftNakedVision, leftSph, leftCyl,
                                               leftCorrectVision, SchoolAge.get(schoolAge))
                    || StatUtil.isRecommendVisit(rightNakedVision, rightSph, rightCyl,
                               rightCorrectVision, SchoolAge.get(schoolAge));

            List<Integer> warningLevelList = new ArrayList() {
                {
                    add(leftAstigmatismWarningLevel);
                    add(rightAstigmatismWarningLevel);

                    add(leftHyperopiaWarningLevel);
                    add(rightHyperopiaWarningLevel);

                    add(leftMyopiaWarningLevel);
                    add(rightMyopiaWarningLevel);

                    add(leftNakedVisionWarningLevel);
                    add(rightNakedVisionWarningLevel);
                }
            };

            Integer warningLevel = Collections.max(warningLevelList);

            StatConclusion statConclusion = new StatConclusion();
            statConclusion.setIsHyperopia(isHyperopia);
            statConclusion.setIsAstigmatism(isAstigmatism);
            statConclusion.setIsMyopia(isMyopia);
            statConclusion.setIsLowVision(isLowVision);
            statConclusion.setIsRefractiveError(isRefractiveError);
            statConclusion.setIsRecommendVisit(isRecommendVisit);
            // statConclusion.setIsRescreen(isRescreen);
            // statConclusion.setIsValid(isValid);
            // statConclusion.setIsWearingGlasses(isWearingGlasses);
            // statConclusion.setPlanId(planId);
            // statConclusion.setTaskId(taskId);
            // statConclusion.setSrcScreeningNoticeId(srcScreeningNoticeId);
            statConclusion.setDistrictId(districtId);
            statConclusion.setGender(gender);
            statConclusion.setVisionL(leftNakedVision);
            statConclusion.setVisionR(rightNakedVision);
            statConclusion.setWarningLevel(warningLevel);
        }
        Assert.assertTrue(list != null);
    }
}
