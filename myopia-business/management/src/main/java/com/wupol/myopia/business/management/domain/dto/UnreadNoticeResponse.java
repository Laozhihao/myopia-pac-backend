package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Notice;
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

    private Integer total;

    private List<Notice> details;
}
