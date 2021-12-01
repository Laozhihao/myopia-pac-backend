package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.HospitalBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 工作台-患者
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospital/workbench/patient")
public class HospitalWorkbenchPatientController {

    @Resource
    private HospitalBizService hospitalBizService;

    @Resource
    private HospitalStudentService hospitalStudentService;

    /**
     * 获取医院学生
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    @GetMapping("list")
    public IPage<HospitalStudentResponseDTO> getByList(@Validated PageRequest pageRequest, @Validated HospitalStudentRequestDTO requestDTO) {
        return hospitalBizService.getHospitalStudent(pageRequest, requestDTO);
    }

    @DeleteMapping("{hospitalStudentId}")
    public ApiResult deleted(@PathVariable("hospitalStudentId") Integer hospitalStudentId) {
        hospitalStudentService.deletedById(hospitalStudentId);
        return ApiResult.success();
    }
}
