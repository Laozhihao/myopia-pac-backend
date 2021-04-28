package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

@Data
public class TableBasicStatParams extends BasicStatParams {
    /** 总值 */
    private Long total;

    public TableBasicStatParams(String title, Float ratio, Long num, Long total) {
        super(title, ratio, num);
        this.total = total;
    }
}
