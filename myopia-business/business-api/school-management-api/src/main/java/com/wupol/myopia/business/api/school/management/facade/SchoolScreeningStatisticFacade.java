package com.wupol.myopia.business.api.school.management.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.school.management.constant.SchoolConstant;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningPlanVO;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
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

import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        String screeningOrgName;
        if (Objects.equals(screeningPlan.getScreeningOrgId(),currentUser.getOrgId())){
            screeningOrgName = SchoolConstant.OUR_SCHOOL;
        }else {
            screeningOrgName = screeningOrganizationService.getNameById(screeningPlan.getScreeningOrgId());
        }
        return buildScreeningPlanVO(screeningPlan,screeningOrgName,schoolGradeList);
    }

    /**
     * 构建筛查计划信息
     * @param screeningPlan
     * @param screeningOrgName
     * @param schoolGradeList
     */
    private ScreeningPlanVO buildScreeningPlanVO(ScreeningPlan screeningPlan,String screeningOrgName,List<SchoolGrade> schoolGradeList) {
        Set<Integer> optionTabs = schoolGradeList.stream().map(this::getSchoolType).filter(Objects::nonNull).collect(Collectors.toSet());
        return new ScreeningPlanVO()
                .setId(screeningPlan.getId())
                .setTitle(screeningPlan.getTitle())
                .setStartTime(screeningPlan.getStartTime())
                .setEndTime(screeningPlan.getEndTime())
                .setScreeningType(screeningPlan.getScreeningType())
                .setScreeningBizType(ScreeningBizTypeEnum.getInstanceByOrgType(screeningPlan.getScreeningOrgType()).getType())
                .setStatus(VisionScreeningService.setMergeStatus(screeningPlan.getReleaseStatus(),ScreeningOrganizationService.getScreeningStatus(screeningPlan.getStartTime(), screeningPlan.getEndTime(), screeningPlan.getReleaseStatus())))
                .setScreeningOrgName(screeningOrgName)
                .setOptionTabs(Lists.newArrayList(optionTabs));
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
