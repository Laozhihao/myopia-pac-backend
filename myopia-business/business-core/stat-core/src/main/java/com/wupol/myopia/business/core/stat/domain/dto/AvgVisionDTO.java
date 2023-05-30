package com.wupol.myopia.business.core.stat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Simple4H
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvgVisionDTO implements Serializable {

    private String leftEyeVision;

    private String rightEyeVision;
}
