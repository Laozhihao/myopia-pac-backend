package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.vo.OrgScreeningCountVO;

import java.util.List;

/**
 * 筛查任务关联的机构表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningTaskOrgMapper extends BaseMapper<ScreeningTaskOrg> {

    List<OrgScreeningCountVO> countScreeningTime();

}
