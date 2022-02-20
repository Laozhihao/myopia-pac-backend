package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
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

    IPage<ScreeningStudentDTO> selectPageByQuery(@Param("page") Page<ScreeningStudentDTO> page, @Param("param") ScreeningStudentQueryDTO query);

    List<ScreeningStudentDTO> selectListByQuery(@Param("param") ScreeningStudentQueryDTO query);

    List<ScreeningPlanSchoolStudent> selectByIdCards(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("idCards") List<String> idCards);

    List<ScreeningStudentDTO> selectByGradeAndClass(@Param("screeningPlanId") Integer screeningPlanId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    List<StudentScreeningInfoWithResultDTO> selectStudentInfoWithResult(@Param("data") ScreeningResultSearchDTO screeningResultSearchDTO);

    List<ScreeningPlanSchoolStudent> findByStudentId(@Param("studentId") Integer studentId);

    IPage<ScreeningPlanSchoolStudent> selectPlanStudentListByPage(@Param("page") Page<?> page, @Param("param") ScreeningStudentQueryDTO query);

    List<ScreeningPlanSchoolStudent> findByPlanId(Integer planId);

    List<ScreeningPlanSchoolStudent> findByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    Integer countByPlanId(Integer planId);

    Integer countBySchoolIdAndNoticeId(@Param("schoolId") Integer schoolId, @Param("noticeId") Integer noticeId);

    Integer deleteByPlanIdAndExcludeSchoolIds(@Param("screeningPlanId") Integer screeningPlanId, @Param("excludeSchoolIds") List<Integer> excludeSchoolIds);

    List<ScreeningPlanSchoolStudent> getByScreeningCodes(@Param("screeningCodes") List<Long> screeningCodes, @Param("planId") Integer planId);

    List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId);

    List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeIdAndClassId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    ScreeningPlanSchoolStudent getOneByStudentName(String name);

    ScreeningPlanSchoolStudent getLastByStudentId(@Param("studentId") Integer studentId);

    List<ScreeningPlanSchoolStudent> getByCondition(@Param("condition") String condition, @Param("name") String name);

    List<ScreeningStudentDTO> getScreeningNoticeResultStudent(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId, @Param("planStudentId") List<Integer> planStudentId,@Param("planStudentName") String planStudentName);

    List<GradeClassesDTO> getByPlanIdAndSchoolIdAndId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("ids") List<Integer> ids);
}
