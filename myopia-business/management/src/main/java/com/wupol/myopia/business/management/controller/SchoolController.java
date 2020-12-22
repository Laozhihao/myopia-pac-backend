package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.SchoolListRequest;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
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
        school.setCreateUserId(1);
        school.setGovDeptId(1);
        return schoolService.saveSchool(school);
    }

    @PutMapping()
    public Object updateSchool(@RequestBody School school) {
        school.setCreateUserId(1);
        school.setGovDeptId(1);
        return schoolService.updateById(school);
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
    public Object getSchoolList(SchoolListRequest request) {
        return schoolService.getSchoolList(request, 1);
    }

    @GetMapping("/export")
    public ApiResult getSchoolExportData(SchoolQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateSchool(query));
    }
}
