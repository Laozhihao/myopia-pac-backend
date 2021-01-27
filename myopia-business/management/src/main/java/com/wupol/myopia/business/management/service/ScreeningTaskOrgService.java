package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningTaskOrgMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningTaskOrgService extends BaseService<ScreeningTaskOrgMapper, ScreeningTaskOrg> {

    /**
     * 通过筛查机构ID获取筛查任务关联
     *
     * @param orgId 筛查机构ID
     * @return 结果 筛查任务关联Lists
     */
    public List<ScreeningTaskOrg> getTaskOrgListsByOrgId(Integer orgId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningTaskOrg>().eq("screening_org_id", orgId));
    }

}
