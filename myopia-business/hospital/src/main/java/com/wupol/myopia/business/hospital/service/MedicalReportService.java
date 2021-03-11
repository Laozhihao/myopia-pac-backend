package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.model.ReportConclusion;
import com.wupol.myopia.business.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.HospitalService;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.service.StudentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
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

        updateReportConclusion(dbReport); // 更新固化数据
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
     * @return responseDTO
     */
    public StudentReportResponseDTO getStudentReport(Integer reportId) {
        StudentReportResponseDTO responseDTO = new StudentReportResponseDTO();
        // 报告
        MedicalReport report = getById(reportId);
        responseDTO.setReport(report);
        // 检查单
        MedicalRecord record = medicalRecordService.getById(report.getMedicalRecordId());
        responseDTO.setRecord(record);
        // 设置学生
        responseDTO.setStudent(studentService.getById(report.getStudentId()));
        // 设置医生
        responseDTO.setDoctor(hospitalDoctorService.getDoctorVoById(report.getDoctorId()));
        return responseDTO;
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
    private void updateReportConclusion(MedicalReport report) {
        ReportConclusion conclusion = new ReportConclusion()
                .setStudent(studentService.getById(report.getStudentId()))
                .setHospitalName(hospitalService.getById(report.getHospitalId()).getName())
                .setSignFileId(hospitalDoctorService.getById(report.getDoctorId()).getSignFileId());
        report.setReportConclusionData(conclusion);
    }

}