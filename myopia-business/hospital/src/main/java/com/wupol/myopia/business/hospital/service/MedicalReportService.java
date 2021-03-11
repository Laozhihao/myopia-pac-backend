package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.hospital.domain.model.*;
import com.wupol.myopia.business.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.HospitalService;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.service.StudentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 医院-检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class MedicalReportService extends BaseService<MedicalReportMapper, MedicalReport> {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    /**
     * 获取学生报告列表
     * @param studentId 学生id
     * @return List<MedicalReportVo>
     */
    public List<MedicalReportVo> getReportListByStudentId(Integer hospitalId, Integer studentId) {
        MedicalReportQuery query = new MedicalReportQuery();
        query.setStudentId(studentId).setHospitalId(hospitalId);
        return baseMapper.getVoBy(query);
    }

    /**
     * 保存报告
     * @param medicalReport    检查单
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void saveReport(MedicalReport medicalReport,
                           Integer hospitalId,
                           Integer doctorId,
                           Integer studentId) {
        saveReport(medicalReport, hospitalId, -1, doctorId, studentId);
    }

    /**
     * 创建报告
     * @param medicalReport    检查单
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReport(MedicalReport medicalReport,
                           Integer hospitalId,
                           Integer departmentId,
                           Integer doctorId,
                           Integer studentId) {
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.isNull(medicalRecord)) {
            throw new BusinessException("无检查数据，不可录入诊断处方");
        }

        MedicalReport dbReport = getById(medicalReport.getId());
        dbReport.setGlassesSituation(medicalReport.getGlassesSituation())
                .setMedicalContent(medicalReport.getMedicalContent())
                .setImageIdList(medicalReport.getImageIdList())
                .setDoctorId(medicalRecord.getDoctorId());

        updateReportConclusion(dbReport, medicalRecord); // 更新固化数据
        saveOrUpdate(dbReport);
    }

    /**
     * 获取学生的就诊档案详情（报告）
     *
     * @param reportId 报告ID
     * @return responseDTO
     */
    public StudentReportResponseDTO getStudentReport(Integer hospitalId, Integer reportId) {

        StudentReportResponseDTO responseDTO = new StudentReportResponseDTO();
        MedicalReportQuery reportQuery = new MedicalReportQuery();
        reportQuery.setHospitalId(hospitalId).setId(reportId);
        // 报告
        MedicalReport report = getBy(reportQuery).stream().findFirst().orElseThrow(()-> new BusinessException("未找到该报告"));
        responseDTO.setReport(report);
        // 检查单
        MedicalRecord record = medicalRecordService.getById(report.getId());
        responseDTO.setRecord(record);

        return responseDTO;
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
        MedicalReport report = getById(reportId);
        if (null == report) {
            throw new BusinessException("数据异常");
        }
        // 学生
        Student student = studentService.getById(report.getStudentId());
        if (null == student) {
            throw new BusinessException("数据异常");
        }
        // 医生
        Doctor doctor = hospitalDoctorService.getById(report.getDoctorId());
        // 医院
        Hospital hospital = hospitalService.getById(report.getHospitalId());

        responseDTO.setStudent(packageStudentInfo(student));
        responseDTO.setHospitalName(hospital.getName());

        responseDTO.setReport(packageReportInfo(report, doctor));

        // 检查单
        if (null != report.getMedicalRecordId()) {
            MedicalRecord record = medicalRecordService.getById(report.getMedicalRecordId());
            responseDTO.setVision(record.getVision());
            responseDTO.setBiometrics(record.getBiometrics());
            responseDTO.setDiopter(record.getDiopter());
            responseDTO.setTosca(packageToscaMedicalRecordImages(record.getTosca()));
            // 问诊内容
            responseDTO.setConsultation(record.getConsultation());
        }
        return responseDTO;
    }

    /**
     * 报告-设置学生信息
     *
     * @param student 学生
     * @return {@link StudentVisitReportResponseDTO.StudentInfo}
     */
    private StudentVisitReportResponseDTO.StudentInfo packageStudentInfo(Student student) {
        StudentVisitReportResponseDTO.StudentInfo studentInfo = new StudentVisitReportResponseDTO.StudentInfo();
        studentInfo.setName(student.getName());
        studentInfo.setBirthday(student.getBirthday());
        studentInfo.setGender(student.getGender());
        return studentInfo;
    }

    /**
     * 报告-设置报告、医生信息
     *
     * @param report 报告
     * @param doctor 医生
     * @return {@link StudentVisitReportResponseDTO.ReportInfo}
     */
    private StudentVisitReportResponseDTO.ReportInfo packageReportInfo(MedicalReport report, Doctor doctor) {
        StudentVisitReportResponseDTO.ReportInfo reportInfo = new StudentVisitReportResponseDTO.ReportInfo();
        reportInfo.setNo(report.getNo());
        reportInfo.setCreateTime(report.getCreateTime());
        reportInfo.setGlassesSituation(report.getGlassesSituation());
        reportInfo.setMedicalContent(report.getMedicalContent());
        if (!CollectionUtils.isEmpty(report.getImageIdList())) {
            reportInfo.setImageUrlList(resourceFileService.getBatchResourcePath(report.getImageIdList()));
        }
        if (null != doctor.getSignFileId()) {
            reportInfo.setDoctorSign(resourceFileService.getResourcePath(doctor.getSignFileId()));
        }
        return reportInfo;
    }

    /**
     * 报告-设置角膜地形图图片
     *
     * @param record 角膜地形图检查数据
     * @return ToscaMedicalRecord
     */
    private ToscaMedicalRecord packageToscaMedicalRecordImages(ToscaMedicalRecord record) {
        ToscaMedicalRecord.Tosco mydriasis = record.getMydriasis();
        ToscaMedicalRecord.Tosco nonMydriasis = record.getNonMydriasis();
        if (Objects.nonNull(mydriasis)) {
            if (CollectionUtils.isEmpty(mydriasis.getImageIdList())) {
                mydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(mydriasis.getImageIdList()));
            }
        }
        if (Objects.nonNull(nonMydriasis)) {
            if (CollectionUtils.isEmpty(nonMydriasis.getImageIdList())) {
                nonMydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(nonMydriasis.getImageIdList()));
            }
        }
        return record;
    }

    /**
     * 统计就诊档案
     *
     * @param studentId 学生ID
     * @return 个数
     */
    public Integer countReport(Integer studentId) {
        return baseMapper.countReportBySchoolId(studentId);
    }

    /**
     * 获取学生的最新一份报告
     *
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReport getLatestVisitsReport(Integer studentId) {
        return baseMapper.getLatestVisitsReport(studentId);
    }

    /**
     * 获取学生的今天的最新一份报告, 没有则创建
     *
     * @param hospitalId 医院ID
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReportVo getOrCreateTodayLastMedicalReportVo(Integer hospitalId, Integer studentId) {
        MedicalReportVo reportVo = getTodayLastMedicalReport(hospitalId, studentId);
        if (Objects.isNull(reportVo)) {
            return new MedicalReportVo();
        }
        if (!CollectionUtils.isEmpty(reportVo.getImageIdList())) {
            reportVo.setImageUrlList(resourceFileService.getBatchResourcePath(reportVo.getImageIdList()));
        }
        return reportVo;
    }

    public List<MedicalReport> getBy(MedicalReportQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 通过学生ID(只取当前时间的前一天)
     *
     * @param studentId 学生ID
     * @return List<ReportAndRecordVo>
     */
    public List<ReportAndRecordVo> getByStudentId(Integer studentId) {
        return baseMapper.getStudentId(studentId);
    }

    /**
     * 获取学生今天最后一条报告
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalReportVo getTodayLastMedicalReport(Integer hospitalId, Integer studentId) {
        return baseMapper.getTodayLastMedicalReportVo(hospitalId, studentId);
    }

    /**
     * 创建报告
     * @param medicalRecordId 检查单id
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public MedicalReport createMedicalReport(Integer medicalRecordId, Integer hospitalId, Integer departmentId, Integer doctorId, Integer studentId) {
        MedicalReport medicalReport = new MedicalReport()
                //TODO 待修改规则
                .setNo(String.valueOf(hospitalId)+System.currentTimeMillis())
                .setMedicalRecordId(medicalRecordId)
                .setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        if (!save(medicalReport)) {
            throw new BusinessException("创建报告失败");
        }
        return medicalReport;
    }

    /** 更新报告的固化数据 */
    private void updateReportConclusion(MedicalReport report, MedicalRecord record) {
        ReportConclusion conclusion = new ReportConclusion()
                .setStudent(studentService.getById(report.getStudentId()))
                .setHospitalName(hospitalService.getById(report.getHospitalId()).getName())
                .setSignFileId(hospitalDoctorService.getById(report.getDoctorId()).getSignFileId());
        if (Objects.nonNull(record)){
            conclusion.setConsultation(record.getConsultation());
        }
        report.setReportConclusionData(conclusion);
    }

}