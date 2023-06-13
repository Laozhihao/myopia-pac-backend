package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common;

import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 各学校表格接口
 *
 * @author Simple4H
 */
public interface ISchoolTable {

    /**
     * 获取表格
     *
     * @param desc           描述
     * @param statConclusion 结论
     * @return Object
     */
    Object getSchoolTable(String desc, String schoolType, List<StatConclusion> statConclusion);


    /**
     * 获取表格
     *
     * @param statConclusions 结论
     * @return List<Object>
     */
    default List<Object> getSchoolTableList(List<StatConclusion> statConclusions, Map<Integer, School> schoolNameMap) {
        Map<Integer, List<StatConclusion>> schoolGroupMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        return schoolGroupMap.entrySet().stream().map(k -> {
            School school = schoolNameMap.getOrDefault(k.getKey(), new School());
            return getSchoolTable(school.getName(), SchoolEnum.getTypeName(school.getType()), k.getValue());
        }).collect(Collectors.toList());
    }
}
