package com.wupol.myopia.business.core.parent.domian.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 视力趋势
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningVisionTrendsResponseDTO {
    /**
     * 矫正视力详情
     */
    private List<CorrectedVisionDetails> correctedVisionDetails;

    /**
     * 柱镜详情
     */
    private List<CylDetails> cylDetails;

    /**
     * 球镜详情
     */
    private List<SphDetails> sphDetails;

    /**
     * 裸眼视力详情
     */
    private List<NakedVisionDetails> nakedVisionDetails;
}
