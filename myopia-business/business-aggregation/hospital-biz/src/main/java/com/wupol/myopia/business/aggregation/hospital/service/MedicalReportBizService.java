package com.wupol.myopia.business.aggregation.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import com.wupol.myopia.business.core.hospital.service.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 医院-报告数据的业务模块
 *
 * @Author Chikong
 * @Date 2020/12/22
 **/
@Slf4j
@Service
public class MedicalReportBizService {
    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private HospitalDoctorService hospitalDoctorService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;


    /**
     * 生成报告的固化结论
     */
    public void generateReportConclusion() {
        log.info("开始生成报告的固化结论");
        List<MedicalReport> list = medicalReportService.getInconclusiveReportList();
        log.info("开始 生成报告的固化结论，共{}条.", list.size());
        int successCount = 0;
        for (MedicalReport report : list) {
            try {
                MedicalRecordQuery query = new MedicalRecordQuery();
                query.setId(report.getMedicalRecordId());
                updateReportConclusion(report); // 更新报告的固化数据
                successCount++;
            } catch (BusinessException e) {
                log.error("生成报告的固化结论失败。report id = ." + report.getId(), e);
            }
        }
        log.info("完成 生成报告的固化结论，共{}条，失败{}条.", list.size(), list.size() - successCount);
    }


    /**
     * 更新报告的固化数据
     */
    private void updateReportConclusion(MedicalReport report) {
        report.setReportConclusionData(generateReportConclusion(report));
        medicalReportService.saveOrUpdate(report);
    }

    /**
     * 获取报告的固化数据
     */
    private ReportConclusion generateReportConclusion(MedicalReport report) {
        ReportConclusion.ReportInfo reportInfo = new ReportConclusion.ReportInfo();
        BeanUtils.copyProperties(report, reportInfo);

        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setStudentId(report.getStudentId()).setHospitalId(report.getHospitalId());
        HospitalStudent hospitalStudent = hospitalStudentService.getBy(query).stream().findFirst()
                .orElseThrow(() -> {
                    log.error("生成固化结论时，未找到对应学生. studentId=" + report.getStudentId() + ", hospitalId=" + report.getHospitalId());
                    return new BusinessException("未找到该学生");
                });
        ReportConclusion conclusion = new ReportConclusion()
                .setReport(reportInfo)
                .setStudent(hospitalStudent)
                .setHospitalName(hospitalService.getById(report.getHospitalId()).getName());
        Doctor doctor = hospitalDoctorService.getById(report.getDoctorId());
        if (Objects.nonNull(doctor)) {
            conclusion.setSignFileId(doctor.getSignFileId());
        }
        MedicalRecord record = medicalRecordService.getById(report.getMedicalRecordId());
        if (Objects.nonNull(record)) {
            conclusion.setConsultation(record.getConsultation());
        } else {
            log.warn("未找到该报告对应的检查单。report id = {}.", report.getId());
        }
        return conclusion;
    }

    /**
     * 获取报告结论，如果有固化的则直接使用，如果没有则组装
     *
     * @param reportId 报告id
     */
    public ReportConclusion getReportConclusion(Integer reportId) {
        return getReportConclusion(medicalReportService.getById(reportId));
    }

    /**
     * 获取报告结论，如果有固化的则直接使用，如果没有则组装
     *
     * @param report 报告
     */
    public ReportConclusion getReportConclusion(MedicalReport report) {
        if (Objects.isNull(report)) {
            throw new BusinessException("报告不能为空");
        }
        ReportConclusion conclusion = report.getReportConclusionData();
        if (Objects.nonNull(conclusion)) { // 如果已经有结论，则直接返回
            return conclusion;
        }
        conclusion = generateReportConclusion(report);
        return conclusion;
    }

    /**
     * 获取学生的就诊档案详情（报告）
     *
     * @param reportId 报告ID
     * @return StudentVisitReportResponseDTO
     */
    public StudentVisitReportResponseDTO getStudentVisitReport(Integer reportId) {
        StudentVisitReportResponseDTO responseDTO = new StudentVisitReportResponseDTO();

        // 报告
        MedicalReport report = medicalReportService.getById(reportId);
        if (null == report) {
            throw new BusinessException("报告数据异常");
        }
        // 获取固化报告
        ReportConclusion reportConclusionData = getReportConclusion(report);
        if (Objects.nonNull(reportConclusionData)) {
            // 学生
            HospitalStudent student = reportConclusionData.getStudent();
            // 医生签名资源ID
            Integer doctorSignFileId = reportConclusionData.getSignFileId();
            responseDTO.setStudent(packageStudentInfo(student));

            responseDTO.setReport(packageReportInfo(reportId, reportConclusionData, doctorSignFileId, report.getDoctorId()));
            responseDTO.setHospitalName(reportConclusionData.getHospitalName());
        }
        // 检查单
        if (Objects.nonNull(report.getMedicalRecordId())) {
            MedicalRecord medicalRecord = medicalRecordService.getById(report.getMedicalRecordId());
            medicalRecordService.generateToscaImageUrls(medicalRecord); // 设置角膜地形图的图片
            responseDTO.setVision(medicalRecord.getVision());
            responseDTO.setBiometrics(medicalRecord.getBiometrics());
            responseDTO.setDiopter(medicalRecord.getDiopter());
            responseDTO.setTosca(medicalRecord.getTosca());
            responseDTO.setEyePressure(medicalRecord.getEyePressure());
            responseDTO.setFundusImageUrlList(getFundusImageUrls(medicalRecord));
            // 问诊内容
            responseDTO.setConsultation(medicalRecord.getConsultation());
        }
        return responseDTO;
    }

