package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.dto.ScreeningBasicResult;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学校重点筛查统计对象
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
public class SchoolScreeningMonitorStatisticVO extends ScreeningBasicResult {

    /**
     * 内容
     */
    private Set<Item> contents;

    @Getter
    @Accessors(chain = true)
    @Setter
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;

        /**
         * 私有构造方法
         */
        private Item() {

        }

        /**
         * investigationNumbers
         */
        private Integer investigationNumbers;
        /**
         * actualScreeningNum
         */
        private Integer screeningNum;
        /**
         * 实际筛查学生数
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


        /**
         * 获取实例
         *
         * @param schoolMonitorStatistic
         * @param screeningRangeName     学校范围
         * @return
         */
        public static Item getInstance(SchoolMonitorStatistic schoolMonitorStatistic, String screeningRangeName) {
            Item item = new Item();
            item.setScreeningRangeName(schoolMonitorStatistic.getSchoolName())
                    .setRescreenItemNum(schoolMonitorStatistic.getRescreeningItemNumbers())
                    .setRescreenNum(schoolMonitorStatistic.getDsn())
                    .setScreeningNum(schoolMonitorStatistic.getPlanScreeningNumbers())
                    .setActualScreeningNum(schoolMonitorStatistic.getRealScreeningNumbers())
                    .setScreeningFinishedRatio(schoolMonitorStatistic.getFinishRatio())
                    .setIncorrectItemNum(schoolMonitorStatistic.getErrorNumbers())
                    .setIncorrectRatio(schoolMonitorStatistic.getErrorRatio())
                    .setInvestigationNumbers(schoolMonitorStatistic.getInvestigationNumbers())
                    .setWearingGlassesRescreenIndexNum(schoolMonitorStatistic.getWearingGlassDsin())
                    .setWithoutGlassesRescreenIndexNum(schoolMonitorStatistic.getWithoutGlassDsin())
                    .setWearingGlassesRescreenNum(schoolMonitorStatistic.getWearingGlassDsn())
                    .setWithoutGlassesRescreenNum(schoolMonitorStatistic.getWithoutGlassDsn());
            return item;
        }
    }

    /**
     * 获取空的实体
     *
     * @return
     */
    public static SchoolScreeningMonitorStatisticVO getEmptyInstance() {
        SchoolScreeningMonitorStatisticVO schoolScreeningMonitorStatisticVO = new SchoolScreeningMonitorStatisticVO();
        return schoolScreeningMonitorStatisticVO;
    }

    /**
     * 获取实例
     *
     * @param schoolMonitorStatistics
     * @param districtName
     * @param screeningNotice
     * @return
     */
    public static SchoolScreeningMonitorStatisticVO getInstance(List<SchoolMonitorStatistic> schoolMonitorStatistics, String districtName, ScreeningNotice screeningNotice) {
        if (CollectionUtils.isEmpty(schoolMonitorStatistics)) {
            return null;
        }
        SchoolScreeningMonitorStatisticVO schoolScreeningMonitorStatisticVO = new SchoolScreeningMonitorStatisticVO();
        //设置基础数据
        schoolScreeningMonitorStatisticVO.setBasicData(screeningNotice);
        //设置统计数据
        schoolScreeningMonitorStatisticVO.setItemData(schoolMonitorStatistics, districtName);
        return schoolScreeningMonitorStatisticVO;
    }

    /**
     * 设置item数据
     *
     * @param schoolMonitorStatistics
     * @param districtName
     * @return
     */
    private void setItemData(List<SchoolMonitorStatistic> schoolMonitorStatistics, String districtName) {
        Set<Item> items = schoolMonitorStatistics.stream().map(schoolMonitorStatistic ->
                Item.getInstance(schoolMonitorStatistic, districtName)
        ).collect(Collectors.toSet());
        contents = items;
    }

    /**
     * 设置基础数据
     * @param screeningNotice
     */
    private void setBasicData(ScreeningNotice screeningNotice) {
        super.setDataByScreeningNotice(screeningNotice);
    }
}
