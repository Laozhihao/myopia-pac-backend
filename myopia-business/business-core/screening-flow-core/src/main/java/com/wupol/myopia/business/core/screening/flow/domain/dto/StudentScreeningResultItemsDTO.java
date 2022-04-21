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

    private ScreeningInfoDTO details;


    /**
     * 筛查标题
     */
    private String screeningTitle;

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

    /**
     * 其他眼病
     */
    private List<String> otherEyeDiseases;

    /**
     * 预警级别
     */
    private Integer warningLevel;

    /**
     * 近视等级
     */
    private Integer myopiaLevel;

    /**
     * 远视等级
     */
    private Integer hyperopiaLevel;

    /**
     * 散光等级
     */
    private Integer astigmatismLevel;

    /**
     * 筛查计划id
     */
    private Integer planId;

    /**
     * 是否已经筛查过
     **/
    private Boolean hasScreening;

    /**
     * 筛查编码
     */
    private Long screeningCode;

    /**
     * 筛查类型
     */
    private Integer screeningType;
    /**
     * 筛查机构名称
     */
    private String screeningOrgName;
    /**
     * 性别
     */
    private Integer gender;

}
