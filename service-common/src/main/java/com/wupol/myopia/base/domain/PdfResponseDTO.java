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

    private String s3ket;

    private String url;

    private String status;

    private String message;
}
