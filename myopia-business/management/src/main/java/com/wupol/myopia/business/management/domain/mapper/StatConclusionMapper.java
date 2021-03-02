package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;

import java.util.Date;
import java.util.List;

/**
 * 筛查数据结论Mapper接口
 *
 * @Author Jacob
 * @Date 2021-02-22
 */
public interface StatConclusionMapper extends BaseMapper<StatConclusion> {
    /**
     * 获取行政区域内最后一条数据
     */
    StatConclusion selectLastOne(List<Integer> districtIds);

    /**
     * 获取行政区域以及日期范围内的数据
     * @param districtIds 行政区域ID列表
     * @param startDate 起始日期
     * @param endDate 终止日期
     * @return
     */
    List<StatConclusion> listByDateRange(List<Integer> districtIds, Date startDate, Date endDate);

    /**
     * 获取政府通知下的区域范围的数据
     * @param noticeId 政府通知ID
     * @param districtIds 行政区域ID列表
     * @return
     */
    List<StatConclusion> listByNoticeId(int noticeId, List<Integer> districtIds);
}
