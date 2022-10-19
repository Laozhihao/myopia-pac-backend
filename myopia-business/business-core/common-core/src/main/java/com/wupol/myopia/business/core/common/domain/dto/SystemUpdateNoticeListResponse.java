package com.wupol.myopia.business.core.common.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.SystemUpdateNotice;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 系统更新列表返回
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SystemUpdateNoticeListResponse extends SystemUpdateNotice {

    /**
     * 平台
     */
    private List<Integer> platform;
}
