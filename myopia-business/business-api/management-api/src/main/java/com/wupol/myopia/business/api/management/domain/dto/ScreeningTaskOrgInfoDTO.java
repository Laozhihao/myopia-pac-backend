package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 筛查任务筛查机构信息
 *
 * @author hang.yuan 2022/9/27 17:22
 */
@Data
public class ScreeningTaskOrgInfoDTO implements Serializable {
    /**
     * 筛查任务ID
     */
    @NotNull(message = "筛查任务ID不能为空")
    private Integer screeningTaskId;
    /**
     * 筛查机构列表
     */
    @NotEmpty(message = "筛查机构不能为空")
    private List<ScreeningTaskOrg> screeningTaskOrgs;
}
