package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比例和人数
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadioAndCount {

    /**
     * 比例
     */
    private String radio;

    /**
     * 人数
     */
    private Long count;
}
