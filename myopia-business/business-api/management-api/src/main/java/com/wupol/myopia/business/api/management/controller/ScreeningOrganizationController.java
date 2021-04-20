package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * 筛查机构controller
 *
 * @author Simple4H
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

    /**
     * 新增筛查机构
     *
     * @param screeningOrganization 筛查机构实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public UsernameAndPasswordDTO saveScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganization.setCreateUserId(user.getId());
        screeningOrganization.setGovDeptId(user.getOrgId());
        if (user.isGovDeptUser()) {
            screeningOrganization.setConfigType(0);
        }
        return screeningOrganizationService.saveScreeningOrganization(screeningOrganization);
    }

    /**
     * 更新筛查机构
     *
     * @param screeningOrganization 筛查机构实体
     * @return 筛查机构实体
     */
    @PutMapping()
    public ScreeningOrgResponseDTO updateScreeningOrganization(@RequestBody @Valid ScreeningOrganization screeningOrganization) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.updateScreeningOrganization(user, screeningOrganization);
    }

    /**
     * 通过ID获取筛查机构
     *
     * @param id 筛查机构ID
     * @return 筛查机构实体
     */
    @GetMapping("{id}")
    public ScreeningOrgResponseDTO getScreeningOrganization(@PathVariable("id") Integer id) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.getScreeningOrgDetails(id);
    }

    /**
     * 删除筛查机构
     *
     * @param id 筛查机构ID
     * @return 机构ID
     */
    @DeleteMapping("{id}")
    public Integer deletedScreeningOrganization(@PathVariable("id") Integer id) {
        return screeningOrganizationService.deletedById(id);
    }

    /**
     * 获取筛查机构列表
     *
     * @param pageRequest 分页请求
     * @param query       查询条件
     * @return 机构列表
     */
    @GetMapping("list")
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationService.getScreeningOrganizationList(pageRequest, query, user);
    }

    /**
     * 更新状态
     *
     * @param request 请求入参
     * @return 更新个数
     */
    @PutMapping("status")
    public Integer updateStatus(@RequestBody @Valid StatusRequest request) {
        return screeningOrganizationService.updateStatus(request);
    }

    /**
     * 导出筛查机构
     *
     * @param districtId 行政区域ID
     * @return 是否成功
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     */
    @GetMapping("/export")
    public ApiResult getOrganizationExportData(Integer districtId) throws IOException, UtilException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        excelFacade.generateScreeningOrganization(user.getId(), districtId);
        return ApiResult.success();
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("/reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return screeningOrganizationService.resetPassword(request.getId());
    }

    /**
     * 获取筛查记录列表
     *
     * @param request 请求体
     * @param orgId   筛查机构ID
     * @return 筛查记录列表
     */
    @GetMapping("/record/lists/{orgId}")
    public IPage<ScreeningOrgPlanResponse> getRecordLists(PageRequest request, @PathVariable("orgId") Integer orgId) {
        return screeningOrganizationService.getRecordLists(request, orgId);
    }

    /**
     * 根据部门ID获取筛查机构列表
     *
     * @param query 请求体
     * @return 筛查机构列表
     */
    @GetMapping("/listByGovDept")
    public List<ScreeningOrgResponseDTO> getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQuery query) {
        return screeningOrganizationService.getScreeningOrganizationListByGovDeptId(query);
    }

    /**
     * 获取当前用户的行政区域
     *
     * @return 行政区域
     */
    @GetMapping("/getDistrictId")
    public GovDept getDistrictId() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isGovDeptUser()) {
            return govDeptService.getById(currentUser.getOrgId());
        }
        return null;
    }
}