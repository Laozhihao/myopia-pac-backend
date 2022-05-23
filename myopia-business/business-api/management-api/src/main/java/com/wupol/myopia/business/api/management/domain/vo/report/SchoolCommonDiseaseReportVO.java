package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 按学校常见病报告实体
 *
 * @author hang.yuan 2022/5/16 14:53
 */
@Data
public class SchoolCommonDiseaseReportVO {
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
    private SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO;

    @Data
    public static class GlobalVariableVO {
        /**
         * 学龄段
         */
        private String schoolAgeName;

        /**
         * 报告生成日期
         */
        private Date  reportDate;

        /**
         * 筛查时间段
         */
        private String screeningTimePeriod;

        /**
         * 开始有数据的年度
         */
        private String dataYear;


        /**
         * 参与问卷调查人数
         */
        private Integer takeQuestionnaireNum;
        /**
         * 筛查机构名称
         */
        private String screeningOrgName;
    }

    @Data
    public static class VisionAnalysisVO{
        /**
         * 有效筛查人数
         */
        private Integer validScreeningNum;

        /**
         * 视力低下人数
         */
        private String lowVisionNum;
        /**
         *  视力低下率
         */
        private String lowVisionRatio;

        /**
         * 平均视力
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal avgVision;

        /**
         * 近视人数
         */
        private String myopiaNum;
        /**
         * 近视率
         */
        private String myopiaRatio;

        /**
         * 夜戴角膜塑形镜人数
         */
        private Integer nightWearingOrthokeratologyLensesNum;

        /**
         * 夜戴角膜塑形镜率
         */
        private String nightWearingOrthokeratologyLensesRatio;

        /**
         * 近视前期人数
         */
        private Integer myopiaLevelEarlyNum;

        /**
         * 近视前期率
         */
        private String myopiaLevelEarlyRatio;

        /**
         * 低度近视人数
         */
        private Integer lowMyopiaNum;

        /**
         * 低度近视率
         */
        private String lowMyopiaRatio;

        /**
         * 高度近视人数
         */
        private Integer highMyopiaNum;
        /**
         * 高度近视率
         */
        private String highMyopiaRatio;

        /**
         * 散光数人数
         */
        private Integer astigmatismNum;

        /**
         * 散光数率
         */
        private String astigmatismRatio;

        /**
         * 近视足矫人数
         */
        private Integer myopiaEnoughCorrectedNum;

        /**
         * 近视足矫率
         */
        private String myopiaEnoughCorrectedRatio;

        /**
         * 近视未矫人数
         */
        private String myopiaUncorrectedNum;

        /**
         * 近视未矫率
         */
        private String myopiaUncorrectedRatio;

        /**
         * 近视欠矫人数
         */
        private String myopiaUnderCorrectedNum;

        /**
         * 近视欠矫率
         */
        private String myopiaUnderCorrectedRatio;

    }
}
