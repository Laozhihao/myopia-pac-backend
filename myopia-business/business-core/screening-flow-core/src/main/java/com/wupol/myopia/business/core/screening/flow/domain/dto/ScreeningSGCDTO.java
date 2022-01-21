package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/20/17:54
 * @Description:
 */
@Data
public class ScreeningSGCDTO extends ScreeningPlanSchool {
    private Integer id;

    private String name;



}
