package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffSaveRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/management/school")
public class SchoolStaffController {

    /**
     * 获取学校员工列表
     *
     * @return IPage<SchoolStaff>
     */
    @GetMapping("list/{schoolId}")
    public IPage<SchoolStaff> getSchoolStaffList(@PathVariable("schoolId") Integer schoolId) {
        return null;
    }

    /**
     * 保存员工
     *
     * @param requestDTO 请求DTO
     * @param schoolId   学校ID
     */
    @PostMapping("save/{schoolId}")
    public void saveSchoolStaff(@PathVariable("schoolId") Integer schoolId, @RequestBody SchoolStaffSaveRequestDTO requestDTO) {
    }

    /**
     * 启用/停用
     *
     * @param id id
     */
    @PostMapping("editStatus/{id}")
    public void editStatus(@PathVariable("id") Integer id) {

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
