package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.builder.ItemBuilder;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
public class PrimarySchoolAndAboveResultVO implements ResultVO {

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;

    /**
     * 合计数据
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


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Item extends PrimarySchoolAndAboveItem{
        /**
         * 学校数
         */
        private Integer schoolNum;
    }


    public void setItemData(Integer districtId, List<ScreeningResultStatistic> visionStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        this.childDataSet = visionStatistics.stream().map(visionStatistic->{
            Integer statDistrictId = visionStatistic.getDistrictId();
            String statRangeName;
            //是合计数据
            if (Objects.equals(districtId,statDistrictId)) {
                statRangeName = "合计";
                this.totalData = (Item) ItemBuilder.getPrimarySchoolAndAboveItem(Boolean.FALSE,visionStatistic, statRangeName,statDistrictId ,null);
                return null;
            }
            statRangeName = districtIdNameMap.get(statDistrictId);
            return (Item) ItemBuilder.getPrimarySchoolAndAboveItem(Boolean.FALSE,visionStatistic, statRangeName,statDistrictId ,null);
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void setCurrentData(ScreeningResultStatistic currentVisionStatistic) {
        if (Objects.isNull(currentVisionStatistic)){return;}
        this.currentData = (Item) ItemBuilder.getPrimarySchoolAndAboveItem(Boolean.FALSE,currentVisionStatistic,getRangeName(),getDistrictId(),null);
    }


}
