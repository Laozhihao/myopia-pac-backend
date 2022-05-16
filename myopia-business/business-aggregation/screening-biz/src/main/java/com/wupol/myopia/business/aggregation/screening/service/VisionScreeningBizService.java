package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    @Autowired
    private SchoolStudentService schoolStudentService;

    @Resource
    private VisionScreeningResultMapper visionScreeningResultMapper;

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @param clientId 客户端ID
     * @return 返回statconclusion
     */
    @Transactional(rollbackFor = Exception.class)
    public TwoTuple<VisionScreeningResult, StatConclusion> saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData,String clientId) {
        TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult = getAllFirstAndSecondResult(screeningResultBasicData);
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        currentVisionScreeningResult = getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        allFirstAndSecondResult.setFirst(currentVisionScreeningResult);
        //更新vision_result表
        visionScreeningResultService.saveOrUpdateStudentScreenData(allFirstAndSecondResult.getFirst());
        //更新statConclusion表
        StatConclusion statConclusion = statConclusionService.saveOrUpdateStudentScreenData(getScreeningConclusionResult(allFirstAndSecondResult,clientId));
        // 更新是否绑定手机号码
        setIsBindMq(statConclusion);
        //更新学生表的数据
        this.updateStudentVisionData(allFirstAndSecondResult.getFirst(),statConclusion);
        updateSchoolStudent(statConclusion,allFirstAndSecondResult.getFirst().getUpdateTime());
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
    private StatConclusion getScreeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult,String clientId) {
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
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .setGradeCode(schoolGrade.getGradeCode())
                .setClientId(clientId)
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
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQueryDTO = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getPlanStudentId());
        //倒叙取出来最新的一条
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQueryDTO);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getScreeningResult(screeningPlanSchoolStudent.getScreeningPlanId(), screeningPlanSchoolStudent.getScreeningOrgId(), screeningResultBasicData.getPlanStudentId());
        VisionScreeningResult currentVisionScreeningResult = null;
        VisionScreeningResult anotherVisionScreeningResult = null;
        for (VisionScreeningResult visionScreeningResult : visionScreeningResults) {
            if (visionScreeningResult.getIsDoubleScreen().equals(screeningResultBasicData.getIsState() == 1)) {
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
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQueryDTO = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getPlanStudentId());
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQueryDTO);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        return  screeningPlanSchoolStudent;
    }

    /**
     * 更新学生数据
     *
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
        if (statConclusion.getAge() >= 6){
            student.setMyopiaLevel(statConclusion.getMyopiaLevel());
        }
        studentService.updateScreenStudent(student);
    }

    /**
     * 更新学校学生
     *
     * @param statConclusion    结论
     * @param lastScreeningTime 上次筛查时间
     */
    private void updateSchoolStudent(StatConclusion statConclusion, Date lastScreeningTime) {
        List<SchoolStudent> schoolStudents = schoolStudentService.getByStudentId(statConclusion.getStudentId());
        if (CollectionUtils.isEmpty(schoolStudents)) {
            return;
        }
        schoolStudents.forEach(schoolStudent->{
            schoolStudent.setGlassesType(statConclusion.getGlassesType());
            schoolStudent.setLastScreeningTime(lastScreeningTime);
            schoolStudent.setVisionLabel(statConclusion.getWarningLevel());
            schoolStudent.setMyopiaLevel(statConclusion.getMyopiaLevel());
            schoolStudent.setHyperopiaLevel(statConclusion.getHyperopiaLevel());
            schoolStudent.setAstigmatismLevel(statConclusion.getAstigmatismLevel());
            schoolStudent.setUpdateTime(new Date());
        });
        schoolStudentService.updateBatchById(schoolStudents);
    }

    /**
     * 是否绑定公众号
     *
     * @param statConclusion 结论
     */
    private void setIsBindMq(StatConclusion statConclusion) {
        Student student = studentService.getById(statConclusion.getStudentId());
        statConclusion.setIsBindMp(Objects.isNull(student) ? Boolean.FALSE : StringUtils.isNotBlank(student.getMpParentPhone()));
    }

    /**
     * 获取筛查区域
     *
     * @param districtIds 行政区域ids
     */
    public int getScreeningResult(List<Integer> districtIds, List<Integer> taskIds) {
        int resultCount  = visionScreeningResultMapper.selectScreeningResultByDistrictIdAndTaskId(districtIds,taskIds);
        return resultCount;
    }
}
