package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
public class PrimarySchoolAndAboveResultVO {

    /**
     * 所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;

    /**
     * 合计数据
     */
    private Item totalData;

    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private Set<Item> childDataSet;

    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 学校数
         */
        private Integer schoolNum;
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
         * 建议就诊数量（默认0）
         */
        private Integer treatmentAdviceNum;

        /**
         * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
         */
        private BigDecimal treatmentAdviceRatio;

        /**
         * 小学及以上--近视人数（默认0）
         */
        private Integer myopiaNum;

        /**
         * 小学及以上--近视比例（均为整数，如10.01%，数据库则是1001）
         */
        private BigDecimal myopiaRatio;

        /**
         *  视力筛查项
         */
        private VisionItem visionItem;

        /**
         *  常见病筛查项
         */
        private CommonDiseaseItem commonDiseaseItem;


    }

    @Data
    @Accessors(chain = true)
    public static class VisionItem{
        /**
         * 小学及以上--近视前期人数（默认0）
         */
        private Integer myopiaLevelEarlyNum;

        /**
         * 小学及以上--近视前期率
         */
        private BigDecimal myopiaLevelEarlyRatio;

        /**
         * 小学及以上--低度近视人数（默认0）
         */
        private Integer lowMyopiaNum;

        /**
         * 小学及以上--低度近视率
         */
        private BigDecimal lowMyopiaRatio;

        /**
         * 小学及以上--高度近视人数（默认0）
         */
        private Integer highMyopiaNum;

        /**
         * 小学及以上--高度近视率
         */
        private BigDecimal highMyopiaRatio;
    }

    @Data
    @Accessors(chain = true)
    public static class CommonDiseaseItem{
        /**
         * 小学及以上--龋均人数（默认0）
         */
        private Integer dmftNum;

        /**
         * 小学及以上--龋均率
         */
        private BigDecimal dmftRatio;

        /**
         * 小学及以上--超重人数（默认0）
         */
        private Integer overweightNum;

        /**
         * 小学及以上--超重率
         */
        private BigDecimal overweightRatio;

        /**
         * 小学及以上--肥胖人数（默认0）
         */
        private Integer obeseNum;

        /**
         * 小学及以上--肥胖率
         */
        private BigDecimal obeseRatio;

        /**
         * 小学及以上--脊柱弯曲异常人数（默认0）
         */
        private Integer abnormalSpineCurvatureNum;

        /**
         * 小学及以上--脊柱弯曲异常率
         */
        private BigDecimal abnormalSpineCurvatureRatio;

        /**
         * 小学及以上--血压偏高人数（默认0）
         */
        private Integer highBloodPressureNum;

        /**
         * 小学及以上--血压偏高率
         */
        private BigDecimal highBloodPressureRatio;

        /**
         * 小学及以上--复查学生人数（默认0）
         */
        private Integer reviewStudentNum;

        /**
         * 小学及以上--复查学生率
         */
        private BigDecimal reviewStudentRatio;
    }
}
