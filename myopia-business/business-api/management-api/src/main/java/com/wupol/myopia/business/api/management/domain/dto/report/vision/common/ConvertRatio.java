package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 变化比例
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConvertRatio {

    /**
     * 名称
     */
    private String name;

    /**
     * 比例
     */
    private String proportion;
}
