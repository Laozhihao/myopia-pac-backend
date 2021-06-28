package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构绑定设备报告模板表Mapper接口
 *
 * @author Simple4H
 */
public interface ScreeningOrgBindDeviceReportMapper extends BaseMapper<ScreeningOrgBindDeviceReport> {

    List<ScreeningOrgBindDeviceReport> getByTemplateId(@Param("templateId") Integer templateId);
}
