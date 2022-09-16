package com.wupol.myopia.business.api.school.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.KindergartenResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.PrimarySchoolAndAboveResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.ResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.facade.StatSchoolFacade;
import com.wupol.myopia.business.api.school.management.domain.vo.SchoolScreeningResultVO;
import com.wupol.myopia.business.api.school.management.facade.SchoolScreeningStatisticFacade;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 学校筛查统计
 *
 * @author hang.yuan 2022/9/16 20:05
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/screening/statistic")
public class SchoolScreeningStatisticController {

    @Autowired
    private SchoolScreeningStatisticFacade schoolScreeningStatisticFacade;
    @Autowired
    private StatSchoolFacade statSchoolFacade;

    /**
     * 获取学校筛查结果
     * @param
     */
    @GetMapping("/screeningResult")
    public SchoolScreeningResultVO screeningResult(){
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolScreeningStatisticFacade.screeningResult(currentUser);
    }

    /**
     * 获取统计详情
     * @param screeningPlanId 筛查计划
     * @param type tab类型(8:幼儿园，0:小学及以上)
     */
    @GetMapping("/statisticDetail")
    public ResultDetailVO getStatisticDetail(@RequestParam Integer screeningPlanId,@RequestParam Integer type) {
        StatisticDetailBO statisticDetailBO = new StatisticDetailBO()
                .setScreeningPlanId(screeningPlanId)
                .setSchoolId(CurrentUserUtil.getCurrentUser().getOrgId())
                .setType(type);
        if (Objects.equals(type, SchoolEnum.TYPE_KINDERGARTEN.getType())){
            return statSchoolFacade.getSchoolStatisticDetail(statisticDetailBO).getKindergartenResultDetail();
        }else {
            return statSchoolFacade.getSchoolStatisticDetail(statisticDetailBO).getPrimarySchoolAndAboveResultDetail();
        }

    }

}
