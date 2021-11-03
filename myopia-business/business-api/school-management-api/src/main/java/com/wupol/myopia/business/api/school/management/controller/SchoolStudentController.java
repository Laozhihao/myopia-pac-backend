package com.wupol.myopia.business.api.school.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学校端学生
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/student")
public class SchoolStudentController {

    @Autowired
    private SchoolStudentService schoolStudentService;

    @PostMapping
    public Boolean save(@RequestBody SchoolStudent student) {
        return schoolStudentService.saveOrUpdate(student);
    }

    @GetMapping
    public List<SchoolStudent> getList() {
        return schoolStudentService.list();
    }

}
