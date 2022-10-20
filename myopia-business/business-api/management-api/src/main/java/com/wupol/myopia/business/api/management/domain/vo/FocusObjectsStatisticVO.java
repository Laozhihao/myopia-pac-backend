package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.dto.ScreeningBasicResult;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 重点
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FocusObjectsStatisticVO extends ScreeningBasicResult {

    private static final String TOTAL_RANGE_NAME = "合计";

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
    private Set<Item> childDataSet;

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
     * @param currentDistrictAttentiveObjectsStatistic
     * @return
     */
    public static FocusObjectsStatisticVO getInstance(List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, Integer currentDistrictId, String currentRangeName, Map<Integer, String> districtIdNameMap, DistrictAttentiveObjectsStatistic currentDistrictAttentiveObjectsStatistic ) {
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics)) {
            return null;
        }
        FocusObjectsStatisticVO focusObjectsStatisticVO = new FocusObjectsStatisticVO();
        //设置基础数据
        focusObjectsStatisticVO.setBasicData(currentDistrictId, currentRangeName);
        //设置当前数据
        focusObjectsStatisticVO.setCurrentData(currentDistrictAttentiveObjectsStatistic);
        //设置统计数据
        focusObjectsStatisticVO.setItemData(currentDistrictId, districtAttentiveObjectsStatistics, districtIdNameMap);
        return focusObjectsStatisticVO;
    }

    /**
     * 设置当前数据
     * @param currentDistrictAttentiveObjectsStatistic
     */
    private void setCurrentData(DistrictAttentiveObjectsStatistic currentDistrictAttentiveObjectsStatistic) {
        if (currentDistrictAttentiveObjectsStatistic != null) {
            currentData = this.getItem(districtId,getScreeningRangeName(),currentDistrictAttentiveObjectsStatistic);
        }
    }

    /**
     * 设置统计数据
     *
     * @param currentDistrictId
     * @param districtAttentiveObjectsStatistics
     * @param districtIdNameMap
     */
    private void setItemData(Integer currentDistrictId, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        this.childDataSet = districtAttentiveObjectsStatistics.stream().map(districtAttentiveObjectsStatistic -> {
            Integer districtAttentiveDistrictId = districtAttentiveObjectsStatistic.getDistrictId();
            String rangeName;
            //是合计数据
            if (currentDistrictId.equals(districtAttentiveObjectsStatistic.getDistrictId())) {
                rangeName = TOTAL_RANGE_NAME;
                totalData = this.getItem(districtAttentiveDistrictId, rangeName, districtAttentiveObjectsStatistic);
                return null;
            }
            rangeName = districtIdNameMap.get(districtAttentiveDistrictId);
            return this.getItem(districtAttentiveDistrictId, rangeName, districtAttentiveObjectsStatistic);
        }).filter(Objects::nonNull).collect(Collectors.toSet());
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
        item.setScreeningRangeName(rangeName)
                .setFocusTargetsNum(districtAttentiveObjectsStatistic.getKeyWarningNumbers())
                .setScreeningStudentsNum(districtAttentiveObjectsStatistic.getStudentNumbers());
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
