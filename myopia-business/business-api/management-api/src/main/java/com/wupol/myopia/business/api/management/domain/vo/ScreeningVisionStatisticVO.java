package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.dto.ScreeningBasicResult;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(callSuper = false)
public class ScreeningVisionStatisticVO extends ScreeningBasicResult {

    /**
     * 私有构造方法
     */
    private ScreeningVisionStatisticVO() {

    }

    /**
     * 筛查范围 地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;
    /**
     * 当前级的数据
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

    public static ScreeningVisionStatisticVO getInstance(List<DistrictVisionStatistic> districtVisionStatistics, Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice, Map<Integer, String> districtIdNameMap, DistrictVisionStatistic currentDistrictVisionStatistic) {
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return null;
        }
        ScreeningVisionStatisticVO screeningVisionStatisticVO = new ScreeningVisionStatisticVO();
        //设置基础数据
        screeningVisionStatisticVO.setBasicData(currentDistrictId, currentRangeName, screeningNotice);
        //设置当前数据
        screeningVisionStatisticVO.setCurrentData(currentDistrictVisionStatistic);
        //设置统计数据
        screeningVisionStatisticVO.setItemData(currentDistrictId, districtVisionStatistics, districtIdNameMap);
        return screeningVisionStatisticVO;
    }

    /**
     *  设置当前层级的数据
     * @param currentDistrictVisionStatistic
     */
    private void setCurrentData(DistrictVisionStatistic currentDistrictVisionStatistic) {
        if (currentDistrictVisionStatistic != null) {
            currentData = this.getItem(districtId, rangeName, currentDistrictVisionStatistic);
        }
    }

    /**
     *
     * @return
     */
    public static ScreeningVisionStatisticVO getImmutableEmptyInstance() {
        return new ScreeningVisionStatisticVO();
    }

    /**
     * 设置item数据
     * @param currentDistrictId
     * @param districtVisionStatistics
     * @param districtIdNameMap
     */
    private void setItemData(Integer currentDistrictId, List<DistrictVisionStatistic> districtVisionStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        this.subordinateDatas = districtVisionStatistics.stream().map(districtVisionStatistic -> {
            Integer visionStatisticDistrictId = districtVisionStatistic.getDistrictId();
            String itemRangeName;
            //是合计数据
            if (currentDistrictId.equals(districtVisionStatistic.getDistrictId())) {
                itemRangeName = "合计";
                totalData = this.getItem(visionStatisticDistrictId, itemRangeName, districtVisionStatistic);
                return null;
            }
            itemRangeName = districtIdNameMap.get(visionStatisticDistrictId);
            return this.getItem(visionStatisticDistrictId, itemRangeName, districtVisionStatistic);
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Item getItem(Integer districtId, String rangeName, DistrictVisionStatistic districtVisionStatistic) {
        ScreeningVisionStatisticVO.Item item = new ScreeningVisionStatisticVO.Item();
        item.setFocusTargetsNum(districtVisionStatistic.getKeyWarningNumbers())
                .setActualScreeningNum(districtVisionStatistic.getRealScreeningNumbers())
                .setValidScreeningNum(districtVisionStatistic.getValidScreeningNumbers())
                .setAverageVisionLeft(districtVisionStatistic.getAvgLeftVision())
                .setAverageVisionRight(districtVisionStatistic.getAvgRightVision())
                .setLowVisionNum(districtVisionStatistic.getLowVisionNumbers())
                .setLowVisionRatio(districtVisionStatistic.getLowVisionRatio())
                .setMyopiaNum(districtVisionStatistic.getMyopiaNumbers())
                .setMyopiaRatio(districtVisionStatistic.getMyopiaRatio())
                .setRecommendVisitNum(districtVisionStatistic.getTreatmentAdviceNumbers())
                .setRecommendVisitRatio(districtVisionStatistic.getTreatmentAdviceRatio())
                .setRefractiveErrorNum(districtVisionStatistic.getAmetropiaNumbers())
                .setWearingGlassesRatio(districtVisionStatistic.getWearingGlassesRatio())
                .setWearingGlassesNum(districtVisionStatistic.getWearingGlassesNumbers())
                .setWarningLevelOneNum(districtVisionStatistic.getVisionLabel1Numbers())
                .setWarningLevelOneRatio(districtVisionStatistic.getVisionLabel1Ratio())
                .setWarningLevelTwoNum(districtVisionStatistic.getVisionLabel2Numbers())
                .setWarningLevelTwoRatio(districtVisionStatistic.getVisionLabel2Ratio())
                .setWarningLevelThreeNum(districtVisionStatistic.getVisionLabel3Numbers())
                .setWarningLevelThreeRatio(districtVisionStatistic.getVisionLabel3Ratio())
                .setScreeningNum(districtVisionStatistic.getPlanScreeningNumbers())
                .setScreeningRangeName(rangeName)
                .setRefractiveErrorRatio(districtVisionStatistic.getAmetropiaRatio())
                .setWarningLevelZeroNum(districtVisionStatistic.getVisionLabel0Numbers())
                .setWarningLevelZeroRatio(districtVisionStatistic.getVisionLabel0Ratio())
                .setDistrictId(districtId)
                .setScreeningNoticeId(super.getScreeningNoticeId());
        return item;
    }


    private void setBasicData(Integer currentDistrictId, String currentRangeName, ScreeningNotice screeningNotice) {
        this.districtId = currentDistrictId;
        this.rangeName = currentRangeName;
        super.setDataByScreeningNotice(screeningNotice);
    }

    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 筛查学生数
         */
        private Integer screeningNum;
        /**
         * 实际筛查学生数
         */
        private Integer actualScreeningNum;
        /**
         * 实际筛查学生数
         */
        private Integer validScreeningNum;
        /**
         * 左眼平均视力
         */
        private BigDecimal averageVisionLeft;
        /**
         * 右眼平均视力
         */
        private BigDecimal averageVisionRight;
        /**
         * 视力低下率
         */
        private BigDecimal lowVisionRatio;
        /**
         * 视力低下人数
         */
        private Integer lowVisionNum;
        /**
         * 屈光不正率
         */
        private BigDecimal refractiveErrorRatio;
        /**
         * 屈光不正人数
         */
        private Integer refractiveErrorNum;
        /**
         * 戴镜率
         */
        private BigDecimal wearingGlassesRatio;
        /**
         * 戴镜人数
         */
        private Integer wearingGlassesNum;
        /**
         * 近视人数
         */
        private Integer myopiaNum;

        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

        /**
         * 0级预警率
         */
        private BigDecimal warningLevelZeroRatio;

        /**
         * 0级预警人数
         */
        private Integer warningLevelZeroNum;

        /**
         * 1级预警率
         */
        private BigDecimal warningLevelOneRatio;

        /**
         * 1级预警人数
         */
        private Integer warningLevelOneNum;

        /**
         * 2级预警率
         */
        private BigDecimal warningLevelTwoRatio;

        /**
         * 2级预警人数
         */
        private Integer warningLevelTwoNum;

        /**
         * 3级预警率
         */
        private BigDecimal warningLevelThreeRatio;

        /**
         * 3级预警人数
         */
        private Integer warningLevelThreeNum;

        /**
         * 重点视力对象数量
         */
        private Integer focusTargetsNum;

        /**
         * 建议就诊数
         */
        private Integer recommendVisitNum;

        /**
         * 筛查通知id
         */
        private Integer screeningNoticeId;

        /**
         * 地区id
         */
        private Integer districtId;

        /**
         * 建议就诊数比例
         */
        private BigDecimal recommendVisitRatio;
    }

}
