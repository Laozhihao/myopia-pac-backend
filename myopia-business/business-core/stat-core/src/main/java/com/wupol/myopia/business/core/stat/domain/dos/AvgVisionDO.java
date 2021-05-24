package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
@NoArgsConstructor
public class AvgVisionDO implements Serializable {
    private Double leftEyeVision;
    private Double rightEyeVision;

    public AvgVisionDO(Double leftEyeVision, Double rightEyeVision) {
        this.leftEyeVision = leftEyeVision;
        this.rightEyeVision = rightEyeVision;
    }
}
