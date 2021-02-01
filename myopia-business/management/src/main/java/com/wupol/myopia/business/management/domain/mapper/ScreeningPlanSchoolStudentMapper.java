package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 参与筛查计划的学生表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningPlanSchoolStudentMapper extends BaseMapper<ScreeningPlanSchoolStudent> {

    List<GradeClassesDTO> selectSchoolGradeVoByPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId);
}
