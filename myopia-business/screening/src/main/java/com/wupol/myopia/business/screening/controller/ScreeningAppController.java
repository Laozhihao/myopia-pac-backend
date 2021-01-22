package com.wupol.myopia.business.screening.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.screening.service.ScreeningAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/app")
public class ScreeningAppController {

    @Autowired
    private ScreeningAppService screeningAppService;


    /**
     * 模糊查询所有学校名称
     * @param schoolName 模糊查询
     * @param deptId    机构id
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public List<String> getSchoolNameByNameLike(@RequestParam String schoolName, @RequestParam Integer deptId) {
        deptId = 1;
        return screeningAppService.getSchoolNameBySchoolNameLike(schoolName, deptId);
    }

    /**
     * 查询学校的年级名称
     * @param schoolName 学校名
     * @param deptId    机构id
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public List<String> getGradeNameBySchoolName(@RequestParam String schoolName, @RequestParam Integer deptId) {
        deptId = 1;
        return screeningAppService.getGradeNameBySchoolName(schoolName, deptId);
    }


    /**
     * 获取班级名称
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param deptId        部门id
     * @return
     */
    @GetMapping("/school/findAllClazzNameBySchoolNameAndGradeName")
    public List<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId) {
        deptId = 1;
        return screeningAppService.getClassNameBySchoolNameAndGradeName(schoolName, gradeName, deptId);
    }

    /**
     * 获取学校年级班级对应的学生名称
     * @param schoolId      学校id, 仅复测时有
     * @param schoolName    学校名称
     * @param gradeName     年级名称
     * @param clazzName     班级名称
     * @param deptId        部门id
     * @param isReview      是否复测
     * @return
     */
    @GetMapping("/school/findAllStudentName")
    public List<Student> getStudentNameBySchoolNameAndGradeNameAndClassName(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, Integer deptId, Boolean isReview) {
        deptId = 1;
        return screeningAppService.getStudentNameBySchoolNameAndGradeNameAndClassName(pageRequest, schoolId, schoolName, gradeName, clazzName,  deptId, isReview);
    }

    /**
     * 获取筛查就机构对应的学校
     * @param deptId        部门id
     * @return
     */
    @GetMapping("/screening/findSchoolByDeptId")
    public List<School> getSchoolByDeptId(Integer deptId) {
        deptId = 1;
        return screeningAppService.getSchoolByDeptId(deptId);
    }

}
