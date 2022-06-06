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
     * 龋齿监测 - 不同年龄段
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;




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


}
