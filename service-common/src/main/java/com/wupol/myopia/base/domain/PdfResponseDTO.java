package com.wupol.myopia.base.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * HTML转PDF
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Data
public class PdfResponseDTO implements Serializable {

    /**
     * UUID
     */
    @NotBlank(message = "uuid不能为空")
    private String uuid;

    /**
     * s3key
     */
    private String s3key;

    /**
     * URL
     */
    private String url;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * 消息
     */
    private String message;

    /**
     * bucket
     */
    private String bucket;
}
