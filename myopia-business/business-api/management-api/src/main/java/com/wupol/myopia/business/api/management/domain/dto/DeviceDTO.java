package com.wupol.myopia.business.api.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.api.management.validator.DeviceAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.DeviceUpdateValidatorGroup;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.query.DeviceQuery;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

/**
 * 设备
 *
 * @Author HaoHao
 * @Date 2021/6/28
 **/
@Data
public class DeviceDTO {

    /**
     * 设备表id
     */
    @NotNull(message = "设备ID不能为空", groups = {DeviceUpdateValidatorGroup.class})
    private Integer id;

    /**
     * 设备唯一id
     */
    @NotBlank(message = "设备唯一标志码不能为空", groups = {DeviceUpdateValidatorGroup.class, DeviceAddValidatorGroup.class})
    @Length(max = 14, message = "设备唯一标志码超长")
    private String deviceSn;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编号不能为空", groups = {DeviceUpdateValidatorGroup.class, DeviceAddValidatorGroup.class})
    @Length(max = 20, message = "设备编号超长")
    private String deviceCode;

    /**
     * 销售名字
     */
    private String salespersonName;

    /**
     * 销售电话
     */
    @Pattern(regexp = RegularUtils.REGULAR_MOBILE, message = "手机号码格式错误")
    private String salespersonPhone;

    /**
     * 绑定机构id
     */
    @NotNull(message = "绑定机构不能为空", groups = {DeviceUpdateValidatorGroup.class, DeviceAddValidatorGroup.class})
    private Integer bindingScreeningOrgId;

    /**
     * 绑定机构名称
     */
    private String bindingScreeningOrgName;

    /**
     * 客户名字
     */
    private String customerName;

    /**
     * 客户电话
     */
    @Pattern(regexp = RegularUtils.REGULAR_MOBILE, message = "手机号码格式错误")
    private String customerPhone;

    /**
     * 销售时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date saleDate;

    /**
     * 开始-销售时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startSaleDate;

    /**
     * 结束-销售时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endSaleDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 筛查机构ID集
     */
    private List<Integer> screeningOrgIds;

    public Device toDevice() {
        Device device = new Device();
        BeanUtils.copyProperties(this, device);
        return device;
    }

    public DeviceQuery toDeviceQuery() {
        DeviceQuery deviceQuery = new DeviceQuery();
        BeanUtils.copyProperties(this, deviceQuery);
        return deviceQuery;
    }
}
