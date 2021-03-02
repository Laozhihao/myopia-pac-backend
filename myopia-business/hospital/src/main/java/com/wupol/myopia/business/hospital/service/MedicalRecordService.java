package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.MedicalRecordMapper;
import com.wupol.myopia.business.hospital.domain.model.*;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 医院-检查单
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class MedicalRecordService extends BaseService<MedicalRecordMapper, MedicalRecord> {

    @Autowired
    private ConsultationService consultationService;


    /**
     * 获取今天最新的3个就诊的学生id
     * @param hospitalId 医院id
     * @throws IOException
     */
    public List<Integer> getTodayLastThreeStudentList(Integer hospitalId) throws IOException {
        // 今天建档的患者姓名【前3名】
        return findByPage(new MedicalRecord().setHospitalId(hospitalId), 0, 3)
                .getRecords().stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date()))
                .map(MedicalRecord::getStudentId).collect(Collectors.toList());
    }

    /**
     * 完成检查单
     * @param medicalRecord    检查单
     */
    public void finishMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.isFinish()) {
            throw new BusinessException(String.format("该检查单已经完成. id=%s", medicalRecord.getId()));
        }
        medicalRecord.setStatus(MedicalRecord.STATUS_FINISH);
        if (!updateById(medicalRecord)) {
            throw new BusinessException("完成检查单失败");
        }
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
        addCheckDateToMedicalRecord(vision, null, null, null,hospitalId, -1, doctorId, studentId);
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
        addCheckDateToMedicalRecord(null, biometrics, null, null,hospitalId, -1, doctorId, studentId);
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
        addCheckDateToMedicalRecord(null, null, diopter, null,hospitalId, -1, doctorId, studentId);
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
        addCheckDateToMedicalRecord(null, null, null, tosca,hospitalId, -1, doctorId, studentId);
    }

    /**
     * 追加检查检查数据到检查单
     * @param vision    视力检查检查数据
     * @param biometrics    生物测量检查数据
     * @param diopter    屈光检查数据
     * @param tosca    角膜地形图检查数据
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addCheckDateToMedicalRecord(VisionMedicalRecord vision,
                                         BiometricsMedicalRecord biometrics,
                                         DiopterMedicalRecord diopter,
                                         ToscaMedicalRecord tosca,
                                         Integer hospitalId,
                                         Integer departmentId,
                                         Integer doctorId,
                                         Integer studentId) {
        MedicalRecord medicalRecord = getOrCreateTodayMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        if (Objects.nonNull(vision)) medicalRecord.setVision(vision);
        if (Objects.nonNull(biometrics)) medicalRecord.setBiometrics(biometrics);
        if (Objects.nonNull(diopter)) medicalRecord.setDiopter(diopter);
        if (Objects.nonNull(tosca)) medicalRecord.setTosca(tosca);
        if (!updateById(medicalRecord)) {
            throw new BusinessException("修改失败");
        }
    }

    /**
     * 获取 或者 创建 学生今天最后一条问诊
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
        MedicalRecord medicalRecord = getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.nonNull(medicalRecord)) {
            return medicalRecord;
        }
        medicalRecord = new MedicalRecord()
                .setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setStudentId(studentId)
                .setDoctorId(doctorId);
        return createMedicalRecordWithConsultation(medicalRecord);
    }

    /**
     * 获取学生今天最后一条问诊
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalRecord getTodayLastMedicalRecord(Integer hospitalId, Integer studentId) {
        MedicalRecord medicalRecord;
        try {
            medicalRecord = findByListOrderByIdDesc(new MedicalRecord().setHospitalId(hospitalId).setStudentId(studentId).setStatus(MedicalRecord.STATUS_CHECKING))
                    .stream().findFirst().orElse(null);
            // 如果今天有检查单
            Date date = new Date();
            date.setHours(date.getHours()-8);
            if (Objects.nonNull(medicalRecord) && DateUtils.isSameDay(medicalRecord.getCreateTime(), date)) {
                return medicalRecord;
            }
        } catch (IOException e) {
            log.error("获取学生今天最后一条问诊失败. ", e);
        }
        return null;
    }

    /**
     * 创建检查单, 自动关联今天的问诊信息
     * @param medicalRecord 检查单
     */
    public MedicalRecord createMedicalRecordWithConsultation(MedicalRecord medicalRecord) {
        if (Objects.isNull(medicalRecord.getHospitalId())) {
            throw new BusinessException("医院id不能为空");
        }
        Consultation consultation = consultationService.getTodayLastConsultation(medicalRecord.getHospitalId(), medicalRecord.getStudentId());
        medicalRecord.setConsultationId(Objects.isNull(consultation) ? null : consultation.getId());
        baseMapper.insert(medicalRecord);
        return medicalRecord;
    }

    /** 创建检查单 */
    public MedicalRecord createMedicalRecord(Integer hospitalId,
                                             Integer doctorId,
                                             Integer studentId) {
        return createMedicalRecord(hospitalId, -1, doctorId, studentId);
    }

    /** 创建检查单 */
    public MedicalRecord createMedicalRecord(Integer hospitalId,
                                             Integer departmentId,
                                             Integer doctorId,
                                             Integer studentId) {
        return new MedicalRecord().setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
    }

}