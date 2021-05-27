package com.wupol.myopia.business.aggregation.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.domain.model.ReportConclusion;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import com.wupol.myopia.business.core.hospital.service.*;
import com.wupol.myopia.business.core.school.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 医院-报告数据的业务模块
 * @Author Chikong
 * @Date 2020/12/22
 **/
@Slf4j
@Service
public class MedicalReportBizService {
    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private HospitalService hospitalService;


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
                successCount ++;
            } catch (BusinessException e) {
                log.error("生成报告的固化结论失败。report id = ." + report.getId(), e);
            }
        }
        log.info("完成 生成报告的固化结论，共{}条，失败{}条.", list.size(), list.size() - successCount);
    }


    /** 更新报告的固化数据 */
    private void updateReportConclusion(MedicalReport report) {
        report.setReportConclusionData(generateReportConclusion(report));
        medicalReportService.saveOrUpdate(report);
    }

    /** 获取报告的固化数据 */
    private ReportConclusion generateReportConclusion(MedicalReport report) {
        ReportConclusion.ReportInfo reportInfo = new ReportConclusion.ReportInfo();
        BeanUtils.copyProperties(report, reportInfo);

        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setStudentId(report.getStudentId()).setHospitalId(report.getHospitalId());
        ReportConclusion conclusion = new ReportConclusion()
                .setReport(reportInfo)
                .setStudent(hospitalStudentService.getBy(query).stream().findFirst().orElse(null))
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
     * @param reportId 报告id
     */
    public ReportConclusion getReportConclusion(Integer reportId) {
        return getReportConclusion(medicalReportService.getById(reportId));
    }

    /**
     * 获取报告结论，如果有固化的则直接使用，如果没有则组装
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

}
