package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 按学校常见病分析
 *
 * @author hang.yuan 2022/5/16 16:07
 */
@Data
public class SchoolCommonDiseasesAnalysisVO {

    /**
     * 常见病分析变量
     */
    private CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO;


    /**
     * 龋齿监测结果
     */
    private SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO;

    /**
     * 体重身高监测结果
     */
    private SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    private SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO;

    /**
     * 各班级筛查情况
     */
    private List<SchoolClassScreeningMonitorVO> schoolClassScreeningMonitorVOList;



    @Data
    public static class CommonDiseasesAnalysisVariableVO {
        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;

        /**
         * 脊柱弯曲异常（小学、初中、高中）人数
         */
        private Integer abnormalSpineCurvatureNum;


        /**
         * 龋均
         */
        private Item dmft;

        /**
         * 有龋
         */
        private Item saprodontia;

        /**
         * 龋失
         */
        private Item saprodontiaLoss;

        /**
         * 龋补
         */
        private Item saprodontiaRepair;

        /**
         * 龋患（失、补）
         */
        private Item saprodontiaLossAndRepair;

        /**
         * 龋患（失、补）牙数
         */
        private Item saprodontiaLossAndRepairTeeth;

        /**
         * 超重
         */
        private Item overweight;

        /**
         * 肥胖人数
         */
        private Item obese;

        /**
         * 血压偏高人数
         */
        private Item highBloodPressure;

        /**
         * 脊柱弯曲异常人数
         */
        private Item abnormalSpineCurvature;

    }

    @Data
    public static class Item{
        /**
         * 数量
         */
        private Integer num;
        /**
         * 占比
         */
        private BigDecimal ratio;

        public Item(Integer num, BigDecimal ratio) {
            this.num = num;
            this.ratio = ratio;
        }
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HeightAndWeightVO extends HeightAndWeightRatioVO{

        /**
         * 超重人数
         */
        private Integer overweightNum;

        /**
         * 肥胖人数
         */
        private Integer obeseNum;

        /**
         * 营养不良人数
         */
        private Integer malnourishedNum;
        /**
         * 生长迟缓人数
         */
        private Integer stuntingNum;

    }

    @Data
    public static class HeightAndWeightRatioVO {
        /**
         * 超重率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal obeseRatio;

        /**
         * 营养不良率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal malnourishedRatio;

        /**
         * 生长迟缓率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal stuntingRatio;

    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BloodPressureAndSpinalCurvatureVO extends BloodPressureAndSpinalCurvatureRatioVO{
        /**
         * 血压偏高人数
         */
        private Integer highBloodPressureNum;

        /**
         * 脊柱弯曲异常人数
         */
        private Integer abnormalSpineCurvatureNum;

    }

    @Data
    public static class BloodPressureAndSpinalCurvatureRatioVO{

        /**
         * 血压偏高率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal abnormalSpineCurvatureRatio;

    }

    @Data
    public static class BloodPressureAndSpinalCurvatureRatioStrVO{

        /**
         * 血压偏高率
         */
        private String highBloodPressureRatioStr;

        /**
         * 脊柱弯曲异常率
         */
        private String abnormalSpineCurvatureRatioStr;

    }


}
