package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonWarningTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 预警表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningTable extends CommonWarningTable {

    /**
     * 年级名称
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;
}
