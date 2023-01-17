package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.aggregation.student.service.SchoolStudentFacade;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.SchoolStudentDTO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.ReportConclusion;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentListVO;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.facade.SchoolScreeningBizFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 学生
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class StudentBizService {

    @Resource
    private StudentService studentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private VistelToolsService vistelToolsService;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Autowired
    private ResourceFileService resourceFileService;

    @Autowired
    private MedicalReportBizService medicalReportBizService;

    @Autowired
    private StudentFacade studentFacade;

    @Autowired
    private SchoolStudentService schoolStudentService;
    @Autowired
    private SchoolStudentFacade schoolStudentFacade;
    @Autowired
    private SchoolStudentExcelImportService schoolStudentExcelImportService;
    @Autowired
    private SchoolScreeningBizFacade schoolScreeningBizFacade;

    @Autowired
    private UserQuestionRecordService userQuestionRecordService;

    /**
     * 获取学生列表
     *
     * @param pageRequest     分页
     * @param studentQueryDTO 请求体
     * @return IPage<Student> {@link IPage}
     */
    public IPage<StudentDTO> getStudentLists(PageRequest pageRequest, StudentQueryDTO studentQueryDTO) {

        TwoTuple<List<Integer>, List<Integer>> conditionalFilter = studentService.conditionalFilter(
                studentQueryDTO.getGradeIds(), studentQueryDTO.getVisionLabels());

        IPage<StudentDTO> pageStudents = studentService.getStudentListByCondition(pageRequest,
                studentQueryDTO, conditionalFilter);
        List<StudentDTO> students = pageStudents.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(students)) {
            return pageStudents;
        }
        // 获取学生ID
        List<Integer> studentIds = students.stream().map(Student::getId).distinct().collect(Collectors.toList());

        // 筛查次数
        Map<Integer, Integer> countMap = visionScreeningResultService.countScreeningTimeMap(studentIds);

        // 获取就诊记录
        Map<Integer, List<ReportAndRecordDO>> visitMap = medicalReportService.getMapByStudentIds(studentIds);

        // 获取筛查记录
        List<ScreeningPlanSchoolStudent> plans = screeningPlanSchoolStudentService.getByStudentIds(studentIds);
        Map<Integer, List<ScreeningPlanSchoolStudent>> studentPlanMap = plans.stream()
                .collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getStudentId));

        // 问卷记录
        Map<Integer, Long> questionRecordMap = userQuestionRecordService.studentRecordCount(studentIds);

        // 封装DTO
        for (StudentDTO student : students) {
            SchoolStudentInfoBuilder.setStudentInfo(countMap,visitMap,studentPlanMap, questionRecordMap, student);
        }

        return pageStudents;
    }

    /**
     * 获取学校学生列表
     *
     * @param pageRequest     分页
     * @param studentQueryDTO 请求体
     * @return IPage<Student> {@link IPage}
     */
    public IPage<SchoolStudentListVO> getSchoolStudentList(PageRequest pageRequest, SchoolStudentQueryDTO studentQueryDTO) {
        Assert.notNull(studentQueryDTO.getSchoolId(),"学校ID不能为空");

        TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove = schoolStudentFacade.kindergartenAndPrimaryAbove(studentQueryDTO.getSchoolId());
        SchoolStudentQueryBO schoolStudentQueryBO = SchoolStudentInfoBuilder.builderSchoolStudentQueryBO(studentQueryDTO,kindergartenAndPrimaryAbove);

        IPage<SchoolStudent> schoolStudentPage  = schoolStudentService.listByCondition(pageRequest,schoolStudentQueryBO);

        IPage<SchoolStudentListVO> studentDTOPage = new Page<>(schoolStudentPage.getCurrent(),schoolStudentPage.getSize(),schoolStudentPage.getTotal());

        List<SchoolStudent> schoolStudentList = schoolStudentPage.getRecords();

        // 为空直接返回
        if (CollUtil.isEmpty(schoolStudentList)) {
            return studentDTOPage;
        }

        // 封装DTO
        List<SchoolStudentListVO> studentDTOList = schoolStudentList.stream()
                .map(SchoolStudentInfoBuilder::buildSchoolStudentListVO)
                .collect(Collectors.toList());
        studentDTOPage.setRecords(studentDTOList);
        return studentDTOPage;
    }



    /**
     * 删除学生
     *
     * @param id 学生id
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletedStudent(Integer id) {
        if (screeningPlanSchoolStudentService.checkStudentHavePlan(id)) {
            throw new BusinessException("该学生有对应的筛查计划，无法进行删除");
        }
        Student student = new Student();
        student.setId(id);
        student.setStatus(CommonConst.STATUS_IS_DELETED);
        return studentService.updateById(student);
    }

    /**
     * 更新学生实体并返回统计信息
     *
     * @param student 学生实体
     * @return 学生
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudentReturnCountInfo(Student student, CurrentUser user) {
        // 判断是否要修改委会行政区域
        isUpdateCommitteeCode(student, user);
        StudentDTO studentDTO = studentService.updateStudent(student);

        // 更新医院学生信息
        studentFacade.updateHospitalStudentRecordNo(studentDTO.getId(),studentDTO.getCommitteeCode(), studentDTO.getRecordNo());
        studentDTO.setScreeningCount(student.getScreeningCount())
                .setQuestionnaireCount(student.getQuestionnaireCount());
        // 就诊次数
        List<ReportAndRecordDO> reportList = medicalReportService.getByStudentId(student.getId());
        if (CollectionUtils.isEmpty(reportList)) {
            studentDTO.setNumOfVisits(reportList.size());
        } else {
            studentDTO.setNumOfVisits(0);
        }
        studentDTO.setScreeningCodes(studentFacade.getScreeningCode(student.getId()));
        return studentDTO;
    }

    /**
     * 获取学生就诊列表
     *
     * @param pageRequest 分页请求
     * @param studentId   学生ID
     * @param currentUser 登录用户
     * @param hospitalId  医院Id
     * @return List<MedicalReportDO>
     */
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, Integer studentId, CurrentUser currentUser, Integer hospitalId) {
        if (!currentUser.isPlatformAdminUser()) {
            hospitalId = currentUser.getOrgId();
        }
        IPage<ReportAndRecordDO> pageReport = medicalReportService.getByStudentIdWithPage(pageRequest, studentId, hospitalId);
        List<ReportAndRecordDO> records = pageReport.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageReport;
        }
        packageReportInfo(records);
        return pageReport;
    }

    /**
     * 设置报告信息
     *
     * @param records 报告
     */
    public void packageReportInfo(List<ReportAndRecordDO> records) {
        // 收集医生Id
        Set<Integer> doctorIds = records.stream().map(ReportAndRecordDO::getDoctorId).collect(Collectors.toSet());
        Map<Integer, String> doctorMap = hospitalDoctorService.listByIds(doctorIds).stream().collect(Collectors.toMap(Doctor::getId, Doctor::getName));
        records.forEach(report -> {
            report.setDoctorName(doctorMap.getOrDefault(report.getDoctorId(), StringUtils.EMPTY));
            if (Objects.nonNull(report.getBirthday())) {
                report.setCreateTimeAge(DateUtil.getAgeInfo(report.getBirthday(), report.getCreateTime()));
            }
            ReportConclusion reportConclusion = medicalReportBizService.getReportConclusion(report.getReportId());
            if (Objects.nonNull(reportConclusion)
                    && Objects.nonNull(reportConclusion.getReport())
                    && !CollectionUtils.isEmpty((reportConclusion.getReport().getImageIdList()))) {
                report.setImageFileUrl(resourceFileService.getBatchResourcePath(reportConclusion.getReport().getImageIdList()));
            }
            report.setCheckStatus(DateUtils.isSameDay(report.getCreateTime(), new Date()));
        });
    }

    /**
     * 发送短信
     *
     * @param studentMaps 学生Maps
     * @return Consumer<VisionScreeningResult>
     */
    public Consumer<VisionScreeningResult> getVisionScreeningResultConsumer(Map<Integer, Student> studentMaps) {
        return result -> {
            Student student = studentMaps.get(result.getStudentId());
            VisionDataDO visionData = result.getVisionData();
            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.isNull(visionData)) {
                return;
            }
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();

            BigDecimal leftNakedVision = leftEyeData.getNakedVision();
            BigDecimal leftCorrectedVision = leftEyeData.getCorrectedVision();
            BigDecimal rightNakedVision = rightEyeData.getNakedVision();
            BigDecimal rightCorrectedVision = rightEyeData.getCorrectedVision();

            // 左右眼的裸眼视力都是为空直接返回
            if (Objects.isNull(leftNakedVision) && Objects.isNull(rightNakedVision)) {
                return;
            }

            TwoTuple<BigDecimal, Integer> nakedVisionResult = ScreeningResultUtil.getResultVision(leftNakedVision, rightNakedVision);
            Integer glassesType = leftEyeData.getGlassesType();

            // 裸眼视力是否小于4.9
            if (nakedVisionResult.getFirst().compareTo(new BigDecimal("4.9")) < 0) {
                // 是否佩戴眼镜
                String noticeInfo;
                if (glassesType >= GlassesTypeEnum.FRAME_GLASSES.code) {
                    noticeInfo = getSMSNoticeInfo(student.getName(), leftNakedVision, rightNakedVision,
                            getWearingGlassesConclusion(leftCorrectedVision, rightCorrectedVision,
                                    leftNakedVision, rightNakedVision, nakedVisionResult));
                } else {
                    // 没有佩戴眼镜
                    noticeInfo = getSMSNoticeInfo(student.getName(),
                            leftNakedVision, rightNakedVision,
                            "裸眼视力下降，建议：请到医疗机构接受检查，明确诊断并及时采取措施。");
                }
                // 发送短信
                sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo, result);
            } else {
                if (Objects.isNull(computerOptometry)) {
                    return;
                }
                BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
                BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
                BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
                BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();
                BigDecimal leftSe = StatUtil.getSphericalEquivalent(leftSph, leftCyl);
                BigDecimal rightSe = StatUtil.getSphericalEquivalent(rightSph, rightCyl);
                // 裸眼视力大于4.9
                String noticeInfo = getSMSNoticeInfo(student.getName(),
                        leftNakedVision, rightNakedVision,
                        nakedVisionNormal(leftNakedVision, rightNakedVision,
                                leftSe, rightSe, nakedVisionResult));
                // 发送短信
                sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo, result);
            }
        };
    }

    /**
     * 戴镜获取结论
     *
     * @param leftCorrectedVision  左眼矫正视力
     * @param rightCorrectedVision 右眼矫正视力
     * @param leftNakedVision      左眼裸眼视力
     * @param rightNakedVision     右眼裸眼视力
     * @param nakedVisionResult    取视力值低的眼球
     * @return 结论
     */
    private String getWearingGlassesConclusion(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                               BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                               TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        if (Objects.isNull(leftCorrectedVision) && Objects.isNull(rightCorrectedVision)) {
            return "";
        }
        BigDecimal visionVal = ScreeningResultUtil.getResultVision(leftCorrectedVision, rightCorrectedVision,
                leftNakedVision, rightNakedVision, nakedVisionResult);
        if (visionVal.compareTo(new BigDecimal("4.9")) < 0) {
            // 矫正视力小于4.9
            return "裸眼视力下降，建议：请及时到医疗机构复查。";
        } else {
            // 矫正视力大于4.9
            return "裸眼视力下降，建议：3个月或半年复查视力。";
        }
    }

    /**
     * 正常裸眼视力获取结论
     *
     * @param leftNakedVision   左眼裸眼视力
     * @param rightNakedVision  右眼裸眼视力
     * @param leftSe            左眼等效球镜
     * @param rightSe           右眼等效球镜
     * @param nakedVisionResult 取视力值低的眼球
     * @return 结论
     */
    private String nakedVisionNormal(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                     BigDecimal leftSe, BigDecimal rightSe,
                                     TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        BigDecimal se = ScreeningResultUtil.getNakedVisionNormalSE(leftNakedVision, rightNakedVision,
                leftSe, rightSe, nakedVisionResult);
        // SE >= 0
        if (se.compareTo(new BigDecimal("0.00")) >= 0) {
            return "建议：目前尚无近视高危风险。";
        } else {
            // SE < 0
            return "建议：可能存在近视高危因素，建议严格注意用眼卫生，到医疗机构检查了解是否可能发展为近视。";
        }
    }

    /**
     * 获取短信通知详情
     *
     * @param studentName      学校名称
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @param advice           建议
     * @return 短信通知详情
     */
    private String getSMSNoticeInfo(String studentName, BigDecimal leftNakedVision, BigDecimal rightNakedVision, String advice) {
        if (Objects.isNull(leftNakedVision)) {
            return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                    "--", rightNakedVision.toString(), advice);
        }
        if (Objects.isNull(rightNakedVision)) {
            return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                    leftNakedVision, "--", advice);
        }
        return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                leftNakedVision, rightNakedVision, advice);
    }

    /**
     * 封装短信内容需要的学生姓名
     * <p>超过4个字符以上：显示前5个字符，其中前3个字符正常回显，后2个字符用*代替。
     * 如陈旭格->陈旭格、陈旭格力->陈旭格力、陈旭格力哈->陈旭格**、陈旭格力哈特->陈旭格**
     * </p>
     *
     * @param studentName 学生姓名
     * @return 学生姓名
     */
    private String packageStudentName(String studentName) {
        if (studentName.length() < 5) {
            return studentName;
        }
        return StringUtils.overlay(studentName, "**", 3, studentName.length());
    }

    /**
     * String 转换成List
     *
     * @param string 字符串
     * @return 字符串
     */
    public static List<String> str2List(String string) {
        if (StringUtils.isNotBlank(string)) {
            return Arrays.stream(string.split(",")).map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 发送短信
     *
     * @param mpParentPhone 家长端绑定的手机号码
     * @param parentPhone   多端绑定的手机号码
     * @param noticeInfo    短信内容
     * @param result        筛查数据
     */
    private void sendSMS(List<String> mpParentPhone, String parentPhone, String noticeInfo, VisionScreeningResult result) {
        // 优先家长端绑定的手机号码
        if (CollUtil.isNotEmpty(mpParentPhone)) {
            mpParentPhone.forEach(phone -> {
                MsgData msgData = new MsgData(phone, "+86", noticeInfo);
                SmsResult smsResult = vistelToolsService.sendMsg(msgData);
                checkSendMsgStatus(smsResult, msgData, result);
            });
            return;
        }
        if (StringUtils.isNotBlank(parentPhone)) {
            MsgData msgData = new MsgData(parentPhone, "+86", noticeInfo);
            SmsResult smsResult = vistelToolsService.sendMsg(msgData);
            checkSendMsgStatus(smsResult, msgData, result);
        }
    }

    /**
     * 检查发送短信是否成功
     *
     * @param smsResult 发送结果
     * @param msgData   请求参数
     * @param result    筛查结果
     */
    private void checkSendMsgStatus(SmsResult smsResult, MsgData msgData, VisionScreeningResult result) {
        if (smsResult.isSuccessful()) {
            result.setIsNotice(true);
        } else {
            log.error("发送通知到手机号码错误，提交信息:{}, 异常信息:{}", JSON.toJSONString(msgData), smsResult);
        }
    }

    /**
     * 判断是否要修改委会行政区域
     *
     * @param student 学生信息
     * @param user    用户
     */
    private void isUpdateCommitteeCode(Student student, CurrentUser user) {
        Long newCommitteeCode = student.getCommitteeCode();
        if (!user.isPlatformAdminUser() || Objects.isNull(newCommitteeCode)) {
            return;
        }
        StudentDTO oldStudent = studentService.getStudentById(student.getId());
        // 如果旧数据没有委会行政区域，或旧数据与新委会行政区域不相同，则生成新的编码
        if (Objects.isNull(oldStudent.getCommitteeCode()) || (!oldStudent.getCommitteeCode().equals(newCommitteeCode))) {
            student.setRecordNo(studentService.getRecordNo(newCommitteeCode));
        }
    }

    /**
     * 保存学校学生
     *
     * @param schoolStudentDTO 学校学生
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolStudent saveSchoolStudent(SchoolStudentDTO schoolStudentDTO) {
        SchoolStudent schoolStudent = BeanCopyUtil.copyBeanPropertise(schoolStudentDTO, SchoolStudent.class);
        setRegionCode(schoolStudentDTO, schoolStudent);
        schoolStudent = schoolStudentFacade.validSchoolStudent(schoolStudent, schoolStudent.getSchoolId());

        boolean isAdd = Objects.isNull(schoolStudent.getId());

        // 更新管理端的数据
        Integer managementStudentId = schoolStudentExcelImportService.updateManagementStudent(schoolStudent);
        schoolStudent.setStudentId(managementStudentId);
        if (Objects.equals(isAdd, Boolean.TRUE)) {
            schoolStudent.setSourceClient(SourceClientEnum.MANAGEMENT.getType());
        }

        schoolStudentService.saveOrUpdate(schoolStudent);
        schoolScreeningBizFacade.addScreeningStudent(schoolStudent, isAdd);
        return schoolStudent;
    }

    /**
     * 设置区域代码
     *
     * @param schoolStudentDTO 学校学生
     * @param schoolStudent    学校学生
     */
    private void setRegionCode(SchoolStudentDTO schoolStudentDTO, SchoolStudent schoolStudent) {
        if (CollUtil.isNotEmpty(schoolStudentDTO.getRegionArr())) {
            schoolStudent.setProvinceCode(schoolStudentDTO.getRegionArr().get(0));
            schoolStudent.setCityCode(schoolStudentDTO.getRegionArr().get(1));
            schoolStudent.setAreaCode(schoolStudentDTO.getRegionArr().get(2));
            schoolStudent.setTownCode(schoolStudentDTO.getRegionArr().get(3));
        }
    }
}