package com.wupol.myopia.business.core.device.domain.dto;

import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/6/28 18:29
 */
@Data
public class DeviceScreeningDataQueryDTO extends DeviceScreeningData {

    /**
     * 筛查机构
     */
    private String screeningOrgNameSearch;
    /**
     * id
     */
    private String patientIdSearch;
    /**
     * 设备唯一号
     */
    private String deviceSnSearch;
    /**
     * 单位
     */
    private String patientOrgSearch;

    private Date screeningTimeStart;
    private Date screeningTimeEnd;
    /**
     * 球镜开始
     */
    private BigDecimal sphStart;
    /**
     * 球镜结束
     */
    private BigDecimal sphEnd;

    /**
     * 柱镜开始
     */
    private BigDecimal cylStart;
    /**
     * 柱镜结束
     */
    private BigDecimal cylEnd;

    /**
     * 机构ids集
     */
    private List<Integer> screeningOrgIds;

}
