package com.wupol.myopia.business.management.domain.builder;

import com.wupol.myopia.business.management.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.management.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.dto.DistributionDTO;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.util.MathUtil;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * DistrictBigScreenStatistic 构造器
 * @Author jacob
 * @Date 2021-03-07
 */
public class DistrictBigScreenStatisticBuilder {


    private List<BigScreenStatDataDTO> bigScreenStatDataDTOList;
    private Long realScreeningNum;
    private Long planScreeningNum;
    private Object mapJsonObject;
    private Map<Integer, List<Double>> cityCenterMap;
    private Integer districtId;
    private Integer noticeId;

    public static DistrictBigScreenStatisticBuilder getBuilder() {
        return new DistrictBigScreenStatisticBuilder();
    }

    public DistrictBigScreenStatisticBuilder setBigScreenStatDataDTOList(List<BigScreenStatDataDTO> bigScreenStatDataDTOList) {
        this.bigScreenStatDataDTOList = bigScreenStatDataDTOList;
        return this;
    }

    /**
     * 获取城市中心经纬度
     *
     * @param cityCenterMap
     * @return
     */
    public DistrictBigScreenStatisticBuilder setCityCenterMap(Map<Integer, List<Double>> cityCenterMap) {
        this.cityCenterMap = cityCenterMap;
        return this;
    }

    /**
     * 构建
     *
     * @return
     */
    public DistrictBigScreenStatistic build() {
        if (CollectionUtils.isEmpty(bigScreenStatDataDTOList)) {

        }

        DistrictBigScreenStatistic districtBigScreenStatistic = new DistrictBigScreenStatistic();
        //获取真实的数据
        BigScreenScreeningDO realScreeningData = this.getScreeningData(bigScreenStatDataDTOList);
        realScreeningData.setPlanScreeningStudentNum(planScreeningNum);
        districtBigScreenStatistic.setRealScreening(realScreeningData);
        //获取视力低下的地区
        List<BigScreenStatDataDTO> lowVisionBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(BigScreenStatDataDTO::getIsLowVision).collect(Collectors.toList());
        BigScreenScreeningDO lowVisionScreeningData = this.getScreeningData(lowVisionBigScreenStatDataDTOs);
        districtBigScreenStatistic.setLowVision(lowVisionScreeningData);
        //获取屈光不正
        List<BigScreenStatDataDTO> refractiveErrorBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(BigScreenStatDataDTO::getIsRefractiveError).collect(Collectors.toList());
        BigScreenScreeningDO refractiveErrorScreeningData = this.getScreeningData(refractiveErrorBigScreenStatDataDTOs);
        districtBigScreenStatistic.setAmetropia(refractiveErrorScreeningData);
        //近视
        List<BigScreenStatDataDTO> myopiaBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(BigScreenStatDataDTO::getIsMyopia).collect(Collectors.toList());
        BigScreenScreeningDO myopiaScreeningData = this.getScreeningData(myopiaBigScreenStatDataDTOs);
        districtBigScreenStatistic.setMyopia(myopiaScreeningData);
        //重点视力对象
        List<BigScreenStatDataDTO> focusObjectBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(bigScreenStatDataDTO -> bigScreenStatDataDTO.getWarningLevel() > 0).collect(Collectors.toList());
        BigScreenScreeningDO focusScreeningData = this.getScreeningData(focusObjectBigScreenStatDataDTOs);
        districtBigScreenStatistic.setFocusObjects(focusScreeningData);
        //平均视力
        TwoTuple<Double, Double> leftRightNakedVision = this.getAvgNakedVision();
        //其他数据
        districtBigScreenStatistic.setValidDataNum(bigScreenStatDataDTOList.stream().count());
        districtBigScreenStatistic.setMapdata(mapJsonObject);
        districtBigScreenStatistic.setAvgVision(new AvgVisionDO(leftRightNakedVision.getFirst(), leftRightNakedVision.getSecond()));
        districtBigScreenStatistic.setDistrictId(districtId);
        districtBigScreenStatistic.setScreeningNoticeId(noticeId);
        return districtBigScreenStatistic;
    }


