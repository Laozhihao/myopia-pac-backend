package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/school")
public class SchoolController {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ExcelFacade excelFacade;

    @PostMapping()
    public Object saveSchool(@RequestBody @Valid School school) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolService.saveSchool(school);
    }

    @PutMapping()
    public Object updateSchool(@RequestBody @Valid School school) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolService.updateSchool(school, user);
    }

    @GetMapping("{id}")
    public Object saveSchool(@PathVariable("id") Integer id) {
        return schoolService.getBySchoolId(id);
    }

    @DeleteMapping("{id}")
    public Object deletedSchool(@PathVariable("id") Integer id) {
        return schoolService.deletedSchool(id);
    }

    @GetMapping("list")
    public Object getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolService.getSchoolList(pageRequest, schoolQuery, user);
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getCurrentUser();
        return schoolService.updateStatus(statusRequest);
    }

    @PostMapping("reset")
    public Object resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        CurrentUserUtil.getCurrentUser();
        return schoolService.resetPassword(request.getId());
    }

    @GetMapping("/export")
    public Object getSchoolExportData(Integer districtId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.generateSchool(currentUser.getId(), districtId);
        return ApiResult.success();
    }

    @GetMapping("screening/record/lists/{schoolId}")
    public Object getScreeningRecordLists(PageRequest pageRequest, @PathVariable("schoolId") Integer schoolId) {
        return schoolService.getScreeningRecordLists(pageRequest, schoolId);
    }

    @GetMapping("/checkSchoolNo/{schoolId}/{schoolNo}")
    public Object checkSchoolNo(@PathVariable("schoolId") Integer schoolId, @PathVariable("schoolNo") String schoolNo) {
        return schoolService.checkSchoolNo(schoolId, schoolNo);
    }

    @GetMapping("/getSchools/{schoolName}")
    public Object getSchoolByName(@PathVariable("schoolName") String schoolName) {
        return schoolService.getBySchoolName(schoolName);
    }

    @GetMapping("/getSchoolsByDistrictId/{districtId}")
    public Object getSchoolsByDistrictId(@PathVariable("districtId") Integer districtId) {
        return schoolService.getByDistrictId(districtId);
    }

    @GetMapping("/listByDistrict")
    public Object getSchoolListByDistctId(SchoolQuery schoolQuery) {
        return schoolService.getSchoolListByDistrictId(schoolQuery);
    }

    @GetMapping("/schoolAge/list")
    public Object getSchoolAge() {
        return SchoolAge.getSchoolAgeList();
    }
}
