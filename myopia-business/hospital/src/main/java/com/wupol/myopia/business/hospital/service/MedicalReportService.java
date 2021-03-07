package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    /**
     * 获取学生报告列表
     * @param studentId 学生id
     * @return List<MedicalReportVo>
     */
    public List<MedicalReportVo> getReportListByStudentId(Integer studentId) {
        MedicalReportQuery query = new MedicalReportQuery();
        query.setStudentId(studentId);
        return baseMapper.getVoBy(query);
    }

    /**
     * 创建报告
     * @param medicalReport    检查单
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void createReport(MedicalReport medicalReport,
                                         Integer hospitalId,
                                         Integer doctorId,
                                         Integer studentId) {
        createReport(medicalReport, hospitalId, -1, doctorId, studentId);
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
    public void createReport(MedicalReport medicalReport,
                                         Integer hospitalId,
                                         Integer departmentId,
                                         Integer doctorId,
                                         Integer studentId) {
//        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
//        if (Objects.isNull(medicalRecord)) {
//            throw new BusinessException("未找到检查单");
//        }
//        medicalRecordService.finishMedicalRecord(medicalRecord);
        medicalReport.setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        if (!save(medicalReport)) {
            throw new BusinessException("创建报告失败");
        }

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
        MedicalReport report = baseMapper.selectById(reportId);
        responseDTO.setReport(report);
        // 检查单
        MedicalRecord record = medicalRecordService.getById(report.getId());
        responseDTO.setRecord(record);

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

    public List<MedicalReport> getBy(MedicalReportQuery query) {
        return baseMapper.getBy(query);
    }
}