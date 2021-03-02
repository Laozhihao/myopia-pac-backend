package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Notice;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
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
}
