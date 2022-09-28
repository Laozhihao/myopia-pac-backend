package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.SchoolResultDetailVO;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.api.management.service.*;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeNameDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanNameDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private StatService statService;
    @Autowired
    private BigScreeningStatService bigScreeningStatService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;
    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;
    @Autowired
    private SchoolBizService schoolBizService;
    @Autowired
    private DistrictAttentiveObjectsStatisticBizService districtAttentiveObjectsStatisticBizService;
    @Autowired
    private ScreeningResultStatisticService screeningResultStatisticService;

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
        return screeningPlanService.getYears(managementScreeningPlanBizService.getReleaseScreeningPlanByUser(CurrentUserUtil.getCurrentUser()));
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
    public List<ScreeningPlanNameDTO> getPlanDetailByYearAndUser(@RequestParam Integer year) {
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getReleaseScreeningPlanByUser(CurrentUserUtil.getCurrentUser());
        return screeningPlanService.getScreeningPlanNameDTOs(screeningPlans, year);
    }

    /**
     * 根据筛查通知获取任务所有筛查学校的地区
     *
     * @param
     * @return
     */
    @GetMapping("/district")
    public List<District> getDistrictByNoticeId(@RequestParam Integer noticeId) {
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (!currentUser.isGovDeptUser()) {
            //查看该通知所有筛查学校的层级的 地区树
            List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(noticeId, currentUser);
            Set<Integer> districts = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
            return districtBizService.getValidDistrictTree(currentUser, districts);
        }
        // 政府人员走新逻辑
        return districtBizService.getChildDistrictValidDistrictTree(currentUser, screeningResultStatisticService.getDistrictIdByNoticeId(noticeId));
    }

    @GetMapping("/plan-district")
    public List<District> getDistrictByPlanId(@RequestParam Integer planId) {
        return districtBizService.getValidDistrictTree(CurrentUserUtil.getCurrentUser(),
                schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(Collections.singletonList(planId)));
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


    /**
     * 重点视力对象
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/attentive-objects-statistic")
    public FocusObjectsStatisticVO getAttenticeObjectsStatistic(@RequestParam Integer districtId) {
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
    public List<District> getAttenticeObjectsStatisticAllDistrictTree() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = districtAttentiveObjectsStatisticBizService.getDataByUser(currentUser);
        Set<Integer> districtIds = districtAttentiveObjectsStatistics.stream().map(DistrictAttentiveObjectsStatistic::getDistrictId).collect(Collectors.toSet());
        return districtBizService.getValidDistrictTree(currentUser, districtIds);
    }



    /**
     * 获取大屏展示的数据
     *
     * @return
     */
    @GetMapping("/big-screen")
    public BigScreeningVO getBigScreeningVO(Integer noticeId) {
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
     * 按区域-幼儿园
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/district/kindergartenResult")
    public KindergartenResultVO getKindergartenResult(@RequestParam Integer districtId,
                                                      @RequestParam Integer noticeId) {
        return statService.getKindergartenResult(districtId,noticeId);
    }

    /**
     * 按区域-小学及以上
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/district/primarySchoolAndAboveResult")
    public PrimarySchoolAndAboveResultVO getPrimarySchoolAndAboveResult(@RequestParam Integer districtId,
                                                                        @RequestParam Integer noticeId) {

        return statService.getPrimarySchoolAndAboveResult(districtId,noticeId);
    }

    /**
     *  按区域-合计详情
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/district/screeningResultTotalDetail")
    public ScreeningResultStatisticDetailVO getScreeningResultTotalDetail(@RequestParam Integer districtId,
                                                                          @RequestParam Integer noticeId) {

        return statService.getScreeningResultTotalDetail(districtId,noticeId);
    }


    /**
     * 按学校-幼儿园
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/school/kindergartenResult")
    public SchoolKindergartenResultVO getSchoolKindergartenResult(@RequestParam Integer districtId,
                                                                  @RequestParam(required = false) Integer noticeId,
                                                                  @RequestParam(required = false) Integer planId) {

        return statService.getSchoolKindergartenResult(districtId,noticeId,planId);
    }

    /**
     * 按学校-小学及以上
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/school/primarySchoolAndAboveResult")
    public SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResult(@RequestParam Integer districtId,
                                                                                    @RequestParam(required = false) Integer noticeId,
                                                                                    @RequestParam(required = false) Integer planId) {

        return statService.getSchoolPrimarySchoolAndAboveResult(districtId,noticeId,planId);
    }

    /**
     * 按学校-查看详情
     * @author hang.yuan
     * @date 2022/4/7
     */
    @GetMapping("/school/schoolStatisticDetail")
    public SchoolResultDetailVO getSchoolStatisticDetail(@RequestParam(required = false) Integer screeningPlanId,
                                                         @RequestParam(required = false) Integer screeningNoticeId,
                                                         @RequestParam(required = false) Integer type,
                                                         @RequestParam Integer schoolId) {
        StatisticDetailBO statisticDetailBO = new StatisticDetailBO()
                .setScreeningPlanId(screeningPlanId)
                .setScreeningNoticeId(screeningNoticeId)
                .setSchoolId(schoolId)
                .setType(type);
        return statService.getSchoolStatisticDetail(statisticDetailBO);
    }


}
