package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 学生筛查档案
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Data
public class StudentScreeningResultItemsDTO {

    /**
     * 筛查数据详情
     */
    private ScreeningInfoDTO details;

    /**
     * 常见病编码code
     */
    private String commonDiseasesCode;

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
    private String glassesTypeDes;

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
    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 筛查结果--所属的学生id
     */
    private Integer planStudentId;

    /**
     * 筛查计划--发布状态 （0-未发布、1-已发布、2-作废）
     */
    private Integer releaseStatus;
}
