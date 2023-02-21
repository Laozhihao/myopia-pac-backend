package com.wupol.myopia.business.aggregation.screening.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.aggregation.student.domain.builder.StudentInfoBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.NationalDataDownloadStatusEnum;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningResultBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Resource
    private S3Utils s3Utils;
    @Resource
    private NationalDataDownloadRecordService nationalDataDownloadRecordService;
    @Resource
    private NoticeService noticeService;

    private static final String UNDONE_MSG = "该学生初筛项目未全部完成，无法进行复测！";

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @return 返回statconclusion
     */
    @Transactional(rollbackFor = Exception.class)
    public TwoTuple<VisionScreeningResult, StatConclusion> saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData) {
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
        Assert.isTrue(CommonConst.STATUS_RELEASE.equals(screeningPlan.getReleaseStatus()), "保存失败，筛查计划已作废！");
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
        StatConclusion statConclusion = statConclusionService.saveOrUpdateStudentScreenData(getScreeningConclusionResult(currentAndOtherResult));
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
            throw new BusinessException(UNDONE_MSG);
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
            if (!screeningResultBasicData.isNewerUpdateTime(screeningResultBasicData.getDataType(), firstResult)) {
                throw exception;
            }
        } else if (Objects.nonNull(secondResult)){
            // 复测
            if (!screeningResultBasicData.isNewerUpdateTime(screeningResultBasicData.getDataType(), secondResult)) {
                throw exception;
            }
        }

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
        SchoolClass schoolClass = schoolClassService.getById(screeningPlanSchoolStudent.getClassId());
        //需要新增
        SchoolGrade schoolGrade = schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId());
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult, secondVisionScreeningResult).setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .setGradeCode(schoolGrade.getGradeCode())
                .setSchoolClass(schoolClass)
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
            if (Objects.equals(visionScreeningResult.getIsDoubleScreen(),(screeningResultBasicData.getIsState() == 1))) {
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
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentVisionData(VisionScreeningResult visionScreeningResult, StatConclusion statConclusion) {
        //获取学生数据
        Integer studentId = visionScreeningResult.getStudentId();
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new ManagementUncheckedException("无法通过id找到student，id = " + studentId);
        }
        //填充数据
        StudentInfoBuilder.setStudentInfoByStatConclusion(student,statConclusion,visionScreeningResult.getUpdateTime());
        studentService.updateScreenStudent(student);
    }

    /**
     * 更新学校学生
     *
     * @param statConclusion    结论
     * @param lastScreeningTime 上次筛查时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSchoolStudent(StatConclusion statConclusion, Date lastScreeningTime) {
        SchoolStudent schoolStudent = schoolStudentService.getByStudentIdAndSchoolId(statConclusion.getStudentId(),statConclusion.getSchoolId(),CommonConst.STATUS_NOT_DELETED);
        if (Objects.isNull(schoolStudent)) {
            return;
        }
        SchoolStudentInfoBuilder.setSchoolStudentInfoByStatConclusion(schoolStudent,statConclusion,lastScreeningTime);
        schoolStudentService.updateById(schoolStudent);
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
     * 验证复测规则
     * 托幼机构：视力检查、眼位
     * 中小学生：视力检查、眼位、裂隙灯、电脑验光
     *
     * @param result         第一次筛查结果
     * @param isKindergarten 是否幼儿园
     */
    public void verifyHaiNanScreening(VisionScreeningResult result, boolean isKindergarten) {

        // 视力检查
        VisionDataDO visionData = result.getVisionData();
        // 眼位
        OcularInspectionDataDO ocularInspectionData = result.getOcularInspectionData();
        // 裂隙灯
        SlitLampDataDO slitLampData = result.getSlitLampData();
        // 电脑验光
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        // 生物测量
        BiometricDataDO biometricData = result.getBiometricData();
        // 小瞳验光
        PupilOptometryDataDO pupilOptometryData = result.getPupilOptometryData();
        // 眼压
        EyePressureDataDO eyePressureData = result.getEyePressureData();
        // 眼底
        FundusDataDO fundusData = result.getFundusData();

        if (isKindergarten) {
            if (Objects.isNull(visionData) || Objects.isNull(ocularInspectionData)) {
                throw new BusinessException(UNDONE_MSG);
            }
            if ((!visionData.isNormal() || !ocularInspectionData.isNormal()) &&
                    (Objects.isNull(slitLampData) || Objects.isNull(pupilOptometryData)
                            || Objects.isNull(fundusData))) {
                throw new BusinessException(UNDONE_MSG);
            }
            return;
        }
        if (Objects.isNull(visionData) || Objects.isNull(ocularInspectionData)
                || Objects.isNull(slitLampData) || Objects.isNull(computerOptometry)) {
            throw new BusinessException(UNDONE_MSG);
        }

        // 视力不正常
        if ((!visionData.isNormal() || !ocularInspectionData.isNormal()
                || !slitLampData.isNormal() || !computerOptometry.isNormal())
                && (Objects.isNull(biometricData) || Objects.isNull(pupilOptometryData)
                || Objects.isNull(eyePressureData) || Objects.isNull(fundusData))) {
            throw new BusinessException(UNDONE_MSG);
        }
    }

    /**
     * 生成Excel文件
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dealDataSubmit(List<Map<Integer, String>> listMap, NationalDataDownloadRecord nationalDataDownloadRecord, Integer userId, Integer schoolId,Integer screeningPlanId) throws IOException, UtilException {

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        Map<String, VisionScreeningResult> screeningData;
        if (Objects.isNull(screeningPlanId)) {
            screeningData = getScreeningData(listMap, schoolId);
        }else {
            screeningData = getScreeningData(listMap, schoolId,screeningPlanId);
        }
        Map<String, VisionScreeningResult> finalScreeningData = screeningData;
        List<DataSubmitExportDTO> collect = listMap.stream().map(s -> {
            DataSubmitExportDTO exportDTO = new DataSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, finalScreeningData, s, exportDTO);
            return exportDTO;
        }).collect(Collectors.toList());
        File excel = ExcelUtil.exportListToExcel(CommonConst.FILE_NAME, collect, DataSubmitExportDTO.class);
        Integer fileId = s3Utils.uploadFileToS3(excel);
        nationalDataDownloadRecord.setSuccessMatch(success.get());
        nationalDataDownloadRecord.setFailMatch(fail.get());
        nationalDataDownloadRecord.setFileId(fileId);
        nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.SUCCESS.getType());
        nationalDataDownloadRecordService.updateById(nationalDataDownloadRecord);
        noticeService.createExportNotice(userId, userId, CommonConst.SUCCESS, CommonConst.SUCCESS, fileId, CommonConst.NOTICE_STATION_LETTER);
    }



    /**
     * 通过学号获取筛查信息
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId) {
        List<String> snoList = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        List<Student> studentList = studentService.getLastBySno(snoList, schoolId);
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(studentList.stream().map(Student::getId).collect(Collectors.toList()), schoolId);
        return studentList.stream().filter(ListUtil.distinctByKey(Student::getSno))
                .filter(s -> StringUtils.isNotBlank(s.getSno()))
                .collect(Collectors.toMap(Student::getSno, s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }

    /**
     *
     * 通过学号在筛查计划中获取筛查数据
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId) {
        List<String> snoList = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        // 筛查计划中学生数据查询
        List<PlanStudentInfoDTO> studentList = screeningPlanSchoolStudentService.findStudentBySchoolIdAndScreeningPlanIdAndSno(schoolId,screeningPlanId,snoList);
        // 根据学生id查询筛查信息
        List<VisionScreeningResult> resultList = visionScreeningResultService.getFirstByPlanStudentIds(studentList.stream().map(PlanStudentInfoDTO::getId).collect(Collectors.toList()));
        Map<Integer, VisionScreeningResult> resultMap = resultList.stream()
                .filter(s -> Objects.equals(s.getScreeningType(), ScreeningTypeEnum.VISION.getType()))
                .filter(s -> Objects.equals(s.getSchoolId(), schoolId))
                .filter(s -> Objects.equals(s.getPlanId(), screeningPlanId))
                .collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId,
                        Function.identity(),
                        (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));

        return studentList.stream().filter(ListUtil.distinctByKey(PlanStudentInfoDTO::getSno))
                .filter(s -> StringUtils.isNotBlank(s.getSno()))
                .collect(Collectors.toMap(PlanStudentInfoDTO::getSno, s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }

    /**
     * 获取原始数据
     */
    private void getOriginalInfo(Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        exportDTO.setGradeCode(s.get(0));
        exportDTO.setClassCode(s.get(1));
        exportDTO.setClassName(s.get(2));
        exportDTO.setStudentNo(s.get(3));
        exportDTO.setNation(s.get(4));
        exportDTO.setName(s.get(5));
        exportDTO.setGender(s.get(6));
        exportDTO.setBirthday(s.get(7));
        exportDTO.setAddress(s.get(8));
    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(s.get(3));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setRightNakedVision(getNakedVision(EyeDataUtil.rightNakedVision(result)));
            exportDTO.setLeftNakedVision(getNakedVision(EyeDataUtil.leftNakedVision(result)));
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            BigDecimal rightAxial = EyeDataUtil.rightAxial(result);
            exportDTO.setRightAxial(Objects.isNull(rightAxial) ? "" : rightAxial.toString());
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
            BigDecimal leftAxial = EyeDataUtil.leftAxial(result);
            exportDTO.setLeftAxial(Objects.isNull(leftAxial) ? "" : leftAxial.toString());
            exportDTO.setIsOk(Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.code) ? "是" : "否");
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }

    /**
     * 处理裸眼视力
     *
     * @param nakedVision 裸眼视力
     *
     * @return 裸眼视力
     */
    private String getNakedVision(BigDecimal nakedVision) {
        if (Objects.isNull(nakedVision)) {
            return StringUtils.EMPTY;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "3.0")) {
            return "9.0";
        }
        return nakedVision.setScale(1, RoundingMode.DOWN).toString();
    }

}
