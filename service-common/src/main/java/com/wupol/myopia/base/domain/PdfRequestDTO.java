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

    /**
     * url需要生成PDF的URL地址
     */
    private String url;

    /**
     * 文件名
     */
    private String output;

    /**
     * bucket
     */
    private String bucket;

    /**
     * region
     */
    private String region;

    /**
     * keyPrefix
     */
    private String keyPrefix;

    /**
     * uuid
     */
    private String uuid;

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 打印配置
     */
    private Config config;

    /**
     * 超时时间
     */
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
