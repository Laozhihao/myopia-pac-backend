package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.OrgScreeningCountDTO;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查任务关联的机构表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningTaskOrgMapper extends BaseMapper<ScreeningTaskOrg> {

    List<OrgScreeningCountDTO> countScreeningTimeByOrgId();

    List<ScreeningTaskOrgDTO> selectHasTaskInPeriod(@Param("orgId") Integer orgId, @Param("param") ScreeningTaskQuery screeningTaskQuery);

    List<ScreeningTaskOrgDTO> selectVoListByScreeningTaskId(@Param("screeningTaskId") Integer screeningTaskId);

    List<ScreeningTaskOrg> getByTaskId(@Param("taskId") Integer taskId);

    ScreeningTaskOrg getOneByTaskIdAndOrgId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);
}
