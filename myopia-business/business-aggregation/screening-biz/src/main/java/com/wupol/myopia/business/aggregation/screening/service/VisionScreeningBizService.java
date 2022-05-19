package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.GlassesTypeEnum;
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
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
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
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @param clientId                 客户端ID
     * @return 返回statconclusion
     */
    @Transactional(rollbackFor = Exception.class)
    public TwoTuple<VisionScreeningResult, StatConclusion> saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData, String clientId) {
        // 1: 根据筛查计划获得 初筛和复测数据
        // 2: 本次检查数据如果是复测，要验证是否符合初筛条件
        TwoTuple<VisionScreeningResult, VisionScreeningResult> currentAndOtherResult = getAllFirstAndSecondResult(screeningResultBasicData);
        VisionScreeningResult currentVisionScreeningResult = currentAndOtherResult.getFirst();
        // 获取了筛查计划
        currentVisionScreeningResult = getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        if (Objects.isNull(currentAndOtherResult.getFirst())) {
            currentAndOtherResult.setFirst(currentVisionScreeningResult);
        }

        ScreeningPlan screeningPlan = screeningPlanService.findOne(new ScreeningPlan().setId(currentVisionScreeningResult.getPlanId()));
        if (screeningResultBasicData.getIsState() != 0) {
            verifyScreening(currentAndOtherResult.getFirst(), screeningPlan.getScreeningType() == 1);
        }
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getScreeningPlanSchoolStudent(screeningResultBasicData);
        // 初筛数据清空未检查说明
        screeningPlanSchoolStudent.setState(0);
        screeningPlanSchoolStudentService.updateById(screeningPlanSchoolStudent);
        // 设置类型，来自筛查计划
        currentVisionScreeningResult.setScreeningType(screeningPlan.getScreeningType());
        //更新statConclusion表
        visionScreeningResultService.saveOrUpdateStudentScreenData(currentVisionScreeningResult);
        //更新statConclusion表（获取的初筛或复测的数据）
        StatConclusion statConclusion = statConclusionService.saveOrUpdateStudentScreenData(getScreeningConclusionResult(currentAndOtherResult, clientId));
        // 更新是否绑定手机号码
        setIsBindMq(statConclusion);
        //更新学生表的数据（复测覆盖了初筛的结论）
        this.updateStudentVisionData(currentVisionScreeningResult, statConclusion);
        updateSchoolStudent(statConclusion, currentVisionScreeningResult.getUpdateTime());
        //返回最近一次的statConclusion
        TwoTuple<VisionScreeningResult, StatConclusion> visionScreeningResultStatConclusionTwoTuple = new TwoTuple<>();
        visionScreeningResultStatConclusionTwoTuple.setFirst(currentVisionScreeningResult);
        visionScreeningResultStatConclusionTwoTuple.setSecond(statConclusion);
        return visionScreeningResultStatConclusionTwoTuple;
    }


    /**
     * 验证复测规则
     *
     * @param firstResult 第一次筛查结果
     * @param checkHeight 是否验证身高体重
     * @return
     */
    public void verifyScreening(VisionScreeningResult firstResult, boolean checkHeight) {
        VisionDataDO visionData = firstResult.getVisionData();
        ComputerOptometryDO computerOptometry = firstResult.getComputerOptometry();
        if (Objects.isNull(visionData) || Objects.isNull(computerOptometry)) {
            throw new BusinessException("该学生初筛项目未全部完成，无法进行复测！");
        }
        // 夜戴角膜镜不需要复测
        if (visionData.getLeftEyeData().getGlassesType().equals(GlassesTypeEnum.ORTHOKERATOLOGY.code)
                || visionData.getRightEyeData().getGlassesType().equals(GlassesTypeEnum.ORTHOKERATOLOGY.code)) {
            throw new BusinessException("夜戴角膜镜不需要复测");
        }
        // 裸眼视力
        if (Objects.isNull(firstResult.getVisionData()) ||
                Objects.isNull(visionData.getLeftEyeData()) ||
                Objects.isNull(visionData.getLeftEyeData().getNakedVision()) ||
                Objects.isNull(visionData.getRightEyeData()) ||
                Objects.isNull(visionData.getRightEyeData().getNakedVision())

        ) {
            throw new BusinessException("需要完成裸眼视力检查");
        }
        // 检查矫正视力
        if (!GlassesTypeEnum.NOT_WEARING.code.equals(visionData.getLeftEyeData().getGlassesType())
                && (Objects.isNull(visionData.getLeftEyeData().getCorrectedVision()) ||
                Objects.isNull(visionData.getRightEyeData().getCorrectedVision()))) {
            throw new BusinessException("需要完成矫正视力检查");
        }
        // 球镜 柱镜 轴位
        // 球镜
        if ((Objects.isNull(computerOptometry.getLeftEyeData()) || Objects.isNull(computerOptometry.getRightEyeData()))
                || (Objects.isNull(computerOptometry.getLeftEyeData().getSph()) && Objects.isNull(computerOptometry.getRightEyeData().getSph()))) {
            throw new BusinessException("需要完成球镜检查");
        }
        if (Objects.isNull(computerOptometry.getLeftEyeData().getCyl()) && Objects.isNull(computerOptometry.getRightEyeData().getCyl())) {
            throw new BusinessException("需要完成柱镜检查");
        }
        if (Objects.isNull(computerOptometry.getLeftEyeData().getAxial()) && Objects.isNull(computerOptometry.getRightEyeData().getAxial())) {
            throw new BusinessException("需要完成柱镜检查");
        }
        if (checkHeight && Objects.isNull(firstResult.getHeightAndWeightData())) {
            throw new BusinessException("需要完成体重检查");
        }
    }

    /**
     * 获取统计数据
     *
     * @param allFirstAndSecondResult
     * @return
     */
    private StatConclusion getScreeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult, String clientId) {
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
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult, secondVisionScreeningResult).setStatConclusion(statConclusion)
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
    public VisionScreeningResult getScreeningResult(ScreeningResultBasicData screeningResultBasicData, VisionScreeningResult visionScreeningResult) {
        //获取VisionScreeningResult以及ScreeningPlanSchoolStudent
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getScreeningPlanSchoolStudent(screeningResultBasicData);
        //构建ScreeningResult
        return new ScreeningResultBuilder().setVisionScreeningResult(visionScreeningResult).setIsDoubleScreen(screeningResultBasicData.getIsState() == 1).setScreeningResultBasicData(screeningResultBasicData).setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).build();
    }


    /**
     * 取出历史初筛和复筛的数据
     *
     * @param screeningResultBasicData 学生基本信息
     * @return com.wupol.myopia.business.common.utils.util.TwoTuple<com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult, com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult>
     **/
    public TwoTuple<VisionScreeningResult, VisionScreeningResult> getAllFirstAndSecondResult(ScreeningResultBasicData screeningResultBasicData) {
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
        ScreeningPlanSchoolStudent screeningPlanSchoolStudentQueryDTO = new ScreeningPlanSchoolStudent().setScreeningOrgId(screeningResultBasicData.getDeptId()).setId(screeningResultBasicData.getPlanStudentId());
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(screeningPlanSchoolStudentQueryDTO);
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("无法找到screeningPlanSchoolStudent");
        }
        // 获取已经存在的数据
        return screeningPlanSchoolStudent;
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
        if (statConclusion.getAge() >= 6) {
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
        schoolStudents.forEach(schoolStudent -> {
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
        int resultCount = visionScreeningResultMapper.selectScreeningResultByDistrictIdAndTaskId(districtIds, taskIds);
        return resultCount;
    }
}
