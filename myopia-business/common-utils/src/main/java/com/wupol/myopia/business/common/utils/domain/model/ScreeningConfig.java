package com.wupol.myopia.business.common.utils.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.common.utils.domain.dto.DeviceGrantedDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 筛查类型的配置
 * @author Chikong
 * @date 2022-08-26
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScreeningConfig implements Serializable {
    /** 筛查类型 */
    private List<Integer> screeningTypeList;
    /** 渠道 */
    private String channel;
    /** 视力表型号 */
    private List<String> visionDeviceNoList;
    /** 视筛仪型号 */
    private List<String> visionScreeningDeviceNoList;
    /** 电脑验光仪型号 */
    private List<String> computerDeviceNoList;
    /** 开通的检查项目列表 */
    private List<String> medicalProjectList;
    /** 已授权的设备列表 */
    private List<DeviceGrantedDTO> grantedDeviceList;
}
