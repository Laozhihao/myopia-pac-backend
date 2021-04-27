package com.wupol.myopia.business.core.stat.domain.dos;

import com.wupol.myopia.business.core.stat.domain.dto.DistributionDTO;
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
     * 计划筛查数
     */
    private Long  planScreeningNum;
    /**
     * 占比情况
     */
    private DistributionDTO distribution;
    /**
     * 实际筛查数
     */
    private Long realScreeningNum;
    /**
     * 数量
     */
    private Long num;
    /**
     * 百分比
     */
    private double ratio;
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
        if (distributionDTO.getNum() != null) {
            this.num = distributionDTO.getNum().getStudentNum();
            this.ratio = distributionDTO.getNum().getStudentDistribution();
        }
    }

    public void setPlanScreeningStudentNum(Long  planScreeningNum){
        this.planScreeningNum = planScreeningNum;
    }

}
