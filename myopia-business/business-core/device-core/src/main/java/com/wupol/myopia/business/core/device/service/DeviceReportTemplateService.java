package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceTemplateListDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceReportTemplateMapper;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备报告模板Service
 *
 * @author Simple4H
 */
@Service
public class DeviceReportTemplateService extends BaseService<DeviceReportTemplateMapper, DeviceReportTemplate> {

    @Resource
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;

    /**
     * 获取设备报告模板列表
     *
     * @return List<DeviceTemplateListDTO>
     */
    public List<DeviceTemplateListDTO> getTemplateList() {
        List<DeviceTemplateListDTO> responseList = new ArrayList<>();
        List<DeviceReportTemplate> templateList = baseMapper.getTemplateList();
        if (CollectionUtils.isEmpty(templateList)) {
            return responseList;
        }

        templateList.forEach(t -> {
            DeviceTemplateListDTO listDTO = new DeviceTemplateListDTO();
            listDTO.setId(t.getId());
            listDTO.setName(t.getName());
            List<ScreeningOrgBindDeviceReport> orgBindDeviceList = screeningOrgBindDeviceReportService.getOrgByTemplateIdLimit11(t.getId());
            listDTO.setReports(orgBindDeviceList.stream().limit(10).collect(Collectors.toList()));
            listDTO.setHaveMore(orgBindDeviceList.size() > 10);
            responseList.add(listDTO);
        });
        return responseList;
    }

    /**
     * 获取排序第一的模板
     *
     * @return 模板
     */
    public DeviceReportTemplate getSortFirstTemplate() {
        return baseMapper.getSortFirstTemplate();
    }
}
