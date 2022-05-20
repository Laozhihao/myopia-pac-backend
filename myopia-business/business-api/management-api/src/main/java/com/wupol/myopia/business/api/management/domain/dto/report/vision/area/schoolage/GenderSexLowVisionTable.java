package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import lombok.Getter;
import lombok.Setter;

/**
 * 不同性别视力低下表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderSexLowVisionTable {

    /**
     * 性别
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 幼儿园-人数
     */
    private Long kCount;

    /**
     * 幼儿园-占比
     */
    private String kProportion;

    /**
     * 幼儿园-平均视力
     */
    private String kAvgVision;

    /**
     * 小学以上-人数
     */
    private Long pCount;

    /**
     * 小学以上-占比
     */
    private String pProportion;

    /**
     * 小学以上-平均视力
     */
    private String pAvgVision;



}
