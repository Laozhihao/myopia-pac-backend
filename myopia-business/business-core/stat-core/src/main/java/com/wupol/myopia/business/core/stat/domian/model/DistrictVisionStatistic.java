package com.wupol.myopia.business.core.stat.domian.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.util.MathUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 地区层级某次筛查计划统计视力情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_district_vision_statistic")
public class DistrictVisionStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视力情况--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 视力情况--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 视力情况--所属的地区id
     */
    private Integer districtId;
    /**
     * 视力情况--是否 合计  0=否 1=是
     */
    private Integer isTotal;
    /**
     * 视力情况--平均左眼视力（小数点后一位，默认0.0）
     */
    private BigDecimal avgLeftVision;

    /**
     * 视力情况--平均右眼视力（小数点后一位，默认0.0）
     */
    private BigDecimal avgRightVision;

    /**
     * 视力情况--视力低下人数（默认0）
     */
    private Integer lowVisionNumbers;

    /**
     * 视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal lowVisionRatio;

    /**
     * 视力情况--戴镜人数（默认0）
     */
    private Integer wearingGlassesNumbers;

    /**
     * 视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal wearingGlassesRatio;

    /**
     * 视力情况--近视人数（默认0）
     */
    private Integer myopiaNumbers;

    /**
     * 视力情况--近视比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal myopiaRatio;

    /**
     * 视力情况--屈光不正人数（默认0）
     */
    private Integer ametropiaNumbers;

    /**
     * 视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal ametropiaRatio;

    /**
     * 视力情况--零级预警人数（默认0）
     */
    private Integer visionLabel0Numbers;

    /**
     * 视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel0Ratio;

    /**
     * 视力情况--一级预警人数（默认0）
     */
    private Integer visionLabel1Numbers;

    /**
     * 视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel1Ratio;

    /**
     * 视力情况--二级预警人数（默认0）
     */
    private Integer visionLabel2Numbers;

    /**
     * 视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel2Ratio;

    /**
     * 视力情况--三级预警人数（默认0）
     */
    private Integer visionLabel3Numbers;

    /**
     * 视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel3Ratio;

    /**
     * 视力情况--重点视力对象数量（默认0）
     */
    private Integer keyWarningNumbers;

    /**
     * 视力情况--建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNumbers;

    /**
     * 视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal treatmentAdviceRatio;

    /**
     * 视力情况--计划的学生数量（默认0）
     */
    private Integer planScreeningNumbers;

    /**
     * 视力情况--实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNumbers;

    /**
     * 视力情况--纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNumbers;

    /**
     * 视力情况--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static DistrictVisionStatistic build(Integer screeningNoticeId, Integer screeningTaskId, Integer districtId, Integer isTotal,
                                                List<StatConclusion> statConclusions, Integer realScreeningNumber, Integer planScreeningNumbers) {
        DistrictVisionStatistic statistic = new DistrictVisionStatistic();
        Integer wearingGlassNumber = (int) statConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        Integer myopiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        Integer ametropiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer lowVisionNumber = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        // 预警人群、建议就诊使用所有筛查数据（有效、无效）
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer treatmentAdviceNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionL).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionR).average().orElse(0);
        int validScreeningNumbers = statConclusions.size();
        statistic.setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setDistrictId(districtId).setIsTotal(isTotal)
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setWearingGlassesNumbers(wearingGlassNumber).setWearingGlassesRatio(MathUtil.divide(wearingGlassNumber, validScreeningNumbers))
                .setMyopiaNumbers(myopiaNumber).setMyopiaRatio(MathUtil.divide(myopiaNumber, validScreeningNumbers))
                .setAmetropiaNumbers(ametropiaNumber).setAmetropiaRatio(MathUtil.divide(ametropiaNumber, validScreeningNumbers))
                .setLowVisionNumbers(lowVisionNumber).setLowVisionRatio(MathUtil.divide(lowVisionNumber, validScreeningNumbers))
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel0Ratio(MathUtil.divide(visionLabel0Numbers, validScreeningNumbers))
                .setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel1Ratio(MathUtil.divide(visionLabel1Numbers, validScreeningNumbers))
                .setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel2Ratio(MathUtil.divide(visionLabel2Numbers, validScreeningNumbers))
                .setVisionLabel3Numbers(visionLabel3Numbers).setVisionLabel3Ratio(MathUtil.divide(visionLabel3Numbers, validScreeningNumbers))
                .setTreatmentAdviceNumbers(treatmentAdviceNumber).setTreatmentAdviceRatio(MathUtil.divide(treatmentAdviceNumber, validScreeningNumbers))
                .setKeyWarningNumbers(keyWarningNumbers).setValidScreeningNumbers(validScreeningNumbers)
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(realScreeningNumber);
        return statistic;
    }

}
