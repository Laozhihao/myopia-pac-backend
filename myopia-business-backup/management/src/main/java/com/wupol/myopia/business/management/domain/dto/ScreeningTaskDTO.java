package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 筛查任务新增/更新的数据结构
 *
 * @author Alix
 */
@Data
public class ScreeningTaskDTO extends ScreeningTask {
    /**
     * 筛查任务中的筛查机构
     */
    List<ScreeningTaskOrg> screeningOrgs;

    /**
     * 行政区明细
     */
    private List<District> districtDetail;

    public static ScreeningTaskDTO build(ScreeningTask screeningTask) {
        ScreeningTaskDTO screeningTaskDTO = new ScreeningTaskDTO();
        if (Objects.nonNull(screeningTask)) {
            BeanUtils.copyProperties(screeningTask, screeningTaskDTO);
        }
        return screeningTaskDTO;
    }
}
