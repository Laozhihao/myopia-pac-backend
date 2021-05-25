package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.dto.SchoolMonitorStatisticDTO;
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
         * 是否拥有复测报告
         */
        private boolean hasRescreenReport;

        /**
         * 学校标识
         */
        private Integer schoolId;

        /**
         * 计划标识
         */
        private Integer planId;

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
         * 机构质控员名字
         */
        private String qualityControllerName;

        /**
         * 机构质控员队长
         */
        private String qualityControllerCommander;


        /**
         * 获取实例
         *
         * @param schoolMonitorStatisticDTO
         * @return
         */
        public static Item getInstance(SchoolMonitorStatisticDTO schoolMonitorStatisticDTO) {
            Item item = new Item();
            SchoolMonitorStatistic schoolMonitorStatistic = schoolMonitorStatisticDTO.getSchoolMonitorStatistic();
            item.setHasRescreenReport(schoolMonitorStatisticDTO.isHasRescreenReport())
                    .setSchoolId(schoolMonitorStatistic.getSchoolId())
                    .setPlanId(schoolMonitorStatistic.getScreeningPlanId())
                    .setScreeningRangeName(schoolMonitorStatistic.getSchoolName())
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
                    .setWithoutGlassesRescreenNum(schoolMonitorStatistic.getWithoutGlassDsn())
                    .setQualityControllerName(schoolMonitorStatisticDTO.getQualityControllerName())
                    .setQualityControllerCommander(schoolMonitorStatisticDTO.getQualityControllerCommander());
            return item;
        }
    }

    /**
     * 获取空的实体
     *
     * @return
     */
    public static SchoolScreeningMonitorStatisticVO getEmptyInstance() {
        return new SchoolScreeningMonitorStatisticVO();
    }

    public static SchoolScreeningMonitorStatisticVO getInstance(List<SchoolMonitorStatisticDTO> schoolMonitorStatisticDTOS, ScreeningNotice screeningNotice) {
        if (CollectionUtils.isEmpty(schoolMonitorStatisticDTOS)) {
            return null;
        }
        SchoolScreeningMonitorStatisticVO schoolScreeningMonitorStatisticVO = new SchoolScreeningMonitorStatisticVO();
        //设置基础数据
        schoolScreeningMonitorStatisticVO.setBasicData(screeningNotice);
        //设置统计数据
        schoolScreeningMonitorStatisticVO.setItemData(schoolMonitorStatisticDTOS);
        return schoolScreeningMonitorStatisticVO;
    }

    /**
     * 设置item数据
     *
     * @param schoolMonitorStatisticDTOS
     * @return
     */
    private void setItemData(List<SchoolMonitorStatisticDTO> schoolMonitorStatisticDTOS) {
        contents = schoolMonitorStatisticDTOS.stream().map(Item::getInstance).collect(Collectors.toSet());
    }

    /**
     * 设置基础数据
     * @param screeningNotice
     */
    private void setBasicData(ScreeningNotice screeningNotice) {
        super.setDataByScreeningNotice(screeningNotice);
    }
}
