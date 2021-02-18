package com.wupol.myopia.business.management.domain.dto.stat;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class ClassStat extends BasicStatParams {
    /** 分类统计项 */
    private List<BasicStatParams> items;

    @Builder
    public ClassStat(String title, Float ratio, Integer num, List<BasicStatParams> items) {
        super(title, ratio, num);
        this.items = items;
    }
}
