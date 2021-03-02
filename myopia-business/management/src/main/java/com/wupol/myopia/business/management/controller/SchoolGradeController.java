package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    public Object saveGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        return schoolGradeService.saveGrade(schoolGrade);
    }

    @DeleteMapping("{id}")
    public Object deletedGrade(@PathVariable("id") Integer id) {
        return schoolGradeService.deletedGrade(id);
    }

    @GetMapping("list")
    public Object getGradeList(PageRequest pageRequest, Integer schoolId) {
        if (null == schoolId) {
            throw new BusinessException("学校ID不能为空");
        }
        return schoolGradeService.getGradeList(pageRequest, schoolId);
    }

    @GetMapping("all")
    public Object getAllGradeList(Integer schoolId) {
        if (null == schoolId) {
            throw new BusinessException("学校ID不能为空");
        }
        return schoolGradeService.getAllGradeList(schoolId);
    }

    @GetMapping("getGradeCode")
    public Object getGradeCode() {
        return GradeCodeEnum.getGradeCodeList();
    }

    @PutMapping("")
    public Object updateGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        return schoolGradeService.updateGrade(schoolGrade);
    }
}
