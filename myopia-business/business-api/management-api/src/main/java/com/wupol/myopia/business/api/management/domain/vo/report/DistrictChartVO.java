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
public class DistrictChartVO {


    @Data
    public static class SchoolAgeChart{
        /**
         * x轴
         */
        private List<String> x;
        /**
         * y轴
         */
        private List<SchoolAgeData> y;

    }
    @Data
    public static class SchoolAgeData{
        /**
         * 标签
         */
        private String name;
        /**
         * 数据
         */
        private List<BigDecimal> data;

        public SchoolAgeData(String name, List<BigDecimal> data) {
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
}
