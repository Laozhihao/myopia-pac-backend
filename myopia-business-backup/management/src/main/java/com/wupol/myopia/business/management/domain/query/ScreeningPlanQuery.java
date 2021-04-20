package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.interfaces.HasCreatorNameLikeAndCreateUserIds;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 筛查计划查询
 *
 * @author Alix
 * @Date 2021-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ScreeningPlanQuery extends ScreeningPlan implements HasCreatorNameLikeAndCreateUserIds<ScreeningPlanQuery> {
    /**
     * 筛查计划--开始时间（时间戳）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startCreateTime;
    /**
     * 筛查计划--结束时间（时间戳）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endCreateTime;
    /**
     * 名称
     */
    private String titleLike;
    /**
     * 创建人
     */
    private String creatorNameLike;
    /**
     * 筛查机构名称
     */
    private String screeningOrgNameLike;
    /**
     * 创建人ID列表
     */
    private List<Integer> createUserIds;
    /**
     * 筛查机构ID列表
     */
    private List<Integer> screeningOrgIds;
    /**
     * 筛查计划--操作状态（0未读 1 是已读 3是已创建）
     */
    private Integer operationStatus;
    /**
     * 层级ID列表
     */
    private List<Integer> districtIds;
    /**
     * 排除的计划ID
     */
    private Integer excludedScreeningPlanId;

    public LocalDate getEndCreateTime() {
        return Objects.isNull(endCreateTime) ? null : endCreateTime.plusDays(1L);
    }
}
