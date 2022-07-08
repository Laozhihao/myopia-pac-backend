package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.QuestionTaskVO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷管理
 *
 * @author xz
 */
@Service
@Log4j2
public class QuestionnaireService {
    @Autowired
    private ScreeningTaskService screeningTaskService;

    @Autowired
    private DistrictBizService districtBizService;

    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;

    @Autowired
    private SchoolBizService schoolBizService;
    /**
     * 根据机构id获得所有任务
     *
     * @param orgId orgId
     * @return
     */
    public List<QuestionTaskVO> getQuestionTaskByUnitId(Integer orgId) {
        List<ScreeningTask> screeningTasks = screeningTaskService.getScreeningTaskByOrgId(orgId);
        Map<Integer, Set<ScreeningTask>> yearTaskMap = getYears(screeningTasks);
        return yearTaskMap.entrySet().stream().map(item->{
            QuestionTaskVO questionTaskVO = new QuestionTaskVO();
            questionTaskVO.setAnnual(item.getKey()+"年度");
            questionTaskVO.setTasks(item.getValue().stream().map(it2->{
                QuestionTaskVO.Item taskItem = new QuestionTaskVO.Item();
                taskItem.setTaskId(it2.getId());
                taskItem.setTaskTitle(it2.getTitle());
                taskItem.setScreeningEndTime(it2.getEndTime());
                taskItem.setScreeningStartTime(it2.getStartTime());
                return taskItem;
            }).collect(Collectors.toList()));
            return questionTaskVO;
        }).collect(Collectors.toList());
    }

    public List<District> getQuestionTaskAreas(Integer taskId,CurrentUser user){
        try {
            ScreeningTask task = screeningTaskService.getById(taskId);
            if (Objects.isNull(task)) {
                return Lists.newArrayList();
            }
            if (!user.isGovDeptUser()) {
                //查看该通知所有筛查学校的层级的 地区树
                List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByUser(user);
                Set<Integer> districts = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
                return districtBizService.getValidDistrictTree(user, districts);
            }
            return districtBizService.getChildDistrictValidDistrictTree(user, Sets.newHashSet(task.getDistrictId()));
        }catch (Exception e){
            e.printStackTrace();
            log.error("获得任务区域失败");
            throw new BusinessException("获得任务区域失败！");
        }
    }


    /**
     * 获取年度
     *
     * @return
     */
    private Map<Integer, Set<ScreeningTask>> getYears(List<ScreeningTask> screeningTasks) {
        Map<Integer, Set<ScreeningTask>> yearTask = Maps.newConcurrentMap();
        screeningTasks.forEach(screeningTask -> {
            Integer startYear = DateUtil.getYear(screeningTask.getStartTime());
            Integer endYear = DateUtil.getYear(screeningTask.getEndTime());
            Set<ScreeningTask> startYearTask = Objects.nonNull(yearTask.get(startYear)) ? yearTask.get(startYear) : Sets.newHashSet();
            startYearTask.add(screeningTask);
            yearTask.put(startYear,startYearTask);
            Set<ScreeningTask> endYearTask = Objects.nonNull(yearTask.get(endYear)) ? yearTask.get(endYear) : Sets.newHashSet();
            endYearTask.add(screeningTask);
            yearTask.put(endYear,endYearTask);
        });
        yearTask.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(e -> yearTask.put(e.getKey(), e.getValue()));
        return yearTask;
    }
}
