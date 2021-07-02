package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.ConfigurationReportRequestDTO;
import com.wupol.myopia.business.core.device.domain.mapper.ScreeningOrgBindDeviceReportMapper;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import com.wupol.myopia.business.core.device.domain.vos.DeviceReportTemplateVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 获取模板Id获取筛查机构(11条)
     *
     * @param templateId 模板Id
     * @param limit      条数
     * @return List<ScreeningOrgBindDeviceReport>
     */
    public List<ScreeningOrgBindDeviceReport> getOrgByTemplateIdLimit(Integer templateId, Integer limit) {
        return baseMapper.getByTemplateIdLimit(templateId, limit);
    }

    /**
     * 通过OrgId修改TemplateId
     *
     * @param requestDTO 请求入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void configurationReport(ConfigurationReportRequestDTO requestDTO) {
        baseMapper.updateTemplateByOrgId(requestDTO.getTemplateId(), requestDTO.getScreeningOrgId());
    }

    /**
     * 筛查机构绑定报告模板
     *
     * @param templateId 模板Id
     * @param orgId      筛查机构Id
     * @param name       筛查机构名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void orgBindReportTemplate(Integer templateId, Integer orgId, String name) {
        baseMapper.orgBindReportTemplate(templateId, orgId, name);
    }

    /**
     * 通过筛查机构Id获取模板
     *
     * @param orgIds 筛查机构Ids
     * @return 模板列表
     */
    public List<DeviceReportTemplateVO> getByOrgIds(List<Integer> orgIds) {
        return baseMapper.getByOrgIds(orgIds);
    }
}
