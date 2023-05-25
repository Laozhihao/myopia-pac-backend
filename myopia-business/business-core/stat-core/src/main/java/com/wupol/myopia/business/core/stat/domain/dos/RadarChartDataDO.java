package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 雷达图数据
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
public class RadarChartDataDO implements Serializable {
    private Double leftEyeVision;
    private Double rightEyeVision;

    public RadarChartDataDO(Double leftEyeVision, Double rightEyeVision) {
        this.leftEyeVision = leftEyeVision;
        this.rightEyeVision = rightEyeVision;
    }
}
