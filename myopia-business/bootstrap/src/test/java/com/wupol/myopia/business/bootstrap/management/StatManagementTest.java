package com.wupol.myopia.business.bootstrap.management;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.business.api.management.service.CommonDiseaseReportService;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.facade.StatConclusionCheck;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据结论和判断工具测试
 *
 * @author hang.yuan 2022/5/11 18:29
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatManagementTest {

    @Autowired
    VisionScreeningResultService visionScreeningResultService;
    @Autowired
    ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    SchoolGradeService schoolGradeService;
    @Autowired
    StatConclusionCheck statConclusionCheck;
    @Autowired
    CommonDiseaseReportService commonDiseaseReportService;


    /**
     * 获取数据
     */
    public ThreeTuple<ScreeningPlanSchoolStudent,SchoolGrade,List<VisionScreeningResult>>  getTupleResult(Integer planId, Integer planSchoolStudentId){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(VisionScreeningResult::getScreeningPlanSchoolStudentId,planSchoolStudentId);
        queryWrapper.eq(VisionScreeningResult::getPlanId,planId);
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.list(queryWrapper);
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planSchoolStudentId);
        SchoolGrade schoolGrade = schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId());
        return new ThreeTuple<>(screeningPlanSchoolStudent,schoolGrade,visionScreeningResults);

    }


    @Test
    public void statConclusionTest(){
        //筛查计划ID
        Integer planId = 232;
        //参与筛查计划的学生ID
        Integer planSchoolStudentId = 523663;

        ThreeTuple<ScreeningPlanSchoolStudent, SchoolGrade, List<VisionScreeningResult>> tupleResult = getTupleResult(planId, planSchoolStudentId);
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tupleResult.getFirst();
        SchoolGrade schoolGrade = tupleResult.getSecond();
        List<VisionScreeningResult> visionScreeningResults = tupleResult.getThird();
        if (CollectionUtil.isEmpty(visionScreeningResults) || Objects.isNull(screeningPlanSchoolStudent)){
            return;
        }
        Map<Boolean, VisionScreeningResult> visionScreeningResultMap = visionScreeningResults.stream().collect(Collectors.toMap(VisionScreeningResult::getIsDoubleScreen, Function.identity()));
        VisionScreeningResult currentVisionScreeningResult = visionScreeningResultMap.get(Boolean.FALSE);
        if (Objects.nonNull(currentVisionScreeningResult)){
            log.info("初筛=========");
            StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
            StatConclusion statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult, null)
                    .setStatConclusion(null)
                    .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                    .setGradeCode(schoolGrade.getGradeCode())
                    .setClientId("1")
                    .build();
            log.info(JSONObject.toJSONString(statConclusion,true));
        }

        VisionScreeningResult anotherVisionScreeningResult = visionScreeningResultMap.get(Boolean.TRUE);
        if (Objects.nonNull(anotherVisionScreeningResult)){
            log.info("复测=========");
            StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
            StatConclusion statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(anotherVisionScreeningResult, currentVisionScreeningResult)
                    .setStatConclusion(null)
                    .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                    .setGradeCode(schoolGrade.getGradeCode())
                    .setClientId("1")
                    .build();
            log.info(JSONObject.toJSONString(statConclusion,true));
        }

    }


    @Test
    public void check(){
        Integer planId= 232;
        StatConclusionCheck.DataCheckResult checkResult = statConclusionCheck.getCheckResult(planId, 4, false);
        log.info(JSONObject.toJSONString(checkResult,true));
    }

    @Test
    public void test(){
        Integer districtId =1665;
        Integer noticeId =null;
        commonDiseaseReportService.districtCommonDiseaseReport(districtId,noticeId);
    }
}
