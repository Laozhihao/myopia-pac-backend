package com.wupol.myopia.business.api.hospital.app.facade;

import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.vo.MedicalReportVO;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/4/21
 **/
@Service
public class MedicalReportFacade {

    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalDoctorService hospitalDoctorService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * 获取学生的今天的最新一份报告, 没有则创建
     *
     * @param hospitalId 医院ID
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReportVO getOrCreateTodayLastMedicalReportVo(Integer hospitalId, Integer studentId) {
        MedicalReportDO reportDO = medicalReportService.getTodayLastMedicalReport(hospitalId, studentId);
        MedicalReportVO reportVO = new MedicalReportVO();
        if (Objects.isNull(reportDO)) {
            return reportVO;
        }
        BeanUtils.copyProperties(reportDO, reportVO);
        if (!CollectionUtils.isEmpty(reportDO.getImageIdList())) {
            reportVO.setImageUrlList(resourceFileService.getBatchResourcePath(reportDO.getImageIdList()));
        }
        return reportVO;
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
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.isNull(medicalRecord)) {
            throw new BusinessException("无检查数据，不可录入诊断处方");
        }

        MedicalReport dbReport = medicalReportService.getById(medicalReport.getId());
        if (Objects.nonNull(dbReport)) {
            dbReport.setGlassesSituation(medicalReport.getGlassesSituation())
                    .setMedicalContent(medicalReport.getMedicalContent())
                    .setImageIdList(medicalReport.getImageIdList())
                    .setDoctorId(medicalReport.getDoctorId());
        } else {
            dbReport = medicalReport;
        }
        dbReport.setMedicalRecordId(medicalRecord.getId());
        updateReportConclusion(dbReport, medicalRecord); // 更新固化数据
        medicalReportService.saveOrUpdate(dbReport);
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
        if (!medicalReportService.save(medicalReport)) {
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
    public void updateReportConclusionWithSave(MedicalRecord record) {
        MedicalReportQuery medicalReportQuery = new MedicalReportQuery();
        medicalReportQuery.setMedicalRecordId(record.getId());
        MedicalReport report = medicalReportService.getMedicalReportList(medicalReportQuery).stream().findFirst().orElseThrow(()-> new BusinessException("未找到该检查单"));
        updateReportConclusion(report, record);
        medicalReportService.updateById(report);
    }

    /** 更新报告的固化数据 */
    private void updateReportConclusion(MedicalReport report, MedicalRecord record) {
        ReportConclusion.ReportInfo reportInfo = new ReportConclusion.ReportInfo();
        BeanUtils.copyProperties(report, reportInfo);
        Student student = studentService.getById(report.getStudentId());
        MedicalReportStudent medicalReportStudent = new MedicalReportStudent();
        BeanUtils.copyProperties(student, medicalReportStudent);
        ReportConclusion conclusion = new ReportConclusion()
                .setReport(reportInfo)
                .setStudent(medicalReportStudent)
                .setHospitalName(hospitalService.getById(report.getHospitalId()).getName());
        Doctor doctor = hospitalDoctorService.getById(report.getDoctorId());
        if (Objects.nonNull(doctor)) {
            conclusion.setSignFileId(doctor.getSignFileId());
        }
        if (Objects.nonNull(record)){
            conclusion.setConsultation(record.getConsultation());
        }
        report.setReportConclusionData(conclusion);
    }


}
