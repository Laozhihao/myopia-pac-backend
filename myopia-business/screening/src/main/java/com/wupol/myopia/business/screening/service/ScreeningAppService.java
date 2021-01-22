package com.wupol.myopia.business.screening.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.service.SchoolClassService;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@Service
public class ScreeningAppService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private StudentService studentService;

    /**
     * 模糊查询所有学校名称
     * @param nameLike 模糊查询
     * @param screeningOrgId    机构id
     * @param isReview      是否复测
     * @return
     */
    public List<String> getSchoolNameBySchoolNameLike(String nameLike, Integer screeningOrgId, Boolean isReview) {
        //TODO 管理端，待修改
        // TODO 待增加复测的逻辑
        SchoolQuery query = new SchoolQuery().setNameLike(nameLike);
        query.setGovDeptId(screeningOrgId);
        return schoolService.getBy(query).stream().map(School::getName).collect(Collectors.toList());
    }

    /**
     * 查询学校的年级名称
     * @param schoolName 学校名
     * @param screeningOrgId    机构id
     * @return
     */
    public List<String> getGradeNameBySchoolName(String schoolName, Integer screeningOrgId) {
        return schoolGradeService.getBySchoolName(schoolName, screeningOrgId).stream().map(SchoolGrade::getName).collect(Collectors.toList());
    }


    /**
     * 获取学校年级的班级名称
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param screeningOrgId        机构id
     * @return
     */
    public List<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer screeningOrgId) {
        return schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, screeningOrgId).stream()
                .map(SchoolClass::getName).collect(Collectors.toList());
    }

    /**
     * 获取学校年级班级对应的学生名称
     * @param schoolId      学校id, 仅复测时有
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param clazzName     班级名称
     * @param studentName     学生名称
     * @param screeningOrgId        机构id
     * @param isReview      是否复测
     * @return
     */
    public List<Student> getStudentBySchoolNameAndGradeNameAndClassName(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, String studentName, Integer screeningOrgId, Boolean isReview) {
        //TODO 管理端，待修改
        //TODO 待增加复测的逻辑
        List<SchoolClass> classList = schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, screeningOrgId);
        for (SchoolClass item : classList) {
            if (item.getName().equals(clazzName)) { // 匹配对应的班级
                //TODO 增加学生名过滤
                return studentService.getStudentsByClassId(item.getId());
            }
        }
        return Collections.emptyList();

    }


    /**
     * 随机获取学生复测信息
     * @param pageRequest   分页
     * @param screeningOrgId        机构id
     * @param schoolId      学校id
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param clazzName     班级名称
     * @return
     */
    public List<Student> getStudentReviewWithRandom(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, Integer screeningOrgId) {
        //TODO 管理端，待修改
        //TODO 待做随机
        return getStudentBySchoolNameAndGradeNameAndClassName(pageRequest, schoolId, schoolName, gradeName, clazzName, "", screeningOrgId, true);
    }

    /**
     * 保存学生眼镜信息
     * @return
     */
    public Object addStudentVision() {
        //TODO 管理端，待修改
        return new Object();
    }

    /**
     * 保存电脑验光
     * @return
     */
    public Object addStudentComputer() {
        //TODO 管理端，待修改
        return new Object();
    }

    /**
     * 保存生物测量数据
     * @return
     */
    public Object addStudentBiology() {
        //TODO 管理端，待修改
        return new Object();
    }

    /**
     * 获取筛查就机构对应的学校
     * @param screeningOrgId        机构id
     * @return
     */
    public List<School> getSchoolByScreeningOrgId(Integer screeningOrgId) {
        //TODO 筛查端，待修改
        SchoolQuery query = new SchoolQuery();
        query.setGovDeptId(screeningOrgId);
        return schoolService.getBy(query);
    }

    /**
     * 获取学生
     * @param id        学生id
     * @return
     */
    public Student getStudentById(Integer id) {
        return studentService.getById(id);
    }

    /**
     * 查询眼睛疾病
     * @return
     */
    public List<Object> getAllEyeDisease() {
        //TODO 筛查端，待修改
        return Collections.emptyList();
    }

    /**
     * 增加该学生的眼睛疾病
     * @return
     */
    public Object addEyeDisease() {
        //TODO 筛查端，待修改
        return new Object();
    }

    /**
     * 保存慢性病信息
     * @return
     */
    public Object addChronic() {
        //TODO 筛查端，待修改
        return new Object();
    }

    /**
     * 查询学生录入的最新一条数据(慢性病)
     * @param studentId    学生id
     * @param screeningOrgId        机构id
     * @return
     */
    public List<Object>  getStudentChronicNewByStudentId(Integer studentId, Integer screeningOrgId) {
        //TODO 筛查端，待修改
        return Collections.emptyList();
    }

    /**
     * 获取复测质控结果
     * @return
     */
    public List<Object> getAllReviewResult(Integer deptId, String gradeName, String clazzName, Integer schoolId) {
        //TODO 筛查端，待修改
        return Collections.emptyList();
    }

    /**
     * 更新复测质控结果
     * @return
     */
    public Boolean updateReviewResult(Integer eyeId) {
        //TODO 筛查端，待修改
        return true;
    }

    /**
     * 上传筛查机构用户的签名图片
     * @param deptId    筛查机构id
     * @param userId    用户id
     * @param file      签名
     * @return
     */
    public Boolean uploadSignPic(Integer deptId, Integer userId, MultipartFile file) {
        //TODO 筛查端，待修改
        return true;
    }

    /**
     * 保存学生信息
     * @return
     */
    public Object saveStudent(Student student) {
        return studentService.updateStudent(student);
    }


    /**
     * 人脸识别
     * @return
     */
    public Object recognitionFace(Integer deptId, MultipartFile file) {
        //TODO
        return new Object();
    }
}