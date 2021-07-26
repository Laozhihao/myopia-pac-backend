package com.wupol.myopia.business.api.device.domain.dto;

import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname DeviceUploadDto
 * @Description 设备上传(目前针对vs666设备)实体
 * @Date 2021/7/5 2:59 下午
 * @Author Jacob
 *
 * @Version
 */
@Data
public class DeviceUploadDTO implements Serializable {

    /**
     * 设备编号
     */
    @NotBlank
    @Length(max = 32)
    private String imei;

    /**
     * 数据
     */
    private List<DeviceScreenDataDTO> data;

}
