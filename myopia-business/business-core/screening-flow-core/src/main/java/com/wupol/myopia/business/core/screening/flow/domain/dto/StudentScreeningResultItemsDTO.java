package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 学生筛查档案
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentScreeningResultItemsDTO {

    /**
     * 详情
     */
    private List<StudentResultDetailsDTO> details;

    /**
     * 筛查时间
     */
    private Date screeningDate;

    /**
     * 眼睛类型
     */
    private String glassesType;

    /**
     * 筛查结果表ID
     */
    private Integer resultId;

    /**
     * 筛查结果--是否复筛（0否，1是）
     */
    private Boolean isDoubleScreen;

    /**
     * 模板Id
     */
    private Integer templateId;
}
