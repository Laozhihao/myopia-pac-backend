package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 龋齿监测结果实体
 *
 * @author hang.yuan 2022/5/16 17:10
 */
@Data
public class DistrictSaprodontiaMonitorVO {

    /**
     * 说明变量
     */
    private SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private SaprodontiaSexVO saprodontiaSex;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private SaprodontiaSchoolAgeVO saprodontiaSchoolAge;
    /**
     * 龋齿监测 - 不同年龄
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;



    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SaprodontiaMonitorVariableVO extends DistrictCommonDiseasesAnalysisVO.SaprodontiaRatioVO{
        /**
         * 龋均
         */
        private String dmftRatio;


    }

    @Data
    public static class SaprodontiaSexVO{
        /**
         * 性别说明
         */
        private SaprodontiaSexVariableVO saprodontiaSexVariableVO;
        /**
         * 性别数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaMonitorTableList;


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
    }


    @Data
    public static class SaprodontiaSchoolAgeVO{

        /**
         * 学龄段说明
         */
        private SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO;
        /**
         * 学龄段数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaMonitorTableList;

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

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SaprodontiaSchoolAge extends DistrictCommonDiseasesAnalysisVO.SaprodontiaRatioVO{

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
         * 年龄段数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaMonitorTableList;

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
         * 年龄
         */
        private String grade;
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

        /**
         * 龋均
         */
        private String dmftRatio;
    }
}
