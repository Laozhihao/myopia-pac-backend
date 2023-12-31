package com.wupol.myopia.business.core.screening.flow.domain.dto;


import com.wupol.myopia.business.common.utils.interfaces.HasCreatorNameLikeAndCreateUserIds;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 筛查任务查询
 *
 * @author Alix
 * @Date 2021-01-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ScreeningTaskQueryDTO extends ScreeningTask implements HasCreatorNameLikeAndCreateUserIds<ScreeningTaskQueryDTO> {
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
    private List<Integer> createUserIds;
    /**
     * 筛查通知--操作状态（0未读 1 是已读 2是已创建）
     */
    private Integer operationStatus;

    /**
     * 筛查机构类型
     */
    private Integer screeningOrgType;

    public LocalDate getEndCreateTime() {
        return Objects.isNull(endCreateTime) ? null : endCreateTime.plusDays(1L);
    }
}
