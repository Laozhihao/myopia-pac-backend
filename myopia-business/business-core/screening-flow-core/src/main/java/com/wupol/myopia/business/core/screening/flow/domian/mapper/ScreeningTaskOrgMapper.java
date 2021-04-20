package com.wupol.myopia.business.core.screening.flow.domian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.OrgScreeningCountVO;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查任务关联的机构表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningTaskOrgMapper extends BaseMapper<ScreeningTaskOrg> {

    List<OrgScreeningCountVO> countScreeningTimeByOrgId();

    List<ScreeningTaskOrgVo> selectHasTaskInPeriod(@Param("orgId") Integer orgId, @Param("param") ScreeningTaskQuery screeningTaskQuery);

    List<ScreeningTaskOrgVo> selectVoListByScreeningTaskId(@Param("screeningTaskId") Integer screeningTaskId);

    List<ScreeningTaskOrg> getByTaskId(@Param("taskId") Integer taskId);

    ScreeningTaskOrg getOneByTaskIdAndOrgId(@Param("taskId") Integer taskId, @Param("orgId") Integer orgId);
}
