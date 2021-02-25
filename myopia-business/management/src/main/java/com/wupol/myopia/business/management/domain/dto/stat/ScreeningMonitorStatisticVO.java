package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ScreeningMonitorStatisticVO extends ScreeningBasicResult {


    /**
     * 私有构造方法
     */
    private ScreeningMonitorStatisticVO() {

    }

    /**
     * 筛查范围 地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String screeningRangeName;
    /**
     * 当前级的数据
     */
    private  Item totalData;
    /**
     * 当前级的数据
     */
    private  Item currentData;
    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private Set<Item> subordinateDatas;

    public static ScreeningMonitorStatisticVO getEmptyInstance() {
        return new ScreeningMonitorStatisticVO();
    }

    public static ScreeningMonitorStatisticVO getInstance(List<DistrictMonitorStatistic> districtMonitorStatistics, Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice, Map<Integer, String> districtIdNameMap) {
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return null;
        }
        ScreeningMonitorStatisticVO screeningMonitorStatisticVO = new ScreeningMonitorStatisticVO();
        //设置基础数据
        screeningMonitorStatisticVO.setBasicData(currentDistrictId, currentRangeName, screeningNotice);
        //设置统计数据
        screeningMonitorStatisticVO.setItemData(currentDistrictId, districtMonitorStatistics, districtIdNameMap);
        return screeningMonitorStatisticVO;
    }

    private void setItemData(Integer currentDistrictId, List<DistrictMonitorStatistic> districtMonitorStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        Set<ScreeningMonitorStatisticVO.Item> subordinateItemSet = districtMonitorStatistics.stream().map(districtVisionStatistic -> {
            Integer districtId = districtVisionStatistic.getDistrictId();
            String rangeName = "";
            //是合计数据
            if (districtVisionStatistic.getIsTotal() == 1 && currentDistrictId.equals(districtVisionStatistic.getDistrictId())) {
                rangeName = "合计";
                ScreeningMonitorStatisticVO.Item item = this.getItem(districtId, rangeName, districtVisionStatistic);
                totalData = item;
                return null;
            }
            rangeName = districtIdNameMap.get(districtId);
            ScreeningMonitorStatisticVO.Item item = this.getItem(districtId, rangeName, districtVisionStatistic);
            if (currentDistrictId.equals(districtVisionStatistic.getDistrictId())) {
                currentData = item;
                return null;
            } else {
                return item;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        this.subordinateDatas = subordinateItemSet;


    }

    private Item getItem(Integer districtId, String rangeName, DistrictMonitorStatistic districtMonitorStatistic) {
        ScreeningMonitorStatisticVO.Item item = new ScreeningMonitorStatisticVO.Item();
        item.setScreeningRangeName(rangeName)
                .setRescreenItemNum(10)
                .setRescreenNum(districtMonitorStatistic.getPlanScreeningNumbers())
                .setActualScreeningNum(districtMonitorStatistic.getRealScreeningNumbers())
                .setScreeningNum(districtMonitorStatistic.getPlanScreeningNumbers())
                .setScreeningFinishedRatio(new BigDecimal(12))
                .setIncorrectItemNum(districtMonitorStatistic.getErrorNumbers())
                .setIncorrectRatio(districtMonitorStatistic.getErrorRatio())
                .setInvestigationNumbers(districtMonitorStatistic.getInvestigationNumbers())
                .setWearingGlassesRescreenIndexNum(districtMonitorStatistic.getWearingGlassDsin())
                .setWithoutGlassesRescreenIndexNum(districtMonitorStatistic.getWithoutGlassDsin())
                .setWearingGlassesRescreenNum(districtMonitorStatistic.getWearingGlassDsn())
                .setWithoutGlassesRescreenNum(districtMonitorStatistic.getWithoutGlassDsn());
        return item;

    }

    private void setBasicData(Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice) {
        this.districtId = currentDistrictId;
        this.screeningRangeName = currentRangeName;
        super.setDataByScreeningNotice(screeningNotice);
    }


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
