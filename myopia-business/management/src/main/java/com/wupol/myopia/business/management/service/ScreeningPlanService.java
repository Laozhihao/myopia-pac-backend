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
     * @param schoolId
     * @param screeningOrgId
     * @param screeningReleaseStatus
     * @param currentTimeMillis
     * @return
     */
    public ScreeningPlan getScreeningPlan(Integer schoolId, Integer screeningOrgId, int screeningReleaseStatus, long currentTimeMillis) {
        ScreeningPlan screeningPlan = baseMapper.selectScreeningPlanDetailByOrgIdAndSchoolId(schoolId, screeningOrgId, screeningReleaseStatus, currentTimeMillis);
        if (screeningPlan == null) {
            throw new ManagementUncheckedException("获取ScreeningPlanDto失败，schoolId = " + schoolId + ",orgId = " + screeningOrgId + ",currentTimeMillis = " + currentTimeMillis);
        }
        return screeningPlan;
    }

    /**
     * @param screeningOrgId
     * @return
     */
    public List<Long> getScreeningSchoolIdByScreeningOrgId(Integer screeningOrgId) {
       return baseMapper.selectScreeningSchoolIds(screeningOrgId, ScreeningConstant.SCREENING_RELEASE_STATUS, System.currentTimeMillis());
    }
    /**
     * @return
     */
    public ScreeningPlan getScreeningPlanDTO(ScreeningResultBasicData screeningResultBasicData) {
        ScreeningPlan screeningPlan = getScreeningPlan(screeningResultBasicData.getSchoolId(), screeningResultBasicData.getDeptId(), ScreeningConstant.SCREENING_RELEASE_STATUS, System.currentTimeMillis());
        getScreeningPlan(screeningResultBasicData.getSchoolId(), screeningResultBasicData.getDeptId(), ScreeningConstant.SCREENING_RELEASE_STATUS, System.currentTimeMillis());
        return screeningPlan;
    }
}
