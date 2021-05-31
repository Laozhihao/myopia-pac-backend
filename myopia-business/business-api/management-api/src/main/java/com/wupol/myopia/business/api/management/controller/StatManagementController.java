package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.SchoolMonitorStatisticDTO;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.api.management.schedule.ScheduledTasksExecutor;
import com.wupol.myopia.business.api.management.service.*;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeNameDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanNameDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screening-statistic")
@Slf4j
public class StatManagementController {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private DistrictBizService districtBizService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private StatService statService;
    @Autowired
    private BigScreeningStatService bigScreeningStatService;
    @Autowired
    private ScheduledTasksExecutor scheduledTasksExecutor;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;
    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;
    @Autowired
    private SchoolBizService schoolBizService;
    @Autowired
    private DistrictAttentiveObjectsStatisticBizService districtAttentiveObjectsStatisticBizService;
    @Autowired
    private SchoolVisionStatisticBizService schoolVisionStatisticBizService;
    @Autowired
    private SchoolMonitorStatisticBizService schoolMonitorStatisticBizService;
    @Autowired
    private StatRescreenService statRescreenService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 根据查找当前用户所处层级能够查找到的年度
     *
     * @param
     * @return
     */
    @GetMapping("/notice-year")
    public List<Integer> getYearsByUser() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //获取当前部门下的所有id
        return screeningNoticeService.getYears(screeningNoticeBizService.getRelatedNoticeByUser(user));
    }

    @GetMapping("/plan-year")
    public List<Integer> getPlanYearsByUser() {
        return screeningPlanService.getYears(managementScreeningPlanBizService.getScreeningPlanByUser(CurrentUserUtil.getCurrentUser()));
    }

    /**
     * 查找所在年度的筛查任务
     *
     * @param
     * @return
     */
    @GetMapping("/notice")
    public List<ScreeningNoticeNameDTO> getNoticeDetailByYearAndUser(@RequestParam Integer year) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //找到筛查通知year的所有相关的screeningNotice
        List<ScreeningNotice> screeningNotices = screeningNoticeBizService.getRelatedNoticeByUser(user);
        return screeningNoticeService.getScreeningNoticeNameDTO(screeningNotices, year);
    }


    /**
     * 查找所在年度的筛查计划
     *
     * @param
     * @return
     */
    @GetMapping("/plan")
    public Set<ScreeningPlanNameDTO> getPlanDetailByYearAndUser(@RequestParam Integer year) {
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByUser(CurrentUserUtil.getCurrentUser());
        return screeningPlanService.getScreeningPlanNameDTOs(screeningPlans, year);
    }

    /**
     * 根据筛查通知获取任务所有筛查学校的地区
     *
     * @param
     * @return
     */
    @GetMapping("/district")
    public List<District> getDistrictByNoticeId(@RequestParam Integer noticeId) throws IOException {
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("找不到该notice");
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //查看该通知所有筛查学校的层级的 地区树
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(noticeId, currentUser);
        Set<Integer> districts = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        return districtBizService.getValidDistrictTree(currentUser, districts);
    }

    @GetMapping("/plan-district")
    public List<District> getDistrictByPlanId(@RequestParam Integer planId) throws IOException {
        return districtBizService.getValidDistrictTree(CurrentUserUtil.getCurrentUser(),
                schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(Arrays.asList(planId)));
    }

    /**
     * 根据地区id获取学校情况 todo 可能不用
     *
     * @param
     * @return
     */
    @GetMapping("/school")
    public Set<ScreeningPlanSchoolInfoDTO> getSchoolByDistrictId(@RequestParam Integer districtId, @RequestParam Integer taskId) {
        return screeningPlanService.getByDistrictIdAndTaskId(districtId, taskId);
    }


    /**
     * 重点视力对象
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/attentive-objects-statistic")
    public FocusObjectsStatisticVO getAttenticeObjectsStatistic(@RequestParam Integer districtId) throws IOException {
        //下级层级
        List<District> districts = districtService.getChildDistrictByParentIdPriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        districtIds.add(districtId);
        return statService.getFocusObjectsStatisticVO(districtId, districts, districtIds);
    }

    /**
     * 当前用户可查看的重点视力对象的districtId
     *
     * @return
     */
    @GetMapping("/district/attentive-objects-statistic/districtId")
    public List<District> getAttenticeObjectsStatisticAllDistrictTree() throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = districtAttentiveObjectsStatisticBizService.getDataByUser(currentUser);
        Set<Integer> districtIds = districtAttentiveObjectsStatistics.stream().map(DistrictAttentiveObjectsStatistic::getDistrictId).collect(Collectors.toSet());
        return districtBizService.getValidDistrictTree(currentUser, districtIds);
    }


    /**
     * 地区视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-vision-result")
    public ScreeningVisionStatisticVO getDistrictVisionStatistic(
            @RequestParam Integer districtId, @RequestParam Integer noticeId) throws IOException {
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("找不到该notice");
        }
        return statService.getScreeningVisionStatisticVO(districtId, noticeId, screeningNotice);
    }

    /**
     * 地区监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-monitor-result")
    public DistrictScreeningMonitorStatisticVO getDistrictMonitorStatistic(
            @RequestParam Integer districtId, @RequestParam Integer noticeId) throws IOException {
        //查找notice
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("找不到该notice");
        }
        return statService.getDistrictScreeningMonitorStatisticVO(districtId, noticeId, screeningNotice);
    }


    /**
     * 学校视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/school/screening-vision-result")
    public ScreeningSchoolVisionStatisticVO getSchoolVisionStatistic(@RequestParam Integer districtId, @RequestParam Integer noticeId) {
        // 获取当前层级下，所有参与任务的学校
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        List<SchoolVisionStatistic> schoolVisionStatistics = schoolVisionStatisticBizService.getStatisticDtoByNoticeIdAndOrgId(screeningNotice.getId(), CurrentUserUtil.getCurrentUser(), districtId);
        return getSchoolVisionStatisticVO(schoolVisionStatistics, screeningNotice);
    }

    /**
     * 获取学校监控统计
     * @param districtId
     * @param noticeId
     * @return
     * @throws IOException
     */
    @GetMapping("/school/screening-monitor-result")
    public SchoolScreeningMonitorStatisticVO getSchoolMonitorStatistic(@RequestParam Integer districtId, @RequestParam Integer noticeId) throws IOException {
        // 获取当前层级下，所有参与任务的学校
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("找不到该notice");
        }
        List<SchoolMonitorStatistic> schoolMonitorStatistics = schoolMonitorStatisticBizService.getStatisticDtoByNoticeIdAndOrgId(screeningNotice.getId(), CurrentUserUtil.getCurrentUser(), districtId);
        if (CollectionUtils.isEmpty(schoolMonitorStatistics)) {
            return SchoolScreeningMonitorStatisticVO.getEmptyInstance();
        }
        //获取数据
        return SchoolScreeningMonitorStatisticVO.getInstance(getPlanSchoolReportStatus(schoolMonitorStatistics), screeningNotice);
    }

    private List<SchoolMonitorStatisticDTO> getPlanSchoolReportStatus(List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        return schoolMonitorStatistics.stream().map(schoolMonitorStatistic -> {
            ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(schoolMonitorStatistic.getScreeningPlanId(), schoolMonitorStatistic.getSchoolId());
            return new SchoolMonitorStatisticDTO(schoolMonitorStatistic, statRescreenService.hasRescreenReport(schoolMonitorStatistic.getScreeningPlanId(), schoolMonitorStatistic.getSchoolId()),
                    screeningPlanSchool.getQualityControllerName(), screeningPlanSchool.getQualityControllerCommander());
        }).collect(Collectors.toList());
    }

    /**
     * 获取大屏展示的数据
     *
     * @return
     */
    @GetMapping("/big-screen")
    public BigScreeningVO getBigScreeningVO(Integer noticeId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (ObjectsUtil.hasNull(currentUser, noticeId)) {
            throw new ManagementUncheckedException("noticeId 或者 currentUser 不能为空");
        }
        //查找 district
        District district = districtBizService.getNotPlatformAdminUserDistrict(currentUser);
        if (district == null) {
            throw new ManagementUncheckedException("无法找到该用户的找到所在区域，user = " + JSON.toJSONString(currentUser));
        }
        //查找notice
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        if (screeningNotice == null) {
            throw new ManagementUncheckedException("无法找到该noticeId = " + noticeId);
        }
        return bigScreeningStatService.getBigScreeningVO(screeningNotice, district);
    }

    /**
     * 为了测试方便
     */
    @GetMapping("/trigger")
    public void statTaskTrigger() {
        scheduledTasksExecutor.statistic();
    }

    /**
     * 触发大屏统计（todo 为了测试方便）
     * @throws IOException
     */
    @GetMapping("/big")
    public void statBigScreen() throws IOException {
        bigScreeningStatService.statisticBigScreen();
    }

    @GetMapping("/triggerAll")
    public void statTaskTriggerAll() {
        List<Integer> yesterdayScreeningPlanIds = screeningPlanService.list().stream().map(ScreeningPlan::getId).collect(Collectors.toList());
        if (com.wupol.framework.core.util.CollectionUtils.isEmpty(yesterdayScreeningPlanIds)) {
            log.info("筛查数据统计：历史无筛查数据，无需统计");
            return;
        }
        scheduledTasksExecutor.statisticByPlanIds(yesterdayScreeningPlanIds);
    }

    /**
     * 学校视力情况
     *
     * @param districtId
     * @param planId
     * @return
     */
    @GetMapping("/plan/school/screening-vision-result")
    public ScreeningSchoolVisionStatisticVO getSchoolVisionStatisticByPlan(@RequestParam Integer districtId, @RequestParam Integer planId) {
        // 获取当前层级下，所有参与任务的学校
        ScreeningPlan plan = screeningPlanService.getReleasedPlanById(planId);
        ScreeningNotice notice = screeningNoticeService.getById(plan.getSrcScreeningNoticeId());
        List<SchoolVisionStatistic> schoolVisionStatistics = schoolVisionStatisticBizService.getStatisticDtoByPlanIdsAndOrgId(Arrays.asList(plan), districtId);
        return getSchoolVisionStatisticVO(schoolVisionStatistics, notice);
    }

   /**
     * 获取学校监控统计
     * @param districtId
     * @param planId
     * @return
     * @throws IOException
     */
    @GetMapping("/plan/school/screening-monitor-result")
    public SchoolScreeningMonitorStatisticVO getSchoolMonitorStatisticByPlan(@RequestParam Integer districtId, @RequestParam Integer planId) throws IOException {
        // 获取当前层级下，所有参与任务的学校
        ScreeningPlan plan = screeningPlanService.getReleasedPlanById(planId);
        ScreeningNotice notice = screeningNoticeService.getById(plan.getSrcScreeningNoticeId());
        List<SchoolMonitorStatistic> schoolMonitorStatistics = schoolMonitorStatisticBizService.getStatisticDtoByPlansAndOrgId(Arrays.asList(plan), districtId);
        if (CollectionUtils.isEmpty(schoolMonitorStatistics)) {
            return SchoolScreeningMonitorStatisticVO.getEmptyInstance();
        }
        //获取数据
        return SchoolScreeningMonitorStatisticVO.getInstance(getPlanSchoolReportStatus(schoolMonitorStatistics), notice);
    }

    private ScreeningSchoolVisionStatisticVO getSchoolVisionStatisticVO(List<SchoolVisionStatistic> schoolVisionStatistics, ScreeningNotice notice) {
        if (CollectionUtils.isEmpty(schoolVisionStatistics)) {
            return ScreeningSchoolVisionStatisticVO.getEmptyInstance();
        }
        //学校id
        List<Integer> schoolIds = schoolVisionStatistics.stream().map(SchoolVisionStatistic::getSchoolId).collect(Collectors.toList());
        List<Integer> schoolDistrictIdList = schoolService.getByIds(schoolIds).stream().map(School::getDistrictId).collect(Collectors.toList());
        //获取学校的地区
        Map<Integer, String> schoolIdDistrictNameMap = districtService.getByIds(schoolDistrictIdList);
        //获取数据
        return ScreeningSchoolVisionStatisticVO.getInstance(schoolVisionStatistics, schoolIdDistrictNameMap, notice);
    }

}
