package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
     * 医院名称
     */
    private String hospitalName;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 检查开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportStartTime;

    /**
     * 检查结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportEndTIme;
}
