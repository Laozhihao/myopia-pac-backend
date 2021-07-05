package com.wupol.myopia.business.core.device.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.domain.model.ScreeningOrgBindDeviceReport;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 模板列表DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class DeviceTemplateListDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板的筛查机构
     */
    private List<ScreeningOrgBindDeviceReport> reports;

    /**
     * 是否查看更多
     */
    private boolean haveMore;
}
