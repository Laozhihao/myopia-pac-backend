package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningNoticeBizFacadeService;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningSchoolOrgVO;
import com.wupol.myopia.business.api.management.service.SchoolBizService;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.SchoolAgeDTO;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.dos.SimpleSchoolDO;
import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.ScreeningSchoolOrgDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanLinkNoticeRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.organization.domain.dto.CacheOverviewInfoDTO;
import com.wupol.myopia.business.core.screening.organization.service.OverviewSchoolService;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * 学校Controller
 *
 * @author Simple4H
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/school")
public class SchoolController {

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolBizService schoolBizService;

    @Resource
    private ExportStrategy exportStrategy;

    @Resource
    private SchoolFacade schoolFacade;

    @Resource
    private OverviewService overviewService;

    @Resource
    private DistrictService districtService;

    @Resource
    private OverviewSchoolService overviewSchoolService;

    @Resource
    private ScreeningNoticeBizFacadeService screeningNoticeBizFacadeService;

    /**
     * 新增学校
     *
     * @param requestDTO 学校实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public UsernameAndPasswordDTO saveSchool(@RequestBody @Valid SaveSchoolRequestDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        requestDTO.setCreateUserId(user.getId());
        requestDTO.setGovDeptId(user.getOrgId());
        if (user.isPlatformAdminUser()) {
            schoolService.checkSchoolCooperation(requestDTO);
        } else {
            // 默认合作信息
            requestDTO.initCooperationInfo();
        }
        if (user.isHospitalUser()) {
            requestDTO.setGovDeptId(user.getScreeningOrgId());
        }
        if (user.isOverviewUser()) {
            // 总览机构
            CacheOverviewInfoDTO overview = overviewService.getSimpleOverviewInfo(user.getOrgId());
            // 绑定学校已达上线或不在同一个省级行政区域下
            if ((!overview.isCanAddSchool()) || (!districtService.isSameProvince(requestDTO.getDistrictId(), overview.getDistrictId()))) {
                throw new BusinessException("非法请求！");
            }
            requestDTO.initCooperationInfo(overview.getCooperationType(), overview.getCooperationTimeType(),
                    overview.getCooperationStartTime(), overview.getCooperationEndTime());
        }
        requestDTO.setStatus(requestDTO.getCooperationStopStatus());
        UsernameAndPasswordDTO nameAndPassword = schoolService.saveSchool(requestDTO);
        // 平台管理员、总览账号显示账号密码信息
        if (user.isPlatformAdminUser() || user.isOverviewUser()) {
            nameAndPassword.setDisplay(Boolean.TRUE);
        } else {
            nameAndPassword.setNoDisplay();
        }
        if (user.isOverviewUser()) {
            overviewSchoolService.saveOverviewSchool(user.getOrgId(), nameAndPassword.getId());
        }
        return nameAndPassword;
    }

    /**
     * 更新学校
     *
     * @param requestDTO 学校实体
     * @return 学校实体
     */
    @PutMapping()
    public SchoolResponseDTO updateSchool(@RequestBody @Valid SaveSchoolRequestDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isPlatformAdminUser()){
            schoolService.checkSchoolCooperation(requestDTO);
            // 设置学校状态
            requestDTO.setStatus(requestDTO.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新合作信息
            requestDTO.clearCooperationInfo();
            requestDTO.setStatus(null);
        }
        return schoolFacade.updateSchool(requestDTO);
    }


    /**
     * 通过ID获取学校详情
     *
     * @param id 学校ID
     * @return 学校实体
     */
    @GetMapping("{id}")
    public SchoolResponseDTO getSchoolDetail(@PathVariable("id") Integer id) {
        return schoolFacade.getBySchoolId(id, false);
    }

    /**
     * 删除学校
     *
     * @param id 学校ID
     * @return 删除数量
     */
    @DeleteMapping("{id}")
    public Integer deletedSchool(@PathVariable("id") Integer id) {
        return schoolService.deletedSchool(id);
    }

