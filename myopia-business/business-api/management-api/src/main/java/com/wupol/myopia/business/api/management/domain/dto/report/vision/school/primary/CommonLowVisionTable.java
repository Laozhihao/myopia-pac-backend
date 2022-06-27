package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.LowVisionLevelTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 年龄段视力低下表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CommonLowVisionTable extends LowVisionLevelTable {

    /**
     * 平均视力
     */
    private String avgVision;

}
