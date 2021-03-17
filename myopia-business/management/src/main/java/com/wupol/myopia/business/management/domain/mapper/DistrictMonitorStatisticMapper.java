package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地区层级某次筛查计划统计监控监测情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface DistrictMonitorStatisticMapper extends BaseMapper<DistrictMonitorStatistic> {
    Integer batchSaveOrUpdate(@Param("list") List<DistrictMonitorStatistic> districtMonitorStatistics);
}
