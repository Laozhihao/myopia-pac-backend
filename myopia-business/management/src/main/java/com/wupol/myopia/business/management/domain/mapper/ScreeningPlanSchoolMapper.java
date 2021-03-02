package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.vo.SchoolScreeningCountVO;

import java.util.List;

import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查计划关联的学校表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningPlanSchoolMapper extends BaseMapper<ScreeningPlanSchool> {

    List<SchoolScreeningCountVO> countScreeningTime();

    List<ScreeningPlanSchoolVo> selectVoListByPlanId(@Param("screeningPlanId") Integer screeningPlanId);

    List<ScreeningPlanSchool> selectHasPlanInPeriod(@Param("param") ScreeningPlanQuery screeningPlanQuery);

    List<ScreeningPlanSchool> countBySchoolId(@Param("schoolId") Integer schoolId);
}
