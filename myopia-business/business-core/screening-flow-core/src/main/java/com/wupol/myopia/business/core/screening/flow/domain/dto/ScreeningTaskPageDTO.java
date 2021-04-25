package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/4/25 17:19
 */
@Data
@Accessors(chain = true)
public class ScreeningTaskPageDTO extends ScreeningTask {
    /**
     * 行政区域名
     */
    private String creatorName;
    /**
     * 行政区域名称
     */
    private String districtName;
    /**
     * 部门名称
     */
    private String govDeptName;
}
