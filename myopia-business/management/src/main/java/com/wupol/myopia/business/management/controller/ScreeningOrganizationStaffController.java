package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.dto.StaffResetPasswordRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.management.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
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
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.getOrganizationStaffList(request, currentUser);
    }

    @DeleteMapping("{id}")
    public Object deletedOrganizationStaff(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.deletedOrganizationStaff(id, user.getId());
    }

    @PostMapping()
    public Object insertOrganizationStaff(@RequestBody @Valid ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganizationStaff.setCreateUserId(user.getId());
        screeningOrganizationStaff.setGovDeptId(user.getOrgId());
        return screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaff);
    }

    @PutMapping()
    public Object updateOrganizationStaffList(@RequestBody @Valid ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganizationStaff.setCreateUserId(user.getId());
        return screeningOrganizationStaffService.updateOrganizationStaff(screeningOrganizationStaff);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getCurrentUser();
        return ApiResult.success(screeningOrganizationStaffService.updateStatus(statusRequest));
    }

    @PostMapping("reset")
    public Object resetPassword(@RequestBody @Valid StaffResetPasswordRequest request) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.resetPassword(request);
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> getOrganizationStaffExportData(Integer screeningOrgId) throws IOException {
        return FileUtils.getResponseEntity(excelFacade.generateScreeningOrganizationStaff(screeningOrgId));
    }


    @PostMapping("/import/{screeningOrgId}")
    public ApiResult importOrganizationStaff(MultipartFile file, @PathVariable("screeningOrgId") Integer screeningOrgId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.importScreeningOrganizationStaff(currentUser.getId(), file, screeningOrgId);
        return ApiResult.success();
    }

    @GetMapping("/import/demo")
    public ResponseEntity<FileSystemResource> getImportDemo() throws IOException {
        return FileUtils.getResponseEntity(excelFacade.getScreeningOrganizationStaffImportDemo());
    }
}