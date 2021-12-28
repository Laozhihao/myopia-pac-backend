package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.api.management.service.SchoolBizService;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.SchoolAgeDTO;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.CollectionUtils;
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

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public UsernameAndPasswordDTO saveSchool(@RequestBody @Valid School school) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        if (user.isPlatformAdminUser()) {
            schoolService.checkSchoolCooperation(school);
        } else {
            // 默认合作信息
            school.initCooperationInfo();
        }
        if (user.isHospitalUser()) {
            school.setGovDeptId(user.getScreeningOrgId());
        }
        school.setStatus(school.getCooperationStopStatus());
        UsernameAndPasswordDTO nameAndPassword = schoolService.saveSchool(school);
        // 非平台管理员屏蔽账号密码信息
        if (!user.isPlatformAdminUser()) {
            nameAndPassword.setNoDisplay();
        }
        return nameAndPassword;
    }

    /**
     * 更新学校
     *
     * @param school 学校实体
     * @return 学校实体
     */
    @PutMapping()
    public SchoolResponseDTO updateSchool(@RequestBody @Valid School school) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isPlatformAdminUser()){
            schoolService.checkSchoolCooperation(school);
            // 设置学校状态
            school.setStatus(school.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新合作信息
            school.clearCooperationInfo();
            school.setStatus(null);
        }
        return schoolFacade.updateSchool(school);
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
     * @return 是否成功
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
        return schoolBizService.getScreeningRecordLists(pageRequest, schoolId);
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
    public List<SchoolResponseDTO> getSchoolListByDistctId(SchoolQueryDTO schoolQuery) {
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
     * @param districtAreaCode 行政Code
     * @param areaType         片区
     * @param monitorType      监测点
     * @return 学校编码
     */
    @GetMapping("/getLatestSchoolNo")
    public ApiResult getLatestSchoolNo(@NotBlank(message = "districtAreaCode不能为空") @Length(min = 9, max = 9, message = "无效districtAreaCode") String districtAreaCode,
                                       @NotNull(message = "areaType不能为空") @Max(value = 3, message = "无效areaType") Integer areaType,
                                       @NotNull(message = "monitorType不能为空") @Max(value = 3, message = "无效monitorType") Integer monitorType) {
        List<School> schoolList = schoolService.list(new QueryWrapper<>(new School().setDistrictAreaCode(Long.valueOf(districtAreaCode))));
        String schoolNo = districtAreaCode.substring(0, 4) + areaType + districtAreaCode.substring(4, 6) + monitorType;
        if (CollectionUtils.isEmpty(schoolList)) {
            return ApiResult.success(schoolNo + "01");
        }
        String maxSchoolNo = String.valueOf(schoolList.stream().mapToLong(s -> Long.parseLong(s.getSchoolNo())).max().orElse(0));
        String size = String.format("%02d", Integer.parseInt(maxSchoolNo.substring(maxSchoolNo.length() - 2)) + 1);
        return ApiResult.success(schoolNo + size);
    }

}
