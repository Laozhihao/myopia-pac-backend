package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author wulizhou
 * @Date 2021/5/20 10:11
 */
public interface StatRescreenMapper extends BaseMapper<StatRescreen> {

    int countByPlanAndSchool(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("createTime") Date createTime);

    List<SchoolCountDO> getSchoolCountByPlanIdAndSchoolIds(@Param("planId") Integer planId, @Param("schoolIds") Set<Integer> schoolIds, @Param("createTime") Date createTime);

    int deleteByScreeningTime(@Param("screeningTime") Date screeningTime);

    List<Date> getSchoolDate(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("createTime") Date createTime);

    List<StatRescreen> getByPlanAndSchool(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("screeningTime") Date screeningTime);

}