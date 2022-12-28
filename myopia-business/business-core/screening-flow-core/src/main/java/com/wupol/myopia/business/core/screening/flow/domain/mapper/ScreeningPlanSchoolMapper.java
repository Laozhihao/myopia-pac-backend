package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 筛查计划关联的学校表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningPlanSchoolMapper extends BaseMapper<ScreeningPlanSchool> {

    List<SchoolScreeningCountDTO> countScreeningTime();

    List<ScreeningPlanSchoolDTO> selectVoListByPlanId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolName") String schoolName);

    List<ScreeningPlanSchool> selectHasPlanInPeriod(@Param("param") ScreeningPlanQueryDTO screeningPlanQuery);

    List<ScreeningPlanSchool> countBySchoolId(@Param("schoolId") Integer schoolId);

    Integer updateSchoolNameBySchoolId(@Param("schoolId") Integer schoolId, @Param("schoolName") String schoolName);

    List<ScreeningPlanSchool> getBySchoolId(@Param("schoolId") Integer schoolId);

    ScreeningPlanSchool getOneByPlanIdAndSchoolId(@Param("planId") Integer planId, @Param("schoolId") Integer schoolId);

    List<ScreeningPlanSchool> getByPlanId(@Param("planId") Integer planId);

    Integer deleteByPlanIdAndExcludeSchoolIds(@Param("planId") Integer planId, @Param("schoolIds") List<Integer> schoolIds);

    List<ScreeningPlanSchool> getBySchoolIds(@Param("schoolIds") List<Integer> schoolIds);

    List<ScreeningPlanSchool> getScreeningSchoolsByOrgId(@Param("screeningOrgId") Integer screeningOrgId, @Param("releaseStatus") Integer releaseStatus, @Param("currentDate") Date currentDate);

    List<ScreeningPlanSchool> getByPlanIds(@Param("screeningPlanIds") List<Integer> screeningPlanIds);

    List<Integer> getByPlanIdNotInSchoolIds(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolIds") List<Integer> schoolIds);

    Long getCurrentMaxScreeningCode();

    IPage<ScreeningListResponseDTO> getReleasePlanSchoolPageBySchoolId(@Param("page") Page<?> page, @Param("schoolId") Integer schoolId);

    IPage<ScreeningListResponseDTO> listByCondition(@Param("page") Page<?> page, @Param("param") ScreeningPlanListDTO screeningPlanListDTO);

    /**
     * 获取指定学校及筛查类型信息
     * @param schoolId
     * @param screeningType
     * @return
     */
    ScreeningPlanSchool getLastBySchoolIdAndScreeningType(@Param("schoolId") Integer schoolId, @Param("screeningType") Integer screeningType);

    List<ScreeningPlanSchool> listBySchoolIdAndOrgId(@Param("schoolId") Integer schoolId,@Param("orgId") Integer orgId, @Param("screeningType") Integer screeningType,@Param("currentDate") Date currentDate);

    ScreeningPlanSchool getReleasePlanByScreeningOrgIdAndSchoolId(@Param("screeningOrgId") Integer screeningOrgId, @Param("releaseStatus") Integer releaseStatus, @Param("currentDate") Date currentDate, @Param("schoolId") Integer schoolId, @Param("channel") Integer channel);
}
