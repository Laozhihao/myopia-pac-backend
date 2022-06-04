package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
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
    private List<SchoolScreeningMonitorTable> schoolScreeningMonitorTableList;
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

    @Data
    public static class SchoolRatioExtremum{
        /**
         * 最高占比学校
         */
        private String maxSchoolName;

        /**
         * 最低占比学校
         */
        private String minSchoolName;
        /**
         * 最高占比
         */
        private BigDecimal maxRatio;
        /**
         * 最低占比
         */
        private BigDecimal minRatio;
    }

    @Data
    public static class SchoolScreeningMonitorTable {

        /**
         * 项目 （性别、学龄段、年龄段）
         */
        private String itemName;

        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;


        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 超重数
         */
        private Integer overweightNum;
        /**
         * 肥胖数
         */
        private Integer obeseNum;
        /**
         * 营养不良数
         */
        private Integer malnourishedNum;
        /**
         * 生长迟缓数据
         */
        private Integer stuntingNum;

        /**
         * 血压偏高人数
         */
        private Integer highBloodPressureNum;

        /**
         * 脊柱弯曲异常人数
         */
        private Integer abnormalSpineCurvatureNum;


        //=========== 不带% =============
        /**
         * 龋患率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal saprodontiaRatio;

        /**
         * 龋患（失、补）率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal saprodontiaLossAndRepairRatio;
        /**
         * 超重率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal obeseRatio;

        /**
         * 营养不良率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal malnourishedRatio;

        /**
         * 生长迟缓率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal stuntingRatio;

        /**
         * 血压偏高率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal abnormalSpineCurvatureRatio;

    }
}
