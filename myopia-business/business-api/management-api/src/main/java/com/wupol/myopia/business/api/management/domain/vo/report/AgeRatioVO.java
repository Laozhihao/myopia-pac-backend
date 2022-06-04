package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

@Data
public class AgeRatioVO {
    /**
     * 最高年龄段
     */
    private String maxAge;
    /**
     * 最小年龄段
     */
    private String minAge;
    /**
     * 最高占比
     */
    private String maxRatio;
    /**
     * 最低占比
     */
    private String minRatio;
}