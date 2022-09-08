package com.wupol.myopia.business.api.hospital.app.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
    private String json;

    private String name;

    // 大小
    private long length;

    // 文件 zip压缩包
    @JSONField(serialize = false)
    private MultipartFile pic;
}
