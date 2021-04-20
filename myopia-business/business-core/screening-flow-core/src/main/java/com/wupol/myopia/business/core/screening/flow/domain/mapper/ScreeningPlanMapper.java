package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 筛查通知任务或者计划表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningPlanMapper extends BaseMapper<ScreeningPlan> {

    IPage<ScreeningPlanResponseDTO> getPlanLists(@Param("page") Page<?> page, @Param("ids") List<Integer> ids);

    IPage<ScreeningPlanDTO> selectPageByQuery(@Param("page") Page<ScreeningPlan> page, @Param("param") ScreeningPlanQueryDTO query);

    Set<ScreeningPlanSchoolInfoDTO> selectSchoolInfo(Integer districtId, Integer taskId, Integer releaseStatus);

    IPage<ScreeningOrgPlanResponseDTO> getPageByOrgId(@Param("page") Page<?> page, @Param("orgId") Integer orgId);

    List<ScreeningPlan> getByOrgId(@Param("orgId") Integer orgId);

    /**
     * 查找学校的id
     *
     * @param districtIds
     * @param taskId
     * @return
     */
    Set<Integer> selectSchoolIds(Set<Integer> districtIds, Integer taskId);

    ScreeningPlan selectScreeningPlanDetailByOrgIdAndSchoolId(Integer schoolId, Integer screeningOrgId, Integer releaseStatus, Date currentDate);

    Integer countByTaskIdAndOrgId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);

    List<ScreeningPlan> getByOrgIds(@Param("orgIds") List<Integer> orgIds);

}
