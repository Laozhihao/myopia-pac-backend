package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.parent.service.ParentStudentBizService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 0到6岁
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/zeroAndSix")
public class ParentZeroAndSixController {

    @Resource
    private StudentService studentService;

    @Resource
    private ParentStudentBizService parentStudentBizService;

    /**
     * 通过身份证获取学生
     *
     * @param idCard 身份证
     * @return 学生
     */
    @GetMapping("getByIdCard")
    public Student getByIdCard(String idCard) {
        return studentService.getByIdCard(idCard);
    }

    /**
     * 更新学生信息
     *
     * @param student 学生实体
     * @return 学生信息
     */
    @PutMapping("")
    public StudentDTO updateParentStudent(@RequestBody Student student) {
//        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(2);
        return parentStudentBizService.updateStudent(currentUser, student);
    }

    /**
     * 新增孩子（没有绑定则绑定）
     *
     * @param student 学生信息
     * @return 更新个数
     */
    @PostMapping("")
    public Integer saveParentStudent(@RequestBody Student student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.saveStudent(student, currentUser);
    }
}
