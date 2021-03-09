package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class AmetropiaDTO {


    /**
     * num
     */
    /**
     * num : 545
     * ratio : 25.5
     * distribution : {"num":{"studentNum":24332,"studentDistribution":15.3},"gender":{"female":15.3,"male":15.3},"age":{"0-3":23.3,"4-6":23.3,"7-9":23.3,"10-12":23.3,"13-15":23.3,"16-18":23.3,"18-20":23.3},"schoolAge":{"kindergarten":23.3,"primary":23.3,"junior":23.3,"high":23.3,"vocationalHigh":23.3,"university":23.3},"statisticDistrict":[{"cityName":"太原市","ratio":42.3}]}
     * mapLocationData : [{"name":"北京","coords":[[116.24,39.55],[120.24,46.55]],"value":75}]
     */

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
