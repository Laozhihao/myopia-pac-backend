package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地区层级某次筛查计划统计视力情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface DistrictVisionStatisticMapper extends BaseMapper<DistrictVisionStatistic> {
    Integer batchSaveOrUpdate(@Param("list") List<DistrictVisionStatistic> districtVisionStatistics);
}
