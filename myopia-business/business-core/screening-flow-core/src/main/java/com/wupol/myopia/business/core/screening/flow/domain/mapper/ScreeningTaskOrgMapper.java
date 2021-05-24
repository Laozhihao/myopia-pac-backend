package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.dto.OrgScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
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

    List<ScreeningTaskOrgDTO> selectHasTaskInPeriod(@Param("orgId") Integer orgId, @Param("param") ScreeningTaskQueryDTO screeningTaskQuery);

    List<ScreeningTaskOrgDTO> selectVoListByScreeningTaskId(@Param("screeningTaskId") Integer screeningTaskId);

    List<ScreeningTaskOrg> getByTaskId(@Param("taskId") Integer taskId);

    ScreeningTaskOrg getOneByTaskIdAndOrgId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);
}
