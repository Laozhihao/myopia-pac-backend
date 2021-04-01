package com.wupol.myopia.business.management.domain.builder;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.management.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.dto.DistributionDTO;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.util.MathUtil;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DistrictBigScreenStatistic 构造器
 *
 * @Author jacob
 * @Date 2021-03-07
 */
public class DistrictBigScreenStatisticBuilder {


    private List<BigScreenStatDataDTO> bigScreenStatDataDTOList;
    private long realValidScreeningNum;
    private long planScreeningNum;
    private Object mapJsonObject;
    private Map<Integer, List<Double>> cityCenterMap;
    private Integer districtId;
    private Integer noticeId;
    private long realScreeningNum;

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
        // 最基本的参数校验
        if (ObjectsUtil.hasNull(realScreeningNum, realScreeningNum, planScreeningNum, mapJsonObject, districtId, noticeId)) {
            throw new ManagementUncheckedException("构建DistrictBigScreenStatistic失败，基本参数不足");
        }
        DistrictBigScreenStatistic districtBigScreenStatistic = new DistrictBigScreenStatistic();
        if (realScreeningNum > 0 && realValidScreeningNum > 0 && CollectionUtils.size(bigScreenStatDataDTOList) > 0) {
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
            districtBigScreenStatistic.setAvgVision(new AvgVisionDO(leftRightNakedVision.getFirst(), leftRightNakedVision.getSecond()));
        }
        //其他数据
        districtBigScreenStatistic.setValidDataNum(realValidScreeningNum);
        districtBigScreenStatistic.setRealScreeningNum(realScreeningNum);
        districtBigScreenStatistic.setPlanScreeningNum(planScreeningNum);
        Double progressRate = 0.0D;
        if (planScreeningNum > 0) {
            progressRate = MathUtil.getFormatNumWith2Scale((double) realScreeningNum / planScreeningNum * 100);
        }
        districtBigScreenStatistic.setProgressRate(progressRate);
        districtBigScreenStatistic.setMapdata(mapJsonObject);
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
     * @param realValidScreeningNum
     * @return
     */
    public DistrictBigScreenStatisticBuilder setRealValidScreeningNum(Long realValidScreeningNum) {
        this.realValidScreeningNum = realValidScreeningNum;
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
     *
     * @param districtId
     * @return
     */
    public DistrictBigScreenStatisticBuilder setDistrictId(Integer districtId) {
        this.districtId = districtId;
        return this;
    }

    /**
     * 设置jsonMap
     *
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
                .setRealScreeningNum(realValidScreeningNum)
                .build();
        //设置地图数据
        List<BigScreenScreeningDO.MapLocationDataDTO> mapLocationData = this.getMapLocationData(distributionDTO.getStatisticDistrict());
        return new BigScreenScreeningDO(distributionDTO, realValidScreeningNum, mapLocationData);
    }

    /**
     * 获取地图数据
     *
     * @param statisticDistrictList
     * @return
     */
    private List<BigScreenScreeningDO.MapLocationDataDTO> getMapLocationData(List<DistributionDTO.StatisticDistrictDTO> statisticDistrictList) {
        List<BigScreenScreeningDO.MapLocationDataDTO> mapLocationData = statisticDistrictList.stream().map(statisticDistrictDTO -> {
            BigScreenScreeningDO.MapLocationDataDTO mapLocationDataDTO = new BigScreenScreeningDO.MapLocationDataDTO();
            mapLocationDataDTO.setName(statisticDistrictDTO.getCityName());
            mapLocationDataDTO.setValue(statisticDistrictDTO.getNum());
            List<Double> cityCenter = cityCenterMap.get(statisticDistrictDTO.getCityDistrictId() + "");
            ArrayList<List<Double>> locationList = new ArrayList<>();
            locationList.add(cityCenter);
            locationList.add(cityCenter);
            mapLocationDataDTO.setCoords(locationList);
            return mapLocationDataDTO;
        }).collect(Collectors.toList());
        return mapLocationData;
    }

}
