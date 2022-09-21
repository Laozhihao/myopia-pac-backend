package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.student.service.SchoolStaffFacade;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffListResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStaffSaveRequestDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学校端-学校视力分队
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/staff")
public class StaffController {

    @Resource
    private SchoolStaffFacade schoolStaffFacade;

    /**
     * 获取学校员工列表
     *
     * @param pageRequest 分页
     *
     * @return IPage<SchoolStaff>
     */
    @GetMapping("list")
    public IPage<SchoolStaffListResponseDTO> getSchoolStaffList(PageRequest pageRequest) {
        return schoolStaffFacade.getSchoolStaff(pageRequest, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 保存员工
     *
     * @param requestDTO 请求DTO
     */
    @PostMapping("save")
    public List<UsernameAndPasswordDTO> saveSchoolStaff(@RequestBody SchoolStaffSaveRequestDTO requestDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolStaffFacade.saveSchoolStaff(currentUser, currentUser.getOrgId(), requestDTO);
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
    @PostMapping("resetPassword")
    public List<UsernameAndPasswordDTO> resetPassword(Integer id) {
        return schoolStaffFacade.resetPassword(id);
    }

    /**
     * 是否超过人数配置
     *
     * @return 是否超过人数配置
     */
    @GetMapping("checkTeamCount")
    public Boolean isMoreThanConfig() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolStaffFacade.isMoreThanConfig(currentUser, currentUser.getOrgId());
    }
}
