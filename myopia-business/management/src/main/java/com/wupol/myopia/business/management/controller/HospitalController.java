package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.HospitalListRequest;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Object saveHospital(@RequestBody Hospital hospital) {
        // TODO: 获取登陆用户id, 部门id
        hospital.setCreateUserId(Const.CREATE_USER_ID);
        hospital.setGovDeptId(Const.GOV_DEPT_ID);
        return hospitalService.saveHospital(hospital);
    }

    @PutMapping
    public Object updateHospital(@RequestBody Hospital hospital) {
        // TODO: 获取登陆用户id, 部门id
        hospital.setCreateUserId(Const.CREATE_USER_ID);
        hospital.setGovDeptId(Const.GOV_DEPT_ID);
        return hospitalService.updateById(hospital);
    }

    @DeleteMapping("{id}")
    public Object deletedHospital(@PathVariable("id") Integer id) {
        Hospital hospital = new Hospital();
        // TODO: 获取登陆用户id, 部门id
        hospital.setId(id);
        hospital.setCreateUserId(Const.CREATE_USER_ID);
        hospital.setGovDeptId(Const.GOV_DEPT_ID);
        hospital.setStatus(Const.IS_DELETED);
        return hospitalService.updateById(hospital);
    }

    @GetMapping("{id}")
    public Object getHospital(@PathVariable("id") Integer id) {
        return hospitalService.getById(id);
    }

    @GetMapping("list")
    public Object getHospitalList(HospitalListRequest request) {
        return hospitalService.getHospitalList(request, Const.GOV_DEPT_ID);
    }

    @GetMapping("/export")
    public ApiResult getHospitalExportData(HospitalQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateHospital(query));
    }

}
