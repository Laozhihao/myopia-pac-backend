package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
public class SchoolKindergartenResultVO {

    /**
     * 通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型
     */
    private Integer screeningType;

    /**
     *  内容
     */
    private Set<Item> contents;


    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 学校id
         */
        private Integer schoolId;

        /**
         * 筛查计划id
         */
        private Integer screeningPlanId;

        /**
         * 计划的学生数量（默认0）
         */
        private Integer planScreeningNum;

        /**
         * 实际筛查的学生数量（默认0）
         */
        private Integer realScreeningNum;

        /**
         * 纳入统计的实际筛查学生数量（默认0）
         */
        private Integer validScreeningNum;

        /**
         * 视力低下人数（默认0）
         */
        private Integer lowVisionNum;

        /**
         * 视力低下比例（均为整数，如10.01%，数据库则是1001）
         */
        private BigDecimal lowVisionRatio;

        /**
         * 平均左眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgLeftVision;

        /**
         * 平均右眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgRightVision;

        /**
         * 幼儿园--屈光不正人数（默认0）
         */
        private Integer ametropiaNum;

        /**
         * 幼儿园--屈光不正比例（均为整数，如10.01%，数据库则是1001）
         */
        private BigDecimal ametropiaRatio;

        /**
         * 幼儿园--屈光参差人数（默认0）
         */
        private Integer anisometropiaNum;

        /**
         * 幼儿园--屈光参差率
         */
        private BigDecimal anisometropiaRatio;

        /**
         * 幼儿园--远视储备不足人数（默认0）
         */
        private Integer myopiaLevelInsufficientNum;

        /**
         * 幼儿园--远视储备不足率
         */
        private BigDecimal myopiaLevelInsufficientNumRatio;

        /**
         * 建议就诊数量（默认0）
         */
        private Integer treatmentAdviceNum;

        /**
         * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
         */
        private BigDecimal treatmentAdviceRatio;

    }

}
