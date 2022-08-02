package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查计划学校里的问卷年级
 * @author xz
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class GradeQuestionnaireInfo implements Serializable {
    private static final long serialVersionUID = -5197795030214492033L;
    /**
     * 年级id
     */
    private Integer gradeId;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 有数据的学生
     */
    private Integer studentCount;

    public static List<GradeQuestionnaireInfo> buildGradeInfo(Integer schoolId, Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap,
                                                              Map<Integer, List<Student>> userGradeIdMap,Boolean isTotal){
        List<GradeQuestionnaireInfo> collect = gradeIdMap.get(schoolId).stream()
                .map(grade -> {
                    GradeQuestionnaireInfo questionnaireInfo = new GradeQuestionnaireInfo();
                    questionnaireInfo.setGradeName(grade.getName());
                    questionnaireInfo.setGradeId(grade.getId());
                    questionnaireInfo.setStudentCount(Optional.ofNullable(userGradeIdMap.get(grade.getId())).map(List::size).orElse(0));
                    return questionnaireInfo;
                })
                .sorted(Comparator.comparing(gradeInfo -> Integer.valueOf(GradeCodeEnum.getByName(gradeInfo.getGradeName()).getCode())))
                .collect(Collectors.toList());

        if (Objects.equals(Boolean.TRUE,isTotal)){
            int sum = collect.stream().mapToInt(GradeQuestionnaireInfo::getStudentCount).sum();
            collect.add(new GradeQuestionnaireInfo().setGradeName("合计").setStudentCount(sum));
        }
        return collect;
    }
}
