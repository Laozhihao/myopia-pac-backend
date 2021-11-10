package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生跟踪预警请求DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentTrackWarningRequestDTO {

    /**
     * 计划Id
     */
    private Integer planId;

    /**
     * 班级Id
     */
    private Integer classId;

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 是否绑定公众号
     */
    private Boolean isBindMq;

    /**
     * 预警级别
     */
    private Integer visionLabel;

    /**
     * 是否复查
     */
    private Boolean isReview;
}
