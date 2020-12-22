package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.service.ScreeningOrganizationStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningOrganizationStaff")
public class ScreeningOrganizationStaffController {

    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @GetMapping("list")
    public Object getOrganizationStaffList(OrganizationStaffRequest request) {
        return screeningOrganizationStaffService.getOrganizationStaffList(request);
    }

    @GetMapping("{id}")
    public Object getOrganizationStaffList(@PathVariable("id") Integer id) {
        return screeningOrganizationStaffService.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedOrganizationStaffList(@PathVariable("id") Integer id) {
        return null;
    }

    @PostMapping()
    public Object insertOrganizationStaffList(@RequestBody ScreeningOrganizationStaff screeningOrganizationStaff) {
        return null;
    }

    @PutMapping()
    public Object updateOrganizationStaffList(@RequestBody ScreeningOrganizationStaff screeningOrganizationStaff) {
        screeningOrganizationStaff.setCreateUserId(1);
        return screeningOrganizationStaffService.updateById(screeningOrganizationStaff);
    }
}
