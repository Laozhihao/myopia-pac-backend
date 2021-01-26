package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningNoticeQuery extends ScreeningNotice {
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

    public LocalDate getEndCreateTime() {
        return Objects.isNull(endCreateTime) ? null : endCreateTime.plusDays(1L);
    }
}
