package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.service.SchoolBizService;
import com.wupol.myopia.business.api.management.service.ScreeningOrganizationBizService;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningRecordItems;
import com.wupol.myopia.business.core.screening.organization.domain.dto.CacheOverviewInfoDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 筛查机构controller
 *
 * @author Simple4H
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningOrganization")
public class ScreeningOrganizationController {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private GovDeptService govDeptService;
    @Resource
    private ScreeningOrganizationBizService screeningOrganizationBizService;
    @Resource
    private ExportStrategy exportStrategy;
    @Resource
    private OrgCooperationHospitalService orgCooperationHospitalService;
    @Autowired
    private OverviewService overviewService;
    @Autowired
    private DistrictService districtService;
    @Resource
    private SchoolBizService schoolBizService;

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

        if (user.isPlatformAdminUser()) {
            screeningOrganizationService.checkScreeningOrganizationCooperation(screeningOrganization);

        } else {
            screeningOrganization.setAccountNum(ScreeningOrganization.ACCOUNT_NUM);
            if (user.isGovDeptUser()) {
                screeningOrganization.setConfigType(0);
                screeningOrganization.initCooperationInfo();
            } else if (user.isOverviewUser()) {
                // 总览机构
                CacheOverviewInfoDTO overview = overviewService.getSimpleOverviewInfo(user.getOrgId());
                // 绑定医院已达上线或不在同一个省级行政区域下
                if ((!overview.isCanAddScreeningOrganization()) || (!districtService.isSameProvince(screeningOrganization.getDistrictId(), overview.getDistrictId()))) {
                    throw new BusinessException("非法请求！");
                }
                screeningOrganization.initCooperationInfo(overview.getCooperationType(), overview.getCooperationTimeType(),
                        overview.getCooperationStartTime(), overview.getCooperationEndTime());
                screeningOrganization.setConfigType(overview.getScreeningOrganizationConfigType());
            } else {
                throw new BusinessException("非法的用户类型");
            }
        }
        screeningOrganization.setStatus(screeningOrganization.getCooperationStopStatus());
        return screeningOrganizationBizService.saveScreeningOrganization(screeningOrganization, user);
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
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), screeningOrganization.getId());
        if (user.isPlatformAdminUser()) {
            screeningOrganizationService.checkScreeningOrganizationCooperation(screeningOrganization);
            // 设置机构状态
            screeningOrganization.setStatus(screeningOrganization.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新以下信息
            screeningOrganization.clearCooperationInfo();
            screeningOrganization.setStatus(null);
            screeningOrganization.setAccountNum(null);
            screeningOrganization.setConfigType(null);
            screeningOrganization.setScreeningConfig(null);
        }
        ScreeningOrgResponseDTO screeningOrgResponseDTO = screeningOrganizationBizService.updateScreeningOrganization(user, screeningOrganization);
        // 若为平台管理员且修改了用户名，则回显账户名
        if (user.isPlatformAdminUser() && StringUtils.isNotBlank(screeningOrgResponseDTO.getUsername())) {
            screeningOrgResponseDTO.setDisplayUsername(true);
        }
        // 合作医院
        Integer countCooperationHospital = orgCooperationHospitalService.countCooperationHospital(screeningOrganization.getId());
        screeningOrgResponseDTO.setCountCooperationHospital(Objects.isNull(countCooperationHospital) ? 0 : countCooperationHospital);
        return screeningOrgResponseDTO;
    }

    /**
     * 通过ID获取筛查机构
     *
     * @param id 筛查机构ID
     * @return 筛查机构实体
     */
    @GetMapping("{id}")
    public ScreeningOrgResponseDTO getScreeningOrganization(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();

        if(user.isSchoolScreeningUser()) {
            return schoolBizService.school2ScreeningOrgResponseDTO(id);
        }

        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), id);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            id = user.getScreeningOrgId();
        }
        return screeningOrganizationBizService.getScreeningOrgDetails(id);
    }

    /**
     * 删除筛查机构
     *
     * @param id 筛查机构ID
     * @return 机构ID
     */
    @DeleteMapping("{id}")
    public Integer deletedScreeningOrganization(@PathVariable("id") Integer id) {
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), id);
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
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQueryDTO query){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isOverviewUser()) {
            query.setIds(overviewService.getBindScreeningOrganization(user.getOrgId()));
            if (CollectionUtils.isEmpty(query.getIds())) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
        }
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
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), request.getId());
        return screeningOrganizationService.updateStatus(request);
    }

    /**
     * 导出筛查机构
     *
     * @param districtId 行政区域ID
     */
    @GetMapping("/export")
    public void getOrganizationExportData(Integer districtId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition()
                        .setApplyExportFileUserId(user.getId())
                        .setDistrictId(districtId),
                ExportExcelServiceNameConstant.SCREENING_ORGANIZATION_EXCEL_SERVICE);
    }

    /**
     * 导出指定计划下的单个学校的学生的预计跟踪档案
     *
     * @param planId         筛查计划ID
     * @param schoolId       学校ID
     * @param screeningOrgId 筛查机构ID
     * @return void
     **/
    @GetMapping("/export/student/warning/archive")
    public void exportStudentWarningArchive(@NotNull(message = "planId不能为空") Integer planId,
                                            @NotNull(message = "schoolId不能为空") Integer schoolId,
                                            @NotNull(message = "screeningOrgId不能为空") Integer screeningOrgId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition().setApplyExportFileUserId(user.getId()).setPlanId(planId).setSchoolId(schoolId).setScreeningOrgId(screeningOrgId),
                ExportExcelServiceNameConstant.STUDENT_WARNING_ARCHIVE_EXCEL_SERVICE);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("/reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), request.getId());
        return screeningOrganizationService.resetPassword(request);
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
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), orgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            orgId = user.getScreeningOrgId();
        }
        return screeningOrganizationBizService.getRecordLists(request, orgId, user);
    }

    /**
     * 获取筛查计划的学校信息
     * @param screeningPlanId 筛查计划ID
     */
    @GetMapping("/record/schoolInfo")
    public ScreeningRecordItems getRecordSchoolInfo(@RequestParam Integer screeningPlanId){
        return screeningOrganizationBizService.getRecordSchoolInfo(screeningPlanId);
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
     * @return IPage<CooperationHospitalDTO>
     */
    @GetMapping("/getOrgCooperationHospital/{screeningOrgId}")
    public IPage<CooperationHospitalDTO> getOrgCooperationHospital(PageRequest request,
                                                                   @PathVariable("screeningOrgId") Integer screeningOrgId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), screeningOrgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            screeningOrgId = user.getScreeningOrgId();
        }
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
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), requestDTO.getScreeningOrgId());
        if (Objects.nonNull(user.getScreeningOrgId())) {
            requestDTO.setScreeningOrgId(user.getScreeningOrgId());
        }
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
     * @param orgId 筛查机构Id
     * @param name  名称
     * @return IPage<HospitalResponseDTO>
     */
    @GetMapping("/getOrgCooperationHospitalList")
    public List<HospitalResponseDTO> getOrgCooperationHospitalList(Integer orgId, String name) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), orgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            orgId = user.getScreeningOrgId();
        }
        return screeningOrganizationBizService.getHospitalList(orgId, name);
    }

    /**
     * 获取筛查机构的行政区域
     *
     * @param orgId 筛查机构Id
     * @return List<District>
     */
    @GetMapping("/getDistrictTree/{orgId}")
    public List<District> getDistrictTree(@PathVariable("orgId") Integer orgId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), orgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            orgId = user.getScreeningOrgId();
        }
        return screeningOrganizationBizService.getDistrictTree(orgId);
    }

    /**
     * 根据名称模糊查询
     *
     * @param name 筛查机构名称
     * @return List<ScreeningOrganization>
     */
    @GetMapping("getByName")
    public List<ScreeningOrganization> getByName(String name) {
        Assert.notNull(name, "筛查机构名称不能为空");
        return screeningOrganizationService.getByNameLike(name,Boolean.TRUE);
    }

    /**
     * 获取筛查机构账号列表
     *
     * @param orgId 机构Id
     * @return List<OrgAccountListDTO>
     */
    @GetMapping("/accountList/{orgId}")
    public List<OrgAccountListDTO> getAccountList(@PathVariable("orgId") Integer orgId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), orgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            orgId = user.getScreeningOrgId();
        }
        return screeningOrganizationService.getAccountList(orgId);
    }

    /**
     * 添加用户
     *
     * @param screeningOrgId 筛查机构ID
     * @return UsernameAndPasswordDTO
     */
    @PostMapping("/add/account/{screeningOrgId}")
    public UsernameAndPasswordDTO addAccount(@PathVariable("screeningOrgId") Integer screeningOrgId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), screeningOrgId);
        if (Objects.nonNull(user.getScreeningOrgId())) {
            screeningOrgId = user.getScreeningOrgId();
        }
        return screeningOrganizationBizService.addAccount(screeningOrgId);
    }

    /**
     * 模糊查询指定省份下筛查机构
     *
     * @param name                 筛查机构名称
     * @param provinceDistrictCode 省行政区域编码，如：110000000
     * @param configType          配置类型
     *
     * @return java.util.List<com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization>
     **/
    @GetMapping("/province/list")
    public List<ScreeningOrgResponseDTO> getListByProvinceCodeAndNameLike(@NotBlank(message = "筛查机构名称不能为空") String name,
                                                                          @NotNull(message = "省行政区域编码不能为空") Long provinceDistrictCode,
                                                                          Integer configType) {
        return screeningOrganizationService.getListByProvinceCodeAndNameLike(name, provinceDistrictCode, configType);
    }

    /**
     * 更新结果通知配置
     *
     * @param id                 筛查机构Id
     * @param resultNoticeConfig 结果通知
     */
    @PutMapping("/update/resultNoticeConfig/{id}")
    public void updateResultNoticeConfig(@PathVariable("id") @NotNull(message = "筛查机构ID为空") Integer id,
                                         @RequestBody ResultNoticeConfig resultNoticeConfig) {
        overviewService.checkScreeningOrganization(CurrentUserUtil.getCurrentUser(), id);
        screeningOrganizationService.updateResultNoticeConfig(id, resultNoticeConfig);
    }

    /**
     * 通过机构类型获取权限
     *
     * @param configType 配置
     * @return List<String>
     */
    @GetMapping("getPermission/{configType}")
    public List<String> getPermissionByConfigType(@PathVariable @NotNull(message = "配置不能为空") Integer configType) {
        return screeningOrganizationBizService.getPermissionByConfigType(configType);
    }

}