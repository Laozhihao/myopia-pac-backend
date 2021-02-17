package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.myopia.common.constant.ScreeningConstant;
import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningPlanService extends BaseService<ScreeningPlanMapper, ScreeningPlan> {

    /**
     * 通过ids获取
     *
     * @param pageRequest 分页入参
     * @param ids         ids
     * @return IPage<ScreeningPlan>
     */
    public IPage<ScreeningPlan> getListByIds(PageRequest pageRequest, List<Integer> ids) {
        return baseMapper.getPlanLists(pageRequest.toPage(), ids);
    }

    /**
     * @param screeningOrgId
     * @return
     */
    public List<Long> getScreeningSchoolIdByScreeningOrgId(Integer screeningOrgId) {
        return baseMapper.selectScreeningSchoolIds(screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, System.currentTimeMillis());
    }
}
