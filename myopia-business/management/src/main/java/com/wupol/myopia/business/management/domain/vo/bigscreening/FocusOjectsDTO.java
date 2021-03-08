package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class FocusOjectsDTO {

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
