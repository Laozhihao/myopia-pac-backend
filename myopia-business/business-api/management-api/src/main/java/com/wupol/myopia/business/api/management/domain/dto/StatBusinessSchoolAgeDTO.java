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
        this(statBase, null);
    }

    public StatBusinessSchoolAgeDTO(StatBaseDTO statBase, Boolean isAllAge) {
        if (Objects.isNull(statBase)) {
            return;
        }
        firstScreenSchoolAgeMap = statBase.getFirstScreen().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        firstScreenSchoolAgeNumMap = new LinkedHashMap<>();

        validSchoolAgeMap = statBase.getValid().stream().collect(Collectors.groupingBy(x -> SchoolAge.get(x.getSchoolAge()).name()));
        validSchoolAgeNumMap = new LinkedHashMap<>();
        validSchoolAgeDistributionMap = new LinkedHashMap<>();

        if (Boolean.TRUE.equals(isAllAge)) {
            for (SchoolAge schoolAge : SchoolAge.values()) {
                String schoolAgeName = schoolAge.name();
                List<StatConclusion> statConclusionList = firstScreenSchoolAgeMap.get(schoolAgeName);
                firstScreenSchoolAgeNumMap.put(schoolAgeName, statConclusionList == null ? 0 : statConclusionList.size());
            }
            for (SchoolAge schoolAge : SchoolAge.values()) {
                String schoolAgeName = schoolAge.name();
                List<StatConclusion> statConclusionList = validSchoolAgeMap.get(schoolAgeName);
                validSchoolAgeNumMap.put(schoolAgeName, statConclusionList == null ? 0 : statConclusionList.size());
                validSchoolAgeDistributionMap.put(schoolAgeName, statConclusionList == null ? 0 : statConclusionList.stream().map(StatConclusion::getSchoolId).distinct().count());
            }
        } else {
            for (Map.Entry<String, List<StatConclusion>> entry : firstScreenSchoolAgeMap.entrySet()) {
                firstScreenSchoolAgeNumMap.put(entry.getKey(), entry.getValue().size());
            }
            for (Map.Entry<String, List<StatConclusion>> entry : firstScreenSchoolAgeMap.entrySet()) {
                validSchoolAgeNumMap.put(entry.getKey(), entry.getValue().size());
                validSchoolAgeDistributionMap.put(entry.getKey(), entry.getValue().stream()
                        .map(StatConclusion::getSchoolId).distinct().count());
            }
        }
    }

    public Map<String, Long> getSortedDistributionMap() {
        Map<String, Long> sortDistributionMap = new LinkedHashMap<>();
        List<String> sorted = Arrays.asList(SchoolAge.PRIMARY.name(), SchoolAge.JUNIOR.name(), SchoolAge.HIGH.name(), SchoolAge.VOCATIONAL_HIGH.name(), SchoolAge.KINDERGARTEN.name());
        for (String sort : sorted) {
            sortDistributionMap.put(sort, validSchoolAgeDistributionMap.getOrDefault(sort, 0L));
        }
        return sortDistributionMap;
    }

}