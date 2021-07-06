package com.wupol.myopia.business.api.screening.app.domain.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname DeviceUploadDto
 * @Description 设备上传(目前针对vs666设备)实体
 * @Date 2021/7/5 2:59 下午
 * @Author Jacob
 *
 * @Version
 */
@Data
public class DeviceUploadDto implements Serializable {

    @NotBlank
    @Length(max = 32)
    private String imei;

    @NotBlank
    @Length(max = 65535)
    private String data;


}
