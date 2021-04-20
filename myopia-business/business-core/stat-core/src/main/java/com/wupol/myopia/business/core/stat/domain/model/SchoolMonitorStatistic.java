package com.wupol.myopia.business.core.stat.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.vo.StatConclusionDTO;
import com.wupol.myopia.business.management.util.MathUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学校某次筛查计划统计监控监测情况表
 *
 * @Author HaoHao
 * @Date 2021-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_monitor_statistic")
public class SchoolMonitorStatistic implements Serializable {

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
     * 监测情况--所属的学校名称
     */
    private String schoolName;

    /**
     * 监测情况--所属的学校类型
     */
    private Integer schoolType;

    /**
     * 监测情况--筛查机构id
     */
    private Integer screeningOrgId;
    /**
     * 监测情况--复测项数目
     */
    private Integer rescreeningItemNumbers;
    /**
     * 监测情况--筛查机构名
     */
    private String screeningOrgName;
    /**
     * 监测情况--所属的通知id
     */
    private Integer screeningNoticeId;
    /**
     * 监测情况--关联的任务id
     */
    private Integer screeningTaskId;
    /**
     * 监测情况--所属的地区id（筛查范围）
     */
    private Integer districtId;
    /**
     * 监测情况--戴镜人数（默认0）
     */
    private Integer investigationNumbers;

    /**
     * 监测情况--脱镜复测数量（默认0）
     */
    private Integer withoutGlassDsn;

    /**
     * 监测情况--脱镜复测指标数（dsin = double screening index numbers默认0）
     */
    private Integer withoutGlassDsin;

    /**
     * 监测情况--戴镜复测数量（默认0）
     */
    private Integer wearingGlassDsn;

    /**
     * 监测情况--戴镜复测指标数（dsin = double screening index numbers默认0）
     */
    private Integer wearingGlassDsin;

    /**
     * 监测情况--复测数量（默认0）
     */
    private Integer dsn;

    /**
     * 监测情况--筛查错误数（默认0）
     */
    private Integer errorNumbers;

    /**
     * 监测情况--筛查错误率（默认0，单位%）
     */
    private BigDecimal errorRatio;

    /**
     * 监测情况--完成率
     */
    private BigDecimal finishRatio;

    /**
     * 监测情况--计划的学生数量（默认0）
     */
    private Integer planScreeningNumbers;

    /**
     * 监测情况--实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNumbers;

    /**
     * 监测情况--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static SchoolMonitorStatistic build(School school, ScreeningOrganization screeningOrg,
                                               Integer screeningNoticeId, Integer screeningTaskId,
                                               List<StatConclusionDTO> statConclusions, Integer planScreeningNumbers, Integer realScreeningNumbers) {
        SchoolMonitorStatistic statistic = new SchoolMonitorStatistic();
        Map<Boolean, Long> isWearGlassNumMap = statConclusions.stream().collect(Collectors.groupingBy(statConclusion -> statConclusion.getGlassesType()>0, Collectors.counting()));
        Integer withoutGlassDsn = isWearGlassNumMap.getOrDefault(false, 0L).intValue();
        Integer wearingGlassDsn = isWearGlassNumMap.getOrDefault(true, 0L).intValue();
        Integer rescreeningItemNumbers = withoutGlassDsn * 4 + wearingGlassDsn * 6;
        Integer errorNumbers = statConclusions.stream().mapToInt(StatConclusion::getRescreenErrorNum).sum();
        int dsn = statConclusions.size();
        statistic.setSchoolId(school.getId()).setSchoolName(school.getName()).setSchoolType(school.getType())
                .setScreeningOrgId(screeningOrg.getId()).setScreeningOrgName(screeningOrg.getName())
                .setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setDistrictId(school.getDistrictId())
                .setFinishRatio(MathUtil.divide(realScreeningNumbers, planScreeningNumbers))
                .setWithoutGlassDsn(withoutGlassDsn).setWithoutGlassDsin(4)
                .setWearingGlassDsn(wearingGlassDsn).setWearingGlassDsin(6)
                .setDsn(dsn).setRescreeningItemNumbers(rescreeningItemNumbers)
                .setErrorNumbers(errorNumbers).setErrorRatio(MathUtil.divide(errorNumbers, rescreeningItemNumbers))
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(realScreeningNumbers);
        //TODO investigationNumbers暂时处理为0
        statistic.setInvestigationNumbers(0);
        return statistic;
    }

}
