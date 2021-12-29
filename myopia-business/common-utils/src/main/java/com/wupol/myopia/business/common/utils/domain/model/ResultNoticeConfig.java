package com.wupol.myopia.business.common.utils.domain.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 结果通知配置
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ResultNoticeConfig {

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
     * 正文内容
     */
    private String content;
}
