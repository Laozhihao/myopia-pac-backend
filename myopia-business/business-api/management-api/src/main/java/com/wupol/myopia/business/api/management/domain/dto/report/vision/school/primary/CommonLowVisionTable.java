package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 年龄段视力低下表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CommonLowVisionTable extends CommonTable {

    /**
     * 平均视力
     */
    private String avgVision;

    /**
     * 视力低下人数
     */
    private Long lowVisionCount;

    /**
     * 视力低下占比
     */
    private String lowVisionProportion;

    /**
     * 轻度-视力低下人数
     */
    private Long lightLowVisionCount;

    /**
     * 轻度-视力低下占比
     */
    private String lightLowVisionProportion;

    /**
     * 中度-视力低下人数
     */
    private Long middleLowVisionCount;

    /**
     * 中度-视力低下占比
     */
    private String middleLowVisionProportion;

    /**
     * 重度-视力低下人数
     */
    private Long highLowVisionCount;

    /**
     * 重度-视力低下占比
     */
    private String highLowVisionProportion;


}
