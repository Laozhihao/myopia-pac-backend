package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

/**
 * 医院的检查单的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/medicalRecord")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping("/consultation/{studentId}")
    public Consultation getConsultation(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getConsultation())? new Consultation() : medicalRecord.getConsultation();
    }

    @PostMapping("/consultation")
    public Boolean createConsultation(@RequestBody @Valid Consultation consultation) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addConsultationToMedicalRecord(consultation, user.getOrgId(), -1, consultation.getStudentId());
        return true;
    }

    @GetMapping("/vision/{studentId}")
    public VisionMedicalRecord getVisionMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getVision())? new VisionMedicalRecord() : medicalRecord.getVision();
    }

    @PostMapping("/vision")
    public Boolean createVisionMedicalRecord(@RequestBody VisionMedicalRecord vision) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addVisionToMedicalRecord(vision, user.getOrgId(), -1, vision.getStudentId());
        return true;
    }

    @GetMapping("/biometrics/{studentId}")
    public BiometricsMedicalRecord getBiometricsMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getBiometrics()) ? new BiometricsMedicalRecord() : medicalRecord.getBiometrics();
    }

    @PostMapping("/biometrics")
    public Boolean createBiometricsMedicalRecord(@RequestBody BiometricsMedicalRecord biometrics) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addBiometricsToMedicalRecord(biometrics, user.getOrgId(), -1, biometrics.getStudentId());
        return true;
    }

    @GetMapping("/diopter/{studentId}")
    public DiopterMedicalRecord getDiopterMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getDiopter()) ? new DiopterMedicalRecord() : medicalRecord.getDiopter();
    }

    @PostMapping("/diopter")
    public Boolean createDiopterMedicalRecord(@RequestBody DiopterMedicalRecord diopter) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addDiopterToMedicalRecord(diopter, user.getOrgId(), -1, diopter.getStudentId());
        return true;
    }

    @GetMapping("/tosca/{studentId}")
    public ToscaMedicalRecord getToscaMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalRecordService.getTodayLastToscaMedicalRecord(user.getOrgId(), studentId);
    }

    @PostMapping("/tosca")
    public Boolean createToscaMedicalRecord(@RequestBody ToscaMedicalRecord tosca) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addToscaToMedicalRecord(tosca, user.getOrgId(), -1, tosca.getStudentId());
        return true;
    }

}
