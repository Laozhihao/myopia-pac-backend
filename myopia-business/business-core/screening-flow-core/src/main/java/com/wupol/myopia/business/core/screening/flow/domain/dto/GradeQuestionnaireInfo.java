package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 筛查计划学校里的问卷年级
 * @author xz
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class GradeQuestionnaireInfo {
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

    public static List<GradeQuestionnaireInfo> buildGradeInfo(Integer schoolId, Map<Integer, List<SchoolGradeExportDTO>> finalGradeIdMap, Map<Integer, List<Student>> finalUserGradeIdMap){
        return finalGradeIdMap.get(schoolId).stream().map(grade -> {
            GradeQuestionnaireInfo questionnaireInfo = new GradeQuestionnaireInfo();
            questionnaireInfo.setGradeName(grade.getName());
            questionnaireInfo.setGradeId(grade.getId());
            questionnaireInfo.setStudentCount(CollectionUtils.isEmpty(finalUserGradeIdMap.get(grade.getId())) ? 0 : finalUserGradeIdMap.get(grade.getId()).size());
            return questionnaireInfo;
        }).sorted(Comparator.comparing((GradeQuestionnaireInfo gradeInfo) -> Integer.valueOf(GradeCodeEnum.getByName(gradeInfo.getGradeName()).getCode()))).collect(Collectors.toList());
    }
}
