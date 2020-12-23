package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrganizationListRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Object saveScreeningOrganization(@RequestBody ScreeningOrganization screeningOrganization) {
        screeningOrganization.setCreateUserId(Const.CREATE_USER_ID);
        screeningOrganization.setGovDeptId(Const.GOV_DEPT_ID);
        return saveScreeningOrganization.saveScreeningOrganization(screeningOrganization);
    }

    @PutMapping()
    public Object updateScreeningOrganization(@RequestBody ScreeningOrganization screeningOrganization) {
        screeningOrganization.setCreateUserId(Const.CREATE_USER_ID);
        screeningOrganization.setGovDeptId(Const.GOV_DEPT_ID);
        return saveScreeningOrganization.updateScreeningOrganization(screeningOrganization);
    }

    @GetMapping("{id}")
    public Object getScreeningOrganization(@PathVariable("id") Integer id) {
        return saveScreeningOrganization.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedScreeningOrganization(@PathVariable("id") Integer id) {
        return saveScreeningOrganization.deletedById(id);
    }

    @GetMapping("list")
    public Object getScreeningOrganizationList(ScreeningOrganizationListRequest request) {
        return saveScreeningOrganization.getScreeningOrganizationList(request, Const.GOV_DEPT_ID);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody StatusRequest statusRequest) {
        return null;
    }

    @GetMapping("/export")
    public ApiResult getOrganizationExportData(ScreeningOrganizationQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateScreeningOrganization(query));
    }

}
