package com.wupol.myopia.business.aggregation.screening.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查结果转筛查数据结论
 *
 * @author hang.yuan 2022/4/18 19:34
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StatConclusionBizService {

    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    private final VisionScreeningResultService visionScreeningResultService;
    private final StatConclusionService statConclusionService;

    /**
     * 筛查计划id - 筛查结果转筛查数据结论
     * 1、m_screening_plan_school_student ： screeningOrgId  planStudentId（id） 获取最新的数据
     *
     * 2、m_vision_screening_result：screeningPlanId screeningOrgId planStudentId 获取筛查结果 （根据是否复筛，获得当前数据和复筛数据）
     *
     * 3、m_screening_plan_school_student ：screeningOrgId  planStudentId（id）获取最新的数据 ，构建VisionScreeningResult （没数据构建，有数据更新，更加前端传的数据是初筛还是复筛）
     *
     * 4、m_stat_conclusion ： resultId,  isDoubleScreen 获取筛查数据结论
     *
     * 5、m_school_grade ：gradeId 获取学校年级数据
     *
     * 6、数据大组装： StatConclusionBuilder.build() 基础数据， 视力相关的数据，身高体重相关的数据，龋齿，血压，脊柱，疾病史，隐私项
     *
     */

    public void screeningToConclusion(){
        //1.历史初筛和复筛的数据


    }













    public void screeningToConclusion(Integer planId){
        //根据筛查计划Id 获取不是复测的筛查结果
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdsOrderByUpdateTimeDesc(Sets.newHashSet(planId));
        if (CollectionUtils.isEmpty(visionScreeningResultList)) {
            return;
        }
        //筛查结果集 List To Map
        Map<Integer, VisionScreeningResult> screeningResultMap = visionScreeningResultList.stream().collect(Collectors.toMap(VisionScreeningResult::getId, Function.identity()));

        List<Integer> resultId = visionScreeningResultList.stream().map(VisionScreeningResult::getId).collect(Collectors.toList());
        //筛查结果Id集合 获取筛查数据结论数据
        List<StatConclusion> statConclusionList = statConclusionService.getByResultIds(resultId);

        for (StatConclusion statConclusion : statConclusionList) {
            VisionScreeningResult visionScreeningResult = screeningResultMap.get(statConclusion.getResultId());
            if (Objects.nonNull(visionScreeningResult)) {
                ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
                if (Objects.nonNull(computerOptometry)) {
                    Integer age = statConclusion.getAge();
                    ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
                    ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();

                    setMyopia(statConclusion, visionScreeningResult.getVisionData(), age, leftEyeData, rightEyeData);

                    setVisionLevel(statConclusion, visionScreeningResult.getVisionData(), age);

                    statConclusion.setUpdateTime(new Date());
                }
            }
        }
        statConclusionService.updateBatchById(statConclusionList);
    }



    private void setVisionLevel(StatConclusion statConclusion, VisionDataDO visionData, Integer age) {
        if (Objects.nonNull(visionData) && Objects.nonNull(age)) {
            BigDecimal leftNV = visionData.getLeftEyeData().getNakedVision();
            BigDecimal rightNV = visionData.getRightEyeData().getNakedVision();
            Boolean isLeftLowVision;
            Boolean isRightLowVision;
            Integer leftCode = null;
            Integer rightCode = null;
            if (Objects.nonNull(leftNV)) {
                isLeftLowVision = StatUtil.isLowVision(leftNV.floatValue(), age);
                WarningLevel nakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(leftNV.floatValue(), age);
                leftCode = Objects.nonNull(nakedVisionWarningLevel) ? nakedVisionWarningLevel.code : null;
            } else {
                isLeftLowVision = null;
            }

            if (Objects.nonNull(rightNV)) {
                isRightLowVision = StatUtil.isLowVision(rightNV.floatValue(), age);
                WarningLevel nakedVisionWarningLevel = StatUtil.getNakedVisionWarningLevel(rightNV.floatValue(), age);
                rightCode = Objects.nonNull(nakedVisionWarningLevel) ? nakedVisionWarningLevel.code : null;
            } else {
                isRightLowVision = null;
            }

            if (ObjectsUtil.allNull(isLeftLowVision, isRightLowVision)) {
                statConclusion.setIsLowVision(null);
                statConclusion.setNakedVisionWarningLevel(null);
            } else {
                //是否视力低下
                statConclusion.setIsLowVision(ObjectsUtil.allNotNull(isLeftLowVision, isRightLowVision) ? isLeftLowVision || isRightLowVision : Objects.nonNull(isLeftLowVision) ? isLeftLowVision : Boolean.TRUE.equals(isRightLowVision));
                //裸眼视力预警级别
                statConclusion.setNakedVisionWarningLevel(StatUtil.getSeriousLevel(leftCode, rightCode));
            }
        }
    }

    private void setMyopia(StatConclusion statConclusion, VisionDataDO visionData, Integer age, ComputerOptometryDO.ComputerOptometry leftEyeData, ComputerOptometryDO.ComputerOptometry rightEyeData) {
        if (ObjectsUtil.allNotNull(leftEyeData, rightEyeData)) {
            BigDecimal leftSpn = leftEyeData.getSph();
            BigDecimal leftCyl = leftEyeData.getCyl();

            BigDecimal rightSpn = rightEyeData.getSph();
            BigDecimal rightCyl = rightEyeData.getCyl();

            Integer leftMyopiaLevel = null;
            Integer rightMyopiaLevel = null;
            Integer seriousLevel = 0;

            if (Objects.nonNull(visionData)
                    && Objects.nonNull(age)
                    && ObjectsUtil.allNotNull(visionData.getLeftEyeData(), visionData.getRightEyeData())
                    && ObjectsUtil.allNotNull(visionData.getLeftEyeData().getNakedVision(), visionData.getRightEyeData().getNakedVision())) {
                BigDecimal leftNV = visionData.getLeftEyeData().getNakedVision();
                BigDecimal rightNV = visionData.getRightEyeData().getNakedVision();
                if (ObjectsUtil.allNotNull(leftSpn, leftCyl)) {
                    leftMyopiaLevel = StatUtil.getMyopiaLevel(leftSpn.setScale(2, RoundingMode.HALF_UP).floatValue(), leftCyl.setScale(2, RoundingMode.HALF_UP).floatValue(), age, leftNV.floatValue());
                }
                if (ObjectsUtil.allNotNull(rightSpn, rightCyl)) {
                    rightMyopiaLevel = StatUtil.getMyopiaLevel(rightSpn.setScale(2, RoundingMode.HALF_UP).floatValue(), rightCyl.setScale(2, RoundingMode.HALF_UP).floatValue(), age, rightNV.floatValue());
                }
                if (!ObjectsUtil.allNull(leftMyopiaLevel, rightMyopiaLevel)) {
                    seriousLevel = StatUtil.getSeriousLevel(leftMyopiaLevel, rightMyopiaLevel);
                }
            }
            //近视预警等级
            statConclusion.setMyopiaLevel(seriousLevel);
            //是否近视
            statConclusion.setIsMyopia(StatUtil.isMyopia(seriousLevel));
        }
    }


}
