package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import lombok.Data;

/**
 * 幼儿园儿童视力筛查情况
 *
 * @author Simple4H
 */
@Data
public class Table2 {

    /**
     * 有效筛查人数
     */
    private Long validCount;

    /**
     * 裸眼平均视力
     */
    private String avgVision;

    /**
     * 视力低常率
     */
    private RadioAndCount lowVision;

    /**
     * 屈光不正率
     */
    private RadioAndCount refractiveErrorVision;

    /**
     * 屈光参差率
     */
    private RadioAndCount anisometropia;

    /**
     * 远视储备不足率
     */
    private RadioAndCount insufficient;
}
