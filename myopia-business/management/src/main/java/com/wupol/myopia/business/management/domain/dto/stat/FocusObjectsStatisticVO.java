package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
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
@EqualsAndHashCode
@Accessors(chain = true)
public class FocusObjectsStatisticVO extends ScreeningBasicResult {

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
     * 下级的数据列表，如果没有的话，为null todo 通知文欣
     */
    private Set<Item> subordinateDatas;

    /**
     * 设置基础数据
     *
     * @param currentDistrictId
     * @param currentRangeName
     * @return
     */
    public void setBasicData(Integer currentDistrictId, String currentRangeName  ) {
        this.districtId = currentDistrictId;
        this.screeningRangeName = currentRangeName;
      //  super.setDataByScreeningTask(screeningTask);
    }

    public static FocusObjectsStatisticVO getEmptyInstance() {
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
        FocusObjectsStatisticVO focusObjectsStatisticVO = new FocusObjectsStatisticVO();
        //设置基础数据
        focusObjectsStatisticVO.setBasicData(currentDistrictId, currentRangeName);
        //设置统计数据
        focusObjectsStatisticVO.setItemData(currentDistrictId, districtAttentiveObjectsStatistics, districtIdNameMap);
        return focusObjectsStatisticVO;
    }

    /**
     * 设置统计数据
     *
     * @param currentDistrictId
     * @param districtAttentiveObjectsStatistics
     * @param districtIdNameMap
     */
    public void setItemData(Integer currentDistrictId, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        Set<FocusObjectsStatisticVO.Item> subordinateItemSet = districtAttentiveObjectsStatistics.stream().map(districtAttentiveObjectsStatistic -> {
            Integer districtId = districtAttentiveObjectsStatistic.getDistrictId();
            String rangeName = "";
            //是合计数据
            if (districtAttentiveObjectsStatistic.getIsTotal() == 1 && currentDistrictId.equals(districtAttentiveObjectsStatistic.getDistrictId())) {
                rangeName = "合计";
                FocusObjectsStatisticVO.Item item = this.getItem(districtId, rangeName, districtAttentiveObjectsStatistic);
                totalData = item;
                return null;
            }
            rangeName = districtIdNameMap.get(districtId);
            FocusObjectsStatisticVO.Item item = this.getItem(districtId, rangeName, districtAttentiveObjectsStatistic);
            if (currentDistrictId.equals(districtAttentiveObjectsStatistic.getDistrictId())) {
                currentData = item;
                return null;
            } else {
                return item;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        this.subordinateDatas = subordinateItemSet;
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
        FocusObjectsStatisticVO.Item itemtem = new FocusObjectsStatisticVO.Item();
        itemtem.setDistrictId(districtId);
        itemtem.setScreeningRangeName(rangeName).setFocusTargetsNum(districtAttentiveObjectsStatistic.getKeyWarningNumbers()).setScreeningStudentsNum(districtAttentiveObjectsStatistic.getStudentNumbers());
        List<WarningInfo.WarningLevelInfo> warningLevelInfoList = WarningInfo.WarningLevelInfo.getList(districtAttentiveObjectsStatistic);
        itemtem.setWarningLevelInfoList(warningLevelInfoList);
        return itemtem;
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
