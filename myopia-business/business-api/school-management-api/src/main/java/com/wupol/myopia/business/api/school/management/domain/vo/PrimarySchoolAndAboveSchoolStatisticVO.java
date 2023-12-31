package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 小学及以上统计
 *
 * @author hang.yuan 2022/9/18 17:02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrimarySchoolAndAboveSchoolStatisticVO extends VisionStatisticVO implements SchoolStatistic {


    //视力情况统计

    /**
     * 戴镜人数（默认0）
     */
    private Integer wearingGlassesNum;

    /**
     * 戴镜率（均为整数，如10.01%，数据库则是1001）
     */
    private String wearingGlassesRatio;


    /**
     * 夜戴角膜塑形镜人数（默认0）
     */
    private Integer nightWearingOrthokeratologyLensesNum;

    /**
     * 夜戴角膜塑形镜率
     */
    private String nightWearingOrthokeratologyLensesRatio;

    /**
     * 近视人数（默认0）
     */
    private Integer myopiaNum;

    /**
     * 近视比例（均为整数，如10.01%，数据库则是1001）
     */
    private String myopiaRatio;

    /**
     * 近视前期人数（默认0）
     */
    private Integer myopiaLevelEarlyNum;

    /**
     * 近视前期率
     */
    private String myopiaLevelEarlyRatio;

    /**
     * 低度近视人数（默认0）
     */
    private Integer lowMyopiaNum;

    /**
     * 低度近视率
     */
    private String lowMyopiaRatio;

    /**
     * 高度近视人数（默认0）
     */
    private Integer highMyopiaNum;
    /**
     * 高度近视率
     */
    private String highMyopiaRatio;



    //常见病监测

    /**
     * 龋均数（默认0）
     */
    private Integer dmftNum;

    /**
     * 龋均率
     */
    private String dmftRatio;

    /**
     * 龋患人数（默认0）
     */
    private Integer saprodontiaNum;

    /**
     * 龋患率
     */
    private String saprodontiaRatio;

    /**
     * 龋失人数（默认0）
     */
    private Integer saprodontiaLossNum;

    /**
     * 龋失率
     */
    private String saprodontiaLossRatio;

    /**
     * 龋补人数（默认0）
     */
    private Integer saprodontiaRepairNum;

    /**
     * 龋补率
     */
    private String saprodontiaRepairRatio;

    /**
     * 龋患（失、补）人数（默认0）
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 龋患（失、补）率
     */
    private String saprodontiaLossAndRepairRatio;

    /**
     * 龋患（失、补）构成比（默认0）
     */
    private Integer saprodontiaLossAndRepairTeethNum;

    /**
     * 龋患（失、补）构成比率
     */
    private String saprodontiaLossAndRepairTeethRatio;


    /**
     * 脊柱弯曲异常人数（默认0）
     */
    private Integer abnormalSpineCurvatureNum;

    /**
     * 脊柱弯曲异常率
     */
    private String abnormalSpineCurvatureRatio;

    /**
     * 血压偏高人数（默认0）
     */
    private Integer highBloodPressureNum;

    /**
     * 血压偏高率
     */
    private String highBloodPressureRatio;

    /**
     * 超重人数（默认0）
     */
    private Integer overweightNum;

    /**
     * 超重率
     */
    private String overweightRatio;

    /**
     * 肥胖人数（默认0）
     */
    private Integer obeseNum;

    /**
     * 肥胖率
     */
    private String obeseRatio;

    /**
     * 营养不良人数（默认0）
     */
    private Integer malnourishedNum;

    /**
     * 营养不良率
     */
    private String malnourishedRatio;

    /**
     * 生长迟缓人数（默认0）
     */
    private Integer stuntingNum;

    /**
     * 生长迟缓率
     */
    private String stuntingRatio;
}
