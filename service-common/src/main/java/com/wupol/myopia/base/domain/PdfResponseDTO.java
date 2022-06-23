package com.wupol.myopia.base.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * HTML转PDF
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Data
public class PdfResponseDTO {

    /**
     * UUID
     */
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
