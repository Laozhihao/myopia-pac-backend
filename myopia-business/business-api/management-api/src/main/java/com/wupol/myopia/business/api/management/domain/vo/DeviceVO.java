package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.device.domain.model.Device;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2021/6/28
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceVO extends Device {

    private String bindingScreeningOrgName;

    private String bindingScreeningOrgDistrictName;
}
