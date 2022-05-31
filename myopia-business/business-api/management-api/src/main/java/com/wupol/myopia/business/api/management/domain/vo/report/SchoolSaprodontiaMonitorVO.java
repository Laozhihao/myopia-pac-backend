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
public class SchoolSaprodontiaMonitorVO {

    /**
     * 说明
     */
    private SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private SaprodontiaSexVO saprodontiaSexVO;

    /**
     * 龋齿监测 - 不同年级段
     */
    private SaprodontiaGradeVO saprodontiaGradeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;



    @Data
    public static class SaprodontiaMonitorVariableVO {
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
    public static class SaprodontiaGradeVO{

        /**
         * 年级说明
         */
        private SaprodontiaGradeVariableVO saprodontiaGradeVariableVO;
        /**
         * 年级表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaGradeMonitorTableList;

    }
    @Data
    public static class SaprodontiaGradeVariableVO{
        /**
         * 最高年级龋患率
         */
        private GradeRatio saprodontiaRatio;

        /**
         * 最高年级龋失率
         */
        private GradeRatio saprodontiaLossRatio;

        /**
         * 最高年级龋补率
         */
        private GradeRatio saprodontiaRepairRatio;

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
    public static class SaprodontiaAgeVO{
        /**
         * 年龄段说明
         */
        private SaprodontiaAgeVariableVO saprodontiaAgeVariableVO;
        /**
         * 年龄段表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaAgeMonitorTableList;

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
    public static class SaprodontiaMonitorTable extends SchoolCommonDiseasesAnalysisVO.SaprodontiaVO{

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
