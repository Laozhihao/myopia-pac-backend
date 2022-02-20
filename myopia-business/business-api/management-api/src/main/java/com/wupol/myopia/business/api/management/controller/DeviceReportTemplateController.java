package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.service.DeviceBizService;
import com.wupol.myopia.business.core.device.domain.dto.ConfigurationReportRequestDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceTemplateListDTO;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import org.springframework.validation.annotation.Validated;
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
@Validated
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

    @Resource
    private ExportStrategy exportStrategy;

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
    public List<DeviceReportPrintResponseDTO> getPrintReportInfo(@RequestParam("ids") @NotEmpty(message = "Id不能为空") List<Integer> ids) {
        return deviceBizService.getPrintReportInfo(ids);
    }


    /**
     * 打印VS666数据
     *
     * @param ids ids
     *
     */
    @GetMapping("/excel")
    public Object  getExcelExportData(@RequestParam("ids") @NotEmpty(message = "Id不能为空") List<Integer> ids) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
       String path  =  exportStrategy.syncExport(new ExportCondition()
                        .setApplyExportFileUserId(currentUser.getId())
                        .setIds(ids),
                ExportExcelServiceNameConstant.VS_DATA_EXCEL_SERVICE);
        return ApiResult.success(path);
    }


}
