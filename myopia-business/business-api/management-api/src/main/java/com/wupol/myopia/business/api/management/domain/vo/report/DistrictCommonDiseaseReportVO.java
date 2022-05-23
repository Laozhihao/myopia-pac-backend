package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 按区域常见病报告实体
 *
 * @author hang.yuan 2022/5/16 14:53
 */
@Data
public class DistrictCommonDiseaseReportVO {

    /**
     * 全局变量
     */
    private GlobalVariableVO globalVariableVO;

    /**
     * 筛查学生数
     */
    private Integer screeningStudentNum;
    /**
     * 实际筛查人数
     */
    private Integer actualScreeningNum;

    /**
     * 视力分析
     */
    private VisionAnalysisVO visionAnalysisVO;

    /**
     * 常见病分析
     */
    private DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO;


    @Data
    @Accessors(chain = true)
    public static class GlobalVariableVO {
        /**
         * 筛查时间段的年度
         */
        private String year;
        /**
         * 导出者选的区域（行政区域）
         */
        private String areaName;
        /**
         * 筛查时间段
         */
        private String screeningTimePeriod;
        /**
         * 学校数
         */
        private Integer totalSchoolNum;
        /**
         * 学校列表
         */
        private List<String> schoolItem;
        /**
         * 开始有数据的年度
         */
        private String dataYear;

    }



    @Data
    public static class VisionAnalysisVO {
        /**
         * 有效筛查人数
         */
        private Integer validScreeningNum;
        /**
         * 幼儿园
         */
        private KindergartenVO kindergartenVO;
        /**
         * 小学及以上
         */
        private PrimarySchoolAndAboveVO primarySchoolAndAboveVO;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class KindergartenVO extends BaseVO{

        /**
         * 远视储备不足率
         */
        private BigDecimal myopiaLevelInsufficientRatio;
        /**
         * 屈光参差率
         */
        private BigDecimal anisometropiaRatio;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class PrimarySchoolAndAboveVO extends BaseVO{

        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

        /**
         * 小学阶段
         */
        private MyopiaItemVO primarySchool;
        /**
         * 初中阶段
         */
        private MyopiaItemVO juniorHighSchool;
        /**
         * 高中阶段（普高+职高）
         */
        private MyopiaItemVO highSchool;
        /**
         * 普高
         */
        private MyopiaItemVO normalHighSchool;
        /**
         * 职高
         */
        private MyopiaItemVO vocationalHighSchool;
        /**
         * 大学
         */
        private MyopiaItemVO university;

    }
    @Data
    public static class BaseVO{
        /**
         *  视力低常率
         */
        private BigDecimal lowVisionRatio;

        /**
         * 平均视力(占比,在0-6之间)
         */
        private BigDecimal avgVision;

    }

    @Data
    public static class MyopiaItemVO{
        /**
         * 学龄段
         */
        private String schoolAge;
        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

    }

}
