package com.wupol.myopia.business.api.school.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端-视力筛查
 *
 * @author Simple4H
 */
@Service
public class VisionScreeningService {

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolMonitorStatisticService schoolMonitorStatisticService;

    /**
     * 获取视力筛查列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校Id
     * @return IPage<ScreeningListResponseDTO>
     */
    public IPage<ScreeningListResponseDTO> getList(PageRequest pageRequest, Integer schoolId) {
        IPage<ScreeningListResponseDTO> responseDTO = screeningPlanSchoolService.getResponseBySchoolId(pageRequest, schoolId);
        List<ScreeningListResponseDTO> schoolPlanList = responseDTO.getRecords();

        // 获取筛查计划
        List<Integer> planIds = schoolPlanList.stream().map(ScreeningListResponseDTO::getPlanId).collect(Collectors.toList());
        List<ScreeningPlan> screeningPlans = screeningPlanService.getByIds(planIds);
        Map<Integer, ScreeningPlan> planMap = screeningPlans.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));

        // 获取统计信息
        List<SchoolMonitorStatistic> statisticList = schoolMonitorStatisticService.getBySchoolId(schoolId);
        Map<Integer, SchoolMonitorStatistic> schoolStatisticMap = statisticList.stream()
                .collect(Collectors.toMap(SchoolMonitorStatistic::getScreeningPlanId, Function.identity()));

        schoolPlanList.forEach(schoolPlan -> {
            ScreeningPlan screeningPlan = planMap.get(schoolPlan.getPlanId());
            if (Objects.nonNull(screeningPlan)) {
                schoolPlan.setTitle(screeningPlan.getTitle());
                schoolPlan.setStartTime(screeningPlan.getStartTime());
                schoolPlan.setEndTime(screeningPlan.getEndTime());
                schoolPlan.setReleaseStatus(screeningPlan.getReleaseStatus());
                schoolPlan.setReleaseTime(screeningPlan.getReleaseTime());
                schoolPlan.setContent(screeningPlan.getContent());
            }

            SchoolMonitorStatistic schoolMonitorStatistic = schoolStatisticMap.get(schoolPlan.getPlanId());
            if (Objects.nonNull(schoolMonitorStatistic)) {
                schoolPlan.setSchoolStatisticId(schoolMonitorStatistic.getId());
                schoolPlan.setPlanScreeningNumbers(schoolMonitorStatistic.getPlanScreeningNumbers());
                schoolPlan.setRealScreeningNumbers(schoolMonitorStatistic.getRealScreeningNumbers());
                schoolPlan.setScreeningOrgName(schoolMonitorStatistic.getScreeningOrgName());
            }
        });
        return responseDTO;
    }
}
