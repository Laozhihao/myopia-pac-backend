package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningSchoolCount;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ScreeningPlanCount;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface VisionScreeningResultMapper extends BaseMapper<VisionScreeningResult> {

    List<Integer> getSchoolIdByTaskId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);

    List<Integer> getCreateUserIdByPlanId(@Param("planId") Integer planId);

    List<StudentScreeningCountDTO> countScreeningTime(@Param("studentIds") List<Integer> studentIds);

    VisionScreeningResult getLatestResultOfReleasePlanByStudentId(@Param("studentId") Integer studentId);

    List<Integer> getHaveSrcScreeningNoticePlanIdsByTime(@Param("startTime") Date yesterdayStartTime, @Param("endTime") Date yesterdayEndTime);

    List<SchoolCountDO> getSchoolCountByPlanIdAndSchoolIds(@Param("planId") Integer planId, Integer isDoubleScreen, @Param("schoolIds") Set<Integer> schoolIds);

    List<VisionScreeningResult> getReleasePlanResultByStudentId(Integer studentId);

    List<VisionScreeningResult> getStudentResults();

    VisionScreeningResult getByPlanStudentId(@Param("planStudentId") Integer planStudentId);

    List<VisionScreeningResult> getByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);

    VisionScreeningResult getLatestByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);

    List<Integer> getBySchoolIdPlanId(@Param("planId") Integer planId);

    List<VisionScreeningResult> getByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    List<VisionScreeningResult> getByPlanId(@Param("planId") Integer planId);

    List<VisionScreeningResult> getByStudentIds(@Param("studentIds") List<Integer> studentIds);

    int selectScreeningResultByDistrictIdAndTaskId(@Param("districtIds") List<Integer> districtId, @Param("taskIds") List<Integer> taskIds);

    List<VisionScreeningResult> getRescreenBySchoolIds(@Param("planId") Integer planId, @Param("schoolIds") List<Integer> schoolIds);

    List<VisionScreeningResult> getFirstByPlanStudentIds(@Param("planStudentIds") List<Integer> planStudentIds);

    IPage<VisionScreeningResultDTO> getByStudentIdWithPage(@Param("page") Page<?> page, @Param("studentId") Integer studentId,@Param("schoolId") Integer schoolId, @Param("needFilterAbolishPlan") boolean needFilterAbolishPlan);

    List<ScreeningSchoolCount> countScreeningSchoolByTaskId(@Param("taskId") Integer taskId);

    List<VisionScreeningResult> getByIdsAndCreateTimeDesc(@Param("ids") List<Integer> ids);

    List<StudentScreeningCountDTO> getVisionScreeningCountBySchoolId(Integer schoolId);

    VisionScreeningResult getOneByPlanIdsOrderByUpdateTimeDesc(@Param("planId") Set<Integer> planId);

    List<ScreeningPlanCount> getCountByPlanId(@Param("planIds")List<Integer> planIds);

}
