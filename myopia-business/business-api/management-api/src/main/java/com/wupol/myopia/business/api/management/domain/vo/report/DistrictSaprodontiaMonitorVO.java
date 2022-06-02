package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 龋齿监测结果实体
 *
 * @author hang.yuan 2022/5/16 17:10
 */
@Data
public class DistrictSaprodontiaMonitorVO {

    /**
     * 说明
     */
    private SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private SaprodontiaSexVO saprodontiaSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;



    @Data
    public static class SaprodontiaMonitorVariableVO{
        /**
         * 龋均
         */
        private String dmftRatio;

        /**
         * 龋患率
         */
        private String saprodontiaRatio;

        /**
         * 龋补率
         */
        private String saprodontiaRepairRatio;
        /**
         * 龋患（失、补）率
         */
        private String saprodontiaLossAndRepairRatio;

        /**
         * 龋患（失、补）构成比
         */
        private String saprodontiaLossAndRepairTeethRatio;


    }

    @Data
    public static class SaprodontiaSexVO{
        /**
         * 性别说明
         */
        private SaprodontiaSexVariableVO saprodontiaSexVariableVO;
        /**
         * 性别表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaSexMonitorTableList;
        /**
         * 性别图表
         */
        private DistrictChartVO.Chart saprodontiaSexMonitorChart;


    }


    @Data
    public static class SaprodontiaSexVariableVO{
        /**
         * 龋患率对比
         */
        private SaprodontiaSex saprodontiaRatioCompare;
        /**
         * 龋失率对比
         */
        private SaprodontiaSex saprodontiaLossRatioCompare;
        /**
         * 龋补率对比
         */
        private SaprodontiaSex saprodontiaRepairRatioCompare;
    }

    @Data
    public static class SaprodontiaSex{
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
        /**
         * 符号
         */
        private String symbol;
    }


    @Data
    public static class SaprodontiaSchoolAgeVO{

        /**
         * 学龄段说明
         */
        private SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO;
        /**
         * 学龄段表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaSchoolAgeMonitorTableList;
        /**
         * 学龄段图表
         */
        private DistrictChartVO.Chart saprodontiaSchoolAgeMonitorChart;

    }

    @Data
    public static class SaprodontiaSchoolAgeVariableVO{
        /**
         * 小学
         */
        private SaprodontiaSchoolAge primarySchool;
        /**
         * 初中
         */
        private SaprodontiaSchoolAge juniorHighSchool;

        /**
         * 高中(普高+职高)
         */
        private SaprodontiaSchoolAge highSchool;
        /**
         * 普高
         */
        private SaprodontiaSchoolAge normalHighSchool;
        /**
         * 职高
         */
        private SaprodontiaSchoolAge vocationalHighSchool;
        /**
         * 大学
         */
        private SaprodontiaSchoolAge university;

    }

    @Data
    public static class SaprodontiaSchoolAge {

        /**
         * 龋患率
         */
        private String saprodontiaRatio;
        /**
         * 龋失率
         */
        private String saprodontiaLossRatio;

        /**
         * 龋补率
         */
        private String saprodontiaRepairRatio;
        /**
         * 最高年级龋患率
         */
        private GradeRatio maxSaprodontiaRatio;

        /**
         * 最高年级龋失率
         */
        private GradeRatio maxSaprodontiaLossRatio;

        /**
         * 最高年级龋补率
         */
        private GradeRatio maxSaprodontiaRepairRatio;

    }

    @Data
    public static class GradeRatio{
        /**
         * 年级
         */
        private String grade;
        /**
         * 占比
         */
        private String ratio;

    }


    @Data
    public static class SaprodontiaAgeVO{
        /**
         * 年龄段说明
         */
        private SaprodontiaAgeVariableVO saprodontiaAgeVariableVO;
        /**
         * 年龄段表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaAgeMonitorTableList;
        /**
         * 年龄段图表
         */
        private DistrictChartVO.AgeChart saprodontiaAgeMonitorChart;

    }


    @Data
    public static class SaprodontiaAgeVariableVO{
        /**
         * 龋患率 最高、最低
         */
        private AgeRatio saprodontiaRatio;
        /**
         * 龋失率 最高、最低
         */
        private AgeRatio saprodontiaLossRatio;
        /**
         * 龋补率 最高、最低
         */
        private AgeRatio saprodontiaRepairRatio;

    }

    @Data
    public static class AgeRatio{
        /**
         * 最高年龄段
         */
        private String maxAge;
        /**
         * 最小年龄段
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
    public static class SaprodontiaMonitorTable extends DistrictCommonDiseasesAnalysisVO.SaprodontiaVO{

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
