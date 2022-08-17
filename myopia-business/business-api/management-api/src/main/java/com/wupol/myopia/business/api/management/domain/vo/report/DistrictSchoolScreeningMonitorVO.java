package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 各学校筛查情况
 *
 * @author hang.yuan 2022/5/16 18:32
 */
@Data
public class DistrictSchoolScreeningMonitorVO {
    /**
     * 学校说明
     */
    private SchoolScreeningMonitorVariableVO schoolScreeningMonitorVariableVO;
    /**
     * 学校表格数据
     */
    private List<ScreeningMonitorTable> schoolScreeningMonitorTableList;
    /**
     * 学校图表
     */
    private List<ChartVO.SchoolRatioExtremumChart> schoolScreeningMonitorChart;


    @Data
    public static class SchoolScreeningMonitorVariableVO{

        /**
         * 龋患率极值
         */
        private SchoolRatioExtremum saprodontiaRatioExtremum;

        /**
         * 龋患（失、补）率极值
         */
        private SchoolRatioExtremum saprodontiaLossAndRepairRatioExtremum;

        /**
         * 超重率极值
         */
        private SchoolRatioExtremum overweightRatioExtremum;
        /**
         * 肥胖率极值
         */
        private SchoolRatioExtremum obeseRatioExtremum;

        /**
         * 营养不良率极值
         */
        private SchoolRatioExtremum malnourishedRatioExtremum;

        /**
         * 生长迟缓率极值
         */
        private SchoolRatioExtremum stuntingRatioExtremum;

        /**
         * 血压偏高率极值
         */
        private SchoolRatioExtremum highBloodPressureRatioExtremum;

        /**
         * 脊柱弯曲异常率极值
         */
        private SchoolRatioExtremum abnormalSpineCurvatureRatioExtremum;

    }

}
