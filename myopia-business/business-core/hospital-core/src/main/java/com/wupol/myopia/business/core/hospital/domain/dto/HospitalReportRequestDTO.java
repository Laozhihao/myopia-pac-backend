package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 医院就诊报告DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalReportRequestDTO {

    /**
     * 医院Id
     */
    private Integer hospitalId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 医生名称
     */
    private String doctorName;

    /**
     * 检查开始时间
     */
    private Date reportStartTime;

    /**
     * 检查结束时间
     */
    private Date reportEndTIme;
}
