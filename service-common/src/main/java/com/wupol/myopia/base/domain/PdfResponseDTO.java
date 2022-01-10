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

    private String uuid;

    private String s3key;

    private String url;

    private Boolean status;

    private String message;

    private String bucket;
}
