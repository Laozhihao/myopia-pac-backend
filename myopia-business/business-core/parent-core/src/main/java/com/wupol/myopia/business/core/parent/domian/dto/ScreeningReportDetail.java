package com.wupol.myopia.business.core.parent.domian.dto;

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
public class ScreeningReportDetail {

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
    private List<VisionItems> visionResultItems;

    /**
     * 医生建议1 0-正常,1-轻度屈光不正,2-中度屈光不正,3-重度屈光不正
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

    /**
     * 生物测量
     */
    private List<BiometricItems> biometricItems;

}
