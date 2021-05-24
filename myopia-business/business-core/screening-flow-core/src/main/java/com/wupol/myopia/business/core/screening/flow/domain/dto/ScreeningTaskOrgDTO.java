package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 筛查任务机构
 * @Author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningTaskOrgDTO extends ScreeningTaskOrg {

    /** 筛查机构名称 */
    private String name;
    /**
     * 筛查任务--开始时间
     */
    private Date startTime;

    /**
     * 筛查任务--结束时间
     */
    private Date endTime;
}