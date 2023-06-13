package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.api.management.util.GroupMapUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 年级表格接口
 *
 * @author Simple4H
 */
public interface IGradeTable {

    /**
     * 获取表格
     *
     * @param desc           描述
     * @param statConclusion 结论
     * @return Object
     */
    Object getGradeTable(String desc, List<StatConclusion> statConclusion);


    /**
     * 获取表格
     *
     * @param statConclusions 结论
     * @return List<Object>
     */
    default List<Object> getGradeTableList(List<StatConclusion> statConclusions) {
        // 先通过学龄段分组
        Map<Integer, List<StatConclusion>> schoolAgeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        List<SchoolAgeCount> schoolAgeCount = GroupMapUtil.getSchoolAgeCount(statConclusions, StatConclusion::getSchoolId, s -> SchoolAge.get(s.getSchoolAge()));

        List<Object> result = new ArrayList<>();
        schoolAgeCount.forEach(s -> {
            List<StatConclusion> schoolAgeStatConclusionList = schoolAgeMap.get(s.getSchoolAge());
            // 再通过班级分组
            List<GradeCodeCount> gradeCodeCount = GroupMapUtil.getGradeCodeCount(schoolAgeStatConclusionList, StatConclusion::getSchoolGradeCode);
            List<Object> gradeResult = gradeCodeCount.stream().map(y -> {
                List<StatConclusion> gradeStatConclusion = schoolAgeStatConclusionList.stream().filter(gradeCode -> StringUtils.equals(gradeCode.getSchoolGradeCode(), y.getCode())).collect(Collectors.toList());
                return getGradeTable(y.getDesc(), gradeStatConclusion);
            }).collect(Collectors.toList());
            result.addAll(gradeResult);
            result.add(getGradeTable(SchoolAge.getDesc(s.getSchoolAge()), schoolAgeStatConclusionList));
        });
        result.add(getGradeTable(CommonConst.TOTAL, statConclusions));
        return result;
    }
}
