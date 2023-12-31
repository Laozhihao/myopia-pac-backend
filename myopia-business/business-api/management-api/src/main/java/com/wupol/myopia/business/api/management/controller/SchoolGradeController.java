package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.BatchSaveGradeRequestDTO;
import com.wupol.myopia.business.core.school.domain.dto.GradeCode;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 年级控制层
 *
 * @author HaoHao
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/schoolGrade")
public class SchoolGradeController {

    @Resource
    private SchoolGradeService schoolGradeService;

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
    @DeleteMapping("{id}")
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

    /**
     * 批量新增班级、年级
     *
     * @param requestDTO 入参
     */
    @PostMapping("batchSave")
    public void batchSaveGrade(@RequestBody @Valid List<BatchSaveGradeRequestDTO> requestDTO) {
        schoolGradeService.batchSaveGrade(requestDTO, CurrentUserUtil.getCurrentUser().getId());
    }
}
