package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Object saveSchool(@RequestBody School school) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolService.saveSchool(school);
    }

    @PutMapping()
    public Object updateSchool(@RequestBody School school) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        school.setCreateUserId(user.getId());
        school.setGovDeptId(user.getOrgId());
        return schoolService.updateSchool(school);
    }

    @GetMapping("{id}")
    public Object saveSchool(@PathVariable("id") Integer id) {
        return schoolService.getById(id);
    }

    @DeleteMapping("{id}")
    public Object deletedSchool(@PathVariable("id") Integer id) {
        return schoolService.deletedSchool(id);
    }

    @GetMapping("list")
    public Object getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        return schoolService.getSchoolList(pageRequest, schoolQuery, user.getOrgId());
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody StatusRequest statusRequest) {
        CurrentUserUtil.getLegalCurrentUser();
        return schoolService.updateStatus(statusRequest);
    }

    @PostMapping("reset")
    public Object resetPassword(@RequestParam("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return schoolService.resetPassword(id);
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> getSchoolExportData(SchoolQuery query) throws IOException {
        return FileUtils.getResponseEntity(excelFacade.generateSchool(query));
    }
}
