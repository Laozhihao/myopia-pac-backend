package com.wupol.myopia.business.api.device.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 请求入参
 *
 * @author Simple4H
 */
@Getter
@Setter
public class DeviceRequestDTO {

    /**
     * 基础数据
     *
     * @see DicomDTO
     */
    @NotBlank(message = "基础数据数据不能为空")
    private String json;

    /**
     * 名称
     */
    private String name;

    /**
     * 大小
     */

    private long length;

    /**
     * 文件 zip压缩包
     */
    @JSONField(serialize = false)
    @NotNull(message = "文件不能为空")
    private MultipartFile pic;
}
