package com.wupol.myopia.business.core.school.facade;

import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校门面类型
 *
 * @author hang.yuan 2022/8/30 09:57
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SchoolFacade {

    private final SchoolService schoolService;
    private final SchoolClassService schoolClassService;
    private final SchoolGradeService schoolGradeService;

    /**
     * 获取班级信息，并带有学校和年级名称
     * @param classIds 班级ID集合
     */
    public List<SchoolClassDTO> getClassWithSchoolAndGradeName(List<Integer> classIds){
        //班级
        List<SchoolClass> schoolClassList = schoolClassService.listByIds(classIds);

        //年级
        Set<Integer> gradeIds = schoolClassList.stream().map(SchoolClass::getGradeId).collect(Collectors.toSet());
        List<SchoolGrade> schoolGradeList = schoolGradeService.listByIds(gradeIds);
        Map<Integer, SchoolGrade> schoolGradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity(), (v1, v2) -> v2));

        //学校
        Set<Integer> schoolIds = schoolGradeList.stream().map(SchoolGrade::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.listByIds(schoolIds);
        Map<Integer, School> schoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity(), (v1, v2) -> v2));

        return schoolClassList.stream().map(schoolClass -> buildSchoolClassDTO(schoolGradeMap, schoolMap, schoolClass)).collect(Collectors.toList());
    }

    /**
     * 构建班级信息
     * @param schoolGradeMap 年级集合
     * @param schoolMap 学校集合
     * @param schoolClass 班级对象
     */
    private SchoolClassDTO buildSchoolClassDTO(Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, School> schoolMap, SchoolClass schoolClass) {

        SchoolGrade schoolGrade = schoolGradeMap.get(schoolClass.getGradeId());
        School school = schoolMap.get(schoolGrade.getSchoolId());
        SchoolClassDTO schoolClassDTO = new SchoolClassDTO()
                .setGradeName(schoolGrade.getName())
                .setSchoolName(school.getName())
                .setSchoolDistrictDetail(school.getDistrictDetail())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolAreaType(school.getAreaType())
                .setSchoolMonitorType(school.getMonitorType());

        schoolClassDTO.setId(schoolClass.getId())
                .setCreateUserId(schoolClass.getCreateUserId())
                .setName(schoolClass.getName())
                .setSeatCount(schoolClass.getSeatCount())
                .setStatus(schoolClass.getStatus())
                .setSchoolId(school.getId())
                .setGradeId(schoolGrade.getId())
                .setCreateTime(schoolClass.getCreateTime())
                .setUpdateTime(schoolClass.getUpdateTime());

        return schoolClassDTO;
    }

}
