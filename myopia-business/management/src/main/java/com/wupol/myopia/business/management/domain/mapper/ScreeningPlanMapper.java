package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanResponse;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanSchoolInfoDTO;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrgPlanResponse;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanVo;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 筛查通知任务或者计划表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningPlanMapper extends BaseMapper<ScreeningPlan> {

    IPage<ScreeningPlanResponse> getPlanLists(@Param("page") Page<?> page, @Param("ids") List<Integer> ids);

    IPage<ScreeningPlanVo> selectPageByQuery(@Param("page") Page<ScreeningPlan> page, @Param("param") ScreeningPlanQuery query);

    Set<ScreeningPlanSchoolInfoDTO> selectSchoolInfo(Integer districtId, Integer taskId, Integer releaseStatus);

    IPage<ScreeningOrgPlanResponse> getPageByOrgId(@Param("page") Page<?> page, @Param("orgId") Integer orgId);

    List<ScreeningPlan> getByOrgId(@Param("orgId") Integer orgId);

    /**
     * 查找学校的id
     * @param districtIds
     * @param taskId
     * @return
     */
    Set<Integer> selectSchoolIds(Set<Integer> districtIds, Integer taskId);
}
