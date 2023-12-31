package com.wupol.myopia.business.core.screening.flow.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查业务
 *
 * @author hang.yuan 2022/9/27 00:25
 */
@UtilityClass
public class ScreeningBizBuilder {

    /**
     * 获取筛查年级ID集合
     * @param screeningGradeIds 筛查年级ID集合
     */
    public List<Integer> getScreeningGradeIds(String screeningGradeIds){
        if (StrUtil.isBlank(screeningGradeIds)){
            return Lists.newArrayList();
        }
        return Arrays.stream(screeningGradeIds.split(StrUtil.COMMA))
                .map(Integer::valueOf).distinct().collect(Collectors.toList());
    }

    /**
     * 筛查计划学校学生
     *
     * @param screeningPlanId                  计划Id
     * @param schoolStudentList                选中的学生集合
     * @param school                           学校
     * @param schoolGradeMap                   年级集合
     * @param screeningPlanSchoolStudentDbList 数据库的筛查学生集合
     */
    public List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudentList(Integer screeningPlanId, List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap,
                                                                                                      List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList) {
        if (CollUtil.isEmpty(screeningPlanSchoolStudentDbList)){
            return getScreeningPlanSchoolStudents(screeningPlanId, schoolStudentList, school, schoolGradeMap);
        }else {
            Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentDbList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity()));
            return processAddAndUpdate(screeningPlanId, schoolStudentList, school, schoolGradeMap, planSchoolStudentMap);
        }
    }

    /**
     * 处理新增和更新数据
     *
     * @param screeningPlanId                计划Id
     * @param schoolStudentList              学生集合
     * @param school                         学校信息
     * @param schoolGradeMap                 年级集合
     * @param planSchoolStudentMap           筛查计划学校学生集合
     */
    public List<ScreeningPlanSchoolStudent> processAddAndUpdate(Integer screeningPlanId,List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap,
                                     Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap) {
        if (CollUtil.isEmpty(schoolStudentList)){
            return Lists.newArrayList();
        }
        //新增或更新
        return schoolStudentList.stream().map(schoolStudent -> {
            SchoolGrade schoolGrade = schoolGradeMap.get(schoolStudent.getGradeId());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planSchoolStudentMap.get(schoolStudent.getStudentId());
            if (Objects.isNull(screeningPlanSchoolStudent)) {
                return buildScreeningPlanSchoolStudent(screeningPlanId, schoolStudent, school, schoolGrade);
            }
            updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent, school, schoolStudent, schoolGrade);
            return screeningPlanSchoolStudent;
        }).collect(Collectors.toList());
    }

    /**
     * 获取筛查计划学生
     *
     * @param screeningPlanId   计划Id
     * @param schoolStudentList 学校学生信息
     * @param school            学校信息
     * @param schoolGradeMap    年级信息
     */
    private List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudents(Integer screeningPlanId, List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap) {
        return schoolStudentList.stream().map(schoolStudent -> {
            SchoolGrade schoolGrade = schoolGradeMap.get(schoolStudent.getGradeId());
            return buildScreeningPlanSchoolStudent(screeningPlanId, schoolStudent, school, schoolGrade);
        }).collect(Collectors.toList());
    }

    /**
     * 构建筛查计划学校学生
     *
     * @param screeningPlanId 计划Id
     * @param schoolStudent   学生信息
     * @param school          学校信息
     * @param schoolGrade     年级集合
     */
    public ScreeningPlanSchoolStudent buildScreeningPlanSchoolStudent(Integer screeningPlanId,SchoolStudent schoolStudent,School school,SchoolGrade schoolGrade){
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent()
                .setSrcScreeningNoticeId(CommonConst.DEFAULT_ID)
                .setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningPlanId(screeningPlanId)
                .setScreeningOrgId(school.getId())
                .setSchoolId(school.getId())
                .setStudentId(schoolStudent.getStudentId())
                .setArtificial(ArtificialStatusConstant.NON_ARTIFICIAL);
        updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent,school,schoolStudent,schoolGrade);
        return screeningPlanSchoolStudent;
    }

    /**
     * 更新筛查计划学校学生
     *
     * @param screeningPlanSchoolStudent 筛查计划学校学生
     * @param school                     学校信息
     * @param schoolStudent              学生信息
     * @param schoolGrade                年级集合
     */
    public void updateScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent,School school, SchoolStudent schoolStudent, SchoolGrade schoolGrade) {
        setStudentChangeData(screeningPlanSchoolStudent, school, schoolStudent);
        if (Objects.isNull(screeningPlanSchoolStudent.getScreeningCode())){
            screeningPlanSchoolStudent.setScreeningCode(ScreeningCodeGenerator.nextId());
        }
        if (Objects.nonNull(schoolGrade)){
            screeningPlanSchoolStudent.setGradeType(GradeCodeEnum.getByCode(schoolGrade.getGradeCode()).getType());
        }
    }

    /**
     * 设置学生变动数据
     * @param screeningPlanSchoolStudent 筛查计划学校学生对象
     * @param school 学校对象
     * @param schoolStudent 学生对象
     */
    private void setStudentChangeData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, School school, SchoolStudent schoolStudent) {
        screeningPlanSchoolStudent
                .setStudentNo(schoolStudent.getSno())
                .setGradeId(schoolStudent.getGradeId())
                .setClassId(schoolStudent.getClassId())
                .setPlanDistrictId(school.getDistrictId())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolName(school.getName())
                .setGradeType(schoolStudent.getGradeType())
                .setIdCard(schoolStudent.getIdCard())
                .setBirthday(schoolStudent.getBirthday())
                .setGender(schoolStudent.getGender())
                .setStudentAge(DateUtil.ageOfNow(schoolStudent.getBirthday()))
                .setStudentSituation(SerializationUtil.serializeWithoutException(schoolStudent))
                .setStudentName(schoolStudent.getName())
                .setProvinceCode(schoolStudent.getProvinceCode())
                .setCityCode(schoolStudent.getCityCode())
                .setAreaCode(schoolStudent.getAreaCode())
                .setTownCode(schoolStudent.getTownCode())
                .setAddress(schoolStudent.getAddress())
                .setParentPhone(schoolStudent.getParentPhone())
                .setNation(schoolStudent.getNation())
                .setPassport(schoolStudent.getPassport());
    }

}
