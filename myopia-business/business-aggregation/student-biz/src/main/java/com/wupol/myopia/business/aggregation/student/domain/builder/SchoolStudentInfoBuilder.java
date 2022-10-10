package com.wupol.myopia.business.aggregation.student.domain.builder;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
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
    public StudentDTO buildStudentDTO(SchoolStudent schoolStudent){
        StudentDTO student = new StudentDTO()
                .setGradeName(schoolStudent.getGradeName())
                .setClassName(schoolStudent.getClassName())
                .setNationDesc(NationEnum.getName(schoolStudent.getNation()))
                .setGenderDesc(GenderEnum.getName(schoolStudent.getGender()));

        student.setId(schoolStudent.getStudentId())
                .setSno(schoolStudent.getSno())
                .setGradeId(schoolStudent.getGradeId())
                .setGradeType(schoolStudent.getGradeType())
                .setClassId(schoolStudent.getClassId())
                .setName(schoolStudent.getName())
                .setGender(schoolStudent.getGender())
                .setBirthday(schoolStudent.getBirthday())
                .setNation(schoolStudent.getNation())
                .setIdCard(schoolStudent.getIdCard())
                .setParentPhone(schoolStudent.getParentPhone())
                .setMpParentPhone(schoolStudent.getMpParentPhone())
                .setAddress(schoolStudent.getAddress())
                .setVisionLabel(schoolStudent.getVisionLabel())
                .setLastScreeningTime(schoolStudent.getLastScreeningTime())
                .setStatus(schoolStudent.getStatus())
                .setGlassesType(schoolStudent.getGlassesType())
                .setCreateTime(schoolStudent.getCreateTime())
                .setUpdateTime(schoolStudent.getUpdateTime())
                .setSchoolId(schoolStudent.getSchoolId())
                .setLowVision(schoolStudent.getLowVision())
                .setMyopiaLevel(schoolStudent.getMyopiaLevel())
                .setScreeningMyopia(schoolStudent.getScreeningMyopia())
                .setHyperopiaLevel(schoolStudent.getHyperopiaLevel())
                .setAstigmatismLevel(schoolStudent.getAstigmatismLevel())
                .setPassport(schoolStudent.getPassport())
                .setSourceClient(schoolStudent.getSourceClient());
        return student;
    }

    /**
     * 构建学校学生查询条件对象
     *
     * @param studentQueryDTO
     */
    public SchoolStudentQueryBO builderSchoolStudentQueryBO(StudentQueryDTO studentQueryDTO) {
        return new SchoolStudentQueryBO()
                .setName(studentQueryDTO.getName())
                .setSno(studentQueryDTO.getSno())
                .setIdCard(studentQueryDTO.getIdCardOrPassportLike())
                .setPassport(studentQueryDTO.getIdCardOrPassportLike())
                .setParentPhone(studentQueryDTO.getParentPhone())
                .setGradeId(studentQueryDTO.getGradeId())
                .setClassId(studentQueryDTO.getClassId())
                .setSchoolIds(Lists.newArrayList(studentQueryDTO.getSchoolId()))
                .setVisionLabels(studentQueryDTO.getVisionLabels());
    }

}
