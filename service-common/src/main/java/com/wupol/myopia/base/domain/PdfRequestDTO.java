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
public class PdfRequestDTO {

    private String url;

    private String output;

    private String bucket;

    private String region;

    private String keyPrefix;

    private String uuid;

    private String callbackUrl;

    private Config config;

    private Integer timeout;

    @Getter
    @Setter
    public static class Config {
        private String size;
        private Boolean displayHeaderFooter;
        private String headerTemplate;
        private String footerTemplate;
        private String margin;
    }

}
