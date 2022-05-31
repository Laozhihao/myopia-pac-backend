package com.wupol.myopia.business.core.school.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.mapper.StudentCommonDiseaseIdMapper;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.StudentCommonDiseaseId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2022-05-27
 */
@Service
public class StudentCommonDiseaseIdService extends BaseService<StudentCommonDiseaseIdMapper, StudentCommonDiseaseId> {

    @Autowired
    private SchoolCommonDiseaseCodeService schoolCommonDiseaseCodeService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 获取学生常见病ID
     *
     * @param districtId    行政区域ID
     * @param schoolId      学校ID
     * @param gradeId       年级ID
     * @param studentId     学校ID
     * @param year          年份
     * @return com.wupol.myopia.business.core.school.domain.model.StudentCommonDiseaseId
     **/
    public StudentCommonDiseaseId getStudentCommonDiseaseId(Integer districtId, Integer schoolId, Integer gradeId, Integer studentId, int year) {
        // 获取已存在的
        StudentCommonDiseaseId studentCommonDiseaseId = findOne(new StudentCommonDiseaseId().setStudentId(studentId).setGradeId(gradeId).setYear(year));
        if (Objects.nonNull(studentCommonDiseaseId)) {
            return studentCommonDiseaseId;
        }
        // 还不存在，则生成
        return createStudentCommonDiseaseId(districtId, schoolId, gradeId, studentId, year);
    }

    /**
     * 创建学生常见病ID
     *
     * @param districtId    行政区域ID
     * @param schoolId      学校ID
     * @param gradeId       年级ID
     * @param studentId     学生ID
     * @param year          年份
     * @return com.wupol.myopia.business.core.school.domain.model.StudentCommonDiseaseId
     **/
    public StudentCommonDiseaseId createStudentCommonDiseaseId(Integer districtId, Integer schoolId, Integer gradeId, Integer studentId, int year) {
        School school = schoolService.getById(schoolId);
        SchoolGrade grade = schoolGradeService.getById(gradeId);
        District district = districtService.getById(districtId);
        String districtCode = String.valueOf(district.getCode());
        String schoolCommonDiseaseCode = schoolCommonDiseaseCodeService.getSchoolCommonDiseaseCode(districtId, schoolId, year);
        String studentCommonDiseaseCode = getStudentCommonDiseaseCode(gradeId, year);
        String commonDiseaseId = new StringBuilder()
                .append(districtCode, 0, 4)
                .append(school.getAreaType())
                .append(districtCode, 4, 6)
                .append(school.getMonitorType())
                .append(schoolCommonDiseaseCode)
                .append(grade.getGradeCode())
                .append(studentCommonDiseaseCode).toString();
        StudentCommonDiseaseId studentCommonDiseaseId = new StudentCommonDiseaseId().setStudentId(studentId).setGradeId(gradeId).setYear(year).setCommonDiseaseCode(studentCommonDiseaseCode).setCommonDiseaseId(commonDiseaseId);
        save(studentCommonDiseaseId);
        return studentCommonDiseaseId;
    }

    /**
     * 获取学生常见病编码
     *
     * @param gradeId   年级ID
     * @param year      年份
     * @return java.lang.String
     **/
    private String getStudentCommonDiseaseCode(Integer gradeId, int year) {
        int total = count(new StudentCommonDiseaseId().setGradeId(gradeId).setYear(year));
        Assert.isTrue(total < 9999, "该学校" + year + "年筛查常见病的学生数量，超过最大限制");
        return String.format("%04d", total + 1);
    }
}
