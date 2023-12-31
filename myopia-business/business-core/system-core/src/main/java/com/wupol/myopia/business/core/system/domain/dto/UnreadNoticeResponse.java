package com.wupol.myopia.business.core.system.domain.dto;

import com.wupol.myopia.business.core.system.domain.model.Notice;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 获取未读消息返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UnreadNoticeResponse {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 站内信
     */
    private List<Notice> stationLetter;

    /**
     * 筛查通知
     */
    private List<Notice> screeningNotice;

    /**
     * 系统更新
     */
    private String systemUpdateNotice;
}
