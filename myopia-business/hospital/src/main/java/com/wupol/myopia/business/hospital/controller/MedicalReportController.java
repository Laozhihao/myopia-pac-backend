package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
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
    public Boolean createReport(@RequestBody MedicalReport medicalReport) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalReportService.createReport(medicalReport, user.getOrgId(), user.getId(), medicalReport.getStudentId());
        return true;
    }

    @GetMapping("/list")
    public List<MedicalReportVo> getStudentReportList(Integer studentId) {
        return medicalReportService.getReportListByStudentId(studentId);
    }

    @GetMapping("/{reportId}")
    public Object getReport(@PathVariable("reportId") Integer reportId) {
        return medicalReportService.getStudentReport(reportId);
    }

}
