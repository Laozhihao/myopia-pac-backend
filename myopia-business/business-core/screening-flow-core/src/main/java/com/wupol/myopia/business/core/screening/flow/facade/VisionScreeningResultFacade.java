package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 筛查结果门面
 *
 * @author hang.yuan 2022/9/23 13:18
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VisionScreeningResultFacade {

    private final VisionScreeningResultService visionScreeningResultService;

    /**
     * 获取筛查结果数据
     * @param screeningPlan
     * @param schoolIds
     */
    public Map<Integer, Integer> getScreeningResultCountMap(ScreeningPlan screeningPlan, Set<Integer> schoolIds) {
        List<SchoolCountDO> schoolCountList = visionScreeningResultService.getSchoolCountByPlanIdAndSchoolIds(screeningPlan.getId(), CommonConst.ZERO, schoolIds);
        return schoolCountList.stream().collect(Collectors.toMap(SchoolCountDO::getSchoolId, SchoolCountDO::getSchoolCount));
    }

    /**
     * 三个组合唯一可以
     * @param one
     * @param two
     * @param three
     */
    public static String getThreeKey(Integer one, Integer two, Integer three) {
        return one + StrUtil.UNDERLINE + two + StrUtil.UNDERLINE + three;
    }

    /**
     * 二个组合唯一可以
     * @param one
     * @param two
     */
    public static String getTwoKey(Integer one,Integer two){
        return one + StrUtil.UNDERLINE + two ;
    }

}
