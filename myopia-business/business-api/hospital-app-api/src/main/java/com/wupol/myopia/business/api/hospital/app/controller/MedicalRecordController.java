package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.facade.MedicalRecordFacade;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

/**
 * 医院的检查单的App接口
 * @author Chikong
 * @date 2021-02-10
 */

@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/medicalRecord")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private MedicalRecordFacade medicalRecordFacade;

    /**
     * 获取该学生的最新的问诊检查
     * @param studentId
     * @return
     */
    @GetMapping("/consultation")
    public Consultation getTodayLastConsultation(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getConsultation())? new Consultation() : medicalRecord.getConsultation();
    }

    @PostMapping("/consultation")
    public Boolean createConsultation(@RequestBody @Valid Consultation consultation) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent(consultation, null, null, null,null, null, user.getOrgId(), -1, consultation.getStudentId(), user.getClientId());
        return true;
    }

    /**
     * 获取该学生的最新的视力检查
     * @param studentId
     * @return
     */
    @GetMapping("/vision")
    public VisionMedicalRecord getTodayLastVisionMedicalRecord(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getVision())? new VisionMedicalRecord() : medicalRecord.getVision();
    }

    @PostMapping("/vision")
    public Boolean createVisionMedicalRecord(@RequestBody VisionMedicalRecord vision) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent( null, vision,null, null,null, null, user.getOrgId(), -1, vision.getStudentId(), user.getClientId());
        return true;
    }

    /**
     * 获取该学生的最新的生物测量
     * @param studentId
     * @return
     */
    @GetMapping("/biometrics")
    public BiometricsMedicalRecord getTodayLastBiometricsMedicalRecord(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getBiometrics()) ? new BiometricsMedicalRecord() : medicalRecord.getBiometrics();
    }

    @GetMapping("/biometrics/{id}")
    public BiometricsMedicalRecord getBiometricsMedicalRecord(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecord(user.getOrgId(), id);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getBiometrics()) ? new BiometricsMedicalRecord() : medicalRecord.getBiometrics();
    }

    @PostMapping("/biometrics")
    public Boolean createBiometricsMedicalRecord(@RequestBody BiometricsMedicalRecord biometrics) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent(null, null, biometrics,null, null, null, user.getOrgId(), -1, biometrics.getStudentId(), user.getClientId());
        return true;
    }

    /**
     * 获取该学生的最新的屈光检查
     * @param studentId
     * @return
     */
    @GetMapping("/diopter")
    public DiopterMedicalRecord getTodayLastDiopterMedicalRecord(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getDiopter()) ? new DiopterMedicalRecord() : medicalRecord.getDiopter();
    }

    @GetMapping("/diopter/{id}")
    public DiopterMedicalRecord getDiopterMedicalRecord(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecord(user.getOrgId(), id);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getDiopter()) ? new DiopterMedicalRecord() : medicalRecord.getDiopter();
    }

    @PostMapping("/diopter")
    public Boolean createDiopterMedicalRecord(@RequestBody DiopterMedicalRecord diopter) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent(null, null, null, diopter, null, null, user.getOrgId(), -1, diopter.getStudentId(), user.getClientId());
        return true;
    }

    /**
     * 获取该学生的最新的角膜地形
     * @param studentId
     * @return
     */
    @GetMapping("/tosca")
    public ToscaMedicalRecord getTodayLastToscaMedicalRecord(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalRecordService.getTodayLastToscaMedicalRecord(user.getOrgId(), studentId);
    }

    @PostMapping("/tosca")
    public Boolean createToscaMedicalRecord(@RequestBody ToscaMedicalRecord tosca) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent(null, null, null, null, tosca, null, user.getOrgId(), -1, tosca.getStudentId(), user.getClientId());
        return true;
    }

    /**
     * 获取该学生的最新的眼压检查
     * @param studentId
     * @return
     */
    @GetMapping("/eyePressure")
    public EyePressure getTodayLastEyePressureMedicalRecord(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(user.getOrgId(), studentId);
        return Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getEyePressure()) ? new EyePressure() : medicalRecord.getEyePressure();
    }

    @PostMapping("/eyePressure")
    public Boolean createEyePressureMedicalRecord(@RequestBody EyePressure eyePressure) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordFacade.addCheckDataAndCreateStudent(null, null, null, null, null, eyePressure, user.getOrgId(), -1, eyePressure.getStudentId(), user.getClientId());
        return true;
    }

    @GetMapping("/detailWithCompare/{id}")
    public CompareMedicalRecord getMedicalRecordWithCompare(@PathVariable("id") @NotNull(message = "检查单id不能为空") Integer id, @NotNull(message = "学生id不能为空") Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return medicalRecordService.getMedicalRecordWithCompare(user.getOrgId(), id, studentId);
    }

}
