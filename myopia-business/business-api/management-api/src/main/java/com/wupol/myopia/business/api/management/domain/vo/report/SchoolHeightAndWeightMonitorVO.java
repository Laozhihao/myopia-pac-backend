package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 身高体重监测实体
 *
 * @author hang.yuan 2022/5/16 18:31
 */
@Data
public class SchoolHeightAndWeightMonitorVO {

    /**
     * 说明
     */
    private HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private HeightAndWeightGradeVO heightAndWeightGradeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;

    @Data
    public static class HeightAndWeightMonitorVariableVO {
        /**
         * 超重率
         */
        private String overweightRatio;
        /**
         * 肥胖率
         */
        private String obeseRatio;

        /**
         * 营养不良率
         */
        private String malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private String stuntingRatio;
    }



    @Data
    public static class HeightAndWeightSexVO{
        /**
         * 性别说明
         */
        private HeightAndWeightSexVariableVO heightAndWeightSexVariableVO;
        /**
         * 性别表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightSexMonitorTableList;


    }

    @Data
    public static class HeightAndWeightSexVariableVO{
        /**
         * 超重率对比
         */
        private HeightAndWeightSex overweightRatioCompare;
        /**
         * 肥胖率对比
         */
        private HeightAndWeightSex obeseRatioCompare;
        /**
         * 营养不良率对比
         */
        private HeightAndWeightSex malnourishedRatioCompare;
        /**
         * 生长迟缓对比
         */
        private HeightAndWeightSex stuntingRatioCompare;
    }

    @Data
    public static class HeightAndWeightSex{
        /**
         * 前：性别
         */
        private String forwardSex;
        /**
         * 前：占比
         */
        private String forwardRatio;
        /**
         * 后：性别
         */
        private String backSex;
        /**
         * 后：占比
         */
        private String backRatio;
    }


    @Data
    public static class HeightAndWeightGradeVO{

        /**
         * 年级说明
         */
        private HeightAndWeightGradeVariableVO heightAndWeightGradeVariableVO;
        /**
         * 年级表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightGradeMonitorTableList;

    }
    @Data
    public static class HeightAndWeightGradeVariableVO{
        /**
         * 最高年级超重率
         */
        private GradeRatio overweightRatio;
        /**
         * 最高年级肥胖率
         */
        private GradeRatio obeseRatio;

        /**
         * 最高年级营养不良率
         */
        private GradeRatio malnourishedRatio;

        /**
         * 最高年级生长迟缓率
         */
        private GradeRatio stuntingRatio;
    }


    @Data
    public static class GradeRatio{
        /**
         * 最高占比年级
         */
        private String maxGrade;
        /**
         * 最低占比年级
         */
        private String minGrade;
        /**
         * 最高占比
         */
        private String maxRatio;
        /**
         * 最低占比
         */
        private String minRatio;

    }


    @Data
    public static class HeightAndWeightAgeVO{
        /**
         * 年龄段说明
         */
        private HeightAndWeightAgeVariableVO heightAndWeightAgeVariableVO;
        /**
         * 年龄段表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightAgeMonitorTableList;

    }

    @Data
    public static class HeightAndWeightAgeVariableVO{
        /**
         * 超重率 最高、最低
         */
        private AgeRatio overweightRatio;
        /**
         * 肥胖率 最高、最低
         */
        private AgeRatio obeseRatio;
        /**
         * 营养不良率 最高、最低
         */
        private AgeRatio malnourishedRatio;
        /**
         * 生长迟缓率 最高、最低
         */
        private AgeRatio stuntingRatio;

    }

    @Data
    public static class AgeRatio{
        /**
         * 最高占比年龄段
         */
        private String maxAge;
        /**
         * 最低占比年龄段
         */
        private String minAge;
        /**
         * 最高占比
         */
        private String maxRatio;
        /**
         * 最低占比
         */
        private String minRatio;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HeightAndWeightMonitorTable extends SchoolCommonDiseasesAnalysisVO.HeightAndWeightVO{

        /**
         * 项目 （性别、学龄段、年龄段）
         */
        private String itemName;

        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;

    }
}
