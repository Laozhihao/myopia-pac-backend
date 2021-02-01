package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 筛查通知表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningNoticeMapper extends BaseMapper<ScreeningNotice> {
    Integer release(Integer id);

    IPage<ScreeningNoticeVo> selectPageByQuery(@Param("page")IPage<?> page, @Param("param")ScreeningNoticeQuery query);

    List<ScreeningNotice> selectByTimePeriods(@Param("param") ScreeningNotice query);
}
