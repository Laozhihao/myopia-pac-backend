package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.*;
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
 * 筛查人员Controller
 *
 * @author Simple4H
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

    /**
     * 筛查人员列表
     *
     * @param request 查询条件
     * @return 机构人员列表
     */
    @GetMapping("list")
    public Object getOrganizationStaffList(@Valid OrganizationStaffRequest request) {
        return screeningOrganizationStaffService.getOrganizationStaffList(request);
    }

    /**
     * 删除筛查人员
     *
     * @param id 人员ID
     * @return 删除
     */
    @DeleteMapping("{id}")
    public Object deletedOrganizationStaff(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.deletedOrganizationStaff(id, user.getId());
    }

    /**
     * 新增筛查人员
     *
     * @param screeningOrganizationStaff 筛查人员实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public Object insertOrganizationStaff(@RequestBody @Valid ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganizationStaff.setCreateUserId(user.getId());
        screeningOrganizationStaff.setGovDeptId(user.getOrgId());
        return screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaff);
    }

    /**
     * 更新筛查人员
     *
     * @param screeningOrganizationStaff 筛查人员实体
     * @return 筛查人员实体
     */
    @PutMapping()
    public Object updateOrganizationStaffList(@RequestBody @Valid ScreeningOrganizationStaffQuery screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningOrganizationStaff.setCreateUserId(user.getId());
        return screeningOrganizationStaffService.updateOrganizationStaff(screeningOrganizationStaff);
    }

    /**
     * 更新筛查人员状态
     *
     * @param statusRequest 请求入参
     * @return 用户信息 {@link UserDTO}
     */
    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getCurrentUser();
        return ApiResult.success(screeningOrganizationStaffService.updateStatus(statusRequest));
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public Object resetPassword(@RequestBody @Valid StaffResetPasswordRequest request) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.resetPassword(request);
    }

    /**
     * 导出筛查人员
     *
     * @param screeningOrgId 筛查机构ID
     * @return 是否成功
     * @throws IOException   IO异常
     * @throws UtilException 文件异常
     */
    @GetMapping("/export")
    public Object getOrganizationStaffExportData(Integer screeningOrgId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.generateScreeningOrganizationStaff(currentUser.getId(), screeningOrgId);
        return ApiResult.success();
    }

    /**
     * 导入筛查人员
     *
     * @param file           上传人员的文件
     * @param screeningOrgId 筛查机构
     * @return 是否成功
     */
    @PostMapping("/import/{screeningOrgId}")
    public ApiResult importOrganizationStaff(MultipartFile file, @PathVariable("screeningOrgId") Integer screeningOrgId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.importScreeningOrganizationStaff(currentUser, file, screeningOrgId);
        return ApiResult.success();
    }

    /**
     * 导出-导入模板
     *
     * @return 导入模板
     * @throws IOException IO异常
     */
    @GetMapping("/import/demo")
    public ResponseEntity<FileSystemResource> getImportDemo() throws IOException {
        return FileUtils.getResponseEntity(excelFacade.getScreeningOrganizationStaffImportDemo());
    }
}