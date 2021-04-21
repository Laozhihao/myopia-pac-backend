package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQueryDTO;
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
    public Page<UserExtDTO> getOrganizationStaffList(@Valid OrganizationStaffRequest request) {
        return screeningOrganizationStaffService.getOrganizationStaffList(request);
    }

    /**
     * 新增筛查人员
     *
     * @param screeningOrganizationStaff 筛查人员实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public UsernameAndPasswordDTO insertOrganizationStaff(@RequestBody @Valid ScreeningOrganizationStaffQueryDTO screeningOrganizationStaff) {
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
    public ScreeningOrganizationStaffQueryDTO updateOrganizationStaffList(@RequestBody @Valid ScreeningOrganizationStaffQueryDTO screeningOrganizationStaff) {
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
    public UserDTO updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.updateStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid StaffResetPasswordRequest request) {
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
    public ApiResult getOrganizationStaffExportData(Integer screeningOrgId) throws IOException, UtilException {
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