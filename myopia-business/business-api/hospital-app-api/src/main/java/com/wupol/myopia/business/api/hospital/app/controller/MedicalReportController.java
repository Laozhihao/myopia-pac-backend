package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.facade.MedicalReportFacade;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医院的报告的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/medicalReport")
public class MedicalReportController {

    @Autowired
    private MedicalReportService medicalReportService;

    @PostMapping()
    public Boolean saveReport(@RequestBody MedicalReport medicalReport) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalReport.setHospitalId(user.getOrgId());
        medicalReportService.saveReport(medicalReport, user.getOrgId(), medicalReport.getDoctorId(), medicalReport.getStudentId());
        return true;
    }

    /**
     * 获取学生的诊断列表
     * @param studentId         学生id
     * @param filterHospital    是否按医院过滤
     * @return
     */
    @GetMapping("/list")
    public List<MedicalReportDO> getStudentReportList(Integer studentId, Boolean filterHospital) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = filterHospital ? user.getOrgId() : null;
        return medicalReportService.getReportListByStudentId(hospitalId, studentId);
    }

    @GetMapping("/{reportId}")
    public StudentReportResponseDTO getReport(@PathVariable("reportId") Integer reportId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalReportService.getStudentReport(user.getOrgId(), reportId);
    }

    @GetMapping("/todayLast")
    public MedicalReport getTodayLastMedicalReport(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalReportService.getOrCreateTodayLastMedicalReportDO(user.getOrgId(), studentId);
    }

}
