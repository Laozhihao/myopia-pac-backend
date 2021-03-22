package com.wupol.myopia.business.management.domain.dos;

import com.wupol.myopia.business.management.domain.dto.DistributionDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Date 2021/3/18 19:28
 * @Author by Jacob
 */
@Getter
@Setter
public class BigScreenScreeningDO implements Serializable {


    public BigScreenScreeningDO() {

    }

    /**
     * 占比情况
     */
    private DistributionDTO distribution;
    /**
     * 实际筛查数
     */
    private Long realScreeningNum;
    /**
     * 地图位置
     */
    private List<MapLocationDataDTO> mapLocationData;

    @Data
    public static class MapLocationDataDTO {
        private String name;
        private Long value;
        private List<List<Double>> coords;
    }

    public BigScreenScreeningDO(DistributionDTO distributionDTO, Long realScreeningNum, List<MapLocationDataDTO> mapLocationData) {
        this.distribution = distributionDTO;
        this.realScreeningNum = realScreeningNum;
        this.mapLocationData = mapLocationData;
    }

}
