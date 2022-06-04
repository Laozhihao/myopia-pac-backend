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
    public static class SaprodontiaGradeVO{

        /**
         * 年级说明
         */
        private SaprodontiaGradeVariableVO saprodontiaGradeVariableVO;
        /**
         * 年级表格数据
         */
        private List<SaprodontiaMonitorTable> saprodontiaGradeMonitorTableList;
        /**
         * 年级图表
         */
        private ChartVO.Chart saprodontiaGradeMonitorChart;

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


}
