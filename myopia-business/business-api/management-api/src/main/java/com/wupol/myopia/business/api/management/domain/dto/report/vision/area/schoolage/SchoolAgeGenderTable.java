package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import lombok.Getter;
import lombok.Setter;

/**
 * 学龄段性别统计表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolAgeGenderTable {

    /**
     * 学龄段
     */
    private String name;

    /**
     * 男-筛查人数
     */
    private Long mCount;

    /**
     * 男-有效人数
     */
    private Long mValidCount;

    /**
     * 女-筛查人数
     */
    private Long fCount;

    /**
     * 女-有效人数
     */
    private Long fValidCount;

    /**
     * 合计-筛查人数
     */
    private Long totalCount;

    /**
     * 合计-有效人数
     */
    private Long totalValidCount;

    /**
     * 有效率
     */
    private String totalValidProportion;

}
