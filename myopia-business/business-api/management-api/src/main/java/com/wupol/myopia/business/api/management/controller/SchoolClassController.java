package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 班级控制层
 *
 * @author HaoHao
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/schoolClass")
public class SchoolClassController {

    @Resource
    private SchoolClassService schoolClassService;

    /**
     * 保存班级
     *
     * @param schoolClass 班级实体
     * @return 新增数量
     */
    @PostMapping()
    public Integer saveGrade(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        return schoolClassService.saveClass(schoolClass);
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 删除数量
     */
    @DeleteMapping("{id}")
    public Integer deletedGrade(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolClassService.deletedClass(id, user.getId());
    }

    /**
     * 更新班级
     *
     * @param schoolClass 班级实体
     * @return 班级实体
     */
    @PutMapping()
    public SchoolClass updateClass(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        return schoolClassService.updateClass(schoolClass);
    }

    /**
     * 获取年级ID获取班级列表
     *
     * @param gradeId 年级ID
     * @return 班级列表
     */
    @GetMapping("all")
    public List<SchoolClass> getAllClassList(Integer gradeId) {
        if (null == gradeId) {
            throw new BusinessException("年级ID不能为空");
        }
        return schoolClassService.getByGradeId(gradeId);
    }
}
