package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.DeviceBizService;
import com.wupol.myopia.business.core.device.domain.dto.ConfigurationReportRequestDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceTemplateListDTO;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 设备管理控制层
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/device/report")
public class DeviceReportTemplateController {

    @Resource
    private DeviceReportTemplateService deviceReportTemplateService;

    @Resource
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;

    @Resource
    private DeviceBizService deviceBizService;

    /**
     * 获取设备报告模板列表
     *
     * @return List<DeviceReportTemplate>
     */
    @GetMapping("/template/list")
    public List<DeviceTemplateListDTO> list() {
        return deviceReportTemplateService.getTemplateList();
    }

    /**
     * 通过模板Id获取筛查机构
     *
     * @param templateId 模板Id
     * @return List<ScreeningOrgBindDeviceReport>
     */
    @GetMapping("/template/getOrgList/{templateId}")
    public List<ScreeningOrgBindDeviceReport> getOrgList(@PathVariable("templateId") @NotNull(message = "报告模板ID不能为空") Integer templateId) {
        return screeningOrgBindDeviceReportService.getOrgByTemplateId(templateId);
    }

    /**
     * 配置机构
     *
     * @param requestDTO 请求入参
     */
    @PostMapping("/template/configuration")
    public void configuration(@RequestBody @Valid ConfigurationReportRequestDTO requestDTO) {
        screeningOrgBindDeviceReportService.configurationReport(requestDTO);
    }

    /**
     * 获取打印需要的信息
     *
     * @param ids ids
     * @return List<DeviceReportPrintResponseDTO>
     */
    @GetMapping("/print")
    public List<DeviceReportPrintResponseDTO> getPrintReportInfo(@RequestParam("ids") @NotEmpty List<Integer> ids) {
        return deviceBizService.getPrintReportInfo(ids);
    }
}
