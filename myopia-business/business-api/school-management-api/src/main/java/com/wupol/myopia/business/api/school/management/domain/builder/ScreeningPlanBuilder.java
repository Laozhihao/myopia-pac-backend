package com.wupol.myopia.business.api.school.management.domain.builder;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.school.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查计划相关构建
 *
 * @author hang.yuan 2022/9/14 17:17
 */
@UtilityClass
public class ScreeningPlanBuilder {

    /**
     * 构建筛查计划
     * @param screeningPlanDTO 筛查计划参数
     * @param currentUser 当前用户
     * @param districtId 区域ID
     */
    public ScreeningPlan buildScreeningPlan(ScreeningPlanDTO screeningPlanDTO, CurrentUser currentUser, Integer districtId) {
        return new ScreeningPlan()
                .setId(screeningPlanDTO.getId())
                .setSrcScreeningNoticeId(CommonConst.DEFAULT_ID)
                .setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setTitle(screeningPlanDTO.getTitle())
                .setContent(screeningPlanDTO.getContent())
                .setStartTime(screeningPlanDTO.getStartTime())
                .setEndTime(screeningPlanDTO.getEndTime())
                .setGovDeptId(CommonConst.DEFAULT_ID)
                .setScreeningOrgId(currentUser.getOrgId())
                .setScreeningOrgType(ScreeningOrgTypeEnum.SCHOOL.getType())
                .setDistrictId(districtId)
                .setReleaseStatus(CommonConst.STATUS_NOT_RELEASE)
                .setCreateUserId(currentUser.getId())
                .setOperatorId(currentUser.getId())
                .setScreeningType(screeningPlanDTO.getScreeningType());
    }


    /**
     * 构建筛查计划学校
     * @param screeningPlanSchoolDb 数据库的筛查计划学校
     * @param school 学校对象
     */
    public ScreeningPlanSchool buildScreeningPlanSchool(ScreeningPlanSchool screeningPlanSchoolDb, School school) {

        if (Objects.nonNull(screeningPlanSchoolDb)){
            screeningPlanSchoolDb.setSchoolName(school.getName());
            return screeningPlanSchoolDb;
        }

        return new ScreeningPlanSchool()
                .setScreeningOrgId(school.getId())
                .setSchoolId(school.getId())
                .setSchoolName(school.getName());
    }

    /**
     * 筛查计划学校学生
     * @param studentList 选中的学生集合
     * @param school 学校
     * @param schoolGradeMap 年级集合
     * @param schoolClassMap 班级集合
     * @param screeningPlanSchoolStudentDbList 数据库的筛查学生集合
     */
    public TwoTuple<List<ScreeningPlanSchoolStudent>,List<Integer>> getScreeningPlanSchoolStudentList(List<Student> studentList, School school, Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, SchoolClass> schoolClassMap, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList) {
        if (CollUtil.isEmpty(screeningPlanSchoolStudentDbList)){
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getScreeningPlanSchoolStudents(studentList, school, schoolGradeMap, schoolClassMap);
            return TwoTuple.of(screeningPlanSchoolStudentList, Lists.newArrayList());
        }else {
            Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentDbList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity()));
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList =Lists.newArrayList();
            List<Integer> addOrUpdateStudentIds=Lists.newArrayList();
            processAddAndUpdate(studentList, school, schoolGradeMap, schoolClassMap, planSchoolStudentMap, screeningPlanSchoolStudentList, addOrUpdateStudentIds);

