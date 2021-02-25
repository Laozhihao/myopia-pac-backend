package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanSchoolInfoDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanNameVO;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskNameVO;
import com.wupol.myopia.business.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    public List<Integer> getYearsByUser() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //获取当前部门下的所有id
        List<ScreeningNotice> screeningNotices = screeningNoticeService.getRelatedNoticeByUser(user);
        List<Integer> years = screeningNoticeService.getYears(screeningNotices);
        return years;
    }

    /**
     * 查找所在年度的筛查任务
     *
     * @param
     * @return
     */
    @GetMapping("/task")
    public Set<ScreeningTaskNameVO> getTaskDetailByYearAndUser(@RequestParam Integer year) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //找到筛查通知year的所有相关的screeningNotice
        List<ScreeningNotice> screeningNotices = screeningNoticeService.getRelatedNoticeByUser(user);
        Set<Integer> screeningNoticeIds = screeningNotices.stream().map(ScreeningNotice::getId).collect(Collectors.toSet());
        return screeningTaskService.getScreeningTaskNameVO(screeningNoticeIds, year);
    }


    /**
     * 查找所在年度的筛查计划
     *
     * @param
     * @return
     */
    @GetMapping("/plan")
    public Set<ScreeningPlanNameVO> getPlanDetailByYearAndUser(@RequestParam Integer year) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        List<ScreeningNotice> screeningNotices = screeningNoticeService.getRelatedNoticeByUser(user);
        Set<Integer> screeningNoticeIds = screeningNotices.stream().map(ScreeningNotice::getId).collect(Collectors.toSet());
        List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdsAndUser(screeningNoticeIds, user);
        return screeningPlanService.getScreeningPlanNameVOs(screeningPlans, year);
    }

    /**
     * 根据筛查任务获取地区id
     *
     * @param
     * @return
     */
    @GetMapping("/district")
    public List<District> getDistrictByTaskId(@RequestParam Integer noticeId) throws IOException {
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("无法找到该通知");
        }
        if (screeningNotice.getReleaseStatus() != CommonConst.STATUS_RELEASE) {
            throw new BusinessException("该通知未发布");
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //查看该通知发布层级的 地区树
        List<ScreeningPlan> screeningPlans = screeningPlanService.getScreeningPlanByNoticeIdAndUser(noticeId, currentUser);
        Set<Integer> districts = screeningPlans.stream().map(ScreeningPlan::getDistrictId).collect(Collectors.toSet());
        return districtService.getValidDistrictTree(currentUser, districts);
    }

    /**
     * 根据地区id获取学校情况
     *
     * @param
     * @return
     */
    @GetMapping("/school")
    public Set<ScreeningPlanSchoolInfoDTO> getSchoolByDistrictId(@RequestParam Integer districtId, @RequestParam Integer taskId) {
        return screeningPlanService.getByDistrictIdAndTaskId(districtId, taskId);
    }

}
