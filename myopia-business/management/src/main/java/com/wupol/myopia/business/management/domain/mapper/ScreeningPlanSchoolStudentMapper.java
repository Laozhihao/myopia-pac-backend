package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 参与筛查计划的学生表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningPlanSchoolStudentMapper extends BaseMapper<ScreeningPlanSchoolStudent> {

    List<GradeClassesDTO> selectSchoolGradeVoByPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId);

    IPage<StudentDTO> selectPageByQuery(@Param("page") Page<StudentDTO> page, @Param("param") StudentQuery query);

    List<ScreeningPlanSchoolStudent> selectByIdCards(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("idCards") List<String> idCards);
}