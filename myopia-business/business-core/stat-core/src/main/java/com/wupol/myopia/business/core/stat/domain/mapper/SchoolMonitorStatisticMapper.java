package com.wupol.myopia.business.core.stat.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校某次筛查计划统计监控监测情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-02-24
 */
public interface SchoolMonitorStatisticMapper extends BaseMapper<SchoolMonitorStatistic> {
    Integer batchSaveOrUpdate(@Param("list") List<SchoolMonitorStatistic> schoolMonitorStatistics);
}
