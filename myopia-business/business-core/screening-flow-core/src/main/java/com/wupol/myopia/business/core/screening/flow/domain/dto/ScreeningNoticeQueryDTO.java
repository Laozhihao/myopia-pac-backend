package com.wupol.myopia.business.core.screening.flow.domain.dto;


import com.wupol.myopia.business.common.utils.interfaces.HasCreatorNameLikeAndCreateUserIds;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 筛查通知查询
 *
 * @author Alix
 * @Date 2021-1-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ScreeningNoticeQueryDTO extends ScreeningNotice implements HasCreatorNameLikeAndCreateUserIds<ScreeningNoticeQueryDTO> {
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

    public LocalDate getEndCreateTime() {
        return Objects.isNull(endCreateTime) ? null : endCreateTime.plusDays(1L);
    }
}
