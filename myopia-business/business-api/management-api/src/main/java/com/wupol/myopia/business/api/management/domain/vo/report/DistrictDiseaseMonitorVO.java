package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 疾病监测情况
 *
 * @author hang.yuan 2022/5/16 16:47
 */
@Data
public class DistrictDiseaseMonitorVO {

    /**
     * 说明变量
     */
    private DiseaseMonitorVariableVO diseaseMonitorVariableVO;

    /**
     * 疾病监测-不同学龄段-表格数据
     */
    private List<DiseaseMonitorTable> diseaseMonitorTableList;

    /**
     * 图表
     */
    private List<ChartItem> diseaseMonitorChart;

    @Data
    public static class ChartItem{
        /**
         * 标签
         */
        private String  label;
        /**
         * 值
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal value;

        public ChartItem(String label, BigDecimal value) {
            this.label = label;
            this.value = value;
        }
    }

    @Data
    public static class DiseaseMonitorVariableVO{
        /**
         * 高血压占比
         */
        private String hypertensionRatio;
        /**
         * 高血压最高学龄段的占比
         */
        private SchoolAgeRatio maxHypertensionRatio;

        /**
         * 贫血占比
         */
        private String anemiaRatio;

        /**
         * 贫血最高学龄段的占比
         */
        private SchoolAgeRatio maxAnemiaRatio;

        /**
         * 糖尿病占比
         */
        private String diabetesRatio;

        /**
         * 糖尿病最高学龄段的占比
         */
        private SchoolAgeRatio maxDiabetesRatio;
        /**
         * 过敏性哮喘占比
         */
        private String allergicAsthmaRatio;

        /**
         * 过敏性哮喘最高学龄段的占比
         */
        private SchoolAgeRatio maxAllergicAsthmaRatio;

        /**
         * 身体残疾占比
         */
        private String physicalDisabilityRatio;

        /**
         * 身体残疾最高学龄段的占比
         */
        private SchoolAgeRatio maxPhysicalDisabilityRatio;

    }

    @Data
    public static class SchoolAgeRatio{
        /**
         * 学龄段
         */
        private String schoolAge;
        /**
         * 占比
         */
        private String ratio;
    }

    @Data
    public static class DiseaseMonitorTable{
        /**
         * 项目（学龄段）
         */
        private String schoolAge;
        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;
        /**
         * 高血压人数
         */
        private Integer hypertensionNum;
        /**
         * 高血压占比
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal hypertensionRatio;

        /**
         * 贫血人数
         */
        private Integer anemiaNum;
        /**
         * 贫血占比
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal anemiaRatio;

        /**
         * 糖尿病人数
         */
        private Integer diabetesNum;
        /**
         * 糖尿病占比
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal diabetesRatio;

        /**
         * 过敏性哮喘人数
         */
        private Integer allergicAsthmaNum;
        /**
         * 过敏性哮喘占比
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal allergicAsthmaRatio;

        /**
         * 身体残疾人数
         */
        private Integer physicalDisabilityNum;
        /**
         * 身体残疾占比
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal physicalDisabilityRatio;


    }

    @Data
    public static class Ratio{
        /**
         * 人数
         */
        private Integer num;
        /**
         * 占比
         */
        private String ratio;
    }
}
