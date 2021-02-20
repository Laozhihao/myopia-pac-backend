package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanSchoolInfoDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskNameVO;
import com.wupol.myopia.business.management.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screening")
public class StatManagementController {
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    /**
     * 根据查找当前用户所处层级能够查找到的年度
     *
     * @param
     * @return
     */
    @GetMapping("/year")
    public List<Integer> getYearsByUserId() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //获取当前部门下的所有id
        List<Integer> allSubordinateOrgIds = govDeptService.getAllSubordinate(user.getOrgId());
        List<ScreeningTask> screeningTasks = screeningTaskService.getTaskByAllSubordinateGovDeptIds(allSubordinateOrgIds);
        List<Integer> years = screeningTaskService.getYears(screeningTasks);
        return years;
    }

    /**
     * 查找所在年度的筛查任务
     *
     * @param
     * @return
     */
    @GetMapping("/task")
    public Set<ScreeningTaskNameVO> getYearsByUserId(Integer year) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        List<Integer> allSubordinateOrgIds = govDeptService.getAllSubordinate(user.getOrgId());
        List<ScreeningTask> screeningTasks = screeningTaskService.getTaskByAllSubordinateGovDeptIds(allSubordinateOrgIds);
        Set<ScreeningTaskNameVO> screeningTaskNameVOs = screeningTasks.stream().filter(screeningTask ->
                year.equals(screeningTaskService.getYear(screeningTask.getStartTime()))
        ).map(screeningTask -> {
            ScreeningTaskNameVO screeningTaskNameVO = new ScreeningTaskNameVO();
            screeningTaskNameVO.setTaskName(screeningTask.getTitle()).setTaskId(screeningTask.getId()).setScreeningStartTime(screeningTask.getStartTime()).setScreeningEndTime(screeningTask.getEndTime());
            return screeningTaskNameVO;
        }).collect(Collectors.toSet());
        return screeningTaskNameVOs;
    }

    /**
     * 根据筛查任务获取地区id
     *
     * @param
     * @return
     */
    @GetMapping("/district")
    public List<District> getDistrictByTaskId(Integer taskId) {
        ScreeningTask screeningTask = screeningTaskService.getById(taskId);
        if (screeningTask == null) {
            throw new BusinessException("该taskId不存在");
        }
        //task 有可能找不到
        Integer govDeptId = screeningTask.getGovDeptId();
        List<GovDept> govDepts = govDeptService.getAllSubordinateWithDistrictId(govDeptId);
        if (CollectionUtils.isEmpty(govDepts)) {
            throw new BusinessException("找不到该任务的政府部门Id");
        }
        Set<Integer> govDeptIds = govDepts.stream().map(GovDept::getId).collect(Collectors.toSet());
        //查找筛查通知了哪些地区id
        Set<Integer> districtIds = screeningNoticeService.listByScreeningTaskId(taskId, govDeptIds);
        List<District> districtTree = districtService.getDistrictTree(screeningTask.getDistrictId());
        return districtService.getDistrictTree(districtTree,districtIds);
    }

    /**
     * 根据地区id获取学校情况
     *
     * @param
     * @return
     */
    @GetMapping("/school")
    public Set<ScreeningPlanSchoolInfoDTO> getSchoolByDistrictId(Integer districtId, Integer taskId) {
        return screeningPlanService.getByDistrictIdAndTaskId(districtId, taskId);
    }

}
