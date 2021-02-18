package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

@Data
public class ScreeningSchoolVisionStatisticDTO extends ScreeningBasicResult {
    /**
     * 行政区域
     */
    private String districtName;
    /**
     * 学校类型
     */
    private String type;
    /**
     * 当前级的数据
     */
    private VisionResultDTO content;
}
