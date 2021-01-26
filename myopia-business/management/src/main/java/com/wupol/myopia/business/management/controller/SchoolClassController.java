package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.service.SchoolClassService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 班级控制层
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/schoolClass")
public class SchoolClassController {

    @Resource
    private SchoolClassService schoolClassService;

    @PostMapping()
    public Object saveGrade(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        return schoolClassService.saveClass(schoolClass);
    }

    @DeleteMapping("{id}")
    public Object deletedGrade(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolClassService.deletedClass(id, user.getId());
    }

    @PutMapping()
    public Object updateClass(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        return schoolClassService.updateClass(schoolClass);
    }
}
