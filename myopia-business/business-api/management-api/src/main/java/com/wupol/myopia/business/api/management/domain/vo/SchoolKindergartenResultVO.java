package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.builder.ItemBuilder;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
public class SchoolKindergartenResultVO {


    /**
     *  内容
     */
    private Set<Item> contents;


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Item extends KindergartenItem {
        /**
         * 学校id
         */
        private Integer schoolId;

        /**
         * 筛查计划id
         */
        private Integer screeningPlanId;

        /**
         * 筛查机构
         */
        private Integer screeningOrgId;

        /**
         * 是否有复测报告
         */
        private Boolean hasRescreenReport;

        @Override
        public void setHasRescreenReport(Boolean hasRescreenReport) {
            this.hasRescreenReport = hasRescreenReport;
        }
    }

    public void setItemData(List<ScreeningResultStatistic> screeningResultStatistics, Map<Integer, String> schoolIdDistrictNameMap,Map<String, Boolean> hasRescreenReportMap) {
        // 下级数据 + 当前数据 + 合计数据
       this.contents = screeningResultStatistics.stream()
               .map(screeningResultStatistic -> {
                    Integer schoolId = screeningResultStatistic.getSchoolId();
                    String schoolDistrictName = schoolIdDistrictNameMap.get(schoolId);
                   return (Item)ItemBuilder.getKindergartenItem(Boolean.TRUE,screeningResultStatistic,schoolDistrictName,screeningResultStatistic.getDistrictId(),hasRescreenReportMap);
                })
               .collect(Collectors.toSet());
    }

}
