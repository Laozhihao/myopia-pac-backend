package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppQueryQrCodeParams {
    /**
     * 筛查计划--计划id
     */
    @NotNull(message = "筛查计划ID不能为空")
    private Integer screeningPlanId;

    /**
     * 筛查计划--参与筛查的学生id
     */
    @NotNull(message = "筛查学生ID不能为空")
    private Integer studentId;

    /**
     * 二维码类型
     */
    @NotNull(message = "二维码类型不能为空")
    private Integer type;
}
