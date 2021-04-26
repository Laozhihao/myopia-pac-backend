package com.wupol.myopia.business.api.hospital.app.facade;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/4/21
 **/
@Service
public class MedicalRecordFacade {

    @Autowired
    private MedicalReportFacade medicalReportFacade;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 获取学生今天最后一条角膜地形图数据
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public ToscaMedicalRecord getTodayLastToscaMedicalRecord(Integer hospitalId, Integer studentId) {
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getTosca())) {
            return new ToscaMedicalRecord();
        }

        ToscaMedicalRecord.Tosco nonMydriasis = medicalRecord.getTosca().getNonMydriasis();
        if (Objects.nonNull(nonMydriasis)) {
            nonMydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(nonMydriasis.getImageIdList()));
        }
        ToscaMedicalRecord.Tosco mydriasis = medicalRecord.getTosca().getMydriasis();
        if (Objects.nonNull(mydriasis)) {
            mydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(mydriasis.getImageIdList()));
        }
        return medicalRecord.getTosca();
    }



    /**
     * 追加问诊到检查单
     * @param consultation    问诊内容
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addConsultationToMedicalRecord(Consultation consultation,
                                               Integer hospitalId,
                                               Integer doctorId,
                                               Integer studentId) {
        addCheckDataToMedicalRecord(consultation,null, null, null, null,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加视力检查数据到检查单
     * @param vision    检查数据
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addVisionToMedicalRecord(VisionMedicalRecord vision,
                                         Integer hospitalId,
                                         Integer doctorId,
                                         Integer studentId) {
        addCheckDataToMedicalRecord(null,vision, null, null, null,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加生物测量检查数据到检查单
     * @param biometrics    检查数据
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addBiometricsToMedicalRecord(BiometricsMedicalRecord biometrics,
                                             Integer hospitalId,
                                             Integer doctorId,
                                             Integer studentId) {
        addCheckDataToMedicalRecord(null,null, biometrics, null, null,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加屈光检查数据到检查单
     * @param diopter    检查数据
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addDiopterToMedicalRecord(DiopterMedicalRecord diopter,
                                          Integer hospitalId,
                                          Integer doctorId,
                                          Integer studentId) {
        addCheckDataToMedicalRecord(null,null, null, diopter, null,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加角膜地形图检查数据到检查单
     * @param tosca    检查数据
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addToscaToMedicalRecord(ToscaMedicalRecord tosca,
                                        Integer hospitalId,
                                        Integer doctorId,
                                        Integer studentId) {
        addCheckDataToMedicalRecord(null,null, null, null, tosca,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加检查检查数据到检查单
     * @param consultation    问诊
     * @param vision    视力检查检查数据
     * @param biometrics    生物测量检查数据
     * @param diopter    屈光检查数据
     * @param tosca    角膜地形图检查数据
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addCheckDataToMedicalRecord(Consultation consultation,
                                            VisionMedicalRecord vision,
                                            BiometricsMedicalRecord biometrics,
                                            DiopterMedicalRecord diopter,
                                            ToscaMedicalRecord tosca,
                                            Integer hospitalId,
                                            Integer departmentId,
                                            Integer doctorId,
                                            Integer studentId) {
        MedicalRecord medicalRecord = getOrCreateTodayMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        if (Objects.nonNull(consultation)) {
            medicalRecord.setConsultation(consultation);
            medicalReportFacade.updateReportConclusionWithSave(medicalRecord);
        }
        if (Objects.nonNull(vision)) medicalRecord.setVision(vision);
        if (Objects.nonNull(biometrics)) medicalRecord.setBiometrics(biometrics);
        if (Objects.nonNull(diopter)) medicalRecord.setDiopter(diopter);
        if (Objects.nonNull(tosca)) medicalRecord.setTosca(tosca);
        if (!medicalRecordService.updateById(medicalRecord)) {
            throw new BusinessException("修改失败");
        }
    }

    /**
     * 获取 或者 创建 学生今天最后一条检查单
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public MedicalRecord getOrCreateTodayMedicalRecord(Integer hospitalId,
                                                       Integer doctorId,
                                                       Integer studentId) {
        return getOrCreateTodayMedicalRecord(hospitalId, -1, doctorId, studentId);
    }

    /**
     * 获取 或者 创建 学生今天最后一条问诊
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public MedicalRecord getOrCreateTodayMedicalRecord(Integer hospitalId,
                                                       Integer departmentId,
                                                       Integer doctorId,
                                                       Integer studentId) {
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.nonNull(medicalRecord)) {
            return medicalRecord;
        }
        medicalRecord = medicalRecordService.createMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        // 创建检查单的同时,创建对应的报告
        medicalReportFacade.createMedicalReport(medicalRecord.getId(), hospitalId, departmentId, doctorId, studentId);
        return medicalRecord;
    }

}
