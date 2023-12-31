package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基础百分比
 *
 * @author Simple4H
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountAndProportion {

    /**
     * 人数
     */
    private Long count;

    /**
     * 百分比
     */
    private String proportion;
}
