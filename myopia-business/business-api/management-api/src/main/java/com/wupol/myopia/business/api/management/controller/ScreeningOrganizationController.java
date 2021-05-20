package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.service.ScreeningOrganizationBizService;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalRequestDTO;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningOrganizationBizService screeningOrganizationBizService;
    @Autowired
    private ExportStrategy exportStrategy;
    @Autowired
    private OrgCooperationHospitalService orgCooperationHospitalService;

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
        return screeningOrganizationBizService.updateScreeningOrganization(user, screeningOrganization);
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
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQueryDTO query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationBizService.getScreeningOrganizationList(pageRequest, query, user);
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
     */
    @GetMapping("/export")
    public void getOrganizationExportData(Integer districtId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition().setApplyExportFileUserId(user.getId()).setDistrictId(districtId), ExportExcelServiceNameConstant.SCREENING_ORGANIZATION_EXCEL_SERVICE);
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
    public IPage<ScreeningOrgPlanResponseDTO> getRecordLists(PageRequest request, @PathVariable("orgId") Integer orgId) {
        return screeningOrganizationBizService.getRecordLists(request, orgId);
    }

    /**
     * 根据部门ID获取筛查机构列表
     *
     * @param query 请求体
     * @return 筛查机构列表
     */
    @GetMapping("/listByGovDept")
    public List<ScreeningOrgResponseDTO> getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQueryDTO query) {
        return screeningOrganizationBizService.getScreeningOrganizationListByGovDeptId(query);
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

    /**
     * 获取合作医院列表
     *
     * @param request        分页请求
     * @param screeningOrgId 筛查机构Id
     * @return List<CooperationHospitalDTO>
     */
    @GetMapping("/getOrgCooperationHospital/{screeningOrgId}")
    public List<CooperationHospitalDTO> getOrgCooperationHospital(PageRequest request,
                                                                  @PathVariable("screeningOrgId") Integer screeningOrgId) {
        return screeningOrganizationBizService.getCooperationHospitalList(request, screeningOrgId);
    }

    /**
     * 新增合作医院
     *
     * @param requestDTO 请求入参
     * @return 是否新增成功
     */
    @PostMapping("/saveOrgCooperationHospital")
    public boolean saveOrgCooperationHospital(@RequestBody CooperationHospitalRequestDTO requestDTO) {
        return orgCooperationHospitalService.saveCooperationHospital(requestDTO);
    }

    /**
     * 删除合作医院
     *
     * @param id Id
     * @return 是否删除成功
     */
    @DeleteMapping("/deletedCooperationHospital/{id}")
    public boolean deletedCooperationHospital(@PathVariable("id") Integer id) {
        return orgCooperationHospitalService.deletedCooperationHospital(id);
    }

    /**
     * 置顶医院
     *
     * @param id 合作医院Id
     * @return 是否置顶成功
     */
    @PutMapping("/topCooperationHospital/{id}")
    public boolean topCooperationHospital(@PathVariable("id") Integer id) {
        return orgCooperationHospitalService.topCooperationHospital(id);
    }

    /**
     * 获取医院（筛查机构只能看到全省）
     *
     * @param pageRequest 分页请求
     * @param name        名称
     * @return IPage<HospitalResponseDTO>
     */
    @GetMapping("/getOrgCooperationHospitalList")
    public Object getOrgCooperationHospitalList(PageRequest pageRequest, String name) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningOrganizationBizService.getHospitalList(currentUser, pageRequest, name);
    }
}