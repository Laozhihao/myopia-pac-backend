package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/4 17:42
 */
@Data
public class StatBusinessSchoolAgeDTO {

    private Map<String, List<StatConclusion>> firstScreenSchoolAgeMap;
    private Map<String, Integer> firstScreenSchoolAgeNumMap;
    private Map<String, List<StatConclusion>> validSchoolAgeMap;
    private Map<String, Integer> validSchoolAgeNumMap;
    /**
     * 各学段学校数
     */
    private Map<String, Long> validSchoolAgeDistributionMap;

    public StatBusinessSchoolAgeDTO(StatBaseDTO statBase) {

        firstScreenSchoolAgeMap = statBase.getFirstScreen().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        firstScreenSchoolAgeNumMap = new LinkedHashMap();
        for (String schoolAge : firstScreenSchoolAgeMap.keySet()) {
            firstScreenSchoolAgeNumMap.put(schoolAge, firstScreenSchoolAgeMap.get(schoolAge).size());
        }
        validSchoolAgeMap = statBase.getValid().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        validSchoolAgeNumMap = new LinkedHashMap();
        for (String schoolAge : validSchoolAgeMap.keySet()) {
            validSchoolAgeNumMap.put(schoolAge, validSchoolAgeMap.get(schoolAge).size());
            validSchoolAgeDistributionMap.put(schoolAge, validSchoolAgeMap.get(schoolAge).stream().map(x -> x.getSchoolId()).distinct().count());
        }

    }

}
