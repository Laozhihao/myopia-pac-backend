package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.student.constant.RefractionSituationEnum;
import com.wupol.myopia.business.aggregation.student.constant.VisionSituationEnum;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.vo.SchoolStudentQuerySelectVO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.util.SchoolUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校学生
 *
 * @author hang.yuan 2022/10/12 09:57
 */
@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SchoolStudentFacade {

    private final SchoolGradeService schoolGradeService;
    private final SchoolClassService schoolClassService;
    private final SchoolStudentService schoolStudentService;

    /**
     * 获取学校学生查询条件下拉框值
     * @param schoolId
     */
    public SchoolStudentQuerySelectVO getSelectValue(Integer schoolId) {

        TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove = kindergartenAndPrimaryAbove(schoolId);

        SchoolStudentQuerySelectVO schoolStudentQuerySelectVO = new SchoolStudentQuerySelectVO(Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList());

        //戴镜类型
        ImmutableMap<Integer, String> typeDescriptionMap = WearingGlassesSituation.getTypeDescriptionMap();
        List<SchoolStudentQuerySelectVO.SelectValue> glassesTypeList = typeDescriptionMap.entrySet().stream().map(entry -> new SchoolStudentQuerySelectVO.SelectValue(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.setGlassesTypeList(glassesTypeList);

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.FALSE)) {
            return schoolStudentQuerySelectVO;
        }
        //年份
        yearList(schoolStudentQuerySelectVO,schoolId);

        //视力类型
        visionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);

        //屈光类型
        refractionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);

        return schoolStudentQuerySelectVO;
    }

    /**
     * 年份
     * @param schoolStudentQuerySelectVO
     */
    private void yearList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO,Integer schoolId) {
        List<SchoolStudent> schoolStudentList = schoolStudentService.listBySchoolId(schoolId);
        if (CollUtil.isEmpty(schoolStudentList)){
            return;
        }
        List<SchoolStudentQuerySelectVO.SelectValue> selectValueList = schoolStudentList.stream()
                .map(SchoolStudent::getParticularYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Integer::intValue))
                .map(year-> new SchoolStudentQuerySelectVO.SelectValue(year,year.toString()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(selectValueList)){
            return;
        }
        schoolStudentQuerySelectVO.getYearList().addAll(selectValueList);
    }

    /**
     * 屈光类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void refractionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = getCondition(kindergartenAndPrimaryAbove);
        List<RefractionSituationEnum> refractionSituationEnumList = RefractionSituationEnum.listByCondition(condition);
        List<SchoolStudentQuerySelectVO.SelectValue> selectValueList = refractionSituationEnumList.stream().map(situationEnum -> new SchoolStudentQuerySelectVO.SelectValue(situationEnum.getCode(), situationEnum.getDesc())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.getRefractionTypeList().addAll(selectValueList);
    }


    /**
     * 视力类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void visionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = getCondition(kindergartenAndPrimaryAbove);
        List<VisionSituationEnum> visionSituationEnumList = VisionSituationEnum.listByCondition(condition);
        List<SchoolStudentQuerySelectVO.SelectValue> selectValueList = visionSituationEnumList.stream().map(visionSituationEnum -> new SchoolStudentQuerySelectVO.SelectValue(visionSituationEnum.getCode(), visionSituationEnum.getDesc())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.getVisionTypeList().addAll(selectValueList);
    }

    /**
     * 获取条件值
     * @param kindergartenAndPrimaryAbove
     */
    private Boolean getCondition(TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = null;

        //幼儿园
        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.FALSE)) {
            condition = Boolean.TRUE;
        }
        //小学及以上
        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.TRUE)) {
            condition = Boolean.FALSE;
        }
        return condition;
    }

    /**
     * 判断学校的学龄段（幼儿园/小学及以上）
     * @param schoolId
     */
    private TwoTuple<Boolean,Boolean> kindergartenAndPrimaryAbove(Integer schoolId) {
        List<SchoolGrade> schoolGradeList = schoolGradeService.getBySchoolId(schoolId);
        if (CollUtil.isEmpty(schoolGradeList)){
            return TwoTuple.of(Boolean.FALSE,Boolean.FALSE);
        }
        List<String> kindergartenSchoolCode = GradeCodeEnum.kindergartenSchoolCode();
        List<String> primaryAbove = GradeCodeEnum.primaryAbove();
        List<SchoolGrade> kindergartenList = schoolGradeList.stream().filter(schoolGrade -> kindergartenSchoolCode.contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        List<SchoolGrade> primaryAboveList = schoolGradeList.stream().filter(schoolGrade -> primaryAbove.contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        return TwoTuple.of(CollUtil.isNotEmpty(kindergartenList),CollUtil.isNotEmpty(primaryAboveList));
    }

    /**
     * 检查和设置学校学生信息
     *
     * @param schoolStudent 学生
     * @param schoolId      学校Id
     * @return SchoolStudent
     */
    public SchoolStudent validSchoolStudent(SchoolStudent schoolStudent, Integer schoolId) {
        SchoolStudentInfoBuilder.validSchoolStudent(schoolStudent);
        setSchoolStudentInfo(schoolStudent, schoolId);
        return schoolStudent;
    }

    /**
     * 设置学生信息
     *
     * @param schoolStudent 学生
     * @param schoolId      学校Id
     */
    public void setSchoolStudentInfo(SchoolStudent schoolStudent, Integer schoolId) {
        schoolStudent.checkStudentInfo();
        checkSnoAndIdCardAndPassport(schoolStudent, schoolId);
        schoolStudent.setSchoolId(schoolId);
        SchoolGrade grade = schoolGradeService.getById(schoolStudent.getGradeId());
        schoolStudent.setGradeName(grade.getName());
        schoolStudent.setClassName(schoolClassService.getById(schoolStudent.getClassId()).getName());
        schoolStudent.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        schoolStudent.setParticularYear(SchoolUtil.getParticularYear(grade.getGradeCode()));

        SchoolStudent havaDeletedStudent = schoolStudentService.getByIdCardAndPassport(schoolStudent.getIdCard(), schoolStudent.getPassport(), schoolId);
        if (Objects.nonNull(havaDeletedStudent)) {
            schoolStudent.setId(havaDeletedStudent.getId());
            schoolStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        }
    }

    /**
     * 检查学号、身份证、护照是否重复
     * @param schoolStudent
     * @param schoolId
     */
    private void checkSnoAndIdCardAndPassport(SchoolStudent schoolStudent, Integer schoolId) {
        List<SchoolStudent> schoolStudentList = schoolStudentService.listByIdCardAndSnoAndPassport(schoolStudent.getId(), schoolStudent.getIdCard(), schoolStudent.getSno(), schoolStudent.getPassport(), schoolId);
        if (CollUtil.isNotEmpty(schoolStudentList)){
            SchoolStudentInfoBuilder.checkParam(schoolStudent, schoolStudentList,SchoolStudent::getSno,"学号重复");
            SchoolStudentInfoBuilder.checkParam(schoolStudent, schoolStudentList,SchoolStudent::getIdCard,"身份证重复");
            SchoolStudentInfoBuilder.checkParam(schoolStudent, schoolStudentList,SchoolStudent::getPassport,"护照重复");
        }
    }



}
