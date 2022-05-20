package com.wupol.myopia.business.api.management.domain.dto.report.vision.school;

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
    private Long earlyMyopiaCount;

    /**
     * 近视前期率
     */
    private String earlyMyopiaProportion;

    /**
     * 近视人数
     */
    private Long myopiaCount;

    /**
     * 近视率
     */
    private String myopiaProportion;

    /**
     * 散光人数
     */
    private Long astigmatismCount;

    /**
     * 散光率
     */
    private String astigmatismProportion;

    /**
     * 低度近视人数
     */
    private Long lightMyopiaCount;

    /**
     * 低度近视率
     */
    private String lightMyopiaProportion;

    /**
     * 高度近视人数
     */
    private Long highMyopiaCount;

    /**
     * 高度近视率
     */
    private String highMyopiaProportion;
}
