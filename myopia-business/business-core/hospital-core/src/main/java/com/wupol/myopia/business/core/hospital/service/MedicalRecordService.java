package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.mapper.MedicalRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医院-检查单
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class MedicalRecordService extends BaseService<MedicalRecordMapper, MedicalRecord> {

    /**
     * 获取学生最后一条检查记录
     * @param studentId 学生id
     * @return
     */
    public MedicalRecord getLastOneByStudentId(Integer studentId) {
        return baseMapper.getLastOneByStudentId(studentId);
    }

    public List<MedicalRecord> getBy(MedicalRecordQuery query) {
        return baseMapper.getBy(query);
    }

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
     * 获取学生今天最后一条检查单
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalRecord getTodayLastMedicalRecord(Integer hospitalId, Integer studentId) {
        return baseMapper.getTodayLastMedicalRecord(hospitalId, studentId);
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
        MedicalRecord medicalRecord = new MedicalRecord().setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        baseMapper.insert(medicalRecord);
        return medicalRecord;
    }
}