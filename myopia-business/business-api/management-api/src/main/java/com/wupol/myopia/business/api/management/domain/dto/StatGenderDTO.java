package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/4 17:59
 */
@Data
public class StatGenderDTO {

    private List<StatConclusion> male;
    private List<StatConclusion> female;

    public StatGenderDTO(List<StatConclusion> validConclusions) {
        male =validConclusions.stream().filter(x -> GenderEnum.MALE.type.equals(x.getGender()))
                .collect(Collectors.toList());
        female = validConclusions.stream().filter(x -> GenderEnum.FEMALE.type.equals(x.getGender()))
                .collect(Collectors.toList());
    }

}
