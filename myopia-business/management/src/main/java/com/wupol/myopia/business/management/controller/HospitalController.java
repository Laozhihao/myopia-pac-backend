package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.HospitalService;
import com.wupol.myopia.business.management.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.bind.ValidationException;
import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private ExcelFacade excelFacade;

    @PostMapping
    public Object saveHospital(@RequestBody @Valid Hospital hospital) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        hospital.setCreateUserId(user.getId());
        hospital.setGovDeptId(user.getOrgId());
        return hospitalService.saveHospital(hospital);
    }

    @PutMapping
    public Object updateHospital(@RequestBody @Valid Hospital hospital) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        hospital.setCreateUserId(user.getId());
        hospital.setGovDeptId(user.getOrgId());
        return hospitalService.updateHospital(hospital);
    }

    @DeleteMapping("{id}")
    public Object deletedHospital(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        return hospitalService.deletedHospital(id, user.getId(), user.getOrgId());
    }

    @GetMapping("{id}")
    public Object getHospital(@PathVariable("id") Integer id) {
        return hospitalService.getById(id);
    }

    @GetMapping("list")
    public Object getHospitalList(PageRequest pageRequest, HospitalQuery query) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        return hospitalService.getHospitalList(pageRequest, query, user.getOrgId());
    }

    @PutMapping("status")
    public Object updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        CurrentUserUtil.getLegalCurrentUser();
        return hospitalService.updateStatus(statusRequest);
    }

    @PostMapping("reset")
    public Object resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        CurrentUserUtil.getLegalCurrentUser();
        return hospitalService.resetPassword(request.getId());
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> getHospitalExportData(HospitalQuery query) throws IOException, ValidationException {
        return FileUtils.getResponseEntity(excelFacade.generateHospital(query));
    }
}