package com.wupol.myopia.business.api.screening.app.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 复测情况
 * @Author xz
 * @Date 2022/4/27
 **/
@Accessors(chain = true)
@Data
public class ClassScreeningProgressState {
    /**
     * 计划筛查人数
     */
    private Integer planCount;
    /**
     * 实际筛查人数（有筛查数据的）
     */
    private Integer screeningCount;
    /**
     * 参与复测学生数（复测中的人数）
     */
    private Integer needReScreeningCount;
    /**
     * 可复测学生数总数
     */
    private Integer reScreeningCount;
    /**
     * 戴镜人数
     */
    private Integer wearingGlasses;
    /**
     * 非戴镜人数
     */
    private Integer noWearingGlasses;
    /**
     * 复测率
     */
    private BigDecimal retestRatio;
    /**
     * 复测项次
     */
    private Integer retestItemCount;
    /**
     * 错误项次数
     */
    private Integer errorItemCount;
    /**
     * 错误率
     */
    private BigDecimal errorRatio;

    private List<RetestStudentVO> retestStudents;
}
