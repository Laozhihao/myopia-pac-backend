package com.wupol.myopia.business.hospital.domain.dto;

import com.wupol.myopia.business.hospital.domain.model.Doctor;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.vo.DoctorVo;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生就诊记录档案卡
 *
 * @author Simple4H
 */
@Data
@Accessors
public class StudentReportResponseDTO {

    /**
     * 报告
     */
    private MedicalReportVo report;

    /**
     * 检查单
     */
    private MedicalRecord record;

    /**
     * 学生
     */
    private Student student;

    /**
     * 医生
     */
    private DoctorVo doctor;
}
