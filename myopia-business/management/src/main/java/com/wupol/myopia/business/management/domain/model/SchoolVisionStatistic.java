package com.wupol.myopia.business.management.domain.model;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学校某次筛查计划统计视力情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_vision_statistic")
public class SchoolVisionStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视力情况--所属的学校id
     */
    private Integer schoolId;

    /**
     * 视力情况--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 视力情况--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 视力情况--关联的筛查计划id
     */
    private Integer screeningPlanId;

    /**
     * 视力情况--所属的地区id
     */
    private Integer districtId;

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
    @TableField("vision_label_0_numbers")
    private Integer visionLabel0Numbers;

    /**
     * 视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_0_ratio")
    private BigDecimal visionLabel0Ratio;

    /**
     * 视力情况--一级预警人数（默认0）
     */
    @TableField("vision_label_1_numbers")
    private Integer visionLabel1Numbers;

    /**
     * 视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_1_ratio")
    private BigDecimal visionLabel1Ratio;

    /**
     * 视力情况--二级预警人数（默认0）
     */
    @TableField("vision_label_2_numbers")
    private Integer visionLabel2Numbers;

    /**
     * 视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_2_ratio")
    private BigDecimal visionLabel2Ratio;

    /**
     * 视力情况--三级预警人数（默认0）
     */
    @TableField("vision_label_3_numbers")
    private Integer visionLabel3Numbers;

    /**
     * 视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_3_ratio")
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
     * 视力情况--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static SchoolVisionStatistic build(Integer schoolId, Integer screeningNoticeId, Integer screeningTaskId, Integer screeningPlanId, Integer districtId,
                                              List<StatConclusionVo> statConclusions, Integer planScreeningNumbers) {
        SchoolVisionStatistic statistic = new SchoolVisionStatistic();
        Integer wearingGlassNumber = (int) statConclusions.stream().filter(StatConclusion::getIsWearingGlasses).count();
        Integer myopiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        Integer ametropiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer lowVisionNumber = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer treatmentAdviceNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionL).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionR).average().orElse(0);
        //TODO ratio
        statistic.setSchoolId(schoolId).setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setScreeningPlanId(screeningPlanId).setDistrictId(districtId)
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setWearingGlassesNumbers(wearingGlassNumber).setWearingGlassesRatio(BigDecimal.ZERO)
                .setMyopiaNumbers(myopiaNumber).setMyopiaRatio(BigDecimal.ZERO)
                .setAmetropiaNumbers(ametropiaNumber).setAmetropiaRatio(BigDecimal.ZERO)
                .setLowVisionNumbers(lowVisionNumber).setLowVisionRatio(BigDecimal.ZERO)
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel0Ratio(BigDecimal.ZERO)
                .setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel1Ratio(BigDecimal.ZERO)
                .setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel2Ratio(BigDecimal.ZERO)
                .setVisionLabel3Numbers(visionLabel3Numbers).setVisionLabel3Ratio(BigDecimal.ZERO)
                .setTreatmentAdviceNumbers(treatmentAdviceNumber).setTreatmentAdviceRatio(BigDecimal.ZERO)
                .setKeyWarningNumbers(keyWarningNumbers).setVisionLabel3Ratio(BigDecimal.ZERO)
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(statConclusions.size());
        return statistic;
    }
}
