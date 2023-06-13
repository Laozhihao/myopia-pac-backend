package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import lombok.Data;

/**
 * 幼儿园儿童视力筛查情况
 *
 * @author Simple4H
 */
@Data
public class Table3 {

    /**
     * 有效筛查人数
     */
    private Long validCount;

    /**
     * 裸眼平均视力
     */
    private String avgVision;

    /**
     * 近视率
     */
    private RadioAndCount myopia;

    /**
     * 视力不良率
     */
    private RadioAndCount lowVision;

    /**
     * 视力未矫率
     */
    private RadioAndCount uncorrected;

    /**
     * 视力欠矫率
     */
    private RadioAndCount under;

    /**
     * 低度近视率
     */
    private RadioAndCount lightMyopia;

    /**
     * 高度近视率
     */
    private RadioAndCount highMyopia;
}
