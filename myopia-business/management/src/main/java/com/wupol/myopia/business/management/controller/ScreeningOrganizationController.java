package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
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
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkParam(screeningOrganization);
        screeningOrganization.setCreateUserId(user.getId());
        screeningOrganization.setGovDeptId(user.getOrgId());
        return saveScreeningOrganization.saveScreeningOrganization(screeningOrganization);
    }

    @PutMapping()
    public Object updateScreeningOrganization(@RequestBody ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkParam(screeningOrganization);
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
        return saveScreeningOrganization.getScreeningOrganizationList(pageRequest, query, user.getOrgId());
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody StatusRequest statusRequest) {
        return null;
    }

    @GetMapping("/export")
    public ApiResult getOrganizationExportData(ScreeningOrganizationQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateScreeningOrganization(query));
    }

    /**
     * 数据校验
     *
     * @param org 入参
     */
    public void checkParam(ScreeningOrganization org) {
        if (StringUtils.isBlank(org.getName()) || null == org.getType()
                || StringUtils.isBlank(org.getTypeDesc()) || null == org.getProvinceCode()
                || null == org.getCityCode() || null == org.getAreaCode()
                || null == org.getTownCode() || StringUtils.isBlank(org.getAddress())) {
            throw new BusinessException("数据异常");
        }

    }

}
