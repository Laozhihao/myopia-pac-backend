package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.GradeCode;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 学校端-学校管理
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/management")
public class SchoolManagementController {

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private SchoolGradeService schoolGradeService;

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
    public Integer deletedClass(@PathVariable("id") Integer id) {
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

    /**
     * 更新年级
     *
     * @param schoolGrade 年级实体
     * @return 新增个数
     */
    @PostMapping()
    public Integer saveGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        return schoolGradeService.saveGrade(schoolGrade);
    }

    /**
     * 删除年级
     *
     * @param id 年级ID
     * @return 删除个数
     */
    @DeleteMapping("/grade/{id}")
    public Integer deletedGrade(@PathVariable("id") Integer id) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolGradeService.deletedGrade(id, currentUser);
    }

    /**
     * 年级列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校ID
     * @return 年级列表
     */
    @GetMapping("list")
    public IPage<SchoolGradeItemsDTO> getGradeList(PageRequest pageRequest, Integer schoolId) {
        if (null == schoolId) {
            throw new BusinessException("学校ID不能为空");
        }
        return schoolGradeService.getGradeList(pageRequest, schoolId);
    }

    /**
     * 获取年级列表（不分页）
     *
     * @param schoolId 学校ID
     * @return 年级列表
     */
    @GetMapping("all")
    public List<SchoolGradeItemsDTO> getAllGradeList(Integer schoolId) {
        if (null == schoolId) {
            throw new BusinessException("学校ID不能为空");
        }
        return schoolGradeService.getAllGradeList(schoolId);
    }

    /**
     * 获取年级编码
     *
     * @return 年级编码
     */
    @GetMapping("getGradeCode")
    public List<GradeCode> getGradeCode() {
        return GradeCodeEnum.getGradeCodeList();
    }

    /**
     * 更新年级
     *
     * @param schoolGrade 年级实体
     * @return 年级实体
     */
    @PutMapping("")
    public SchoolGrade updateGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        return schoolGradeService.updateGrade(schoolGrade);
    }
}
