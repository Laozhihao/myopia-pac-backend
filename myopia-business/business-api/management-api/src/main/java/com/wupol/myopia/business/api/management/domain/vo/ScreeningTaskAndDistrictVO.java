package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/4/25 15:15
 */
@Data
public class ScreeningTaskAndDistrictVO extends ScreeningTaskDTO {

    /**
     * 行政区明细
     */
    private List<District> districtDetail;
    public static ScreeningTaskAndDistrictVO build(ScreeningTask screeningTask) {

        ScreeningTaskAndDistrictVO screeningTaskAndDistrictVO = new ScreeningTaskAndDistrictVO();
        if (Objects.nonNull(screeningTask)) {
            BeanUtils.copyProperties(screeningTask, screeningTaskAndDistrictVO);
        }
        return screeningTaskAndDistrictVO;
    }

}
