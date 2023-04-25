package com.wupol.myopia.business.common.utils.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 筛查机构-告知书配置
 *
 * @author Alix
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationConfig implements Serializable {

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

    /**
     * 关注公众号流程指引
     */
    private String processGuidance;

}
