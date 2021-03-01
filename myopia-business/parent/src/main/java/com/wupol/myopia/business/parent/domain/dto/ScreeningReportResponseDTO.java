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
    private String glassesType;

    /**
     * 视力检查结果
     */
    private List<NakedVisionItems> nakedVisionItems;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResultItems> refractoryResultItems;

}
