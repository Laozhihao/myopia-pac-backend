package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.*;
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

        if (Objects.isNull(statBase)) {
            return ;
        }
        firstScreenSchoolAgeMap = statBase.getFirstScreen().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        firstScreenSchoolAgeNumMap = new LinkedHashMap();
        for (String schoolAge : firstScreenSchoolAgeMap.keySet()) {
            firstScreenSchoolAgeNumMap.put(schoolAge, firstScreenSchoolAgeMap.get(schoolAge).size());
        }
        validSchoolAgeMap = statBase.getValid().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        validSchoolAgeNumMap = new LinkedHashMap();
        validSchoolAgeDistributionMap = new LinkedHashMap<>();
        for (String schoolAge : validSchoolAgeMap.keySet()) {
            validSchoolAgeNumMap.put(schoolAge, validSchoolAgeMap.get(schoolAge).size());
            validSchoolAgeDistributionMap.put(schoolAge, validSchoolAgeMap.get(schoolAge).stream().map(x -> x.getSchoolId()).distinct().count());
        }

    }

    public Map<String, Long> getSortedDistributionMap() {
        Map sortDistributionMap = new LinkedHashMap<>();
        List<String> sorted = Arrays.asList(SchoolAge.PRIMARY.name(), SchoolAge.JUNIOR.name(), SchoolAge.HIGH.name(), SchoolAge.VOCATIONAL_HIGH.name(), SchoolAge.KINDERGARTEN.name());
        for (String sort : sorted) {
            sortDistributionMap.put(sort, validSchoolAgeDistributionMap.getOrDefault(sort, 0L));
        }
        return sortDistributionMap;
    }

}