package com.wupol.myopia.business.core.stat.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 某个地区层级最新统计的重点视力对象情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface DistrictAttentiveObjectsStatisticMapper extends BaseMapper<DistrictAttentiveObjectsStatistic> {

    Integer batchSaveOrUpdate(@Param("list") List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics);
}
