package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSGCDTO;
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

    List<VisionScreeningResult> getByStudentIds( @Param("planId") Integer planId,@Param("studentIds") List<Integer> studentIds);

    /**
    * @Description: 获取筛查计划下的学校
    * @Param: [planId, orgId]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSchoolDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/20
    */
    List<ScreeningSGCDTO> getSchoolInforByPlanIdAndOrgId(@Param("planId") Integer planId, @Param("orgId") Integer orgId);

    /**
     * @Description: 获取筛查计划下学校的年级
     * @Param: [planId, orgId]
     * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSchoolDTO>
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/20
     */
    List<ScreeningSGCDTO> getGradeInforByPlanIdAndOrgId(@Param("planId") Integer planId, @Param("orgId") Integer orgId, @Param("schoolId") Integer schoolId);

    /**
     * @Description: 获取筛查计划下的班级
     * @Param: [planId, orgId]
     * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSchoolDTO>
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/20
     */
    List<ScreeningSGCDTO> getClassInforByPlanIdAndOrgId(@Param("planId") Integer planId, @Param("orgId") Integer orgId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId);





    List<Integer> getBySchoolIdPlanId(@Param("planId") Integer planId);
}
