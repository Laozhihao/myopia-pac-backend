package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 年龄段占比
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class AgeRatioVO {
    /**
     * 最高年龄段
     */
    private String maxName;
    /**
     * 最小年龄段
     */
    private String minName;
    /**
     * 最高占比
     */
    private String maxProportion;
    /**
     * 最低占比
     */
    private String minProportion;
}