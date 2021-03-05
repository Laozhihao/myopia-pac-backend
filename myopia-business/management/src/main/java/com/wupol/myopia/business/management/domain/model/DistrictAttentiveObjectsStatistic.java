package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.constant.WarningLevel;
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
 * 某个地区层级最新统计的重点视力对象情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_district_attentive_objects_statistic")
public class DistrictAttentiveObjectsStatistic implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 重点视力对象--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 重点视力对象--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 重点视力对象--所属的地区id
     */
    private Integer districtId;

    /**
     * 重点视力对象--零级预警人数（默认0）
     */
    private Integer visionLabel0Numbers;
    /**
     * 重点视力对象--是否 合计  0=否 1=是
     */
    private Integer isTotal;
    /**
     * 重点视力对象--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel0Ratio;
    /**
     * 重点视力对象--一级预警人数（默认0）
     */
    private Integer visionLabel1Numbers;

    /**
     * 重点视力对象--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel1Ratio;

    /**
     * 重点视力对象--二级预警人数（默认0）
     */
    private Integer visionLabel2Numbers;

    /**
     * 重点视力对象--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel2Ratio;

    /**
     * 重点视力对象--三级预警人数（默认0）
     */
    private Integer visionLabel3Numbers;

    /**
     * 重点视力对象--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal visionLabel3Ratio;

    /**
     * 重点视力对象--重点视力对象数量（默认0）
     */
    private Integer keyWarningNumbers;

    /**
     * 重点视力对象--学生总数
     */
    private Integer studentNumbers;

    /**
     * 重点视力对象--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static DistrictAttentiveObjectsStatistic build(Integer screeningNoticeId, Integer screeningTaskId, Integer districtId, Integer isTotal,
                                                          List<StatConclusion> statConclusions) {
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        DistrictAttentiveObjectsStatistic statistic = new DistrictAttentiveObjectsStatistic();
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        statistic.setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setDistrictId(districtId).setIsTotal(isTotal)
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel3Numbers(visionLabel3Numbers)
                .setKeyWarningNumbers(keyWarningNumbers).setStudentNumbers(statConclusions.size());
        return statistic;
    }
}
