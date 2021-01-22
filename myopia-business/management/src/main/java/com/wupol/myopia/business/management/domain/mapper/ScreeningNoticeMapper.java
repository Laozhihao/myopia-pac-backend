package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 筛查通知表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningNoticeMapper extends BaseMapper<ScreeningNotice> {
    Integer release(Integer id);
}
