package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

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

    @Autowired
    private ExcelFacade excelFacade;

    @GetMapping("list")
    public Object getOrganizationStaffList(@Valid OrganizationStaffRequest request) {
        return screeningOrganizationStaffService.getOrganizationStaffList(request, Const.GOV_DEPT_ID);
    }

    @GetMapping("{id}")
    public Object getOrganizationStaffList(@PathVariable("id") Integer id) {
        return screeningOrganizationStaffService.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedOrganizationStaff(@PathVariable("id") Integer id) {
        return screeningOrganizationStaffService.deletedOrganizationStaff(id, Const.CREATE_USER_ID);
    }

    @PostMapping()
    public Object insertOrganizationStaff(@RequestBody ScreeningOrganizationStaff screeningOrganizationStaff) {
        screeningOrganizationStaff.setCreateUserId(Const.CREATE_USER_ID);
        screeningOrganizationStaff.setGovDeptId(Const.GOV_DEPT_ID);
        return screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaff);
    }

    @PutMapping()
    public Object updateOrganizationStaffList(@RequestBody ScreeningOrganizationStaff screeningOrganizationStaff) {
        screeningOrganizationStaff.setCreateUserId(Const.CREATE_USER_ID);
        return screeningOrganizationStaffService.updateOrganizationStaff(screeningOrganizationStaff);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody StatusRequest statusRequest) {
        return null;
    }

    @GetMapping("/export")
    public ApiResult getOrganizationStaffExportData(ScreeningOrganizationStaffQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateScreeningOrganizationStaff(query));
    }

    @GetMapping("/import")
    public ApiResult importOrganizationStaff(MultipartFile file) throws IOException {
        Long orgId = 123L;
        excelFacade.importScreeningOrganizationStaff(orgId, file);
        return ApiResult.success();
    }

}
