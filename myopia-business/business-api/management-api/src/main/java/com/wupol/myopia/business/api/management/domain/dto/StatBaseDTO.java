package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/4 18:08
 */
@Data
public class StatBaseDTO {

    private List<StatConclusion> firstScreen;
    private List<StatConclusion> valid;

    public StatBaseDTO(List<StatConclusion> statConclusions) {
        firstScreen = statConclusions.stream().filter(x -> Boolean.FALSE.equals(x.getIsRescreen()))
                        .collect(Collectors.toList());
        valid = firstScreen.stream().filter(x -> Boolean.TRUE.equals(x.getIsValid()))
                .collect(Collectors.toList());
    }

}
