package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/5/24 15:41
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SchoolMonitorStatisticDTO {

    private SchoolMonitorStatistic schoolMonitorStatistic;
    private boolean hasRescreenReport;

}
