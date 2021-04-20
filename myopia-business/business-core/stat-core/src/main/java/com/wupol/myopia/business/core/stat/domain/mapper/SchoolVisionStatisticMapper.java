package com.wupol.myopia.business.core.stat.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校某次筛查计划统计视力情况表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface SchoolVisionStatisticMapper extends BaseMapper<SchoolVisionStatistic> {
    Integer batchSaveOrUpdate(@Param("list") List<SchoolVisionStatistic> schoolVisionStatistics);

    List<SchoolVisionStatistic> getByPlanIdsAndSchoolId(@Param("planIds") List<Integer> planIds, @Param("schoolIds") Integer schoolIds);
}
