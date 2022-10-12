package com.wupol.myopia.business.aggregation.student.domain.builder;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.business.common.utils.constant.AstigmatismLevelEnum;
import com.wupol.myopia.business.common.utils.constant.HyperopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentListVO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 学校学生信息
 *
 * @author hang.yuan 2022/9/29 13:05
 */
@UtilityClass
public class SchoolStudentInfoBuilder {

    /**
     * 设置学生信息
     *
     * @param schoolMap
     * @param student
     */
    public void setStudentInfo(Map<Integer, String> schoolMap, StudentDTO student) {
        student.setSchoolName(schoolMap.getOrDefault(student.getSchoolId(), StrUtil.EMPTY));
        student.setIsMyopia(!Objects.equals(student.getMyopiaLevel(), MyopiaLevelEnum.ZERO.getCode()));
        student.setIsAstigmatism(!Objects.equals(student.getAstigmatismLevel(), AstigmatismLevelEnum.ZERO.getCode()));
        student.setIsHyperopia(!Objects.equals(student.getHyperopiaLevel(), HyperopiaLevelEnum.ZERO.getCode()));
    }

    /**
     * 设置学生信息
     *
     * @param countMap
     * @param visitMap
     * @param studentPlanMap
     * @param student
     */
    public void setStudentInfo(Map<Integer, Integer> countMap, Map<Integer, List<ReportAndRecordDO>> visitMap,
                               Map<Integer, List<ScreeningPlanSchoolStudent>> studentPlanMap,
                               StudentDTO student) {
        // 筛查次数
        student.setScreeningCount(countMap.getOrDefault(student.getId(), 0));
        // 筛查码
        student.setScreeningCodes(StudentBizBuilder.getScreeningCodesByPlan(studentPlanMap.get(student.getId())));
        // 就诊次数
        student.setNumOfVisits(Objects.nonNull(visitMap.get(student.getId())) ? visitMap.get(student.getId()).size() : 0);
        // 问卷次数
        student.setQuestionnaireCount(0);
    }

    /**
     * 构建学生信息
     *
     * @param schoolStudent
     */
    public SchoolStudentListVO buildStudentDTO(SchoolStudent schoolStudent){
        SchoolStudentListVO student = new SchoolStudentListVO()
                .setGradeName(schoolStudent.getGradeName())
                .setClassName(schoolStudent.getClassName());

        student.setId(schoolStudent.getId())
                .setStudentId(schoolStudent.getStudentId())
                .setSno(schoolStudent.getSno())
                .setName(schoolStudent.getName())
                .setVisionLabel(schoolStudent.getVisionLabel());
        return student;
    }

    /**
     * 构建学校学生查询条件对象
     *
     * @param studentQueryDTO
     */
    public SchoolStudentQueryBO builderSchoolStudentQueryBO(SchoolStudentQueryDTO studentQueryDTO) {
        return new SchoolStudentQueryBO()
                .setName(studentQueryDTO.getName())
                .setSno(studentQueryDTO.getSno())
                .setGradeId(studentQueryDTO.getGradeId())
                .setClassId(studentQueryDTO.getClassId())
                .setSchoolId(studentQueryDTO.getSchoolId())
                .setVisionLabels(studentQueryDTO.getVisionLabels());
    }

}
