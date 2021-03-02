package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.model.BiometricsMedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.DiopterMedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.ToscaMedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.VisionMedicalRecord;
import com.wupol.myopia.business.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.hospital.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PostMapping("/vision/{studentId}")
    public Boolean createVisionMedicalRecord(@RequestBody VisionMedicalRecord vision, @PathVariable("studentId") Integer studentId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addVisionToMedicalRecord(vision, user.getOrgId(), user.getId(), studentId);
        return true;
    }
    @PostMapping("/biometrics/{studentId}")
    public Boolean createBiometricsMedicalRecord(@RequestBody BiometricsMedicalRecord biometrics, @PathVariable("studentId") Integer studentId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addBiometricsToMedicalRecord(biometrics, user.getOrgId(), user.getId(), studentId);
        return true;
    }
    @PostMapping("/diopter/{studentId}")
    public Boolean createDiopterMedicalRecord(@RequestBody DiopterMedicalRecord diopter, @PathVariable("studentId") Integer studentId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addDiopterToMedicalRecord(diopter, user.getOrgId(), user.getId(), studentId);
        return true;
    }
    @PostMapping("/tosca/{studentId}")
    public Boolean createToscaMedicalRecord(@RequestBody ToscaMedicalRecord tosca, @PathVariable("studentId") Integer studentId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        medicalRecordService.addToscaToMedicalRecord(tosca, user.getOrgId(), user.getId(), studentId);
        return true;
    }

}
