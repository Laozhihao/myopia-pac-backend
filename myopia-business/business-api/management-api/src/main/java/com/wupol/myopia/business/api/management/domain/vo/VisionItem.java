package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 视力筛查项
 * @author hang.yuan
 * @date 2022/6/20
 */
@Data
public class VisionItem {
    /**
     * 小学及以上--近视前期人数（默认0）
     */
    private Integer myopiaLevelEarlyNum;

    /**
     * 小学及以上--近视前期率
     */
    private String myopiaLevelEarlyRatio;

    /**
     * 小学及以上--低度近视人数（默认0）
     */
    private Integer lowMyopiaNum;

    /**
     * 小学及以上--低度近视率
     */
    private String lowMyopiaRatio;

    /**
     * 小学及以上--高度近视人数（默认0）
     */
    private Integer highMyopiaNum;

    /**
     * 小学及以上--高度近视率
     */
    private String highMyopiaRatio;
}