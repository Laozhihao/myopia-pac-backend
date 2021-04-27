package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class ClassStat extends BasicStatParams {
    /** 分类统计项 */
    private List<BasicStatParams> items;

    @Builder
    public ClassStat(String title, Float ratio, Long num, List<BasicStatParams> items) {
        super(title, ratio, num);
        this.items = items;
    }
}
