package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import com.wupol.myopia.business.management.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningOrganization")
public class ScreeningOrganizationController {

    @Autowired
    private ScreeningOrganizationService saveScreeningOrganization;
    @Autowired
    private ExcelFacade excelFacade;


    @PostMapping()
    public Object saveScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        screeningOrganization.setCreateUserId(user.getId());
        screeningOrganization.setGovDeptId(user.getOrgId());
        return saveScreeningOrganization.saveScreeningOrganization(screeningOrganization);
    }

    @PutMapping()
    public Object updateScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        screeningOrganization.setCreateUserId(user.getId());
        screeningOrganization.setGovDeptId(user.getOrgId());
        return saveScreeningOrganization.updateScreeningOrganization(screeningOrganization);
    }

    @GetMapping("{id}")
    public Object getScreeningOrganization(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return saveScreeningOrganization.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedScreeningOrganization(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return saveScreeningOrganization.deletedById(id);
    }

    @GetMapping("list")
    public Object getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQuery query) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        return saveScreeningOrganization.getScreeningOrganizationList(pageRequest, query, user);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest request) {
        CurrentUserUtil.getLegalCurrentUser();
        return saveScreeningOrganization.updateStatus(request);
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> getOrganizationExportData(ScreeningOrganizationQuery query) throws IOException {
        return FileUtils.getResponseEntity(excelFacade.generateScreeningOrganization(query));
    }

    @PostMapping("/reset")
    public Object resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        CurrentUserUtil.getLegalCurrentUser();
        return saveScreeningOrganization.resetPassword(request.getId());
    }
}