package com.wupol.myopia.device.core.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.device.core.domain.mapper.DeviceReportTemplateMapper;
import com.wupol.myopia.device.core.domain.model.DeviceReportTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备报告模板Service
 *
 * @author Simple4H
 */
@Service
public class DeviceReportTemplateService extends BaseService<DeviceReportTemplateMapper, DeviceReportTemplate> {

    /**
     * 获取设备报告模板列表
     *
     * @return List<DeviceReportTemplate>
     */
    public List<DeviceReportTemplate> getTemplateList() {
        return baseMapper.getTemplateList();
    }
}
