package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
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
    private MedicalReport report;

    /**
     * 检查单
     */
    private MedicalRecord record;
}
