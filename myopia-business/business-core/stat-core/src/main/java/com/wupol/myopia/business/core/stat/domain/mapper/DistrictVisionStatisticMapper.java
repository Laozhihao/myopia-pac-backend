package com.wupol.myopia.business.core.stat.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 地区层级某次筛查计划统计视力情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface DistrictVisionStatisticMapper extends BaseMapper<DistrictVisionStatistic> {
    Integer batchSaveOrUpdate(@Param("list") List<DistrictVisionStatistic> districtVisionStatistics);

    Set<Integer> getDistrictIdByNoticeId(@Param("noticeId") Integer noticeId);
}
