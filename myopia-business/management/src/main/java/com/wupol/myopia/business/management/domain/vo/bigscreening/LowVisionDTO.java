package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@NoArgsConstructor
@Data
public class LowVisionDTO {


    private Integer num;
    /**
     * ratio
     */
    private Double ratio;
    /**
     * distribution
     */
    private DistributionDTO distribution;
    /**
     * mapLocationData
     */
    private List<MapLocationDataDTO> mapLocationData;
}
