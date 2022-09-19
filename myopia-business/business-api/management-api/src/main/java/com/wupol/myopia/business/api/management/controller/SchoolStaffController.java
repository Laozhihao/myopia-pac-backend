package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.student.service.SchoolStaffFacade;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffSaveRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import com.wupol.myopia.business.core.school.service.SchoolStaffService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 学校视力小分队
 *
 * @author Simple4H
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/school/staff")
public class SchoolStaffController {

    @Resource
    private SchoolStaffService schoolStaffService;

    @Resource
    private SchoolStaffFacade schoolStaffFacade;

    /**
     * 获取学校员工列表
     *
     * @param pageRequest 分页
     * @param schoolId    学校Id
     *
     * @return IPage<SchoolStaff>
     */
    @GetMapping("list/{schoolId}")
    public IPage<SchoolStaff> getSchoolStaffList(PageRequest pageRequest, @PathVariable("schoolId") Integer schoolId) {
        return schoolStaffService.getSchoolStaff(pageRequest, schoolId);
    }

    /**
     * 保存员工
     *
     * @param requestDTO 请求DTO
     * @param schoolId   学校ID
     */
    @PostMapping("save/{schoolId}")
    public List<UsernameAndPasswordDTO> saveSchoolStaff(@PathVariable("schoolId") Integer schoolId, @RequestBody SchoolStaffSaveRequestDTO requestDTO) {
        return schoolStaffFacade.saveSchoolStaff(CurrentUserUtil.getCurrentUser(), schoolId, requestDTO);
    }

    /**
     * 启用/停用
     *
     * @param id     id
     * @param status 状态
     */
    @PostMapping("editStatus/{id}/{status}")
    public void editStatus(@PathVariable("id") Integer id, @PathVariable("status") Integer status) {
        schoolStaffFacade.editStatus(id, status);
    }

    /**
     * 重置密码
     *
     * @param id id
     *
     * @return List<UsernameAndPasswordDTO>
     */
    @PostMapping("resetPassword/{id}")
    public List<UsernameAndPasswordDTO> resetPassword(@PathVariable("id") Integer id) {
        return new ArrayList<>();
    }
}
