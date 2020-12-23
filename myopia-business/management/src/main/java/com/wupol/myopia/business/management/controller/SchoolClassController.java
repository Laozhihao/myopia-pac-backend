package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.service.SchoolClassService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    public Object saveGrade(@RequestBody SchoolClass schoolClass) {
        schoolClass.setCreateUserId(Const.CREATE_USER_ID);
        return schoolClassService.saveClass(schoolClass);
    }

    @DeleteMapping("{id}")
    public Object deletedGrade(@PathVariable("id") Integer id) {
        return schoolClassService.deletedClass(id);
    }
}
