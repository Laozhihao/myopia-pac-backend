package com.wupol.myopia.business.aggregation.student.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.exception.BusinessException;
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
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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


    /**
     * 校验学校学生信息
     * @param schoolStudent 学校学生
     */
    public static void validSchoolStudent(SchoolStudent schoolStudent) {
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getSno()),"学号不能为空");
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getName()),"姓名不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGender()),"性别不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGradeId()),"年级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getClassId()),"班级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getBirthday()),"出生日期不能为空");
    }

    /**
     * 检查参数
     * @param schoolStudent
     * @param schoolStudentList
     * @param function
     * @param errorMsg
     */
    public static void checkParam(SchoolStudent schoolStudent, List<SchoolStudent> schoolStudentList, Function<SchoolStudent,String> function, String errorMsg) {
        if (StrUtil.isNotBlank(getValue(schoolStudent,function))){
            List<SchoolStudent> schoolStudents = schoolStudentList.stream().filter(student -> Objects.equals(getValue(student,function),getValue(schoolStudent,function))).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(schoolStudents)){
                throw new BusinessException(errorMsg);
            }
        }
    }


    /**
     * 获取学校学生的参数值
     * @param schoolStudent
     * @param function
     */
    public static String getValue(SchoolStudent schoolStudent,Function<SchoolStudent,String> function){
        return Optional.ofNullable(schoolStudent).map(function).orElse(null);
    }
}
