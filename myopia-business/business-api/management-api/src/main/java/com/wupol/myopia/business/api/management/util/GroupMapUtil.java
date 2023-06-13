package com.wupol.myopia.business.api.management.util;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.GradeCodeCount;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.SchoolAgeCount;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分组
 *
 * @author Simple4H
 */
@UtilityClass
public class GroupMapUtil {

    /**
     * 获取年级信息
     *
     * @param statConclusions 筛查数据
     * @return List<GradeCodeCount>
     */
    public <T> List<GradeCodeCount> getGradeCodeCount(List<T> statConclusions, Function<T, String> gradeCodeFunction) {
        return statConclusions.stream().map(gradeCodeFunction).distinct().map(s -> {
            GradeCodeCount gradeCodeCount = new GradeCodeCount();
            gradeCodeCount.setDesc(GradeCodeEnum.getDesc(s));
            gradeCodeCount.setSort(GradeCodeEnum.getSort(s));
            gradeCodeCount.setCode(s);
            return gradeCodeCount;
        }).sorted(Comparator.comparing(GradeCodeCount::getSort)).collect(Collectors.toList());
    }


    /**
     * 获取学龄统计信息
     *
     * @return List<SchoolAgeCount>
     */
    public <T> List<SchoolAgeCount> getSchoolAgeCount(List<T> t,
                                                      Function<T, Integer> schoolIdFunction,
                                                      Function<T, SchoolAge> schoolAgeFunction) {
        List<SchoolAgeCount> result = new ArrayList<>();
        HashMap<SchoolAge, Long> count = new HashMap<>();
        Map<Integer, List<T>> schoolGroupMap = t.stream().collect(Collectors.groupingBy(schoolIdFunction));
        schoolGroupMap.forEach((k, v) -> {
            Map<SchoolAge, List<T>> schoolAgeMap = v.stream().collect(Collectors.groupingBy(schoolAgeFunction));
            schoolAgeMap.forEach((x, y) -> {
                count.put(x, count.getOrDefault(x, 0L) + 1L);
            });
        });
        count.forEach((k, v) -> {
            SchoolAgeCount schoolAgeCount = new SchoolAgeCount();
            schoolAgeCount.setDesc(k.getDesc());
            schoolAgeCount.setCount(v);
            schoolAgeCount.setSort(k.getSort());
            schoolAgeCount.setSchoolAge(k.getCode());
            result.add(schoolAgeCount);
        });
        return result.stream().sorted(Comparator.comparing(SchoolAgeCount::getSort)).collect(Collectors.toList());
    }

}
