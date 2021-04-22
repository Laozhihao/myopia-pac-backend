package com.wupol.myopia.business.core.hospital.domain.dos;

import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 医院的检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MedicalReportDO extends MedicalReport {
    /** 医院名称 */
    private String hospitalName;
    /** 医生名称 */
    private String doctorName;
}
