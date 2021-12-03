package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
    @NotNull(message = "planId不能为空")
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
    private Boolean isBindMp;

    /**
     * 预警级别
     */
    private Integer visionLabel;

    /**
     * 是否复查
     */
    private Boolean isReview;
}
