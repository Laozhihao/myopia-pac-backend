package com.wupol.myopia.base.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * HTML转PDF
 *
 * @author Simple4H
 */
@Getter
@Setter
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
