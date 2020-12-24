package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 年级控制层
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/schoolGrade")
public class SchoolGradeController {

    @Resource
    private SchoolGradeService schoolGradeService;

    @PostMapping()
    public Object saveGrade(@RequestBody SchoolGrade schoolGrade) {
        schoolGrade.setCreateUserId(Const.CREATE_USER_ID);
        return schoolGradeService.saveGrade(schoolGrade);
    }

    @DeleteMapping("{id}")
    public Object deletedGrade(@PathVariable("id") Integer id) {
        return schoolGradeService.deletedGrade(id);
    }

    @GetMapping("list")
    public Object getGradeList(Integer schoolId) {
        return schoolGradeService.getGradeList(schoolId);
    }

    @GetMapping("getGradeCode")
    public Object getGradeCode() {
        return GradeCodeEnum.getValuesInOrder();
    }
}
