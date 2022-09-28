package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新筛查结束时间
 *
 * @author hang.yuan 2022/9/16 10:10
 */
@Data
public class ScreeningEndTimeDTO implements Serializable {
    /**
     * 筛查计划ID
     */
    @NotNull(message = "筛查计划ID不能为空")
    private Integer screeningPlanId;

    /**
     * 筛查计划--结束时间
     */
    @NotBlank(message = "筛查计划结束时间不能为空")
    private String endTime;
}
