package com.wupol.myopia.device.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.device.core.domain.model.DeviceReportTemplate;

import java.util.List;

/**
 * 设备报告模板表Mapper接口
 *
 * @author Simple4H
 */
public interface DeviceReportTemplateMapper extends BaseMapper<DeviceReportTemplate> {

    List<DeviceReportTemplate> getTemplateList();

}
