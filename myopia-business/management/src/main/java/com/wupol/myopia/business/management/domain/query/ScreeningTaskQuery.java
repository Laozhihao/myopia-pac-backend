package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 筛查任务查询
 *
 * @Author Alix
 * @Date 2021-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningTaskQuery extends ScreeningTask {
    /**
     * 筛查通知--开始时间（时间戳）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startCreateTime;
    /**
     * 筛查通知--结束时间（时间戳）
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
     * 创建人ID列表
     */
    private List<Integer> creatorIds;
    /**
     * 筛查通知--操作状态（0未读 1 是已读 2是已创建）
     */
    private Integer operationStatus;

    public LocalDate getEndCreateTime() {
        return Objects.isNull(endCreateTime) ? null : endCreateTime.plusDays(1L);
    }
}
