package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 排行榜数据
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
public class RankingDataDO implements Serializable {
    private Double leftEyeVision;
    private Double rightEyeVision;

    public RankingDataDO(Double leftEyeVision, Double rightEyeVision) {
        this.leftEyeVision = leftEyeVision;
        this.rightEyeVision = rightEyeVision;
    }
}
