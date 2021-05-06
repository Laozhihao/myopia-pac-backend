package com.wupol.myopia.business.core.hospital.service;

import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalReportQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
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
    private HospitalDoctorService hospitalDoctorService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private RedisUtil redisUtil;


    /**
     * 获取学生的今天的最新一份报告, 没有则创建
     *
     * @param hospitalId 医院ID
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReportDO getOrCreateTodayLastMedicalReportDO(Integer hospitalId, Integer studentId) {
        MedicalReportDO reportDO = getTodayLastMedicalReport(hospitalId, studentId);
        if (Objects.isNull(reportDO)) {
            return null;
        }
        if (!CollectionUtils.isEmpty(reportDO.getImageIdList())) {
            reportDO.setImageUrlList(resourceFileService.getBatchResourcePath(reportDO.getImageIdList()));
        }
        return reportDO;
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
    public void saveReport(MedicalReport medicalReport, Integer hospitalId, Integer departmentId, Integer doctorId, Integer studentId) {
        MedicalRecordDO medicalRecordDO = medicalRecordService.getTodayLastMedicalRecordDO(hospitalId, studentId);
        if (Objects.isNull(medicalRecordDO)) {
            throw new BusinessException("无检查数据，不可录入诊断处方");
        }

        MedicalReport dbReport = getById(medicalReport.getId());
        if (Objects.nonNull(dbReport)) {
            dbReport.setGlassesSituation(medicalReport.getGlassesSituation())
                    .setMedicalContent(medicalReport.getMedicalContent())
                    .setImageIdList(medicalReport.getImageIdList())
                    .setDoctorId(medicalReport.getDoctorId());
        } else {
            dbReport = medicalReport;
        }
        dbReport.setMedicalRecordId(medicalRecordDO.getId());
        updateReportConclusion(dbReport, medicalRecordDO); // 更新固化数据
        saveOrUpdate(dbReport);
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
                .setNo(generateReportSn(hospitalId))
                .setMedicalRecordId(medicalRecordId)
                .setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        updateReportConclusion(medicalReport, null);
        if (!save(medicalReport)) {
            throw new BusinessException("创建报告失败");
        }
        return medicalReport;
    }

    /**
     * 报告编号：医院ID+生成日期时分秒（202011111111）+6位数排序（000001开始）
     * @param hospitalId 医院id
     * @return
     */
    private String generateReportSn(Integer hospitalId) {
        String sn = DateFormatUtil.format(new Date(), DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR);
        String key = sn.substring(0,8);
        sn = hospitalId + sn;
        key = hospitalId + key;
        Long count = redisUtil.incr(key, 1L);
        return String.format(sn+"%06d", count);
    }

    /**
     * 更新报告的固化数据 TODO 当学生信息修改时,也要更新固化结论的学生数据
     * @param record
     * @return void
     **/
    public void updateReportConclusionWithSave(MedicalRecordDO record) {
        MedicalReportQuery medicalReportQuery = new MedicalReportQuery();
        medicalReportQuery.setMedicalRecordId(record.getId());
        MedicalReport report = getMedicalReportList(medicalReportQuery).stream().findFirst().orElseThrow(()-> new BusinessException("未找到该检查单"));
        updateReportConclusion(report, record);
        updateById(report);
    }

    /** 更新报告的固化数据 */
    private void updateReportConclusion(MedicalReport report, MedicalRecordDO record) {
        ReportConclusion.ReportInfo reportInfo = new ReportConclusion.ReportInfo();
        BeanUtils.copyProperties(report, reportInfo);
        HospitalStudent student = hospitalStudentService.getById(report.getStudentId());
        MedicalReportStudent medicalReportStudent = new MedicalReportStudent();
        BeanUtils.copyProperties(student, medicalReportStudent);
        ReportConclusion conclusion = new ReportConclusion()
                .setReport(reportInfo)
                .setStudent(medicalReportStudent)
                .setHospitalName(record.getHospitalName());
        Doctor doctor = hospitalDoctorService.getById(report.getDoctorId());
        if (Objects.nonNull(doctor)) {
            conclusion.setSignFileId(doctor.getSignFileId());
        }
        if (Objects.nonNull(record)){
            conclusion.setConsultation(record.getConsultation());
        }
        report.setReportConclusionData(conclusion);
    }



    /**
     * 获取学生报告列表
     * @param studentId 学生id
     * @return List<MedicalReportVo>
     */
    public List<MedicalReportDO> getReportListByStudentId(Integer hospitalId, Integer studentId) {
        MedicalReportQuery query = new MedicalReportQuery();
        query.setStudentId(studentId).setHospitalId(hospitalId);
        return baseMapper.getMedicalReportDoList(query);
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
        MedicalReport report = getMedicalReportList(reportQuery).stream().findFirst().orElseThrow(()-> new BusinessException("未找到该报告"));
        responseDTO.setReport(report);
        // 检查单
        MedicalRecord record = medicalRecordService.getById(report.getId());
        responseDTO.setRecord(record);

        return responseDTO;
    }


    /**
     * 获取学生的最新一份报告
     *
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReport getLastOneByStudentId(Integer studentId) {
        return baseMapper.getLastOneByStudentId(studentId);
    }

    public List<MedicalReport> getMedicalReportList(MedicalReportQuery query) {
        return baseMapper.getMedicalReportList(query);
    }

    /**
     * 通过学生ID(只取当前时间的前一天)
     *
     * @param studentId 学生ID
     * @return List<ReportAndRecordVo>
     */
    public List<ReportAndRecordDO> getByStudentId(Integer studentId) {
        return baseMapper.getStudentId(studentId);
    }

    /**
     * 批量获取列表通过学生Ids(只取当前时间的前一天)
     *
     * @param studentIds 学生Ids
     * @return List<ReportAndRecordVo>
     */
    public List<ReportAndRecordDO> getByStudentIds(List<Integer> studentIds) {
        return baseMapper.getByStudentIds(studentIds);
    }

    /**
     * 获取学生今天最后一条报告
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalReportDO getTodayLastMedicalReport(Integer hospitalId, Integer studentId) {
        return baseMapper.getTodayLastMedicalReportDO(hospitalId, studentId);
    }

}