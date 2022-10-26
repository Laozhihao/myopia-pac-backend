package com.wupol.myopia.business.aggregation.export.excel.imports;

import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 上传Excel 通用方法
 *
 * @author Simple4H
 */
@Service
public class CommonImportService {

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private SchoolGradeService schoolGradeService;

    /**
     * 插入学校端学生
     *
     * @param importList   多端学生列表
     * @param sourceClient 来源客户端
     */
    public void insertSchoolStudent(List<Student> importList, Integer sourceClient) {
        // 获取学号重复的
        List<String> allSnoList = importList.stream().map(Student::getSno).collect(Collectors.toList());
        List<String> duplicateSnoList = ListUtil.getDuplicateElements(allSnoList);

        // 过滤掉学号为空的和学号重复的，班级、年级为空的
        List<Student> studentList = importList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getSno()))
                .filter(s -> !duplicateSnoList.contains(s.getSno()))
                .filter(s -> Objects.nonNull(s.getGradeId()))
                .filter(s -> Objects.nonNull(s.getClassId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(studentList)) {
            return;
        }

        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(studentList.stream().map(Student::getClassId).collect(Collectors.toList()));
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(studentList.stream().map(Student::getGradeId).collect(Collectors.toList()));

        List<SchoolStudent> addSchoolStudentList = new ArrayList<>();

        // 通过学校分组
        Map<Integer, List<Student>> studentMap = studentList.stream().collect(Collectors.groupingBy(Student::getSchoolId));
        for (Map.Entry<Integer, List<Student>> entry : studentMap.entrySet()) {
            Integer schoolId = entry.getKey();
            List<Student> students = entry.getValue();

            List<String> idCardList = students.stream().map(Student::getIdCard).collect(Collectors.toList());
            List<String> snoList = students.stream().map(Student::getSno).collect(Collectors.toList());
            List<String> passportList = students.stream().map(Student::getPassport).collect(Collectors.toList());
            List<SchoolStudent> schoolStudentList = schoolStudentService.getAllByIdCardAndSnoAndPassports(idCardList, snoList, passportList, schoolId);


            // 过滤学号、身份证、护照已经存在的
            List<Student> needAddList = students.stream()
                    .filter(student -> schoolStudentList.stream().noneMatch(schoolStudent -> schoolStudent.getSno().equals(student.getSno())))
                    .filter(student -> schoolStudentList.stream().noneMatch(schoolStudent -> (Objects.nonNull(schoolStudent.getIdCard()) && schoolStudent.getIdCard().equals(student.getIdCard()))))
                    .filter(student -> schoolStudentList.stream().noneMatch(schoolStudent -> (Objects.nonNull(schoolStudent.getPassport()) && schoolStudent.getPassport().equals(student.getPassport()))))
                    .collect(Collectors.toList());

            needAddList.forEach(s -> {
                SchoolStudent schoolStudent = new SchoolStudent();
                BeanUtils.copyProperties(s, schoolStudent);
                schoolStudent.setId(null);
                schoolStudent.setStudentId(s.getId());
                schoolStudent.setGradeName(gradeMap.get(s.getGradeId()).getName());
                schoolStudent.setClassName(classMap.get(s.getClassId()).getName());
                schoolStudent.setSourceClient(sourceClient);

                schoolStudent.setGlassesType(null);
                schoolStudent.setLastScreeningTime(null);
                schoolStudent.setVisionLabel(null);
                schoolStudent.setLowVision(null);
                schoolStudent.setMyopiaLevel(null);
                schoolStudent.setScreeningMyopia(null);
                schoolStudent.setHyperopiaLevel(null);
                schoolStudent.setAstigmatismLevel(null);
                schoolStudent.setIsAnisometropia(null);
                schoolStudent.setIsRefractiveError(null);
                schoolStudent.setVisionCorrection(null);
                schoolStudent.setIsMyopia(null);
                schoolStudent.setIsHyperopia(null);
                schoolStudent.setIsAstigmatism(null);
                addSchoolStudentList.add(schoolStudent);
            });
        }
        schoolStudentService.saveBatch(addSchoolStudentList);
    }
}
