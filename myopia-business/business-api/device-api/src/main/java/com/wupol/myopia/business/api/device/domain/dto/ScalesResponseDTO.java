package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 体脂称返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class ScalesResponseDTO {
    /** 失败 */
    public static final String FAILED = "0";
    /** 成功 */
    public static final String SUCCESS = "1";

    /**
     * 状态: 1=成功，-1、0=失败(如返回为 1 即成功，设备将不再上传。否则设备将缓存，下次将继续上传)
     */
    private String retCode;

    /**
     * 状态说明
     */
    private String msg;

    public ScalesResponseDTO(String retCode, String msg) {
        this.retCode = retCode;
        this.msg = msg;
    }
}
