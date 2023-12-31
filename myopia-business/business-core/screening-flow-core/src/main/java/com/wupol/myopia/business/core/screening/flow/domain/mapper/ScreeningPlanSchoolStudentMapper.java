package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
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

    List<GradeClassesDTO> selectSchoolGradeVoByPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId);

    IPage<ScreeningStudentDTO> selectPageByQuery(@Param("page") Page<?> page, @Param("param") ScreeningStudentQueryDTO query);

    List<ScreeningPlanSchoolStudent> selectByIdCards(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("idCards") List<String> idCards);

    List<ScreeningStudentDTO> selectByGradeAndClass(@Param("screeningPlanId") Integer screeningPlanId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    List<ScreeningStudentDTO> selectBySchoolGradeAndClass(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId, @Param("studentIds") List<Integer> studentIds);

    List<StudentScreeningInfoWithResultDTO> selectStudentInfoWithResult(@Param("data") ScreeningResultSearchDTO screeningResultSearchDTO);

    List<ScreeningPlanSchoolStudent> getReleasePlanStudentByStudentId(@Param("studentId") Integer studentId);

    IPage<ScreeningPlanSchoolStudent> selectPlanStudentListByPage(@Param("page") Page<?> page, @Param("param") ScreeningStudentQueryDTO query);

    List<ScreeningPlanSchoolStudent> findByPlanId(Integer planId);

    List<ScreeningPlanSchoolStudent> findByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    Integer countByPlanId(Integer planId);

    Integer countBySchoolIdAndNoticeId(@Param("schoolId") Integer schoolId, @Param("noticeId") Integer noticeId);

    List<SchoolCountDO> getSchoolCountByPlanId(Integer screeningPlanId);

    Integer deleteByPlanIdAndExcludeSchoolIds(@Param("screeningPlanId") Integer screeningPlanId, @Param("excludeSchoolIds") List<Integer> excludeSchoolIds);

    List<ScreeningPlanSchoolStudent> getByScreeningCodes(@Param("screeningCodes") List<Long> screeningCodes, @Param("planId") Integer planId);

    List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId);

    List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeIdAndClassId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    ScreeningPlanSchoolStudent getOneByStudentName(String name);

    ScreeningPlanSchoolStudent getLastByStudentId(@Param("studentId") Integer studentId);

    List<ScreeningStudentDTO> getScreeningNoticeResultStudent(@Param("planIds") List<Integer> planIds, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId, @Param("planStudentId") List<Integer> planStudentId, @Param("planStudentName") String planStudentName, @Param("isFilterDoubleScreen") Boolean isFilterDoubleScreen);

    List<GradeClassesDTO> getByPlanIdAndSchoolIdAndId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId, @Param("ids") List<Integer> ids);

    List<ScreeningPlanSchoolStudent> getByIdCardAndPassport(@Param("idCard") String idCard, @Param("passport") String passport, @Param("id") Integer id);

    void deleteByStudentIds(@Param("studentIds") List<Integer> studentIds);

    List<ScreeningPlanSchoolStudent> getByNePlanId(@Param("planId") Integer planId, @Param("studentIds") List<Integer> studentIds);

    ScreeningPlanSchoolStudent getOneByPlanId(@Param("planId") Integer planId);

    List<GradeClassesDTO> getGradeByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    List<Integer> findSchoolIdsByPlanId(Integer planId);

    List<ScreeningPlanSchoolStudent> getReviewStudentList(@Param("planId") Integer planId, @Param("orgId") Integer orgId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    List<ScreeningPlanSchoolStudent> getByPlanIdIdCardAndPassport(@Param("planId") Integer planId, @Param("idCard") String idCard, @Param("passport") String passport, @Param("id") Integer id);

    List<CommonDiseasePlanStudent> selectCommonDiseaseScreeningPlanStudent(@Param("schoolId") Integer schoolId);

    List<ScreeningPlanSchoolStudent> getByNoticeIdsAndSchoolIds(@Param("noticeIds") List<Integer> noticeIds, @Param("schoolIds") List<Integer> schoolIds);

    List<ScreeningPlanSchoolStudent> getLastByCredentialNoAndStudentName(@Param("credentialNo") String credentialNo,@Param("studentName") String studentName);

    ScreeningPlanSchoolStudent getLastByCredentialNoAndStudentIds(@Param("screeningType") Integer screeningType, @Param("planId") List<Integer> planId, @Param("studentIds") List<Integer> studentIds);

    List<ScreeningPlanSchoolStudent> getInfoByPlanId(@Param("planId") Integer planId);

    List<PlanStudentInfoDTO> findStudentBySchoolIdAndScreeningPlanIdAndSno(@Param("schoolId") Integer schoolId,@Param("planId") Integer screeningPlanId,@Param("snoList") List<String> snoList);

    List<PlanStudentInfoDTO> getByCredentials(@Param("schoolId") Integer schoolId, @Param("planId") Integer screeningPlanId, @Param("idCards") List<String> idCards, @Param("passports") List<String> passports);
}
