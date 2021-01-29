package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查通知任务或者计划表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningPlanMapper extends BaseMapper<ScreeningPlan> {

    IPage<ScreeningPlan> getPlanLists(@Param("page") Page<?> page, @Param("ids") List<Integer> ids);

    ScreeningPlan selectScreeningPlanDetailByOrgIdAndSchoolId(Integer schoolId, Integer screeningOrgId, Integer releaseStatus, Long currentTimestamp);

    /**
     * 查找当前机构的未完成的说有学校id
     * @param screeningOrgId
     * @param releaseStatus
     * @param currentTimestamp
     * @return
     */
    List<Long> selectScreeningSchoolIds(Integer screeningOrgId, Integer releaseStatus, Long currentTimestamp);

}
