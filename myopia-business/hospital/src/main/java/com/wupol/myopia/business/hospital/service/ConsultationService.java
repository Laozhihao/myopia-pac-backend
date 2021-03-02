package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.ConsultationMapper;
import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * 医院-学生问诊
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class ConsultationService extends BaseService<ConsultationMapper, Consultation> {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * 追加问诊数据到检查单
     * @param consultation 问诊内容
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addConsulationToMedicalRecord(Consultation consultation,
                                            Integer hospitalId,
                                            Integer departmentId,
                                            Integer doctorId,
                                            Integer studentId) {
        Integer consulationId = createConsultation(consultation);
        MedicalRecord medicalRecord = medicalRecordService.getOrCreateTodayMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        medicalRecord.setConsultationId(consulationId);
        if (!medicalRecordService.updateById(medicalRecord)) {
            throw new BusinessException("修改失败");
        }
    }

    /**
     * 创建问诊
     * @param consultation 问诊内容
     */
    public Integer createConsultation(Consultation consultation) {
        return baseMapper.insert(consultation);

    }

    /**
     * 获取学生今天最后一条问诊
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public Consultation getTodayLastConsultation(Integer hospitalId, Integer studentId) {
        Consultation consultation;
        try {
            consultation = findByListOrderByIdDesc(new Consultation().setHospitalId(hospitalId).setStudentId(studentId))
                    .stream().findFirst().orElse(null);
            // 如果今天有问诊单
            if (Objects.nonNull(consultation) && DateUtils.isSameDay(consultation.getCreateTime(), new Date())) {
                return consultation;
            }
        } catch (IOException e) {
            log.error("获取学生今天最后一条问诊失败. ", e);
        }
        return null;
    }



}