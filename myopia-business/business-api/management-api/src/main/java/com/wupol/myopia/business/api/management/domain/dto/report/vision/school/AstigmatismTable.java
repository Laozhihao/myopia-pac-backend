package com.wupol.myopia.business.api.management.domain.dto.report.vision.school;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 散光表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AstigmatismTable extends CommonTable {

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

    /**
     * 是否本次报告
     */
    private Boolean isSameReport;
}
