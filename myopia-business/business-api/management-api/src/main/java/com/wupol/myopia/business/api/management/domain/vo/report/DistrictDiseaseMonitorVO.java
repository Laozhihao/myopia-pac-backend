package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

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
     * 不同学龄段 - 疾病监测
     */
    private List<DiseaseMonitorTable> diseaseMonitorTableList;



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
         * 项目（学龄）
         */
        private String schoolAge;
        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;
        /**
         * 高血压
         */
        private Ratio hypertension;

        /**
         * 贫血
         */
        private Ratio anemia;

        /**
         * 糖尿病
         */
        private Ratio diabetes;

        /**
         * 过敏性哮喘
         */
        private Ratio allergicAsthma;

        /**
         * 身体残疾占比
         */
        private Ratio physicalDisability;


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
