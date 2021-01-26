package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查通知
 * @Author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningNoticeVo extends ScreeningNotice {
    /** 行政区域名 */
    private String creatorName;

}