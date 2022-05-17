package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 筛查结论和判断标准检查类
 *
 * @author hang.yuan 2022/5/16 23:38
 */
@Component
public class StatConclusionCheck {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;


    /**
     * 筛查学生数 ( m_screening_plan_school_student表 根据筛查计划ID统计)
     */
    public Integer getPlanScreeningNum(@NotNull Integer planId){
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningPlanId,planId);
        return screeningPlanSchoolStudentService.count(queryWrapper);
    }

    /**
     * 实际筛查学生数( m_stat_conclusion表 或 m_vision_screening_result表 根据筛查计划ID统计)
     */
    public Integer getRealScreeningNum(@NotNull Integer planId){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisionScreeningResult::getPlanId,planId);
        return visionScreeningResultService.count(queryWrapper);
    }

    /**
     * 完成率 ( 实际筛查学生数 /筛查学生数 * 100% )
     */
    public String getFinishRatio(@NotNull Integer planId){
        return MathUtil.ratio(getRealScreeningNum(planId),getPlanScreeningNum(planId));
    }


    /**
     * 纳入统计的学生数 (不包含勾选不配合选项的学生)（ m_stat_conclusion表 或 m_vision_screening_result表 和 StatUtil.isCompletedData 方法 ）
     */
    public ThreeTuple getStatisticsStudents(@NotNull Integer planId){
        List<VisionScreeningResult> visionScreeningResultList = getVisionScreeningResultList(planId);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)){
            //初筛数据完整性
            Predicate<VisionScreeningResult> predicate = visionScreeningResult -> StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
            int validDataNum = (int) visionScreeningResultList.stream().filter(predicate).count();
            //不配合检查的学生数
            Predicate<VisionScreeningResult> cooperative = visionScreeningResult -> Objects.equals(StatUtil.isCooperative(visionScreeningResult),1);
            int cooperativeNum = (int) visionScreeningResultList.stream().filter(cooperative).count();

            return new ThreeTuple(validDataNum-cooperativeNum,validDataNum,cooperative);
        }
        return null;
    }



    public List<VisionScreeningResult> getVisionScreeningResultList(@NotNull Integer planId){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisionScreeningResult::getPlanId,planId);
        return visionScreeningResultService.list(queryWrapper);
    }





}
