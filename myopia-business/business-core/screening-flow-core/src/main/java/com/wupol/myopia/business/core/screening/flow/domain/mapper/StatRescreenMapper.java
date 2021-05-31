package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import org.apache.ibatis.annotations.Param;

/**
 * @Author wulizhou
 * @Date 2021/5/20 10:11
 */
public interface StatRescreenMapper extends BaseMapper<StatRescreen> {

    int countByPlanAndSchool(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

}