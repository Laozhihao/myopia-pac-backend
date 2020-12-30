package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.HospitalService;
import org.apache.commons.lang3.StringUtils;
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
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkParam(hospital);
        hospital.setCreateUserId(user.getId());
        hospital.setGovDeptId(user.getOrgId());
        return hospitalService.saveHospital(hospital);
    }

    @PutMapping
    public Object updateHospital(@RequestBody Hospital hospital) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkParam(hospital);
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
    public Object updateStatus(@RequestBody StatusRequest statusRequest) {
        return hospitalService.updateStatus(statusRequest);
    }

    @PostMapping("reset")
    public Object resetPassword(@RequestParam("id") Integer id) {
        return hospitalService.resetPassword(id);
    }

    @GetMapping("/export")
    public ApiResult getHospitalExportData(HospitalQuery query) throws IOException {
        return ApiResult.success(excelFacade.generateHospital(query));
    }

    /**
     * 数据校验
     *
     * @param hospital 医院入参
     */
    private void checkParam(Hospital hospital) {
        if (StringUtils.isBlank(hospital.getName()) || hospital.getLevel() == null
                || StringUtils.isBlank(hospital.getLevelDesc())
                || hospital.getType() == null || hospital.getKind() == null
                || hospital.getProvinceCode() == null || hospital.getCityCode() == null
                || hospital.getAreaCode() == null || hospital.getTownCode() == null
                || StringUtils.isBlank(hospital.getAddress())) {
            throw new BusinessException("数据异常");
        }
    }

}
