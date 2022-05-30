package com.wupol.myopia.business.api.management.domain.vo.report;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
         * 学校名称
         */
        private String schoolName;

        /**
         * 报告生成日期
         */
        @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
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
         * 视力低下
         */
        private Item lowVision;

        /**
         * 平均视力
         */
        private BigDecimal avgVision;

        /**
         * 近视
         */
        private Item myopia;

        /**
         * 夜戴角膜塑形镜
         */
        private Item nightWearingOrthokeratologyLenses;

        /**
         * 近视前期
         */
        private Item myopiaLevelEarly;

        /**
         * 低度近视
         */
        private Item lowMyopia;

        /**
         * 高度近视
         */
        private Item highMyopia;

        /**
         * 散光
         */
        private Item astigmatism;

        /**
         * 近视足矫
         */
        private Item myopiaEnoughCorrected;

        /**
         * 近视未矫
         */
        private Item myopiaUncorrected;

        /**
         * 近视欠矫
         */
        private Item myopiaUnderCorrected;

    }

    @Data
    public static class Item {
        /**
         * 人数
         */
        private Integer num;
        /**
         * 占比(不带%)
         */
        private BigDecimal ratio;

        public Item(Integer num, BigDecimal ratio) {
            this.num = num;
            this.ratio = ratio;
        }
    }
}