            //删除
            List<Integer> dbStudentIds = Lists.newArrayList(planSchoolStudentMap.keySet());
            dbStudentIds.removeAll(addOrUpdateStudentIds);
            return TwoTuple.of(screeningPlanSchoolStudentList,dbStudentIds) ;
        }
    }

    /**
     *  处理新增和更新数据
     *
     * @param studentList 学生集合
     * @param school 学校信息
     * @param schoolGradeMap 年级集合
     * @param schoolClassMap 班级集合
     * @param planSchoolStudentMap 筛查计划学校学生集合
     * @param screeningPlanSchoolStudentList 新增和更新筛查计划学校学生集合
     * @param addOrUpdateStudentIds 新增和更新学生ID集合
     */
    private static void processAddAndUpdate(List<Student> studentList, School school, Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, SchoolClass> schoolClassMap, Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList, List<Integer> addOrUpdateStudentIds) {
        if (CollUtil.isEmpty(studentList)){
            return;
        }
        //新增或更新
        studentList.forEach(student -> {
            addOrUpdateStudentIds.add(student.getId());
            SchoolGrade schoolGrade = schoolGradeMap.get(student.getGradeId());
            SchoolClass schoolClass = schoolClassMap.get(student.getClassId());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planSchoolStudentMap.get(student.getId());
            if (Objects.isNull(screeningPlanSchoolStudent)){
                screeningPlanSchoolStudentList.add(buildScreeningPlanSchoolStudent(student, school, schoolGrade, schoolClass));
            }else {
                updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent,school,student,schoolGrade,schoolClass);
                screeningPlanSchoolStudentList.add(screeningPlanSchoolStudent);
            }
        });
    }

    private static List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudents(List<Student> studentList, School school, Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, SchoolClass> schoolClassMap) {
        return studentList.stream().map(student -> {
                    SchoolGrade schoolGrade = schoolGradeMap.get(student.getGradeId());
                    SchoolClass schoolClass = schoolClassMap.get(student.getClassId());
                    return buildScreeningPlanSchoolStudent(student, school, schoolGrade, schoolClass);
                }).collect(Collectors.toList());
    }

    /**
     * 更新筛查计划学校学生
     * @param screeningPlanSchoolStudent 筛查计划学校学生
     * @param school 学校信息
     * @param student 学生信息
     * @param schoolGrade 年级集合
     * @param schoolClass 班级集合
     */
    private void updateScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent,School school, Student student, SchoolGrade schoolGrade, SchoolClass schoolClass) {
        setStudentChangeData(screeningPlanSchoolStudent, school, student);
        if (Objects.nonNull(schoolGrade)){
            screeningPlanSchoolStudent.setGradeName(schoolGrade.getName());
        }
        if (Objects.nonNull(schoolClass)){
            screeningPlanSchoolStudent.setClassName(schoolClass.getName());
        }
    }

    /**
     * 设置学生变动数据
     * @param screeningPlanSchoolStudent 筛查计划学校学生对象
     * @param school 学校对象
     * @param student 学生对象
     */
    private void setStudentChangeData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, School school, Student student) {
        screeningPlanSchoolStudent
                .setGradeId(student.getGradeId())
                .setClassId(student.getClassId())
                .setPlanDistrictId(school.getDistrictId())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolName(school.getName())
                .setGradeType(student.getGradeType())
                .setIdCard(student.getIdCard())
                .setBirthday(student.getBirthday())
                .setGender(student.getGender())
                .setStudentAge(cn.hutool.core.date.DateUtil.ageOfNow(student.getBirthday()))
                .setStudentSituation(SerializationUtil.serializeWithoutException(student))
                .setStudentName(student.getName())
                .setProvinceCode(student.getProvinceCode())
                .setCityCode(student.getCityCode())
                .setAreaCode(student.getAreaCode())
                .setTownCode(student.getTownCode())
                .setAddress(student.getAddress())
                .setParentPhone(student.getParentPhone())
                .setNation(student.getNation())
                .setPassport(student.getPassport());
    }

    /**
     * 构建筛查计划学校学生
     * @param student 学生信息
     * @param school 学校信息
     * @param schoolGrade 年级集合
     * @param schoolClass 班级集合
     */
    private ScreeningPlanSchoolStudent buildScreeningPlanSchoolStudent(Student student,School school,SchoolGrade schoolGrade,SchoolClass schoolClass){
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent()
                .setSrcScreeningNoticeId(CommonConst.DEFAULT_ID)
                .setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningOrgId(school.getId())
                .setSchoolId(school.getId())
                .setStudentId(student.getId())
                .setStudentNo(student.getSno())
                .setArtificial(ArtificialStatusConstant.NON_ARTIFICIAL);
        updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent,school,student,schoolGrade,schoolClass);
        return screeningPlanSchoolStudent;
    }
}
