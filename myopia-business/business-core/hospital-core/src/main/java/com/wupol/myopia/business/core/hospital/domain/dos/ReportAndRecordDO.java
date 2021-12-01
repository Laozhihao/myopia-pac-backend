package com.wupol.myopia.business.core.hospital.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ReportAndRecordVo
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
public class ReportAndRecordDO {

    private Integer hospitalId;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    private String hospitalName;

    private Integer studentId;

    /**
     * 戴镜类型
     */
    private Integer glassesSituation;

    /**
     * 综合处方
     */
    private String medicalContent;

    /**
     * 医生Id
     */
    private Integer doctorId;

    /**
     * 医生名称
     */
    private String doctorName;
}
