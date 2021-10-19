package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Classname VisionScreeningBizService2
 * @Description 视力筛查业务
 * @Date 2021/7/15 12:01 下午
 * @Author Jacob
 * @Version
 */
@Log4j2
@Service
public class VisionScreeningBizService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @return 返回statconclusion
     */
    @Transactional(rollbackFor = Exception.class)
    public TwoTuple<VisionScreeningResult, StatConclusion> saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData) {
        TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult = getAllFirstAndSecondResult(screeningResultBasicData);
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        currentVisionScreeningResult = getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        allFirstAndSecondResult.setFirst(currentVisionScreeningResult);
        //更新vision_result表
        visionScreeningResultService.saveOrUpdateStudentScreenData(allFirstAndSecondResult.getFirst());
        //更新statConclusion表
        StatConclusion statConclusion = statConclusionService.saveOrUpdateStudentScreenData(getScreeningConclusionResult(allFirstAndSecondResult));
        //更新学生表的数据
        this.updateStudentVisionData(allFirstAndSecondResult.getFirst(),statConclusion);
        //返回最近一次的statConclusion
        TwoTuple<VisionScreeningResult, StatConclusion> visionScreeningResultStatConclusionTwoTuple = new TwoTuple<>();
        visionScreeningResultStatConclusionTwoTuple.setFirst(allFirstAndSecondResult.getFirst());
        visionScreeningResultStatConclusionTwoTuple.setSecond(statConclusion);
        return visionScreeningResultStatConclusionTwoTuple;
    }


    /**
     * 获取统计数据
     *
     * @param allFirstAndSecondResult
     * @return
     */
    private StatConclusion getScreeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult) {
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        VisionScreeningResult secondVisionScreeningResult = allFirstAndSecondResult.getSecond();
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = " + currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        }
        // 根据是否复查，查找结论表
        StatConclusion statConclusion = statConclusionService.getStatConclusion(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen());
        //需要新增
        SchoolGrade schoolGrade = schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId());
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult).setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).setGradeCode(schoolGrade.getGradeCode())
                .build();
        return statConclusion;
    }


    /**
     * 获取筛查数据
     *
     * @param screeningResultBasicData 筛查结果基本数据
     * @return VisionScreeningResult
     * @throws IOException 异常
     */
    public VisionScreeningResult getScreeningResult(ScreeningResultBasicData screeningResultBasicData, VisionScreeningResult visionScreeningResult){
        //获取VisionScreeningResult以及ScreeningPlanSchoolStudent
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getScreeningPlanSchoolStudent(screeningResultBasicData);
        //构建ScreeningResult
        return new ScreeningResultBuilder().setVisionScreeningResult(visionScreeningResult).setIsDoubleScreen(screeningResultBasicData.getIsState() == 1).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).build();
    }

    /**
     * 取出历史初筛和复筛的数据
     *
     * @param screeningResultBasicData 学生基本信息
     * @return com.wupol.myopia.business.common.utils.util.TwoTuple<com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult,com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult>
     **/
    public  TwoTuple<VisionScreeningResult, VisionScreeningResult> getAllFirstAndSecondResult(ScreeningResultBasicData screeningResultBasicData) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQueryDTO = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());
        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQueryDTO);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getScreeningResult(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), screeningResultBasicData.getStudentId());
        VisionScreeningResult currentVisionScreeningResult = null;
        VisionScreeningResult anotherVisionScreeningResult = null;
        for (VisionScreeningResult visionScreeningResult : visionScreeningResults) {
            if (visionScreeningResult.getIsDoubleScreen() == (screeningResultBasicData.getIsState() == 1)) {
                currentVisionScreeningResult = visionScreeningResult;
            } else {
                anotherVisionScreeningResult = visionScreeningResult;
            }
        }
        TwoTuple<VisionScreeningResult, VisionScreeningResult> visionScreeningResultVisionScreeningResultTwoTuple = new TwoTuple<>();
        visionScreeningResultVisionScreeningResultTwoTuple.setFirst(currentVisionScreeningResult);
        visionScreeningResultVisionScreeningResultTwoTuple.setSecond(anotherVisionScreeningResult);
        return visionScreeningResultVisionScreeningResultTwoTuple;

    }

    /**
     * 获取已有的筛查结果
     *
     * @param screeningResultBasicData 筛查结果基本数据
     * @return ScreeningPlanSchoolStudent
     * @throws IOException 异常
     */
    private ScreeningPlanSchoolStudent getScreeningPlanSchoolStudent(ScreeningResultBasicData screeningResultBasicData) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQueryDTO = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getStudentId());
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQueryDTO);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        return  screeningPlanSchoolStudent;
    }

    /**
     * 更新学生数据
     * @param visionScreeningResult
     * @param statConclusion
     */
    private void updateStudentVisionData(VisionScreeningResult visionScreeningResult, StatConclusion statConclusion) {
        //获取学生数据
        Integer studentId = visionScreeningResult.getStudentId();
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new ManagementUncheckedException("无法通过id找到student，id = " + studentId);
        }
        //填充数据
        student.setIsAstigmatism(statConclusion.getIsAstigmatism());
        student.setIsHyperopia(statConclusion.getIsHyperopia());
        student.setIsMyopia(statConclusion.getIsMyopia());
        student.setGlassesType(statConclusion.getGlassesType());
        student.setVisionLabel(statConclusion.getWarningLevel());
        student.setLastScreeningTime(visionScreeningResult.getUpdateTime());
        student.setUpdateTime(new Date());
        student.setAstigmatismLevel(statConclusion.getAstigmatismLevel());
        student.setHyperopiaLevel(statConclusion.getHyperopiaLevel());
        student.setMyopiaLevel(statConclusion.getMyopiaLevel());
        studentService.updateStudent(student);
    }

}
