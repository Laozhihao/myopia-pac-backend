package com.wupol.myopia.business.core.device.domain.dto;

import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import lombok.Data;
import lombok.ToString;

/**
 * @Author wulizhou
 * @Date 2021/6/29 11:49
 */
@Data
@ToString(callSuper = true)
public class DeviceScreeningDataAndOrgDTO extends DeviceScreeningData {

    private String screeningOrgName;

}
