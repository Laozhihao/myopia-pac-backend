package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地区图表实体
 *
 * @author hang.yuan 2022/6/1 18:47
 */
@Data
public class SchoolChartVO {


    @Data
    public static class Chart {
        /**
         * x轴
         */
        private List<String> x;
        /**
         * y轴
         */
        private List<ChartData> y;

    }
    @Data
    public static class ChartData {
        /**
         * 标签
         */
        private String name;
        /**
         * 数据
         */
        private List<BigDecimal> data;

        public ChartData(String name, List<BigDecimal> data) {
            this.name = name;
            this.data = data;
        }
    }

    @Data
    public static class AgeChart{
        /**
         * y轴
         */
        private List<String> y;
        /**
         * x轴
         */
        private List<AgeData> x;

    }
    @Data
    public static class AgeData{
        /**
         * 标签
         */
        private String name;
        /**
         * 数据
         */
        private List<BigDecimal> data;

        public AgeData(String name, List<BigDecimal> data) {
            this.name = name;
            this.data = data;
        }
    }

    @Data
    public static class GradeRatioExtremumChart{
        /**
         * 名称
         */
        private String name;
        /**
         * 班级数据
         */
        private List<BigDecimal> data;
        /**
         * 最高占比班级
         */
        private String maxClassName;
        /**
         * 最低占比班级
         */
        private String minClassName;
        /**
         * 最高占比
         */
        private BigDecimal maxRatio;
        /**
         * 最低占比
         */
        private BigDecimal minRatio;

        public GradeRatioExtremumChart(String name, List<BigDecimal> data) {
            this.name = name;
            this.data = data;
        }
    }

}
