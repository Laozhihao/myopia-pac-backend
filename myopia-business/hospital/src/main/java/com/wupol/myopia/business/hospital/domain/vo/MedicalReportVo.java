package com.wupol.myopia.business.hospital.domain.vo;

import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 医院的检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class MedicalReportVo extends MedicalReport {
    /** 医院名称 */
    private String hospitalName;

    /** 就诊时间 */
    private Date visitDate;
}
