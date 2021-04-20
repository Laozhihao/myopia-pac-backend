package com.wupol.myopia.business.management.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
@NoArgsConstructor
public class AvgVisionDO {
    private Double leftEyeVision;
    private Double rightEyeVision;

    public AvgVisionDO(Double leftEyeVision, Double rightEyeVision) {
        this.leftEyeVision = leftEyeVision;
        this.rightEyeVision = rightEyeVision;
    }
}
