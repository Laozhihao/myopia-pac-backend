package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import com.wupol.myopia.business.core.device.domain.vos.DeviceReportTemplateVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构绑定设备报告模板表Mapper接口
 *
 * @author Simple4H
 */
public interface ScreeningOrgBindDeviceReportMapper extends BaseMapper<ScreeningOrgBindDeviceReport> {

    List<ScreeningOrgBindDeviceReport> getByTemplateId(@Param("templateId") Integer templateId);

    List<ScreeningOrgBindDeviceReport> getByTemplateId11(@Param("templateId") Integer templateId);

    void updateTemplateByOrgId(@Param("templateId") Integer templateId, @Param("orgId") Integer orgId);

    void orgBindReportTemplate(@Param("templateId") Integer templateId, @Param("orgId") Integer orgId, @Param("name") String name);

    List<DeviceReportTemplateVO> getByOrgIds(@Param("orgIds") List<Integer> orgIds);
}
