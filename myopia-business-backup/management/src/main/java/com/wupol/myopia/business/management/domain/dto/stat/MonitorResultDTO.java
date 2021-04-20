package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description
 * @Date 2021/2/8 15:26
 * @Author by Jacob
 */
@Data
public class MonitorResultDTO {
    /**
     * 查看的范围
     */
    private Long rangeName;

    /**
     * 筛查学生数
     */
    private Integer screeningNum;
    /**
     * 现场调查人数
     */
    private Integer investigationNumbers;

    /**
     * 实际筛查学生数
     */
    private Integer actualScreeningNum;

    /**
     * 视力筛查完成率
     */
    private BigDecimal screeningFinishedRatio;

    /**
     * 复测人数
     */
    private Integer rescreenNum;

    /**
     * 戴镜复测人数
     */
    private Integer wearingGlassesRescreenNum;

    /**
     * 戴镜复测指标数
     */
    private Integer wearingGlassesRescreenIndexNum;

    /**
     * 非戴镜复测人数
     */
    private Integer withoutGlassesRescreenNum;

    /**
     * 非戴镜复测指标数
     */
    private Integer withoutGlassesRescreenIndexNum;

    /**
     * 复测项次
     */
    private Integer rescreenItemNum;

    /**
     * 错误项次数
     */
    private Integer incorrectItemNum;

    /**
     * 错误率/发生率
     */
    private BigDecimal incorrectRatio;

}

