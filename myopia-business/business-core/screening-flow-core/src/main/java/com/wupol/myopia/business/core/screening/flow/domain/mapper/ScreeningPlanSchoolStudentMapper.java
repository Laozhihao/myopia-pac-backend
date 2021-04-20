package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.management.domain.query.StudentQueryDTO;
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

    IPage<StudentDTO> selectPageByQuery(@Param("page") Page<StudentDTO> page, @Param("param") StudentQueryDTO query);

    List<ScreeningPlanSchoolStudent> selectByIdCards(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("idCards") List<String> idCards);

    List<StudentDTO> selectByGradeAndClass(@Param("screeningPlanId") Integer screeningPlanId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    List<StudentScreeningInfoWithResultDTO> selectStudentInfoWithResult(@Param("data") ScreeningResultSearchDTO screeningResultSearchDTO);

    List<ScreeningPlanSchoolStudent> findByStudentId(@Param("studentId") Integer studentId);

    List<ScreeningPlanSchoolStudent> findByPlanId(Integer planId);

    List<ScreeningPlanSchoolStudent> findByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    Integer countByPlanId(Integer planId);

    Integer countBySchoolIdAndNoticeId(@Param("schoolId") Integer schoolId, @Param("noticeId") Integer noticeId);

    Integer deleteByPlanIdAndExcludeSchoolIds(@Param("screeningPlanId") Integer screeningPlanId, @Param("excludeSchoolIds") List<Integer> excludeSchoolIds);
}