    /**
     * 学校列表
     *
     * @param pageRequest 分页请求
     * @param schoolQuery 请求条件
     * @return 学校列表
     */
    @GetMapping("list")
    public IPage<SchoolResponseDTO> getSchoolList(PageRequest pageRequest, SchoolQueryDTO schoolQuery) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolBizService.getSchoolList(pageRequest, schoolQuery, user);
    }

    /**
     * 更新学校状态
     *
     * @param statusRequest 请求入参
     * @return 更新个数
     */
    @PutMapping("status")
    public Integer updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        return schoolService.updateStatus(statusRequest);
    }

    /**
     * 更新学校管理员状态
     *
     * @param statusRequest 请求入参
     * @return 更新个数
     */
    @PutMapping("/admin/status")
    public boolean updateSchoolAdminUserStatus(@RequestBody @Valid StatusRequest statusRequest) {
        return schoolService.updateSchoolAdminUserStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return schoolService.resetPassword(request);
    }

    /**
     * 导出学校
     *
     * @param districtId 行政区域
     */
    @GetMapping("/export")
    public void getSchoolExportData(Integer districtId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition()
                        .setApplyExportFileUserId(currentUser.getId())
                        .setDistrictId(districtId),
                ExportExcelServiceNameConstant.SCHOOL_EXCEL_SERVICE);
    }

    /**
     * 获取学校的筛查记录列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校ID
     * @return 筛查记录列表
     */
    @GetMapping("screening/record/lists/{schoolId}")
    public IPage<ScreeningPlanResponseDTO> getScreeningRecordLists(PageRequest pageRequest, @PathVariable("schoolId") Integer schoolId) {
        return schoolBizService.getScreeningRecordLists(pageRequest, schoolId, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 学校编号是否被使用
     *
     * @param schoolId 学校ID
     * @param schoolNo 学校编号
     * @return 是否被使用
     */
    @GetMapping("/checkSchoolNo/{schoolId}/{schoolNo}")
    public Boolean checkSchoolNo(@PathVariable("schoolId") Integer schoolId, @PathVariable("schoolNo") String schoolNo) {
        return schoolService.checkSchoolNo(schoolId, schoolNo);
    }

    /**
     * 通过名字获取学校列表
     *
     * @param schoolName 学校名称
     * @return 学校列表
     */
    @GetMapping("/getSchools/{schoolName}")
    public List<School> getSchoolByName(@PathVariable("schoolName") String schoolName) {
        return schoolService.getBySchoolName(schoolName);
    }

    /**
     * 通过districtId获取学校列表
     *
     * @param districtId 行政区域
     * @return 学校列表
     */
    @GetMapping("/getSchoolsByDistrictId/{districtId}")
    public List<School> getSchoolsByDistrictId(@PathVariable("districtId") Integer districtId) {
        return schoolService.getByDistrictId(districtId);
    }

    /**
     * 筛查计划新增学校：机构所在省份全省学校
     *
     * @param schoolQuery 查询条件
     * @return 学校列表
     */
    @GetMapping("/listByDistrict")
    public List<SimpleSchoolDO> getSchoolListByDistrictId(SchoolQueryDTO schoolQuery) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isGovDeptUser()) {
            // 政府部门，无法新增计划
            throw new ValidationException("无权限");
        }
        return schoolBizService.getSchoolListByDistrictId(schoolQuery);
    }

    /**
     * 获取学龄段列表
     *
     * @return 学龄段列表
     */
    @GetMapping("/schoolAge/list")
    public List<SchoolAgeDTO> getSchoolAge() {
        return SchoolAge.getSchoolAgeList();
    }

    /**
     * 获取学校管理员用户账号列表
     *
     * @param schoolId 学校Id
     * @return List<OrgAccountListDTO>
     */
    @GetMapping("/accountList/{schoolId}")
    public List<OrgAccountListDTO> getAccountList(@PathVariable("schoolId") Integer schoolId) {
        return schoolBizService.getAccountList(schoolId);
    }

    /**
     * 添加用户
     *
     * @param schoolId 请求入参
     * @return UsernameAndPasswordDTO
     */
    @PostMapping("/add/account/{schoolId}")
    public UsernameAndPasswordDTO addAccount(@PathVariable("schoolId") Integer schoolId) {
        return schoolBizService.addSchoolAdminUserAccount(schoolId);
    }

    /**
     * 获取学校编码
     *
     * @param districtAreaCode  区/镇/县的行政区域编号，如：210103000
     * @param areaType          片区类型，如：2-中片区
     * @param monitorType       监测点类型，如：1-城区
     * @return 学校编码
     */
    @GetMapping("/getLatestSchoolNo")
    public ApiResult<String> getLatestSchoolNo(@NotBlank(message = "districtAreaCode不能为空") @Length(min = 9, max = 9, message = "无效districtAreaCode") String districtAreaCode,
                                       @NotNull(message = "areaType不能为空") @Max(value = 3, message = "无效areaType") Integer areaType,
                                       @NotNull(message = "monitorType不能为空") @Max(value = 3, message = "无效monitorType") Integer monitorType) {
        return ApiResult.success(schoolService.getLatestSchoolNo(districtAreaCode, areaType, monitorType));
    }

    /**
     * 模糊查询指定省份下学校
     *
     * @param name                 学校名称
     * @param provinceDistrictCode 省行政区域编码，如：110000000
     */
    @GetMapping("/province/list")
    public List<SchoolResponseDTO> getListByProvinceCodeAndNameLike(@NotBlank(message = "学校名称不能为空") String name,
                                                         @NotNull(message = "省行政区域编码不能为空") Long provinceDistrictCode) {
        return schoolService.getListByProvinceCodeAndNameLike(name, provinceDistrictCode);
    }

    /**
     * 获取筛查机构列表(学校)
     *
     * @param pageRequest 分页请求
     * @param query       查询条件
     * @return 机构列表
     */
    @GetMapping("/getSchoolList")
    public IPage<ScreeningSchoolOrgVO> getScreeningOrganizationList(PageRequest pageRequest, ScreeningSchoolOrgDTO query){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolBizService.getScreeningOrganizationList(pageRequest, query, user);
    }

    /**
     * 更新学校结果通知书配置
     *
     * @param resultNoticeConfig 结果通知书配置
     */
    @PutMapping("/update/resultNoticeConfig/{id}")
    @Transactional(rollbackFor = Exception.class)
    public void updateSchool(@PathVariable("id") Integer id, @RequestBody ResultNoticeConfig resultNoticeConfig) {
        School school = schoolService.getBySchoolId(id);
        school.setResultNoticeConfig(resultNoticeConfig);
        schoolService.updateById(school);
    }

    /**
     * 获取关联的通知
     *
     * @return List<ScreeningNoticeDTO>
     */
    @GetMapping("planLinkNotice/list")
    public List<ScreeningNoticeDTO> getPlanLinkNoticeList(@NotNull(message = "学校Id不能为空") Integer orgId) {
        return screeningNoticeBizFacadeService.getCanLinkNotice(orgId, ScreeningNotice.TYPE_SCHOOL);
    }

    /**
     * 关联通知
     *
     * @param requestDTO requestDTO
     */
    @PostMapping("linkNotice/link")
    public void linkNotice(@RequestBody @Valid PlanLinkNoticeRequestDTO requestDTO) {
        screeningNoticeBizFacadeService.linkNotice(requestDTO);
    }
}
