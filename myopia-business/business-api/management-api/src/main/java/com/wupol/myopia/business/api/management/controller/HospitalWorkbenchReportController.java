package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.HospitalBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalReportRequestDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 工作台-眼健康就诊记录
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospital/workbench/report")
public class HospitalWorkbenchReportController {

    @Resource
    private HospitalBizService hospitalBizService;


    /**
     * 获取医院就诊列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  医院就诊报告DTO
     * @return IPage<ReportAndRecordDO>
     */
    @GetMapping("list")
    public IPage<ReportAndRecordDO> getList(@Validated PageRequest pageRequest, HospitalReportRequestDTO requestDTO) {
        return hospitalBizService.getReportList(pageRequest, requestDTO);
    }
}
