package com.wupol.myopia.business.core.device.domain.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.device.domain.model.Device;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021/6/29
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceQuery extends Device {
    /**
     * 筛查机构ID集
     */
    private List<Integer> screeningOrgIds;

    /**
     * 开始-销售时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startSaleDate;

    /**
     * 结束-销售时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date endSaleDate;
}
