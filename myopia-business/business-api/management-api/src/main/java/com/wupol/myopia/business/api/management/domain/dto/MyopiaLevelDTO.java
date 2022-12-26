package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 近视情况（程度）
 * @Author wulizhou
 * @Date 2022/12/26 17:31
 */
@Data
@Builder
@Accessors(chain = true)
public class MyopiaLevelDTO {

    /**
     * 维度名称，性别/年级名称/班级名称
     */
    private String name;

    /**
     * 有效筛查人数
     */
    private Long validScreeningNum;

    /**
     * 视力不良人数
     */
    private Long lowVisionNum;

    /**
     * 视力不良率
     */
    private Float lowVisionRatio;

    /**
     * 低度近视人数
     */
    private Long lightMyopiaNum;

    /**
     * 低度近视率
     */
    private String lightMyopiaRatio;

    /**
     * 中度近视人数
     */
    private Long middleMyopiaNum;

    /**
     * 中度近视率
     */
    private String middleMyopiaRatio;

    /**
     * 高度近视人数
     */
    private Long highMyopiaNum;

    /**
     * 高度近视率
     */
    private String highMyopiaRatio;

}
