package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.imports.ScreeningOrgStaffExcelImportService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningOrganizationStaffVO;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrganizationStaffRequestDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgStaffUserDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.StaffResetPasswordRequestDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

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
    private ScreeningOrgStaffExcelImportService screeningOrgStaffExcelImportService;

    @Autowired
    private ExportStrategy exportStrategy;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 筛查人员列表
     *
     * @param request 查询条件
     * @return 机构人员列表
     */
    @GetMapping("list")
    public IPage<ScreeningOrgStaffUserDTO> getOrganizationStaffList(@Valid OrganizationStaffRequestDTO request) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (Objects.nonNull(user.getScreeningOrgId())) {
            request.setScreeningOrgId(user.getScreeningOrgId());
        }
        return screeningOrganizationStaffService.getOrganizationStaffList(request);
    }

    /**
     * 新增筛查人员
     *
     * @param screeningOrganizationStaff 筛查人员实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public ScreeningOrganizationStaffVO insertOrganizationStaff(@RequestBody @Valid ScreeningOrganizationStaffQueryDTO screeningOrganizationStaff) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (Objects.nonNull(user.getScreeningOrgId())) {
            screeningOrganizationStaff.setScreeningOrgId(user.getScreeningOrgId());
        }
        // 非平台管理员
        if (!user.isPlatformAdminUser()) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(user.getScreeningOrgId());
            int totalNum = screeningOrganizationStaffService.countByScreeningOrgId(screeningOrganizationStaff.getScreeningOrgId());
            Assert.isTrue(totalNum >= screeningOrganization.getAccountNum(), "超过人数限制");
        }
        screeningOrganizationStaff.setCreateUserId(user.getId());
        screeningOrganizationStaff.setGovDeptId(user.getOrgId());
        UsernameAndPasswordDTO usernameAndPasswordDTO = screeningOrganizationStaffService.saveOrganizationStaff(screeningOrganizationStaff);
        int totalNum = screeningOrganizationStaffService.countByScreeningOrgId(screeningOrganizationStaff.getScreeningOrgId());
        return ScreeningOrganizationStaffVO.parseFromUsernameAndPasswordDTO(usernameAndPasswordDTO).setScreeningStaffTotalNum(totalNum);
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
        if (Objects.nonNull(user.getScreeningOrgId())) {
            screeningOrganizationStaff.setScreeningOrgId(user.getScreeningOrgId());
        }
        screeningOrganizationStaff.setCreateUserId(user.getId());
        return screeningOrganizationStaffService.updateOrganizationStaff(screeningOrganizationStaff);
    }

    /**
     * 更新筛查人员状态
     *
     * @param statusRequest 请求入参
     * @return 用户信息 {@link User}
     */
    @PutMapping("status")
    public User updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
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
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid StaffResetPasswordRequestDTO request) {
        CurrentUserUtil.getCurrentUser();
        return screeningOrganizationStaffService.resetPassword(request);
    }

    /**
     * 导出筛查人员
     *
     * @param screeningOrgId 筛查机构ID
     * @return 是否成功
     */
    @GetMapping("/export")
    public void getOrganizationStaffExportData(Integer screeningOrgId) throws IOException {
        if (Objects.isNull(screeningOrgId)) {
            throw new BusinessException("筛查机构id不能为空");
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.nonNull(currentUser.getScreeningOrgId())) {
            screeningOrgId = currentUser.getScreeningOrgId();
        }
        exportStrategy.doExport(new ExportCondition()
                        .setApplyExportFileUserId(currentUser.getId())
                        .setScreeningOrgId(screeningOrgId),
                ExportExcelServiceNameConstant.SCREENING_ORGANIZATION_STAFF_EXCEL_SERVICE);
    }

    /**
     * 导入筛查人员
     *
     * @param file           上传人员的文件
     * @param screeningOrgId 筛查机构
     * @return 是否成功
     */
    @PostMapping("/import/{screeningOrgId}")
    public void importOrganizationStaff(MultipartFile file, @PathVariable("screeningOrgId") Integer screeningOrgId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.nonNull(currentUser.getScreeningOrgId())) {
            screeningOrgId = currentUser.getScreeningOrgId();
        }
        screeningOrgStaffExcelImportService.importScreeningOrganizationStaff(currentUser, file, screeningOrgId);
    }

}