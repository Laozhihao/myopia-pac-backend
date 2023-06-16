package com.wupol.myopia.business.aggregation.screening.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.third.party.client.ThirdPartyServiceClient;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 同步数据到新疆处理类
 *
 * @Author HaoHao
 * @Date 2023/5/14
 **/
@Log4j2
@Service
public class XinJiangService {

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 推送数据给新疆近视防控系统
     *
     * @param screeningPlan         筛查计划
     * @param visionScreeningResult 筛查数据
     */
    public void pushScreeningDataToXinJiang(ScreeningPlan screeningPlan, VisionScreeningResult visionScreeningResult) {
        // 通过year和time来判断是否为新疆地区的计划，只同步视力检查和屈光检查数据过去
        if (Objects.isNull(screeningPlan.getYear()) || Objects.isNull(screeningPlan.getTime())
                || !ObjectUtils.anyNotNull(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry())) {
            return;
        }
        // 基本信息
        VisionScreeningResultDTO originalData = new VisionScreeningResultDTO();
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(visionScreeningResult.getScreeningPlanSchoolStudentId());
        // 身份证号码和护照号都为空，则不同步
        if (StringUtils.isAllBlank(planStudent.getIdCard(), planStudent.getPassport())) {
            return;
        }
        School school = schoolService.getById(visionScreeningResult.getSchoolId());
        originalData.setPlanId(screeningPlan.getId())
                .setPlanTitle(screeningPlan.getTitle())
                .setSchoolId(school.getId())
                .setSchoolName(school.getName())
                .setYear(screeningPlan.getYear())
                .setTime(screeningPlan.getTime())
                .setStudentName(planStudent.getStudentName())
                .setStudentIdCard(Optional.ofNullable(planStudent.getIdCard()).orElse(planStudent.getPassport()))
                .setStudentNo(planStudent.getStudentNo())
                .setPlanStudentId(planStudent.getId());
        // 视力检查数据
        originalData.setLeftNakedVision(EyeDataUtil.leftNakedVision(visionScreeningResult)).setRightNakedVision(EyeDataUtil.rightNakedVision(visionScreeningResult))
                .setGlassesType(EyeDataUtil.glassesType(visionScreeningResult))
                .setLeftCorrectedVision(EyeDataUtil.leftCorrectedVision(visionScreeningResult)).setRightCorrectedVision(EyeDataUtil.rightCorrectedVision(visionScreeningResult))
                .setLeftGlassesDegree(EyeDataUtil.leftOkDegree(visionScreeningResult)).setRightGlassesDegree(EyeDataUtil.rightOkDegree(visionScreeningResult));
        // 屈光检查数据
        originalData.setLeftSphericalMirror(EyeDataUtil.leftSph(visionScreeningResult)).setRightSphericalMirror(EyeDataUtil.rightSph(visionScreeningResult))
                .setLeftCylindricalMirror(EyeDataUtil.leftCyl(visionScreeningResult)).setRightCylindricalMirror(EyeDataUtil.rightCyl(visionScreeningResult))
                .setLeftAxialPosition(EyeDataUtil.leftAxial(visionScreeningResult)).setRightAxialPosition(EyeDataUtil.rightAxial(visionScreeningResult));
        log.debug("推送数据：" + JSON.toJSONString(originalData));
        thirdPartyServiceClient.pushScreeningResult(originalData);
    }

    /**
     * 同步数据到新疆
     *
     * @param planId    筛查计划ID
     * @param schoolId  学校ID
     * @return void
     **/
    public void syncDataToXinJiang(Integer planId, Integer schoolId) {
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Assert.notNull(plan, "不存在该计划");
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.findByList(new VisionScreeningResult().setPlanId(planId).setSchoolId(schoolId).setIsDoubleScreen(false));
        Assert.isTrue(!visionScreeningResultList.isEmpty(), "没有可同步的数据");
        for (VisionScreeningResult visionScreeningResult : visionScreeningResultList) {
            pushScreeningDataToXinJiang(plan, visionScreeningResult);
        }
    }
}
