package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 散光表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AstigmatismTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 近视前期人数
     */
    private Integer earlyMyopiaCount;

    /**
     * 近视前期率
     */
    private Integer earlyMyopiaProportion;

    /**
     * 近视人数
     */
    private Integer myopiaCount;

    /**
     * 近视率
     */
    private Integer myopiaProportion;

    /**
     * 散光人数
     */
    private Integer astigmatismCount;

    /**
     * 散光率
     */
    private Integer astigmatismProportion;

    /**
     * 低度近视人数
     */
    private Integer lowMyopiaCount;

    /**
     * 低度近视率
     */
    private Integer lowMyopiaProportion;

    /**
     * 高度近视人数
     */
    private Integer highMyopiaCount;

    /**
     * 高度近视率
     */
    private Integer highMyopiaProportion;
}
