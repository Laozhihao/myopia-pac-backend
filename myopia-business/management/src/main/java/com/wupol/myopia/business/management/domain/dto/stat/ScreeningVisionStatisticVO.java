package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
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

    public static ScreeningVisionStatisticVO getInstance(List<DistrictVisionStatistic> districtVisionStatistics, Integer currentDistrictId, String currentRangeName, ScreeningTask screeningTask, Map<Integer, String> districtIdNameMap) {
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return null;
        }
        ScreeningVisionStatisticVO screeningVisionStatisticVO = new ScreeningVisionStatisticVO();
        //设置基础数据
        screeningVisionStatisticVO.setBasicData(currentDistrictId, currentRangeName, screeningTask);
        //设置统计数据
        screeningVisionStatisticVO.setItemData(currentDistrictId, districtVisionStatistics, districtIdNameMap);
        return screeningVisionStatisticVO;
    }

    public static ScreeningVisionStatisticVO getEmptyInstance() {
        return new ScreeningVisionStatisticVO();
    }

    private void setItemData(Integer currentDistrictId, List<DistrictVisionStatistic> districtVisionStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        Set<ScreeningVisionStatisticVO.Item> subordinateItemSet = districtVisionStatistics.stream().map(districtVisionStatistic -> {
            Integer districtId = districtVisionStatistic.getDistrictId();
            String rangeName = "";
            //是合计数据
            if (districtVisionStatistic.getIsTotal() == 1 && currentDistrictId.equals(districtVisionStatistic.getDistrictId())) {
                rangeName = "合计";
                ScreeningVisionStatisticVO.Item item = this.getItem(districtId, rangeName, districtVisionStatistic);
                totalData = item;
                return null;
            }
            rangeName = districtIdNameMap.get(districtId);
            ScreeningVisionStatisticVO.Item item = this.getItem(districtId, rangeName, districtVisionStatistic);
            if (currentDistrictId.equals(districtVisionStatistic.getDistrictId())) {
                currentData = item;
                return null;
            } else {
                return item;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        this.subordinateDatas = subordinateItemSet;
    }

    private Item getItem(Integer districtId, String rangeName, DistrictVisionStatistic districtVisionStatistic) {
        ScreeningVisionStatisticVO.Item item = new ScreeningVisionStatisticVO.Item();
        item.setFocusTargetsNum(districtVisionStatistic.getKeyWarningNumbers())
                .setActualScreeningNum(districtVisionStatistic.getRealScreeningNumners())
                .setAverageVisionLeft(districtVisionStatistic.getAvgLeftVision())
                .setAverageVisionRight(districtVisionStatistic.getAvgRightVision())
                .setLowVisionNum(districtVisionStatistic.getLowVisionNumbers())
                .setLowVisionRatio(districtVisionStatistic.getLowVisionRatio())
                .setMyopiaNum(districtVisionStatistic.getMyopiaNumbers())
                .setMyopiaRatio(districtVisionStatistic.getMyopiaRatio())
                .setRecommendVisitNum(districtVisionStatistic.getTreatmentAdviceNumbers())
                .setRefractiveErrorNum(districtVisionStatistic.getAmetropiaNumbers())
                .setWearingGlassesRatio(districtVisionStatistic.getWearingGlassesRatio())
                .setWearingGlassesNum(districtVisionStatistic.getKeyWarningNumbers())
                .setWarningLevelOneNum(districtVisionStatistic.getVisionLabel0Numbers())
                .setWarningLevelOneRatio(districtVisionStatistic.getVisionLabel0Ratio())
                .setWarningLevelTwoNum(districtVisionStatistic.getVisionLabel2Numbers())
                .setWarningLevelTwoRatio(districtVisionStatistic.getVisionLabel2Ratio())
                .setWarningLevelThreeNum(districtVisionStatistic.getVisionLabel3Numbers())
                .setWarningLevelThreeRatio(districtVisionStatistic.getVisionLabel3Ratio())
                .setScreeningNum(districtVisionStatistic.getPlanScreeningNumbers())
                .setScreeningRangeName(rangeName)
                .setRefractiveErrorRatio(districtVisionStatistic.getAmetropiaRatio())
                .setWarningLevelZeroNum(districtVisionStatistic.getVisionLabel0Numbers())
                .setWarningLevelZeroRatio(districtVisionStatistic.getVisionLabel0Ratio());
        return item;
    }


    private void setBasicData(Integer currentDistrictId, String currentRangeName, ScreeningTask screeningTask) {
        this.districtId = currentDistrictId;
        this.rangeName = currentRangeName;
        super.setDataByScreeningTask(screeningTask);
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


    }



    /*  *//** 视力筛查完成率 *//*
    private BigDecimal screeningFinishedRatio;

    *//** 复测人数 *//*
    private Integer rescreenNum;



    *//** 戴镜复测人数 *//*
    private Integer wearingGlassesRescreenNum;

    *//** 戴镜复测指标数 *//*
    private Integer wearingGlassesRescreenIndexNum;

    *//** 非戴镜复测人数 *//*
    private Integer withoutGlassesRescreenNum;

    *//** 非戴镜复测指标数 *//*
    private Integer withoutGlassesRescreenIndexNum;

    *//** 复测项次 *//*
    private Integer rescreenItemNum;

    *//** 错误项次数 *//*
    private Integer incorrectItemNum;

    *//** 错误率/发生率 *//*
    private BigDecimal incorrectRatio;*/
}
