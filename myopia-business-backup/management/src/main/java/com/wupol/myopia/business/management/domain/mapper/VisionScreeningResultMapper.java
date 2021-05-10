package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
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

    List<StudentScreeningCountVO> countScreeningTime();

    VisionScreeningResult getLatestResultByStudentId(@Param("studentId") Integer studentId);

    List<Integer> getHaveSrcScreeningNoticePlanIdsByTime(@Param("startTime") Date yesterdayStartTime, @Param("endTime") Date yesterdayEndTime);
    List<VisionScreeningResult> getBySchoolIdAndOrgIdAndPlanId(@Param("schoolId") Integer schoolId, @Param("orgId") Integer orgId, @Param("planId") Integer planId);

    List<VisionScreeningResult> getByStudentId(Integer studentId);

    List<VisionScreeningResult> getStudentResults();
}
