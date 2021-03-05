package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.model.*;
import com.wupol.myopia.business.hospital.service.ConsultationService;
import com.wupol.myopia.business.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.hospital.service.MedicalRecordService;
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
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private HospitalDoctorService doctorService;
    @Autowired
    private ConsultationService consultationService;


    @GetMapping("/consultation/{studentId}")
    public Consultation getConsultation(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Consultation consultation = consultationService.getTodayLastConsultation(user.getOrgId(), studentId);
        return Objects.isNull(consultation) ? new Consultation() : consultation;
    }

    @PostMapping("/consultation}")
    public Boolean createConsultation(@RequestBody @Valid Consultation consultation) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO departmentId
        consultationService.addConsultationToMedicalRecord(consultation, user.getOrgId(), -1, user.getId(), consultation.getStudentId());
        return true;
    }

    @GetMapping("/vision/{studentId}")
    public VisionMedicalRecord getVisionMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getVision())? new VisionMedicalRecord() : medicalRecord.getVision();
    }

    @PostMapping("/vision")
    public Boolean createVisionMedicalRecord(@RequestBody VisionMedicalRecord vision) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addVisionToMedicalRecord(vision, user.getOrgId(), user.getId(), vision.getStudentId());
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
        medicalRecordService.addBiometricsToMedicalRecord(biometrics, user.getOrgId(), user.getId(), biometrics.getStudentId());
        return true;
    }

    @GetMapping("/diopter/{studentId}")
    public DiopterMedicalRecord getDiopterMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getDiopter()) ? new DiopterMedicalRecord() : medicalRecord.getDiopter();
    }

    @PostMapping("/diopter")
    public Boolean createDiopterMedicalRecord(@RequestBody DiopterMedicalRecord diopter) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addDiopterToMedicalRecord(diopter, user.getOrgId(), user.getId(), diopter.getStudentId());
        return true;
    }

    @GetMapping("/tosca/{studentId}")
    public ToscaMedicalRecord getToscaMedicalRecord(@PathVariable("studentId") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalRecordService.getTodayLastToscaMedicalRecord(user.getOrgId(), studentId);
    }

    @PostMapping("/tosca")
    public Boolean createToscaMedicalRecord(@RequestBody ToscaMedicalRecord tosca) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addToscaToMedicalRecord(tosca, user.getOrgId(), user.getId(), tosca.getStudentId());
        return true;
    }

}
