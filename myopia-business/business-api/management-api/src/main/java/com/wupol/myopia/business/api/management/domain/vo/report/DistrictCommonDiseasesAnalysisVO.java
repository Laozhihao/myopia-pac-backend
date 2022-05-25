package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 按区域常见病分析
 *
 * @author hang.yuan 2022/5/16 16:07
 */
@Data
public class DistrictCommonDiseasesAnalysisVO {

    /**
     * 常见病分析变量
     */
    private CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO;

    /**
     * 疾病监测情况
     */
    private DistrictDiseaseMonitorVO districtDiseaseMonitorVO;

    /**
     * 龋齿监测结果
     */
    private DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO;

    /**
     * 体重身高监测结果
     */
    private DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    private DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO;

    /**
     * 各学校筛查情况
     */
    private DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO;



    @Data
    public static class CommonDiseasesAnalysisVariableVO {
        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;

        /**
         * 龋齿数据
         */
        private SaprodontiaVO saprodontiaVO;
        /**
         * 身高体重数据
         */
        private HeightAndWeightVO heightAndWeightVO;
        /**
         * 血压与脊柱弯曲数据
         */
        private BloodPressureAndSpinalCurvatureVO bloodPressureAndSpinalCurvatureVO;

    }



    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SaprodontiaVO extends SaprodontiaRatioVO{

        /**
         * 龋失补牙数
         */
        private Integer dmftNum;

        /**
         * 龋均
         */
        private String dmftRatio;

        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋失人数
         */
        private Integer saprodontiaLossNum;

        /**
         * 龋补人数
         */
        private Integer saprodontiaRepairNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 龋患（失、补）牙数
         */
        private Integer saprodontiaLossAndRepairTeethNum;

    }

    @Data
    public static class SaprodontiaRatioVO{
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
         * 龋患（失、补）率
         */
        private String saprodontiaLossAndRepairRatio;

        /**
         * 龋患（失、补）构成比
         */
        private String saprodontiaLossAndRepairTeethRatio;

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
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        private BigDecimal obeseRatio;

        /**
         * 营养不良率
         */
        private BigDecimal malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private BigDecimal stuntingRatio;

    }

    @Data
    public static class HeightAndWeightRatioStrVO {
        /**
         * 超重率
         */
        private String overweightRatio;
        /**
         * 肥胖率
         */
        private String obeseRatio;

        /**
         * 营养不良率
         */
        private String malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private String stuntingRatio;

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
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
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
