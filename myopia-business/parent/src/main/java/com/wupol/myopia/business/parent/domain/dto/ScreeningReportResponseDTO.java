package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 筛查报告统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningReportResponseDTO {

    /**
     * 检查日期
     */
    private Date screeningDate;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 视力检查结果
     */
    private List<VisionItems> visionList;

    /**
     * 医生建议1
     */
    private Integer DoctorAdvice1;

    /**
     * 医生建议2
     */
    private String DoctorAdvice2;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResultItems> refractoryResultItems;

    private List<BiometricItems> biometricItems;

}
