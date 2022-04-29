package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface VisionScreeningResultMapper extends BaseMapper<VisionScreeningResult> {

    List<Integer> getSchoolIdByTaskId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);

    List<Integer> getCreateUserIdByPlanIdAndOrgId(@Param("planId") Integer planId, @Param("orgId") Integer orgId);

    List<StudentScreeningCountDTO> countScreeningTime();

    VisionScreeningResult getLatestResultByStudentId(@Param("studentId") Integer studentId);

    List<Integer> getHaveSrcScreeningNoticePlanIdsByTime(@Param("startTime") Date yesterdayStartTime, @Param("endTime") Date yesterdayEndTime);

    List<VisionScreeningResult> getBySchoolIdAndOrgIdAndPlanId(@Param("schoolId") Integer schoolId, @Param("orgId") Integer orgId, @Param("planId") Integer planId);

    List<VisionScreeningResult> getByStudentId(Integer studentId);

    List<VisionScreeningResult> getStudentResults();

    VisionScreeningResult getByPlanStudentId(@Param("planStudentId") Integer planStudentId);

    List<VisionScreeningResult> getByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);

    VisionScreeningResult getLatestByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);

    List<Integer> getBySchoolIdPlanId(@Param("planId") Integer planId);

    List<VisionScreeningResult> getByStudentIdsAndPlanId(@Param("planId") Integer planId, @Param("studentIds") List<Integer> studentIds, @Param("isDoubleScreen") Integer isDoubleScreen);

    List<VisionScreeningResult> getByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    List<VisionScreeningResult> getByPlanId(@Param("planId") Integer planId);

    List<VisionScreeningResult> getByStudentIds(@Param("studentIds") List<Integer> studentIds);

    int selectScreeningResultByDistrictIdAndTaskId(@Param("districtIds") List<Integer> districtId, @Param("taskIds") List<Integer> taskIds);

    VisionScreeningResult getIsDoubleScreen(@Param("screeningPlanSchoolStudentId")Integer screeningPlanSchoolStudentId,@Param("planId") Integer planId,@Param("screeningType") Integer screeningType);

    List<VisionScreeningResult> getRescreenBySchoolIds(@Param("planId") Integer planId, @Param("schoolIds") List<Integer> schoolIds);

    IPage<VisionScreeningResult> getByStudentIdWithPage(@Param("page") Page<?> page, @Param("studentId") Integer studentId);

    List<VisionScreeningResult> getIsDoubleScreeningResult(List<Integer> planIds, Integer screeningPlanSchoolStudentId,boolean isDoubleScreen);

    List<VisionScreeningResult> getFirstByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);
}
