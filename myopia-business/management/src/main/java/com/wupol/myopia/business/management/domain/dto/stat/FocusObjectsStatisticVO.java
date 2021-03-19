package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.management.util.MathUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 重点
 */
@Getter
@EqualsAndHashCode
@Accessors(chain = true)
public class FocusObjectsStatisticVO extends ScreeningBasicResult {

    private final String TOTAL_RANGE_NAME = "合计";

    /**
     * 私有构造方法
     */
    private FocusObjectsStatisticVO() {

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
     * 汇总层级的数据
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
     * 设置基础数据
     *
     * @param currentDistrictId
     * @param currentRangeName
     * @return
     */
    private void setBasicData(Integer currentDistrictId, String currentRangeName) {
        this.districtId = currentDistrictId;
        this.screeningRangeName = currentRangeName;
    }

    /**
     * 获取默认实例
     *
     * @return
     */
    public static FocusObjectsStatisticVO getImmutableEmptyInstance() {
        return new FocusObjectsStatisticVO();
    }


    /**
     * 获取item数据
     *
     * @param districtAttentiveObjectsStatistics
     * @param currentDistrictId
     * @param currentRangeName
     * @param districtIdNameMap
     * @return
     */
    public static FocusObjectsStatisticVO getInstance(List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, Integer currentDistrictId, String currentRangeName, Map<Integer, String> districtIdNameMap) {
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics)) {
            return null;
        }
        Map<Integer, List<DistrictAttentiveObjectsStatistic>> isTotalListMap = districtAttentiveObjectsStatistics.stream().collect(Collectors.groupingBy(DistrictAttentiveObjectsStatistic::getIsTotal));
        Map<Integer, List<DistrictAttentiveObjectsStatistic>> districtTotalStatisticsMap = isTotalListMap.getOrDefault(CommonConst.IS_TOTAL, Collections.emptyList()).stream().collect(Collectors.groupingBy(DistrictAttentiveObjectsStatistic::getDistrictId));
        Map<Integer, List<DistrictAttentiveObjectsStatistic>> districtNotTotalStatisticsMap = isTotalListMap.getOrDefault(CommonConst.NOT_TOTAL, Collections.emptyList()).stream().collect(Collectors.groupingBy(DistrictAttentiveObjectsStatistic::getDistrictId));
        FocusObjectsStatisticVO focusObjectsStatisticVO = new FocusObjectsStatisticVO();
        //设置基础数据
        focusObjectsStatisticVO.setBasicData(currentDistrictId, currentRangeName);
        //设置当前数据
        focusObjectsStatisticVO.setCurrentData(districtNotTotalStatisticsMap.getOrDefault(currentDistrictId, Collections.emptyList()));
        //设置统计数据
        focusObjectsStatisticVO.setItemData(currentDistrictId, districtIdNameMap, districtTotalStatisticsMap);
        return focusObjectsStatisticVO;
    }

    /**
     * 设置当前数据
     * @param statistics
     */
    private void setCurrentData(List<DistrictAttentiveObjectsStatistic> statistics) {
        if (statistics != null) {
            currentData = this.getItem(districtId,getScreeningRangeName(), reCountByStatisticList(statistics));
        }
    }

    /**
     * 设置统计数据
     *
     * @param currentDistrictId
     * @param districtIdNameMap
     * @param districtTotalStatisticsMap 区域层级汇总数据Map
     */
    private void setItemData(Integer currentDistrictId, Map<Integer, String> districtIdNameMap, Map<Integer, List<DistrictAttentiveObjectsStatistic>> districtTotalStatisticsMap) {
        // 由于重点视力对象的统计没有区分筛查通知，所以需拿到所有的区域的汇总数据再重新计算
        // 当前汇总数据
        this.totalData = this.getItem(districtId, TOTAL_RANGE_NAME, reCountByStatisticList(districtTotalStatisticsMap.getOrDefault(districtId, Collections.emptyList())));
        // 下级汇总数据
        this.subordinateDatas = districtTotalStatisticsMap.keySet().stream().filter(id -> !currentDistrictId.equals(id)).map(id -> this.getItem(id, districtIdNameMap.get(id), reCountByStatisticList(districtTotalStatisticsMap.getOrDefault(id, Collections.emptyList())))).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 根据列表重新计算统计内容
     * 因为数据库中分了筛查通知维度，所以需要重新计算
     * @param statisticList
     * @return
     */
    private DistrictAttentiveObjectsStatistic reCountByStatisticList(List<DistrictAttentiveObjectsStatistic> statisticList) {
        if (CollectionUtils.isEmpty(statisticList)) {
            return DistrictAttentiveObjectsStatistic.empty();
        }
        Integer studentNumbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getStudentNumbers).sum();
        Integer keyWarningNumbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getKeyWarningNumbers).sum();
        Integer visionLabel0Numbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getVisionLabel0Numbers).sum();
        Integer visionLabel1Numbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getVisionLabel1Numbers).sum();
        Integer visionLabel2Numbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getVisionLabel2Numbers).sum();
        Integer visionLabel3Numbers = statisticList.stream().mapToInt(DistrictAttentiveObjectsStatistic::getVisionLabel3Numbers).sum();
        return new DistrictAttentiveObjectsStatistic().setKeyWarningNumbers(keyWarningNumbers).setStudentNumbers(studentNumbers)
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel0Ratio(MathUtil.divide(visionLabel0Numbers, studentNumbers))
                .setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel1Ratio(MathUtil.divide(visionLabel1Numbers, studentNumbers))
                .setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel2Ratio(MathUtil.divide(visionLabel2Numbers, studentNumbers))
                .setVisionLabel3Numbers(visionLabel3Numbers).setVisionLabel3Ratio(MathUtil.divide(visionLabel3Numbers, studentNumbers));
    }

    /**
     * 获取当前的item
     *
     * @param districtId
     * @param rangeName
     * @param districtAttentiveObjectsStatistic
     * @return
     */
    private FocusObjectsStatisticVO.Item getItem(Integer districtId, String rangeName, DistrictAttentiveObjectsStatistic districtAttentiveObjectsStatistic) {
        FocusObjectsStatisticVO.Item item = new FocusObjectsStatisticVO.Item();
        item.setDistrictId(districtId);
        item.setScreeningRangeName(rangeName).setFocusTargetsNum(districtAttentiveObjectsStatistic.getKeyWarningNumbers()).setScreeningStudentsNum(districtAttentiveObjectsStatistic.getStudentNumbers());
        List<WarningInfo.WarningLevelInfo> warningLevelInfoList = WarningInfo.WarningLevelInfo.getList(districtAttentiveObjectsStatistic);
        item.setWarningLevelInfoList(warningLevelInfoList);
        return item;
    }

    @Data
    public static class Item {

        /**
         * 地区id
         */
        private Integer districtId;

        /**
         * 筛查范围 范围名称
         */
        private String screeningRangeName;

        /**
         * 总重点视力对象数
         */
        private Integer focusTargetsNum;

        /**
         * 筛查人数
         */
        private Integer screeningStudentsNum;

        /**
         * 分级预警信息
         */
        private List<WarningInfo.WarningLevelInfo> warningLevelInfoList;
    }
}