    /**
     * first是左边，second 是右边
     *
     * @return
     */
    private TwoTuple<Double, Double> getAvgNakedVision() {
        OptionalDouble avgVisionR = bigScreenStatDataDTOList.stream().mapToDouble(BigScreenStatDataDTO::getVisionR).average();
        OptionalDouble avgVisionL = bigScreenStatDataDTOList.stream().mapToDouble(BigScreenStatDataDTO::getVisionL).average();
        TwoTuple<Double, Double> leftAndRightAvgVisionData = new TwoTuple<>();
        leftAndRightAvgVisionData.setFirst(MathUtil.getFormatNumWith1Scale(avgVisionL.getAsDouble()));
        leftAndRightAvgVisionData.setSecond(MathUtil.getFormatNumWith1Scale(avgVisionR.getAsDouble()));
        return leftAndRightAvgVisionData;
    }

    /**
     * @param noticeId
     * @return
     */
    public DistrictBigScreenStatisticBuilder setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
        return this;
    }
    /**
     * @param realScreeningNum
     * @return
     */
    public DistrictBigScreenStatisticBuilder setRealScreeningNum(Long realScreeningNum) {
        this.realScreeningNum = realScreeningNum;
        return this;
    }
    /**
     * @param planScreeningNum
     * @return
     */
    public DistrictBigScreenStatisticBuilder setPlanScreeningNum(Long planScreeningNum) {
        this.planScreeningNum = planScreeningNum;
        return this;
    }

    /**
     * 设置地区id
     * @param districtId
     * @return
     */
    public DistrictBigScreenStatisticBuilder setDistrictId(Integer districtId) {
        this.districtId = districtId;
        return this;
    }

    /**
     * 设置jsonMap
     * @param mapJsonObject
     * @return
     */
    public DistrictBigScreenStatisticBuilder setMapJson(Object mapJsonObject) {
        this.mapJsonObject = mapJsonObject;
        return this;
    }

    /**
     * 获取筛查数据
     *
     * @param bigScreenStatDataDTOList
     * @return b
     */
    private BigScreenScreeningDO getScreeningData(List<BigScreenStatDataDTO> bigScreenStatDataDTOList) {
        if (CollectionUtils.isEmpty(bigScreenStatDataDTOList)) {
            return null;
        }
        DistributionDTO distributionDTO = DistributionDTO.Builder.getBuilder()
                .setScreeningStudentNum(bigScreenStatDataDTOList.stream().count())
                .setBigScreenStatDataDTOList(bigScreenStatDataDTOList)
                .setRealScreeningNum(realScreeningNum)
                .build();
        //设置地图数据
        List<BigScreenScreeningDO.MapLocationDataDTO> mapLocationData =  this.getMapLocationData(distributionDTO.getStatisticDistrict());
        return new BigScreenScreeningDO(distributionDTO, realScreeningNum, mapLocationData);
    }

    /**
     * 获取地图数据
     * @param statisticDistrictList
     * @return
     */
    private List<BigScreenScreeningDO.MapLocationDataDTO> getMapLocationData(List<DistributionDTO.StatisticDistrictDTO> statisticDistrictList) {
        List<BigScreenScreeningDO.MapLocationDataDTO> mapLocationData = statisticDistrictList.stream().map(statisticDistrictDTO -> {
            BigScreenScreeningDO.MapLocationDataDTO mapLocationDataDTO = new BigScreenScreeningDO.MapLocationDataDTO();
            mapLocationDataDTO.setName(statisticDistrictDTO.getCityName());
            mapLocationDataDTO.setValue(statisticDistrictDTO.getNum());
            List<Double> cityCenter = cityCenterMap.get(statisticDistrictDTO.getCityDistrictId()+"");
            ArrayList<List<Double>> locationList = new ArrayList<>();
            locationList.add(cityCenter);
            locationList.add(cityCenter);
            mapLocationDataDTO.setCoords(locationList);
            return mapLocationDataDTO;
        }).collect(Collectors.toList());
        return mapLocationData;
    }

}
