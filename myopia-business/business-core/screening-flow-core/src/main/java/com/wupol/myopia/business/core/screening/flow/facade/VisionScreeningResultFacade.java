package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    private final StatConclusionService statConclusionService;

    /**
     * 获取筛查结果数据
     * @param screeningPlan
     * @param schoolIds
     * @param orgIds
     */
    public Map<String, Long> getScreeningResultCountMap(ScreeningPlan screeningPlan, Set<Integer> schoolIds, Set<Integer> orgIds) {
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdAndIsDoubleScreen(screeningPlan.getId(), Boolean.FALSE, null);
        return visionScreeningResultList.stream()
                .filter(visionScreeningResult -> orgIds.contains(visionScreeningResult.getScreeningOrgId()))
                .filter(visionScreeningResult -> schoolIds.contains(visionScreeningResult.getSchoolId()))
                .collect(Collectors.groupingBy(visionScreeningResult -> getThreeKey(visionScreeningResult.getPlanId(), visionScreeningResult.getScreeningOrgId(), visionScreeningResult.getSchoolId()), Collectors.counting()));
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

    /**
     * 获取筛查结论集合
     *
     * @param studentIds
     * @param schoolIds
     */
    public Map<String, StatConclusion> getStatConclusionMap(List<Integer> studentIds,List<Integer> schoolIds){
        List<StatConclusion> statConclusionList = statConclusionService.listByStudentIdsAndSchoolIds(studentIds,schoolIds);
        if (CollUtil.isEmpty(statConclusionList)){
            return Maps.newHashMap();
        }
        Map<String, List<StatConclusion>> statConclusionListMap = statConclusionList.stream().collect(Collectors.groupingBy(statConclusion ->getTwoKey(statConclusion.getStudentId(), statConclusion.getSchoolId())));

        return statConclusionListMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entity -> entity.getValue().stream().max(Comparator.comparing(StatConclusion::getUpdateTime)).get()));
    }

}
