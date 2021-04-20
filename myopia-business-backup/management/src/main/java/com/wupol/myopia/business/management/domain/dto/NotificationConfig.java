package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 筛查机构-告知书配置
 *
 * @author Alix
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationConfig {

    /**
     * 二维码文件地址
     */
    private Integer qrCodeFileId;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 称呼，“亲爱的家长朋友：”
     */
    private String call;

    /**
     * 问好，“您好：”
     */
    private String greetings;

    /**
     * 正文内容
     */
    private String content;

}
