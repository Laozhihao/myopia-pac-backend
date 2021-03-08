package com.wupol.myopia.business.hospital.domain.dto;

import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
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
}
