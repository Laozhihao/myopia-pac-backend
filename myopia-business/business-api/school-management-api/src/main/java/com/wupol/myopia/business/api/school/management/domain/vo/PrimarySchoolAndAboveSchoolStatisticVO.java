package com.wupol.myopia.business.api.school.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小学及以上统计
 *
 * @author hang.yuan 2022/9/18 17:02
 */
@Data
@Accessors(chain = true)
public class PrimarySchoolAndAboveSchoolStatisticVO implements SchoolStatistic {

    // 视力筛查情况
    /**
     * 计划的学生数量（默认0）
     */
    private Integer planScreeningNum;

    /**
     * 实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNum;

    /**
     * 完成率
     */
    private String finishRatio;

    /**
     * 纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNum;



    //视力情况统计
    /**
     * 视力低下人数（默认0）
     */
    private Integer lowVisionNum;

    /**
     * 视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private String lowVisionRatio;

    /**
     * 平均左眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgLeftVision;

    /**
     * 平均右眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgRightVision;

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


    //视力监测预警
    /**
     * 视力预警人数
     */
    private Integer visionWarningNum;
    /**
     *  视力预警比例
     */
    private String visionWarningRatio;

    /**
     * 零级预警人数（默认0）
     */
    private Integer visionLabel0Num;

    /**
     * 零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel0Ratio;

    /**
     * 一级预警人数（默认0）
     */
    private Integer visionLabel1Num;

    /**
     * 一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel1Ratio;

    /**
     * 二级预警人数（默认0）
     */
    private Integer visionLabel2Num;

    /**
     * 二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel2Ratio;

    /**
     * 三级预警人数（默认0）
     */
    private Integer visionLabel3Num;

    /**
     * 三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel3Ratio;


    //视力异常

    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private String treatmentAdviceRatio;

    /**
     * 去医院就诊数
     */
    private Integer reviewNum;

    /**
     * 去医院就诊比例
     */
    private String reviewRatio;

    /**
     * 绑定公众号人数
     */
    private Integer bindMpNum;
    /**
     * 绑定公众号比例
     */
    private Integer bindMpRatio;

    //常见病监测

    /**
     * 龋失补牙数 TODO
     */

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
