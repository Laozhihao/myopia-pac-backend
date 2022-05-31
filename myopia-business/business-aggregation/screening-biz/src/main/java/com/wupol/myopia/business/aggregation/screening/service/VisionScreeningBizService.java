package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
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

        // 判断对应的检查项目的更新时间是否新于当前已保存的更新时间，如果是旧的，则不保存。
        checkCanSaveScreeningResultBasicDataWithUpdateTime(screeningResultBasicData, currentAndOtherResult);

        VisionScreeningResult currentVisionScreeningResult = currentAndOtherResult.getFirst();
        // 获取了筛查计划
        currentVisionScreeningResult = getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        if (Objects.isNull(currentAndOtherResult.getFirst())) {
            currentAndOtherResult.setFirst(currentVisionScreeningResult);
        }

        ScreeningPlan screeningPlan = screeningPlanService.findOne(new ScreeningPlan().setId(currentVisionScreeningResult.getPlanId()));
        if (screeningResultBasicData.getIsState() != 0) {
            verifyScreening(currentAndOtherResult.getSecond(), screeningPlan.getScreeningType() == 1);
        }
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getScreeningPlanSchoolStudent(screeningResultBasicData);
        // 初筛数据清空未检查说明
        screeningPlanSchoolStudent.setState(0);
        screeningPlanSchoolStudentService.updateById(screeningPlanSchoolStudent);
        // 设置类型，来自筛查计划
        currentVisionScreeningResult.setScreeningType(screeningPlan.getScreeningType());
        if (Objects.isNull(currentVisionScreeningResult.getCreateUserId())) {
            currentVisionScreeningResult.setCreateUserId(CurrentUserUtil.getCurrentUser().getId());
        }
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
            throw new BusinessException("需要完成轴位检查");
        }
        if (checkHeight && Objects.isNull(firstResult.getHeightAndWeightData())) {
            throw new BusinessException("需要完成体重检查");
        }
    }

    /**
     * 校验是否能够保存检查数据，目前只通过更新时间来判断。如果提交的数据的更新时间比数据库的早，则不保存该数据
     * @param screeningResultBasicData
     * @param tuple
     * @return
     */
    private void checkCanSaveScreeningResultBasicDataWithUpdateTime(ScreeningResultBasicData screeningResultBasicData, TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple) {
        boolean isDoubleScreening = screeningResultBasicData.getIsState() == 1;
        // 如果更新时间为空，则设置成当前的时间。用于兼容旧的App的版本
        if (Objects.isNull(screeningResultBasicData.getUpdateTime()) || screeningResultBasicData.getUpdateTime() == 0) {
            screeningResultBasicData.setUpdateTime(System.currentTimeMillis());
        }
        BusinessException exception  = new BusinessException("检查数据已过期", ResultCode.DATA_UPLOAD_DATA_OUT_DATE.getCode());
        VisionScreeningResult firstResult = tuple.getFirst();
        VisionScreeningResult secondResult = tuple.getSecond();

        // 初筛
        if (!isDoubleScreening && Objects.nonNull(firstResult)) {
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_VISION) && Objects.nonNull(firstResult.getVisionData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getVisionData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_COMPUTER_OPTOMETRY) && Objects.nonNull(firstResult.getComputerOptometry())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getComputerOptometry().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_MULTI_CHECK) && Objects.nonNull(firstResult.getFundusData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getFundusData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_BIOMETRIC) && Objects.nonNull(firstResult.getBiometricData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getBiometricData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_PUPIL_OPTOMETRY) && Objects.nonNull(firstResult.getPupilOptometryData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getPupilOptometryData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_EYE_PRESSURE) && Objects.nonNull(firstResult.getEyePressureData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getEyePressureData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_OTHER_EYE_DISEASE) && Objects.nonNull(firstResult.getOtherEyeDiseases())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getOtherEyeDiseases().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_HEIGHT_WEIGHT) && Objects.nonNull(firstResult.getHeightAndWeightData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getHeightAndWeightData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_DEVIATION) && Objects.nonNull(firstResult.getDeviationData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getDeviationData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_SAPRODONTIA) && Objects.nonNull(firstResult.getSaprodontiaData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getSaprodontiaData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_SPINE) && Objects.nonNull(firstResult.getSpineData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getSpineData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_BLOOD_PRESSURE) && Objects.nonNull(firstResult.getBloodPressureData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getBloodPressureData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_DISEASES_HISTORY) && Objects.nonNull(firstResult.getDiseasesHistoryData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getDiseasesHistoryData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_PRIVACY) && Objects.nonNull(firstResult.getPrivacyData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(firstResult.getPrivacyData().getUpdateTime())) throw exception;
            }
        } else if (Objects.nonNull(secondResult)){
            // 复测
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_VISION) && Objects.nonNull(secondResult.getVisionData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(secondResult.getVisionData().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_COMPUTER_OPTOMETRY) && Objects.nonNull(secondResult.getComputerOptometry())) {
                if (!screeningResultBasicData.isNewerUpdateTime(secondResult.getComputerOptometry().getUpdateTime())) throw exception;
            }
            if (screeningResultBasicData.getDataType().equals(ScreeningConstant.SCREENING_DATA_TYPE_HEIGHT_WEIGHT) && Objects.nonNull(secondResult.getHeightAndWeightData())) {
                if (!screeningResultBasicData.isNewerUpdateTime(secondResult.getHeightAndWeightData().getUpdateTime())) throw exception;
            }
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
        String schoolGradeCode = statConclusion.getSchoolGradeCode();
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
        if (!Objects.equals(SchoolAge.KINDERGARTEN.code,gradeCodeEnum.getType())){
            //小学及以上的数据同步
            student.setMyopiaLevel(statConclusion.getMyopiaLevel());
            student.setScreeningMyopia(statConclusion.getScreeningMyopia());
            if (Objects.nonNull(statConclusion.getIsLowVision()) && statConclusion.getIsLowVision()) {
                student.setLowVision(LowVisionLevelEnum.LOW_VISION.code);
            }else {
                student.setLowVision(null);
            }
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
