package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolHistoryLowVisionTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 历年视力情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowVisionHistory {

    /**
     * 幼儿园
     */
    private String kProportion;

    /**
     * 小学及以上
     */
    private String pProportion;

    /**
     * 表格
     */
    private List<SchoolHistoryLowVisionTable> tables;
}
