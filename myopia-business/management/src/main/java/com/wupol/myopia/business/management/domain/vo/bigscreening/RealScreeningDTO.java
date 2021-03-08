package com.wupol.myopia.business.management.domain.vo.bigscreening;

import com.wupol.myopia.business.management.domain.vo.bigscreening.DistributionDTO;
import com.wupol.myopia.business.management.domain.vo.bigscreening.MapLocationDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class RealScreeningDTO {

    private DistributionDTO distribution;
    /**
     * realScreeningNum
     */
    private Integer realScreeningNum;
    /**
     * planScreeningNum
     */
    private Integer planScreeningNum;
    /**
     * mapLocationData
     */
    private List<MapLocationDataDTO> mapLocationData;
}