    /**
     * 报告-设置学生信息
     *
     * @param student 学生
     * @return {@link StudentVisitReportResponseDTO.StudentInfo}
     */
    private StudentVisitReportResponseDTO.StudentInfo packageStudentInfo(HospitalStudent student) {
        StudentVisitReportResponseDTO.StudentInfo studentInfo = new StudentVisitReportResponseDTO.StudentInfo();
        studentInfo.setName(student.getName());
        studentInfo.setBirthday(student.getBirthday());
        studentInfo.setGender(student.getGender());
        return studentInfo;
    }

    /**
     * 报告-设置报告、医生信息
     *
     * @param reportId         报告ID
     * @param reportConclusion 固化报告
     * @param doctorSignFileId 医生签名资源ID
     * @param doctorId         医生Id
     * @return {@link StudentVisitReportResponseDTO.ReportInfo}
     */
    private StudentVisitReportResponseDTO.ReportInfo packageReportInfo(Integer reportId, ReportConclusion reportConclusion,
                                                                       Integer doctorSignFileId, Integer doctorId) {
        StudentVisitReportResponseDTO.ReportInfo reportResult = new StudentVisitReportResponseDTO.ReportInfo();
        ReportConclusion.ReportInfo reportInfo = reportConclusion.getReport();
        if (Objects.nonNull(reportInfo)) {
            reportResult.setReportId(reportId);
            reportResult.setNo(reportInfo.getNo());
            reportResult.setCreateTime(reportInfo.getCreateTime());
            reportResult.setGlassesSituation(reportInfo.getGlassesSituation());
            reportResult.setMedicalContent(reportInfo.getMedicalContent());
            if (!CollectionUtils.isEmpty(reportInfo.getImageIdList())) {
                reportResult.setImageUrlList(resourceFileService.getBatchResourcePath(reportInfo.getImageIdList()));
            }
        }
        if (null != doctorSignFileId) {
            reportResult.setDoctorSign(resourceFileService.getResourcePath(doctorSignFileId));
        }
        if (Objects.nonNull(doctorId)) {
            Doctor doctor = hospitalDoctorService.getById(doctorId);
            if (Objects.nonNull(doctor)) {
                reportResult.setDoctorName(doctor.getName());
            }
        }
        reportResult.setHospitalName(reportConclusion.getHospitalName());
        return reportResult;
    }

    /**
     * 更新学生统计就诊信息
     *
     * @param report 报告
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatConclusion(MedicalReport report) {
        // 先查出最新的一条筛查学生
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getLastByStudentId(report.getStudentId());
        if (Objects.isNull(planSchoolStudent)) {
            return;
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(planSchoolStudent.getScreeningPlanId());
        if (Objects.isNull(screeningPlan)) {
            return;
        }
        // 判断筛查时间是否大于报告生成时间
        if (screeningPlan.getStartTime().before(report.getCreateTime())) {
            StatConclusion statConclusion = statConclusionService.getByPlanStudentId(planSchoolStudent.getId());
            if (Objects.nonNull(statConclusion)) {
                statConclusion.setReportId(report.getId());
                statConclusionService.updateById(statConclusion);
            }
        }
    }

    /**
     * 设置眼底影像
     *
     * @param medicalRecord 记录
     *
     * @return 眼底影像
     */
    private List<String> getFundusImageUrls(MedicalRecord medicalRecord) {
        Optional<MedicalRecord> optional = Optional.ofNullable(medicalRecord);
        if (!optional.isPresent()) {
            return new ArrayList<>();
        }
        List<Integer> imageIdList = optional.map(MedicalRecord::getFundus).map(FundusMedicalRecord::getImageIdList).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(imageIdList)) {
            return new ArrayList<>();
        }
        return resourceFileService.getBatchResourcePath(imageIdList);
    }
}
