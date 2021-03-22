package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ExcelFacade excelFacade;

    @Autowired
    private GovDeptService govDeptService;


    @PostMapping()
    public Object saveScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganization.setCreateUserId(user.getId());
        screeningOrganization.setGovDeptId(user.getOrgId());
        if (user.isGovDeptUser()) {
            screeningOrganization.setConfigType(0);
        }
        return screeningOrganizationService.saveScreeningOrganization(screeningOrganization);
    }

    @PutMapping()
    public Object updateScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.updateScreeningOrganization(user, screeningOrganization);
    }

    @GetMapping("{id}")
    public Object getScreeningOrganization(@PathVariable("id") Integer id) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.getScreeningOrgDetails(id);
    }

    @DeleteMapping("{id}")
    public Object deletedScreeningOrganization(@PathVariable("id") Integer id) {
        return screeningOrganizationService.deletedById(id);
    }

    @GetMapping("list")
    public Object getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.getScreeningOrganizationList(pageRequest, query, user);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest request) {
        return screeningOrganizationService.updateStatus(request);
    }

    @GetMapping("/export")
    public Object getOrganizationExportData(Integer districtId) throws IOException, UtilException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        excelFacade.generateScreeningOrganization(user.getId(), districtId);
        return ApiResult.success();
    }

    @PostMapping("/reset")
    public Object resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return screeningOrganizationService.resetPassword(request.getId());
    }

    @GetMapping("/record/lists/{orgId}")
    public Object getRecordLists(PageRequest request, @PathVariable("orgId") Integer orgId) {
        return screeningOrganizationService.getRecordLists(request, orgId);
    }

    /**
     * 根据部门ID获取筛查机构列表
     *
     * @param query
     * @return
     */
    @GetMapping("/listByGovDept")
    public Object getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQuery query) {

        return screeningOrganizationService.getScreeningOrganizationListByGovDeptId(query);
    }

    @GetMapping("/getDistrictId")
    public GovDept getDistrictId() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isGovDeptUser()) {
            return govDeptService.getById(currentUser.getOrgId());
        }
        return null;
    }
}