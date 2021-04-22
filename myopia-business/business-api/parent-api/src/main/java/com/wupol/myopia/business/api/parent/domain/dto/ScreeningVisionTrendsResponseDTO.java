package com.wupol.myopia.business.api.parent.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.CorrectedVisionDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dos.CylDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dos.NakedVisionDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SphDetails;
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
