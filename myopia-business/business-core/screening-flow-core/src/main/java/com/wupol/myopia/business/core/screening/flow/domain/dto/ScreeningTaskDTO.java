package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 筛查任务新增/更新的数据结构
 *
 * @author Alix
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningTaskDTO extends ScreeningTask {
    /**
     * 筛查任务中的筛查机构
     */
    private List<ScreeningTaskOrg> screeningOrgs;

}
