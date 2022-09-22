package com.wupol.myopia.business.api.school.management.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.school.management.constant.SchoolConstant;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningPlanVO;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningBizTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校筛查统计门面
 *
 * @author hang.yuan 2022/9/16 20:33
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class SchoolScreeningStatisticFacade {

    private final ScreeningPlanService screeningPlanService;
    private final ScreeningOrganizationService screeningOrganizationService;
    private final SchoolGradeService schoolGradeService;
    private final VisionScreeningService visionScreeningService;


    /**
     * 获取筛查计划信息
     * @param screeningPlanId 筛查计划ID
     * @param currentUser 当前用户
     */
    public ScreeningPlanVO getPlanInfo(Integer screeningPlanId,CurrentUser currentUser) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        List<SchoolGrade> schoolGradeList = schoolGradeService.getBySchoolId(currentUser.getOrgId());
        TwoTuple<Integer,String> screeningOrg;
        if (Objects.equals(screeningPlan.getScreeningOrgId(),currentUser.getOrgId())){
            screeningOrg =TwoTuple.of(currentUser.getOrgId(),SchoolConstant.OUR_SCHOOL);
        }else {
            screeningOrg =TwoTuple.of(screeningPlan.getScreeningOrgId(),screeningOrganizationService.getNameById(screeningPlan.getScreeningOrgId()));
        }

        Map<Integer, Integer> visionScreeningResultMap = visionScreeningService.getVisionScreeningResultMap(Lists.newArrayList(screeningPlanId));
        Integer screeningStatus = VisionScreeningService.getScreeningStatus(screeningPlan.getStartTime(), screeningPlan.getEndTime(), screeningPlan.getReleaseStatus(), visionScreeningResultMap.get(screeningPlanId));
        return buildScreeningPlanVO(screeningPlan,screeningOrg,schoolGradeList,screeningStatus);
    }

    /**
     * 构建筛查计划信息
     * @param screeningPlan
     * @param screeningOrg
     * @param schoolGradeList
     */
    private ScreeningPlanVO buildScreeningPlanVO(ScreeningPlan screeningPlan,TwoTuple<Integer,String> screeningOrg,List<SchoolGrade> schoolGradeList,Integer screeningStatus) {
        List<Integer> optionTabs = schoolGradeList.stream().map(this::getSchoolType).filter(Objects::nonNull).distinct().sorted(Comparator.comparing(Integer::intValue).reversed()).collect(Collectors.toList());

        return new ScreeningPlanVO()
                .setId(screeningPlan.getId())
                .setTitle(screeningPlan.getTitle())
                .setStartTime(screeningPlan.getStartTime())
                .setEndTime(screeningPlan.getEndTime())
                .setScreeningType(screeningPlan.getScreeningType())
                .setScreeningBizType(ScreeningBizTypeEnum.getInstanceByOrgType(screeningPlan.getScreeningOrgType()).getType())
                .setStatus(VisionScreeningService.setMergeStatus(screeningPlan.getReleaseStatus(),screeningStatus))
                .setScreeningOrgName(screeningOrg.getSecond())
                .setScreeningOrgId(screeningOrg.getFirst())
                .setOptionTabs(optionTabs);
    }

    /**
     * 获取学校类型
     * @param schoolGrade 学校年级
     */
    private Integer getSchoolType(SchoolGrade schoolGrade) {
        if (GradeCodeEnum.primaryAbove().contains(schoolGrade.getGradeCode())) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        }
        if (GradeCodeEnum.kindergartenSchoolCode().contains(schoolGrade.getGradeCode())) {
            return SchoolEnum.TYPE_KINDERGARTEN.getType();
        }
        return null;
    }

}
