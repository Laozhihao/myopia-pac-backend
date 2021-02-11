package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

import java.util.List;

@Data
public class ScreeningMonitorStatisticDTO extends ScreeningBasicResult {
    /**
     * 当前级的数据
     */
    private MonitorResultDTO totalData;
    /**
     * 当前级的数据
     */
    private MonitorResultDTO currentData;

    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private List<MonitorResultDTO> subordinateDataList;

}
