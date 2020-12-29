package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.ScreeningOrganizationStaffService;
import org.apache.commons.lang3.StringUtils;
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
        CurrentUserUtil.getLegalCurrentUser();
        return screeningOrganizationStaffService.getOrganizationStaffList(request);
    }

    @DeleteMapping("{id}")
    public Object deletedOrganizationStaff(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        return screeningOrganizationStaffService.deletedOrganizationStaff(id, user.getId());
    }

    @PostMapping()
    public Object insertOrganizationStaff(@RequestBody ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkStaffIsLegal(screeningOrganizationStaff);
        screeningOrganizationStaff.setCreateUserId(user.getId());
        screeningOrganizationStaff.setGovDeptId(user.getOrgId());
        return screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaff);
    }

    @PutMapping()
    public Object updateOrganizationStaffList(@RequestBody ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkStaffIsLegal(screeningOrganizationStaff);
        screeningOrganizationStaff.setCreateUserId(user.getId());
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


    @PostMapping("/import")
    public ApiResult importOrganizationStaff(MultipartFile file) throws IOException {
        Integer orgId = 123;
        Integer createUserId = 3;
        excelFacade.importScreeningOrganizationStaff(orgId, createUserId, file);
        return ApiResult.success();
    }

    /**
     * 数据校验
     *
     * @param query 员工实体类
     */
    private void checkStaffIsLegal(ScreeningOrganizationStaffQuery query) {
        if (null == query.getScreeningOrgId() || StringUtils.isBlank(query.getName())
                || null == query.getGender()) {
            throw new BusinessException("数据异常");
        }
        // 检查身份证
        if (!RegularUtils.isIdCard(query.getIdCard())) {
            throw new BusinessException("身份证不正确");
        }
        // 检查手机号码
        if (!RegularUtils.isMobile(query.getPhone())) {
            throw new BusinessException("手机号不正确");
        }
    }
}
