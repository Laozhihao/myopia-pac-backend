package com.wupol.myopia.device.core.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.device.core.domain.mapper.ScreeningOrgBindDeviceReportMapper;
import com.wupol.myopia.device.core.domain.model.ScreeningOrgBindDeviceReport;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 筛查机构绑定设备报告模板Service
 *
 * @author Simple4H
 */
@Service
public class ScreeningOrgBindDeviceReportService extends BaseService<ScreeningOrgBindDeviceReportMapper, ScreeningOrgBindDeviceReport> {

    /**
     * 获取模板Id获取筛查机构
     *
     * @param templateId 模板Id
     * @return List<ScreeningOrgBindDeviceReport>
     */
    public List<ScreeningOrgBindDeviceReport> getOrgByTemplateId(Integer templateId) {
        return baseMapper.getByTemplateId(templateId);
    }
}
