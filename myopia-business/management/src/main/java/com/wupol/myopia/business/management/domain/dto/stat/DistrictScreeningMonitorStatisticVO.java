package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class DistrictScreeningMonitorStatisticVO extends ScreeningBasicResult {


    /**
     * 私有构造方法，需要创建对象，请使用
     */
    private DistrictScreeningMonitorStatisticVO() {

    }

    /**
     * 筛查范围 地区id
     */
    private Integer districtId;

    /**
     * 筛查范围名称(其实是地区名称）
     */
    private String screeningRangeName;
    /**
     * 总的数据
     */
    private Item totalData;
    /**
     * 当前级的数据
     */
    private Item currentData;
    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private Set<Item> subordinateDatas;

    /**
     * 获取不可变空数据对象
     *
     * @return
     */
    public static DistrictScreeningMonitorStatisticVO getImmutableEmptyInstance() {
        return new DistrictScreeningMonitorStatisticVO();
    }

    /**
     * 获取对象
     *
     * @param districtMonitorStatistics
     * @param currentDistrictId
     * @param currentRangeName
     * @param screeningNotice
     * @param districtIdNameMap
     * @return
     */
    public static DistrictScreeningMonitorStatisticVO getInstance(List<DistrictMonitorStatistic> districtMonitorStatistics, Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice, Map<Integer, String> districtIdNameMap) {
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return getImmutableEmptyInstance();
        }
        DistrictScreeningMonitorStatisticVO districtScreeningMonitorStatisticVO = new DistrictScreeningMonitorStatisticVO();
        //设置基础数据
        districtScreeningMonitorStatisticVO.setBasicData(currentDistrictId, currentRangeName, screeningNotice);
        //设置统计数据
        districtScreeningMonitorStatisticVO.setItemData(currentDistrictId, districtMonitorStatistics, districtIdNameMap);
        return districtScreeningMonitorStatisticVO;
    }

    /**
     * 设置各种当条数据
     *
     * @param currentDistrictId
     * @param districtMonitorStatistics
     * @param districtIdNameMap
     */
    private void setItemData(Integer currentDistrictId, List<DistrictMonitorStatistic> districtMonitorStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        Set<DistrictScreeningMonitorStatisticVO.Item> subordinateItemSet = districtMonitorStatistics.stream().map(districtMonitorStatistic -> {
            Integer districtId = districtMonitorStatistic.getDistrictId();
            String rangeName = "";
            //是合计数据
            if (districtMonitorStatistic.getIsTotal() == 1 && currentDistrictId.equals(districtMonitorStatistic.getDistrictId())) {
                rangeName = "合计";
                Item item = this.getItem(rangeName, districtMonitorStatistic);
                totalData = item;
                return null;
            }
            rangeName = districtIdNameMap.get(districtId);
            DistrictScreeningMonitorStatisticVO.Item item = this.getItem(rangeName, districtMonitorStatistic);
            if (currentDistrictId.equals(districtMonitorStatistic.getDistrictId())) {
                currentData = item;
                return null;
            } else {
                return item;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        this.subordinateDatas = subordinateItemSet;
    }

    /**
     * 获取条目
     *
     * @param rangeName
     * @param districtMonitorStatistic
     * @return
     */
    private Item getItem(String rangeName, DistrictMonitorStatistic districtMonitorStatistic) {
        DistrictScreeningMonitorStatisticVO.Item item = new DistrictScreeningMonitorStatisticVO.Item();
        item.setScreeningRangeName(rangeName)
                //todo
                .setRescreenItemNum(districtMonitorStatistic.getDsn())
                .setRescreenNum(districtMonitorStatistic.getDsn())
                .setActualScreeningNum(districtMonitorStatistic.getRealScreeningNumbers())
                .setScreeningNum(districtMonitorStatistic.getPlanScreeningNumbers())
                .setScreeningFinishedRatio(districtMonitorStatistic.getFinishRatio())
                .setIncorrectItemNum(districtMonitorStatistic.getErrorNumbers())
                .setIncorrectRatio(districtMonitorStatistic.getErrorRatio())
                .setInvestigationNumbers(districtMonitorStatistic.getInvestigationNumbers())
                .setWearingGlassesRescreenIndexNum(districtMonitorStatistic.getWearingGlassDsin())
                .setWithoutGlassesRescreenIndexNum(districtMonitorStatistic.getWithoutGlassDsin())
                .setWearingGlassesRescreenNum(districtMonitorStatistic.getWearingGlassDsn())
                .setWithoutGlassesRescreenNum(districtMonitorStatistic.getWithoutGlassDsn());
        return item;

    }

    /**
     * 设置基础数据
     *
     * @param currentDistrictId
     * @param currentRangeName
     * @param screeningNotice
     */
    private void setBasicData(Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice) {
        this.districtId = currentDistrictId;
        this.screeningRangeName = currentRangeName;
        super.setDataByScreeningNotice(screeningNotice);
    }

    /**
     * 单条数据
     */
    @NoArgsConstructor
    @Data
    @Accessors(chain = true)
    public static class Item {

        private String screeningRangeName;
        /**
         * screeningNum
         */
        private Integer screeningNum;
        /**
         * investigationNumbers
         */
        private Integer investigationNumbers;
        /**
         * actualScreeningNum
         */
        private Integer actualScreeningNum;
        /**
         * screeningFinishedRatio
         */
        private BigDecimal screeningFinishedRatio;
        /**
         * rescreenNum
         */
        private Integer rescreenNum;
        /**
         * wearingGlassesRescreenNum
         */
        private Integer wearingGlassesRescreenNum;
        /**
         * wearingGlassesRescreenIndexNum
         */
        private Integer wearingGlassesRescreenIndexNum;
        /**
         * withoutGlassesRescreenNum
         */
        private Integer withoutGlassesRescreenNum;
        /**
         * withoutGlassesRescreenIndexNum
         */
        private Integer withoutGlassesRescreenIndexNum;
        /**
         * rescreenItemNum
         */
        private Integer rescreenItemNum;
        /**
         * incorrectItemNum
         */
        private Integer incorrectItemNum;
        /**
         * incorrectRatio
         */
        private BigDecimal incorrectRatio;
    }


}
