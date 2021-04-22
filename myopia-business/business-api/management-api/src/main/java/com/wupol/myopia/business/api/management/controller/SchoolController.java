package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.facade.SchoolFacade;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.constant.SchoolAge;
import com.wupol.myopia.business.core.school.domain.dto.SchoolAgeDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanResponseDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;

/**
 * 学校Controller
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/school")
public class SchoolController {

    @Resource
    private SchoolService schoolService;

    @Resource
    private ExcelFacade excelFacade;

    @Resource
    private SchoolFacade schoolFacade;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping()
    public UsernameAndPasswordDTO getSchoolDetail(@RequestBody @Valid School school) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolService.saveSchool(school);
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
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolFacade.updateSchool(school, user);
    }

    /**
     * 通过ID获取学校详情
     *
     * @param id 学校ID
     * @return 学校实体
     */
    @GetMapping("{id}")
    public SchoolResponseDTO getSchoolDetail(@PathVariable("id") Integer id) {
        return schoolFacade.getBySchoolId(id);
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
        return schoolFacade.getSchoolList(pageRequest, schoolQuery, user);
    }

    /**
     * 更新学校状态
     *
     * @param statusRequest 请求入参
     * @return 更新个数
     */
    @PutMapping("status")
    public Integer updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getCurrentUser();
        return schoolService.updateStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        CurrentUserUtil.getCurrentUser();
        return schoolService.resetPassword(request.getId());
    }

    /**
     * 导出学校
     *
     * @param districtId 行政区域
     * @return 是否成功
     * @throws IOException   io异常
     * @throws UtilException 工具异常
     */
    @GetMapping("/export")
    public ApiResult getSchoolExportData(Integer districtId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.generateSchool(currentUser.getId(), districtId);
        return ApiResult.success();
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
        return schoolFacade.getScreeningRecordLists(pageRequest, schoolId);
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
        return schoolFacade.getSchoolListByDistrictId(schoolQuery);
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
}
