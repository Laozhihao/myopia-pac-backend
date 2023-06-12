package com.wupol.myopia.business.api.management.domain.builder;

import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.stat.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.core.stat.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.core.stat.domain.dos.RadarChartDataDO;
import com.wupol.myopia.business.core.stat.domain.dos.RankingDataDO;
import com.wupol.myopia.business.core.stat.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.core.stat.domain.dto.DistributionDTO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
    private Map<String, List<Double>> cityCenterMap;
    private Integer districtId;
    private Integer noticeId;
    private long realScreeningNum;
    private Boolean isProvince;

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
    public DistrictBigScreenStatisticBuilder setCityCenterMap(Map<String, List<Double>> cityCenterMap) {
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
        if (ObjectsUtil.hasNull(realScreeningNum, realScreeningNum, planScreeningNum, districtId, noticeId)) {
            throw new ManagementUncheckedException("构建DistrictBigScreenStatistic失败，基本参数不足");
        }
        DistrictBigScreenStatistic districtBigScreenStatistic = new DistrictBigScreenStatistic();
        if (realScreeningNum > 0 && realValidScreeningNum > 0 && CollectionUtils.size(bigScreenStatDataDTOList) > 0) {
            //获取真实的数据
            BigScreenScreeningDO realScreeningData = this.getScreeningData(bigScreenStatDataDTOList);
            //特殊处理
            realScreeningData.setNum(realValidScreeningNum);
            DistributionDTO.NumDTO num = realScreeningData.getDistribution().getNum();
            num.setRealScreeningNum(realScreeningNum);
            num.setStudentNum(realValidScreeningNum);
            num.setStudentDistribution(MathUtil.getFormatNumWith2Scale(realValidScreeningNum / (double) realScreeningNum * 100));
            districtBigScreenStatistic.setRealScreening(realScreeningData);
            //获取视力低下的地区
            List<BigScreenStatDataDTO> lowVisionBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(bss -> Objects.equals(Boolean.TRUE, bss.getIsLowVision())).collect(Collectors.toList());
            BigScreenScreeningDO lowVisionScreeningData = this.getScreeningData(lowVisionBigScreenStatDataDTOs);
            districtBigScreenStatistic.setLowVision(lowVisionScreeningData);
            //获取屈光不正
            List<BigScreenStatDataDTO> refractiveErrorBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(bss -> Objects.equals(Boolean.TRUE, bss.getIsRefractiveError())).collect(Collectors.toList());
            BigScreenScreeningDO refractiveErrorScreeningData = this.getScreeningData(refractiveErrorBigScreenStatDataDTOs);
            districtBigScreenStatistic.setAmetropia(refractiveErrorScreeningData);
            //近视
            List<BigScreenStatDataDTO> myopiaBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(bss -> Objects.equals(Boolean.TRUE, bss.getIsMyopia())).collect(Collectors.toList());
            BigScreenScreeningDO myopiaScreeningData = this.getScreeningData(myopiaBigScreenStatDataDTOs);
            districtBigScreenStatistic.setMyopia(myopiaScreeningData);
            //重点视力对象
            List<BigScreenStatDataDTO> focusObjectBigScreenStatDataDTOs = bigScreenStatDataDTOList.stream().filter(bss -> Objects.nonNull(bss.getWarningLevel()) && bss.getWarningLevel() > 0).collect(Collectors.toList());
            BigScreenScreeningDO focusScreeningData = this.getScreeningData(focusObjectBigScreenStatDataDTOs);
            districtBigScreenStatistic.setFocusObjects(focusScreeningData);
            //平均视力
            TwoTuple<Double, Double> leftRightNakedVision = this.getAvgNakedVision();
            districtBigScreenStatistic.setAvgVision(new AvgVisionDO(leftRightNakedVision.getFirst(), leftRightNakedVision.getSecond()));

            districtBigScreenStatistic.setRadarChartData(generateRadarChartDataDO());
            districtBigScreenStatistic.setRankingData(generateRankingDataDO());
        }
        //其他数据
        districtBigScreenStatistic.setValidDataNum(realValidScreeningNum);
        districtBigScreenStatistic.setRealScreeningNum(realScreeningNum);
        districtBigScreenStatistic.setPlanScreeningNum(planScreeningNum);
        double progressRate = 0.0D;
        if (planScreeningNum > 0) {
            progressRate = MathUtil.getFormatNumWith2Scale((double) realScreeningNum / planScreeningNum * 100);
        }
        districtBigScreenStatistic.setProgressRate(progressRate);
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
        OptionalDouble avgVisionR = bigScreenStatDataDTOList.stream().filter(x -> Objects.nonNull(x.getVisionR())).mapToDouble(bs -> bs.getVisionR().doubleValue()).average();
        OptionalDouble avgVisionL = bigScreenStatDataDTOList.stream().filter(x -> Objects.nonNull(x.getVisionL())).mapToDouble(bs -> bs.getVisionL().doubleValue()).average();
        TwoTuple<Double, Double> leftAndRightAvgVisionData = new TwoTuple<>();

        leftAndRightAvgVisionData.setFirst(MathUtil.getFormatNumWith1Scale(avgVisionL.isPresent() ? avgVisionL.getAsDouble() : null));
        leftAndRightAvgVisionData.setSecond(MathUtil.getFormatNumWith1Scale(avgVisionR.isPresent() ? avgVisionR.getAsDouble() : null));
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
     * 设置是否省级
     */
    public DistrictBigScreenStatisticBuilder setIsProvince(Boolean isProvince) {
        this.isProvince = isProvince;
        return this;
    }

    /**
     * 获取筛查数据
     *
     * @param bigScreenStatDataDTOList
     * @return b
     */
    private BigScreenScreeningDO getScreeningData(List<BigScreenStatDataDTO> bigScreenStatDataDTOList) {
        DistributionDTO distributionDTO = DistributionDTO.Builder.getBuilder()
                .setScreeningStudentNum((long) bigScreenStatDataDTOList.size())
                .setBigScreenStatDataDTOList(bigScreenStatDataDTOList)
                .setRealScreeningNum(realValidScreeningNum)
                .setIsProvince(isProvince)
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
        if (Objects.equals(isProvince, Boolean.FALSE)) {
            return new ArrayList<>();
        }
        return statisticDistrictList.stream().map(statisticDistrictDTO -> {
            BigScreenScreeningDO.MapLocationDataDTO mapLocationDataDTO = new BigScreenScreeningDO.MapLocationDataDTO();
            mapLocationDataDTO.setName(statisticDistrictDTO.getCityName());
            mapLocationDataDTO.setValue(statisticDistrictDTO.getNum());
            List<Double> cityCenter = cityCenterMap.get(String.valueOf(statisticDistrictDTO.getCityDistrictId()));
            ArrayList<List<Double>> locationList = new ArrayList<>();
            locationList.add(cityCenter);
            locationList.add(cityCenter);
            mapLocationDataDTO.setCoords(locationList);
            return mapLocationDataDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 雷达图
     *
     * @return RadarChartDataDO
     */
    private RadarChartDataDO generateRadarChartDataDO() {
        List<BigScreenStatDataDTO> validList = bigScreenStatDataDTOList.stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).collect(Collectors.toList());
        RadarChartDataDO radarChartDataDO = new RadarChartDataDO();

        long lowVisionCount = validList.stream().filter(s -> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
        long screeningMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getScreeningMyopia(), MyopiaLevelEnum.SCREENING_MYOPIA.getCode())).count();
        long highMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.getCode())).count();
        long lightMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.getCode())).count();
        long earlyMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.getCode())).count();
        long astigmatismCount = validList.stream().filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count();

        radarChartDataDO.setLowVisionCount(lowVisionCount);
        radarChartDataDO.setScreeningMyopiaCount(screeningMyopiaCount);
        radarChartDataDO.setLightMyopiaCount(lightMyopiaCount);
        radarChartDataDO.setHighMyopiaCount(highMyopiaCount);
        radarChartDataDO.setEarlyMyopiaCount(earlyMyopiaCount);
        radarChartDataDO.setAstigmatismCount(astigmatismCount);

        RadarChartDataDO.Item item = new RadarChartDataDO.Item();
        item.setName("全部");
        item.setValue(Lists.newArrayList(lowVisionCount, screeningMyopiaCount, highMyopiaCount, lightMyopiaCount, earlyMyopiaCount, astigmatismCount));

        radarChartDataDO.setData(Lists.newArrayList(item, genderRadarChart(validList, GenderEnum.MALE), genderRadarChart(validList, GenderEnum.FEMALE)));
        return radarChartDataDO;
    }

    /**
     * 性别雷达图
     */
    private RadarChartDataDO.Item genderRadarChart(List<BigScreenStatDataDTO> validList, GenderEnum genderEnum) {
        long lowVisionCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
        long screeningMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getScreeningMyopia(), MyopiaLevelEnum.SCREENING_MYOPIA.getCode())).count();
        long highMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.getCode())).count();
        long lightMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.getCode())).count();
        long earlyMyopiaCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getMyopiaLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.getCode())).count();
        long astigmatismCount = validList.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count();
        RadarChartDataDO.Item item = new RadarChartDataDO.Item();
        item.setName(genderEnum.cnDesc);
        item.setValue(Lists.newArrayList(lowVisionCount, screeningMyopiaCount, highMyopiaCount, lightMyopiaCount, earlyMyopiaCount, astigmatismCount));
        return item;
    }

    /**
     * 排行榜
     *
     * @return RankingDataDO
     */
    private RankingDataDO generateRankingDataDO() {
        Map<Boolean, List<BigScreenStatDataDTO>> schoolTypeMap = bigScreenStatDataDTOList.stream()
                .collect(Collectors.groupingBy(s -> Objects.equals(s.getSchoolType(), SchoolEnum.TYPE_KINDERGARTEN.getType())));
        List<RankingDataDO.Item> primaryItem = getItems(schoolTypeMap.get(Boolean.FALSE), Boolean.FALSE);
        List<RankingDataDO.Item> kindergartenItem = getItems(schoolTypeMap.get(Boolean.TRUE), Boolean.TRUE);

        // 合并小学和幼儿园
        primaryItem.addAll(kindergartenItem);
        return new RankingDataDO(primaryItem);
    }

    /**
     * 排行榜
     */
    private List<RankingDataDO.Item> getItems(List<BigScreenStatDataDTO> bigScreenStatDataList, Boolean isKindergarten) {
        if (CollectionUtils.isEmpty(bigScreenStatDataList)) {
            return new ArrayList<>();
        }
        return bigScreenStatDataList.stream()
                .filter(BigScreenStatDataDTO::getIsValid)
                .collect(Collectors.groupingBy(BigScreenStatDataDTO::getSchoolName))
                .entrySet().stream()
                .map(entry -> {
                    long myopiaCount = entry.getValue().stream().filter(s -> Objects.equals(s.getIsMyopia(), Boolean.TRUE)).count();
                    long lowVisionCount = entry.getValue().stream().filter(s -> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
                    long totalCount = entry.getValue().size();

                    RankingDataDO.Item item = new RankingDataDO.Item(entry.getKey(),
                            BigDecimalUtil.divide(myopiaCount, totalCount),
                            BigDecimalUtil.divide(lowVisionCount, totalCount));

                    if (Objects.equals(isKindergarten, Boolean.TRUE)) {
                        item.setMyopiaRadio(StringUtils.EMPTY);
                    }
                    return item;
                })
                .sorted((o1, o2) -> {
                    if (Objects.equals(isKindergarten, Boolean.TRUE)) {
                        // 幼儿园根据视力低下排序
                        return Double.valueOf(o2.getLowVisionRadio()).compareTo(Double.valueOf(o1.getLowVisionRadio()));
                    }
                    // 小学以上根据近视率排序
                    return Double.valueOf(o2.getMyopiaRadio()).compareTo(Double.valueOf(o1.getMyopiaRadio()));
                }).collect(Collectors.toList());
    }
}
