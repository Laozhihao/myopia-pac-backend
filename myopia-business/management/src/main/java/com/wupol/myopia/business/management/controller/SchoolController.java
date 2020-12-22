package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolListRequest;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/school")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @PostMapping()
    public Object saveSchool(@RequestBody School school) {
        school.setCreateUserId(Const.CREATE_USER_ID);
        school.setGovDeptId(Const.GOV_DEPT_ID);
        return schoolService.saveSchool(school);
    }

    @PutMapping()
    public Object updateSchool(@RequestBody School school) {
        school.setCreateUserId(Const.CREATE_USER_ID);
        school.setGovDeptId(Const.GOV_DEPT_ID);
        return schoolService.updateById(school);
    }

    @GetMapping("{id}")
    public Object saveSchool(@PathVariable("id") Integer id) {
        return schoolService.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedSchool(@PathVariable("id") Integer id) {
        return schoolService.deletedSchool(id);
    }

    @GetMapping("list")
    public Object getSchoolList(SchoolListRequest request) {
        return schoolService.getSchoolList(request, 1);
    }

}
