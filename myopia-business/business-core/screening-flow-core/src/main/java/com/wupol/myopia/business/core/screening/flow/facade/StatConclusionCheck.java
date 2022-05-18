package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 *
 * 筛查结论和判断标准检查类(基于已校验的正确数据验证判断标准是否正确或者是否被修改)
 *
 * @author hang.yuan 2022/5/16 23:38
 */
@Component
public class StatConclusionCheck {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private SchoolGradeService schoolGradeService;


    public DataCheckResult getCheckResult(Integer planId, Integer type ,Boolean isAll){
        DataCheckResult dataCheckResult = new DataCheckResult();
        switch (type){
            case 1:
                dataCheckResult.setPlanScreeningNum(getPlanScreeningNum(planId));
                if (!isAll){
                    break;
                }
            case 2:
                dataCheckResult.setRealScreeningNum(getRealScreeningNum(planId));
                if (!isAll){
                    break;
                }
            case 3:
                dataCheckResult.setFinishRatio(getFinishRatio(planId));
                if (!isAll){
                    break;
                }
            case 4:
                dataCheckResult.setIncludedStatistics(getStatisticsStudents(planId));
                if (!isAll){
                    break;
                }
            case 5:
                dataCheckResult.setLowVision(getLowVision(planId));
                dataCheckResult.setLowVisionLevel(getLowVisionLevel(planId));
                if (!isAll){
                    break;
                }
            case 6:
                dataCheckResult.setAverageVision(getAverageVision(planId));
                if (!isAll){
                    break;
                }
            case 7:
                dataCheckResult.setMyopia(getMyopia(planId));
                dataCheckResult.setMyopiaLevel(getMyopiaLevel(planId));
                if (!isAll){
                    break;
                }
            case 8:
                dataCheckResult.setRefractiveError(getRefractiveError(planId));
                if (!isAll){
                    break;
                }
            case 9:
                dataCheckResult.setWearingGlasses(getWearingGlasses(planId));
                if (!isAll){
                    break;
                }
            case 10:
                dataCheckResult.setHyperopia(getHyperopia(planId));
                dataCheckResult.setHyperopiaLevel(getHyperopiaLevel(planId));
                if (!isAll){
                    break;
                }
            case 11:
                dataCheckResult.setAstigmatism(getAstigmatism(planId));
                dataCheckResult.setAstigmatismLevel(getAstigmatismLevel(planId));
                if (!isAll){
                    break;
                }
            case 12:
                dataCheckResult.setMyopiaLevelInsufficient(getMyopiaLevelInsufficient(planId));
                if (!isAll){
                    break;
                }
            case 13:
                dataCheckResult.setCorrection(getCorrection(planId));
                if (!isAll){
                    break;
                }
            case 14:
                dataCheckResult.setAnisometropia(getAnisometropia(planId));
                if (!isAll){
                    break;
                }
            case 15:
                dataCheckResult.setWarningLevel(getWarningLevel(planId));
                if (!isAll){
                    break;
                }
            case 16:
                dataCheckResult.setRecommendVisit(getRecommendVisit(planId));
                if (!isAll){
                    break;
                }
            case 17:
                dataCheckResult.setRescreenData(getRescreenData(planId));
                if (!isAll){
                    break;
                }
            case 18:
                if (!isAll){
                    break;
                }
            default:
                break;
        }
        return dataCheckResult;
    }

    @Data
    public static class DataCheckResult{
        /**
         * 筛查学生数
         */
        private Integer planScreeningNum;
        /**
         * 实际筛查学生数
         */
        private Integer realScreeningNum;
        /**
         * 完成率
         */
        private String finishRatio;
        /**
         * 纳入统计的学生数，初筛数据完整性，不配合检查数
         */
        private ThreeTuple<Integer,Integer,Integer> includedStatistics;
        /**
         * 视力低下 人数/占比
         */
        private TwoTuple<Integer,String> lowVision;

        /**
         * 视力低下等级
         */
        private List<TwoTuple<Integer,Integer>> lowVisionLevel;
        /**
         * 平均视力
         */
        private TwoTuple<BigDecimal,BigDecimal> averageVision;
        /**
         * 近视 人数/占比
         */
        private TwoTuple<Integer,String> myopia;
        /**
         * 近视等级
         */
        private List<TwoTuple<Integer,Integer>> myopiaLevel;

        /**
         * 屈光不正 人数/占比
         */
        private TwoTuple<Integer,String> refractiveError;
        /**
         * 戴镜 人数/占比
         */
        public TwoTuple<Integer,String> wearingGlasses;
        /**
         * 远视 人数/占比
         */
        public TwoTuple<Integer,String> hyperopia;
        /**
         * 远视等级
         */
        public List<TwoTuple<Integer,Integer>> hyperopiaLevel;

        /**
         * 散光 人数/占比
         */
        public TwoTuple<Integer,String> astigmatism;
        /**
         * 散光等级
         */
        public List<TwoTuple<Integer,Integer>> astigmatismLevel;
        /**
         * 远视储备不足 人数/占比
         */
        public TwoTuple<Integer,String> myopiaLevelInsufficient;
        /**
         * 欠矫、足矫 (学生ID,矫数)
         */
        private List<TwoTuple<Integer,Integer>> correction;
        /**
         * 屈光参差 人数/占比
         */
        public TwoTuple<Integer,String> anisometropia;
        /**
         * 0-3级预警判断 
         */
        public List<TwoTuple<Integer,Integer>> warningLevel;

        /**
         * 建议就诊
         */
        public List<TwoTuple<Integer,Boolean>> recommendVisit;

        /**
         * 复测
         */
        public TwoTuple<Integer,String> rescreenData;


        /**
         * 无龋率
         */

        /**
         * 龋均
         */

        /**
         * 龋患率
         */

        /**
         * 龋失率
         */

        /**
         * 龋补率
         */

        /**
         * 龋患（失、补）率
         */

        /**
         * 龋患（失、补）构成比
         */

        /**
         * 龋患（失、补）构成比
         */

        /**
         * 肥胖率
         */

        /**
         * 营养不良率
         */

        /**
         * 生长迟缓率
         */

        /**
         * 脊柱弯曲检出率
         */

        /**
         * 姿势性脊柱侧弯检出率
         */

        /**
         * 姿势性脊柱后凸检出率
         */

        /**
         * 血压偏高率
         */

        /**
         * 复查学生率
         */
    }




    /**
     * 筛查学生数 ( m_screening_plan_school_student表 根据筛查计划ID统计)
     */
    private Integer getPlanScreeningNum(@NotNull Integer planId){
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningPlanId,planId);
        return screeningPlanSchoolStudentService.count(queryWrapper);
    }

    /**
     * 实际筛查学生数( m_stat_conclusion表 或 m_vision_screening_result表 根据筛查计划ID统计)
     */
    private Integer getRealScreeningNum(@NotNull Integer planId){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisionScreeningResult::getPlanId,planId);
        return visionScreeningResultService.count(queryWrapper);
    }

    /**
     * 完成率 ( 实际筛查学生数 /筛查学生数 * 100% )
     */
    private String getFinishRatio(@NotNull Integer planId){
        return MathUtil.ratio(getRealScreeningNum(planId),getPlanScreeningNum(planId));
    }


    /**
     * 纳入统计的学生数 (不包含勾选不配合选项的学生)（ m_stat_conclusion表 或 m_vision_screening_result表 和 StatUtil.isCompletedData 方法 ）
     */
    private ThreeTuple<Integer,Integer,Integer> getStatisticsStudents(@NotNull Integer planId){
        List<VisionScreeningResult> visionScreeningResultList = getVisionScreeningResultList(planId,Boolean.FALSE);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)){
            //初筛数据完整性
            Predicate<VisionScreeningResult> predicate = visionScreeningResult -> StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
            int validDataNum = (int) visionScreeningResultList.stream().filter(predicate).count();
            //不配合检查的学生数
            Predicate<VisionScreeningResult> cooperative = visionScreeningResult -> Objects.equals(StatUtil.isCooperative(visionScreeningResult),1);
            int cooperativeNum = (int) visionScreeningResultList.stream().filter(cooperative).count();

            return new ThreeTuple<>(validDataNum-cooperativeNum,validDataNum,cooperativeNum);
        }
        return null;
    }


    /**
     * 视力不良/视力低下 人数/占比
     */
    private TwoTuple<Integer,String> getLowVision(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean leftLowVision = StatUtil.isLowVision(basicData.leftNakedVision, screeningPlanSchoolStudent.getStudentAge());
                Boolean rightLowVision = StatUtil.isLowVision(basicData.rightNakedVision, screeningPlanSchoolStudent.getStudentAge());
                return StatUtil.getIsExist(rightLowVision, leftLowVision);
            }).collect(Collectors.toList());
            int lowVisionNum = tupleList.size();
            return TwoTuple.of(lowVisionNum,MathUtil.ratio(lowVisionNum,size));
        }
        return null;
    }

    /**
     * 视力低下等级
     */
    private List<TwoTuple<Integer,Integer>> getLowVisionLevel(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .filter(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        return screeningPlanSchoolStudent.getStudentAge() > 6;
                    })
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        LowVisionLevelEnum leftLowVision = StatUtil.getLowVisionLevel(basicData.leftNakedVision, screeningPlanSchoolStudent.getStudentAge());
                        LowVisionLevelEnum rightLowVision = StatUtil.getLowVisionLevel(basicData.rightNakedVision, screeningPlanSchoolStudent.getStudentAge());
                        Integer seriousLevel = StatUtil.getSeriousLevel(rightLowVision, leftLowVision);
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), seriousLevel);
                    }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 平均视力
     */
    private TwoTuple<BigDecimal,BigDecimal> getAverageVision (@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            List<StatConclusion> statConclusionList = dataList.stream().map(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                return  new StatConclusion().setIsValid(Boolean.TRUE).setVisionL(basicData.leftNakedVision).setVisionR(basicData.rightNakedVision);
            }).collect(Collectors.toList());

            return StatUtil.calculateAverageVision(statConclusionList);
        }
        return null;
    }

    /**
     * 近视 人数/占比
     */
    private TwoTuple<Integer,String> getMyopia(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean leftMyopia = StatUtil.isMyopia(basicData.leftSph,basicData.leftCyl, screeningPlanSchoolStudent.getStudentAge(),basicData.leftNakedVision);
                Boolean rightMyopia = StatUtil.isMyopia(basicData.rightSph,basicData.rightCyl, screeningPlanSchoolStudent.getStudentAge(),basicData.rightNakedVision);
                return StatUtil.getIsExist(leftMyopia, rightMyopia);
            }).collect(Collectors.toList());
            int myopia = tupleList.size();
            return TwoTuple.of(myopia,MathUtil.ratio(myopia,size));
        }
        return null;
    }

    /**
     * 近视等级
     */
    private List<TwoTuple<Integer,Integer>> getMyopiaLevel(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        MyopiaLevelEnum leftMyopiaLevel = StatUtil.getMyopiaLevel(basicData.leftSph, basicData.leftCyl);
                        MyopiaLevelEnum rightMyopiaLevel = StatUtil.getMyopiaLevel(basicData.rightSph,basicData.rightCyl);
                        Integer seriousLevel = StatUtil.getSeriousLevel(leftMyopiaLevel, rightMyopiaLevel);
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), seriousLevel);
                    }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 屈光不正 人数/占比
     */
    private TwoTuple<Integer,String> getRefractiveError(@NotNull Integer planId){
        String clientId = "1";
        boolean zeroToSixPlatform = Objects.equals(SystemCode.PRESCHOOL_CLIENT.getCode() + StrUtil.EMPTY, clientId);
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean leftRefractiveError = StatUtil.isRefractiveError(basicData.leftSph,basicData.leftCyl, screeningPlanSchoolStudent.getStudentAge(),zeroToSixPlatform);
                Boolean rightRefractiveError = StatUtil.isRefractiveError(basicData.rightSph,basicData.rightCyl, screeningPlanSchoolStudent.getStudentAge(),zeroToSixPlatform);
                return StatUtil.getIsExist(leftRefractiveError, rightRefractiveError);
            }).collect(Collectors.toList());
            int refractiveError = tupleList.size();
            return TwoTuple.of(refractiveError,MathUtil.ratio(refractiveError,size));
        }
        return null;
    }

    /**
     * 戴镜 人数/占比
     */
    private TwoTuple<Integer,String> getWearingGlasses(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                return basicData.isWearingGlasses;
            }).collect(Collectors.toList());
            int wearingGlasses = tupleList.size();
            return TwoTuple.of(wearingGlasses,MathUtil.ratio(wearingGlasses,size));
        }
        return null;
    }

    /**
     * 远视 人数/占比
     */
    private TwoTuple<Integer,String> getHyperopia(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean leftHyperopia = StatUtil.isHyperopia(basicData.leftSph,basicData.leftCyl, screeningPlanSchoolStudent.getStudentAge());
                Boolean rightHyperopia = StatUtil.isHyperopia(basicData.rightSph,basicData.rightCyl, screeningPlanSchoolStudent.getStudentAge());
                return StatUtil.getIsExist(leftHyperopia, rightHyperopia);
            }).collect(Collectors.toList());
            int hyperopia = tupleList.size();
            return TwoTuple.of(hyperopia,MathUtil.ratio(hyperopia,size));
        }
        return null;
    }

    /**
     * 远视等级
     */
    private List<TwoTuple<Integer,Integer>> getHyperopiaLevel(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        HyperopiaLevelEnum leftHyperopiaLevel = StatUtil.getHyperopiaLevel(basicData.leftSph, basicData.leftCyl, screeningPlanSchoolStudent.getStudentAge());
                        HyperopiaLevelEnum rightHyperopiaLevel = StatUtil.getHyperopiaLevel(basicData.rightSph,basicData.rightCyl, screeningPlanSchoolStudent.getStudentAge());
                        Integer seriousLevel = StatUtil.getSeriousLevel(leftHyperopiaLevel, rightHyperopiaLevel);
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), seriousLevel);
                    }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 散光 人数/占比
     */
    private TwoTuple<Integer,String> getAstigmatism(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean leftAstigmatism = StatUtil.isAstigmatism(basicData.leftCyl);
                Boolean rightAstigmatism = StatUtil.isAstigmatism(basicData.rightCyl);
                return StatUtil.getIsExist(leftAstigmatism, rightAstigmatism);
            }).collect(Collectors.toList());
            int astigmatism = tupleList.size();
            return TwoTuple.of(astigmatism,MathUtil.ratio(astigmatism,size));
        }
        return null;
    }

    /**
     * 散光等级
     */
    private List<TwoTuple<Integer,Integer>> getAstigmatismLevel(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        AstigmatismLevelEnum leftAstigmatismLevel = StatUtil.getAstigmatismLevel( basicData.leftCyl);
                        AstigmatismLevelEnum rightAstigmatismLevel = StatUtil.getAstigmatismLevel(basicData.rightCyl);
                        Integer seriousLevel = StatUtil.getSeriousLevel(leftAstigmatismLevel, rightAstigmatismLevel);
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), seriousLevel);
                    }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 远视储备不足 人数/占比
     */
    private TwoTuple<Integer,String> getMyopiaLevelInsufficient(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<Integer> tupleList = dataList.stream().map(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                WarningLevel left = StatUtil.myopiaLevelInsufficient(basicData.leftSph, basicData.leftCyl);
                WarningLevel right = StatUtil.myopiaLevelInsufficient(basicData.rightSph, basicData.rightCyl);
                return StatUtil.getSeriousLevel(left, right);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            int myopiaLevelInsufficient = tupleList.size();
            return TwoTuple.of(myopiaLevelInsufficient,MathUtil.ratio(myopiaLevelInsufficient,size));
        }
        return null;
    }

    /**
     * 欠矫、足矫 (学生ID,矫数)
     */
    private List<TwoTuple<Integer,Integer>> getCorrection(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream().map(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                Integer schoolType = Optional.ofNullable(tuple.getThird()).map(code->{
                    GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(code);
                    if (Objects.equals(SchoolAge.KINDERGARTEN.code,gradeCodeEnum.getType())){
                        return SchoolEnum.TYPE_KINDERGARTEN.getType();
                    }else {
                        return SchoolEnum.TYPE_PRIMARY.getType();
                    }
                }).orElse(null);
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Integer correction = StatUtil.correction(basicData.leftNakedVision, basicData.rightNakedVision,
                        basicData.leftCorrectVision, basicData.rightCorrectVision, schoolType, screeningPlanSchoolStudent.getStudentAge(), basicData.isWearingGlasses);
                return TwoTuple.of(screeningPlanSchoolStudent.getId(),correction);
            }).filter(tuple->Objects.nonNull(tuple.getFirst())).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 屈光参差 人数/占比
     */
    private TwoTuple<Integer,String> getAnisometropia(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            int size = dataList.size();
            List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> tupleList = dataList.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                Boolean anisometropiaVision = StatUtil.isAnisometropiaVision(basicData.leftSph,basicData.rightSph);
                Boolean anisometropiaAstigmatism = StatUtil.isAnisometropiaAstigmatism(basicData.leftCyl,basicData.rightCyl);
                return StatUtil.getIsExist(anisometropiaVision, anisometropiaAstigmatism);
            }).collect(Collectors.toList());
            int anisometropia = tupleList.size();
            return TwoTuple.of(anisometropia,MathUtil.ratio(anisometropia,size));
        }
        return null;
    }


    /**
     * 0-3级预警判断 （满足其一即可判断预警级别）
     * 裸眼视力数据
     * 屈光数据（近视、散光）【仅针对小学及以上学生】
     * 屈光数据（远视）【仅针对小学及以上学生】
     */
    private List<TwoTuple<Integer,Integer>> getWarningLevel(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        Integer warningLevelInt = StatUtil.getWarningLevelInt(basicData.leftCyl,basicData.leftSph,basicData.leftNakedVision,
                                basicData.rightCyl,basicData.rightSph,basicData.rightNakedVision,screeningPlanSchoolStudent.getStudentAge(),screeningPlanSchoolStudent.getGradeType());
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), warningLevelInt);
                    }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 建议就诊
     */
    private List<TwoTuple<Integer,Boolean>> getRecommendVisit(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> dataList = getDataList(planId);
        if (CollectionUtil.isNotEmpty(dataList)){
            return dataList.stream()
                    .map(tuple -> {
                        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                        VisionScreeningResult visionScreeningResult = tuple.getSecond();
                        Boolean otherEyeDiseasesNormal = Optional.ofNullable(visionScreeningResult.getOtherEyeDiseases()).map(OtherEyeDiseasesDO::isNormal).orElse(null);
                        BasicData basicData = dealWithData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
                        Boolean isRecommendVisit = ScreeningResultUtil.getDoctorAdvice(
                                basicData.leftNakedVision,basicData.rightNakedVision,
                                basicData.leftCorrectVision,basicData.rightCorrectVision,
                                basicData.glassesType,screeningPlanSchoolStudent.getGradeType(),
                                screeningPlanSchoolStudent.getStudentAge(),otherEyeDiseasesNormal,
                                visionScreeningResult.getComputerOptometry()).getIsRecommendVisit();
                        return TwoTuple.of(screeningPlanSchoolStudent.getId(), isRecommendVisit);
                    }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 复测
     */
    private TwoTuple<Integer,String> getRescreenData(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, TwoTuple<VisionScreeningResult, VisionScreeningResult>, String>> rescreenDataList = getRescreenDataList(planId);
        if (CollectionUtil.isNotEmpty(rescreenDataList)){
            Map<Boolean, List<ThreeTuple<Boolean, Integer, Integer>>> tupleMap = rescreenDataList.stream().map(threeTuple -> {
                TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple = threeTuple.getSecond();
                BasicData basicData = dealWithData(tuple.getFirst().getVisionData(), tuple.getFirst().getComputerOptometry());

                int calculateItemNum = StatUtil.calculateItemNum(tuple.getFirst());
                int calculateErrorNum = StatUtil.calculateErrorNum(tuple.getFirst(), tuple.getSecond(), basicData.isWearingGlasses);
                return new ThreeTuple<>(basicData.isWearingGlasses, calculateItemNum, calculateErrorNum);
            }).collect(Collectors.groupingBy(ThreeTuple::getFirst));

            //未戴镜
            List<ThreeTuple<Boolean, Integer, Integer>> falseTuples = tupleMap.get(Boolean.FALSE);
            int withoutGlassesSize = falseTuples.size();
            int falseCalculateItemNumSum = falseTuples.stream().mapToInt(ThreeTuple::getSecond).sum();
            int falseCalculateErrorNumSum = falseTuples.stream().mapToInt(ThreeTuple::getThird).sum();
            //戴镜
            List<ThreeTuple<Boolean, Integer, Integer>> trueTuples = tupleMap.get(Boolean.TRUE);
            int wearingGlassesSize = trueTuples.size();
            int trueCalculateItemNumSum = trueTuples.stream().mapToInt(ThreeTuple::getSecond).sum();
            int trueCalculateErrorNumSum = trueTuples.stream().mapToInt(ThreeTuple::getThird).sum();

            String ratio = MathUtil.ratio(falseCalculateErrorNumSum + trueCalculateErrorNumSum, falseCalculateItemNumSum * withoutGlassesSize + trueCalculateItemNumSum * wearingGlassesSize);
            return TwoTuple.of(planId,ratio);
        }
        return null;
    }


    /**
     * 无龋率
     */
    private TwoTuple<Integer,String> getSaprodontiaFree(@NotNull Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> freeList = commonDiseaseScreeningNum.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                SaprodontiaDataDO saprodontiaData = visionScreeningResult.getSaprodontiaData();
                if (Objects.isNull(saprodontiaData)) {
                    return true;
                }
                Set<SaprodontiaDataDO.SaprodontiaItem> saprodontias = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("d", "D"));
                Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaLoss = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("m", "M"));
                Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaRepair = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("f", "F"));
                return CollectionUtil.isEmpty(saprodontias) && CollectionUtil.isEmpty(saprodontiaLoss) && CollectionUtil.isEmpty(saprodontiaRepair);
            }).collect(Collectors.toList());

            return TwoTuple.of(freeList.size(),MathUtil.ratio(freeList.size(),commonDiseaseScreeningNum.size()));
        }

        return null;
    }

    /**
     * 龋均
     */
    private TwoTuple<Integer,String> getDmft(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int teethNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                ToIntFunction<SaprodontiaData> totalFunction = sc -> Optional.ofNullable(sc.saprodontiaTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaLossTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaRepairTeeth).orElse(0);
                teethNum = saprodontiaData.stream().mapToInt(totalFunction).sum();
            }
            return TwoTuple.of(teethNum,MathUtil.num(teethNum,commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 龋患率
     */
    private TwoTuple<Integer,String> getSaprodontia(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int saprodontiaNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                saprodontiaNum = (int) saprodontiaData.stream().map(sc->sc.isSaprodontia).filter(Objects::nonNull).count();
            }
            return TwoTuple.of(saprodontiaNum,MathUtil.num(saprodontiaNum,commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 龋失率
     */
    private TwoTuple<Integer,String> getSaprodontiaLoss(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int saprodontiaLossNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                saprodontiaLossNum = (int) saprodontiaData.stream().map(sc->sc.isSaprodontiaLoss).filter(Objects::nonNull).count();
            }
            return TwoTuple.of(saprodontiaLossNum,MathUtil.num(saprodontiaLossNum,commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 龋补率
     */
    private TwoTuple<Integer,String> getSaprodontiaRepair(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int saprodontiaRepairNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                saprodontiaRepairNum = (int) saprodontiaData.stream().map(sc->sc.isSaprodontiaRepair).filter(Objects::nonNull).count();
            }
            return TwoTuple.of(saprodontiaRepairNum,MathUtil.num(saprodontiaRepairNum,commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 龋患（失、补）率
     */
    private TwoTuple<Integer,String> getSaprodontiaLossAndRepair(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int saprodontiaLossAndRepairNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                saprodontiaLossAndRepairNum = (int) saprodontiaData.stream().filter(sc ->Objects.equals(Boolean.TRUE,sc.isSaprodontiaLoss) || Objects.equals(Boolean.TRUE,sc.isSaprodontiaRepair)).count();
            }
            return TwoTuple.of(saprodontiaLossAndRepairNum,MathUtil.num(saprodontiaLossAndRepairNum,commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 龋患（失、补）构成比
     */
    private TwoTuple<Integer,String> getSaprodontiaLossAndRepairTeeth(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<SaprodontiaData> saprodontiaData = getSaprodontiaData(commonDiseaseScreeningNum);
            int totalTeethNum=0;
            int saprodontiaLossAndRepairTeethNum=0;
            if (CollectionUtil.isNotEmpty(saprodontiaData)){
                ToIntFunction<SaprodontiaData> totalFunction = sc -> Optional.ofNullable(sc.saprodontiaTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaLossTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaRepairTeeth).orElse(0);
                totalTeethNum =  saprodontiaData.stream().mapToInt(totalFunction).sum();
                ToIntFunction<SaprodontiaData> lossAndRepairFunction = sc -> Optional.ofNullable(sc.saprodontiaTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaLossTeeth).orElse(0) + Optional.ofNullable(sc.saprodontiaRepairTeeth).orElse(0);
                saprodontiaLossAndRepairTeethNum =  saprodontiaData.stream().mapToInt(lossAndRepairFunction).sum();
            }
            return TwoTuple.of(saprodontiaLossAndRepairTeethNum,MathUtil.num(saprodontiaLossAndRepairTeethNum,totalTeethNum));
        }
        return null;
    }

    /**
     * 超重率
     */
    private TwoTuple<Integer,String> getOverweight(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<Boolean> tupleList = commonDiseaseScreeningNum.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                return Objects.nonNull(visionScreeningResult.getHeightAndWeightData()) && visionScreeningResult.getHeightAndWeightData().valid();
            }).map(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
                HeightAndWeightDataDO heightAndWeightData = tuple.getSecond().getHeightAndWeightData();
                TwoTuple<Boolean, Boolean> overweightAndObesity = StatUtil.isOverweightAndObesity(heightAndWeightData.getBmi(), ageTuple.getSecond(), screeningPlanSchoolStudent.getGender());
                if (Objects.nonNull(overweightAndObesity)){
                    return overweightAndObesity.getFirst();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return TwoTuple.of(tupleList.size(),MathUtil.ratio(tupleList.size(),commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 肥胖率
     */
    private TwoTuple<Integer,String> getObesity(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<Boolean> tupleList = commonDiseaseScreeningNum.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                return Objects.nonNull(visionScreeningResult.getHeightAndWeightData()) && visionScreeningResult.getHeightAndWeightData().valid();
            }).map(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
                HeightAndWeightDataDO heightAndWeightData = tuple.getSecond().getHeightAndWeightData();
                TwoTuple<Boolean, Boolean> overweightAndObesity = StatUtil.isOverweightAndObesity(heightAndWeightData.getBmi(), ageTuple.getSecond(), screeningPlanSchoolStudent.getGender());
                if (Objects.nonNull(overweightAndObesity)){
                    return overweightAndObesity.getSecond();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return TwoTuple.of(tupleList.size(),MathUtil.ratio(tupleList.size(),commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 营养不良率
     */
    private TwoTuple<Integer,String> getMalnutrition(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<Boolean> tupleList = commonDiseaseScreeningNum.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                return Objects.nonNull(visionScreeningResult.getHeightAndWeightData()) && visionScreeningResult.getHeightAndWeightData().valid();
            }).map(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
                HeightAndWeightDataDO heightAndWeightData = tuple.getSecond().getHeightAndWeightData();
                Boolean wasting = StatUtil.isWasting(heightAndWeightData.getBmi(), ageTuple.getSecond(), screeningPlanSchoolStudent.getGender());
                Boolean stunting = StatUtil.isStunting(screeningPlanSchoolStudent.getGender(), ageTuple.getSecond(), heightAndWeightData.getHeight());
                if (ObjectsUtil.hasNull(wasting,stunting)){
                    return null;
                }
                return wasting && stunting;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return TwoTuple.of(tupleList.size(),MathUtil.ratio(tupleList.size(),commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 生长迟缓率
     */
    private TwoTuple<Integer,String> getStunting(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum = getCommonDiseaseScreeningNum(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            List<Boolean> tupleList = commonDiseaseScreeningNum.stream().filter(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getSecond();
                return Objects.nonNull(visionScreeningResult.getHeightAndWeightData()) && visionScreeningResult.getHeightAndWeightData().valid();
            }).map(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                TwoTuple<Integer, String> ageTuple = StatUtil.getAge(screeningPlanSchoolStudent.getBirthday());
                HeightAndWeightDataDO heightAndWeightData = tuple.getSecond().getHeightAndWeightData();
                return StatUtil.isStunting(screeningPlanSchoolStudent.getGender(), ageTuple.getSecond(), heightAndWeightData.getHeight());
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return TwoTuple.of(tupleList.size(),MathUtil.ratio(tupleList.size(),commonDiseaseScreeningNum.size()));
        }
        return null;
    }

    /**
     * 脊柱弯曲检出率
     */

    /**
     * 姿势性脊柱侧弯检出率
     */

    /**
     * 姿势性脊柱后凸检出率
     */

    /**
     * 血压偏高率
     */

    /**
     * 复查学生率
     */



    static class SaprodontiaData{
        /**
         * 是否龋患
         */
        private Boolean isSaprodontia;
        /**
         * 龋患牙齿数
         */
        private Integer saprodontiaTeeth;
        /**
         * 是否龋失
         */
        private Boolean isSaprodontiaLoss;
        /**
         * 龋失牙齿数
         */
        private Integer saprodontiaLossTeeth;
        /**
         * 是否龋补
         */
        private Boolean isSaprodontiaRepair;
        /**
         * 龋补牙齿数
         */
        private Integer saprodontiaRepairTeeth;
    }
    /**
     * 龋齿数据
     */
    private List<SaprodontiaData>  getSaprodontiaData(List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseScreeningNum){
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningNum)){
            return commonDiseaseScreeningNum.stream().filter(threeTuple -> {
                    VisionScreeningResult visionScreeningResult = threeTuple.getSecond();
                    return Objects.nonNull(visionScreeningResult.getSaprodontiaData());
                }).map(threeTuple -> {
                    SaprodontiaData saprodontia = new SaprodontiaData();
                    SaprodontiaDataDO saprodontiaData = threeTuple.getSecond().getSaprodontiaData();
                    Set<SaprodontiaDataDO.SaprodontiaItem> saprodontias = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("d", "D"));
                    Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaLoss = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("m", "M"));
                    Set<SaprodontiaDataDO.SaprodontiaItem> saprodontiaRepair = StatUtil.getSaprodontia(saprodontiaData, Lists.newArrayList("f", "F"));
                    saprodontia.isSaprodontia = CollectionUtil.isNotEmpty(saprodontias);
                    saprodontia.saprodontiaTeeth = CollectionUtil.isNotEmpty(saprodontias) ? saprodontias.size() : 0;
                    saprodontia.isSaprodontiaLoss = CollectionUtil.isNotEmpty(saprodontiaLoss);
                    saprodontia.saprodontiaLossTeeth = CollectionUtil.isNotEmpty(saprodontiaLoss) ? saprodontias.size() : 0;
                    saprodontia.isSaprodontiaRepair = CollectionUtil.isNotEmpty(saprodontiaRepair);
                    saprodontia.saprodontiaRepairTeeth = CollectionUtil.isNotEmpty(saprodontiaRepair) ? saprodontias.size() : 0;
                    return saprodontia;
                }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 纳入常见病统计筛查人数(小学及以上的实际筛查人数)
     */
    private List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>>  getCommonDiseaseScreeningNum(Integer planId){
        List<ThreeTuple<ScreeningPlanSchoolStudent, VisionScreeningResult, String>> commonDiseaseDataList = getCommonDiseaseDataList(planId);
        if (CollectionUtil.isNotEmpty(commonDiseaseDataList)){
            return commonDiseaseDataList.stream().filter(tuple -> {
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = tuple.getFirst();
                return !Objects.equals(SchoolAge.KINDERGARTEN.code, screeningPlanSchoolStudent.getGradeType());
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }


    /**
     * 常见病计算数据
     */
    private List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> getCommonDiseaseDataList(@NotNull Integer planId){
        List<VisionScreeningResult> visionScreeningResultList = getVisionScreeningResultList(planId,Boolean.FALSE);
        List<TwoTuple<ScreeningPlanSchoolStudent,String>> screeningPlanSchoolStudentList = getScreeningPlanSchoolStudentList(planId);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)
                && CollectionUtil.isNotEmpty(screeningPlanSchoolStudentList)){
            Map<Integer, TwoTuple<ScreeningPlanSchoolStudent,String>> planSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(tuple-> tuple.getFirst().getId(), Function.identity()));

            return visionScreeningResultList.stream().map(visionScreeningResult -> {
                Integer screeningPlanSchoolStudentId = visionScreeningResult.getScreeningPlanSchoolStudentId();
                TwoTuple<ScreeningPlanSchoolStudent,String> tuple = planSchoolStudentMap.get(screeningPlanSchoolStudentId);
                return new ThreeTuple<>(tuple.getFirst(),visionScreeningResult,tuple.getSecond());
            }).collect(Collectors.toList());

        }
        return null;
    }

    /**
     * 视力计算数据
     */
    private List<ThreeTuple<ScreeningPlanSchoolStudent,VisionScreeningResult,String>> getDataList(@NotNull Integer planId){
        List<VisionScreeningResult> validScreeningDataList = getValidScreeningDataList(planId);
        List<TwoTuple<ScreeningPlanSchoolStudent,String>> screeningPlanSchoolStudentList = getScreeningPlanSchoolStudentList(planId);
        if (CollectionUtil.isNotEmpty(validScreeningDataList)
                && CollectionUtil.isNotEmpty(screeningPlanSchoolStudentList)){
            Map<Integer, TwoTuple<ScreeningPlanSchoolStudent,String>> planSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(tuple-> tuple.getFirst().getId(), Function.identity()));

            return validScreeningDataList.stream().map(visionScreeningResult -> {
                Integer screeningPlanSchoolStudentId = visionScreeningResult.getScreeningPlanSchoolStudentId();
                TwoTuple<ScreeningPlanSchoolStudent,String> tuple = planSchoolStudentMap.get(screeningPlanSchoolStudentId);
                return new ThreeTuple<>(tuple.getFirst(),visionScreeningResult,tuple.getSecond());
            }).collect(Collectors.toList());

        }
        return null;

    }

    /**
     * 复测计算数据
     */
    private List<ThreeTuple<ScreeningPlanSchoolStudent,TwoTuple<VisionScreeningResult,VisionScreeningResult>,String>> getRescreenDataList(@NotNull Integer planId){
        List<TwoTuple<VisionScreeningResult,VisionScreeningResult>> validScreeningDataList = getRescreenList(planId);
        List<TwoTuple<ScreeningPlanSchoolStudent,String>> screeningPlanSchoolStudentList = getScreeningPlanSchoolStudentList(planId);
        if (CollectionUtil.isNotEmpty(validScreeningDataList)
                && CollectionUtil.isNotEmpty(screeningPlanSchoolStudentList)){
            Map<Integer, TwoTuple<ScreeningPlanSchoolStudent,String>> planSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(tuple-> tuple.getFirst().getId(), Function.identity()));

            return validScreeningDataList.stream().map(tuple -> {
                VisionScreeningResult visionScreeningResult = tuple.getFirst();
                Integer screeningPlanSchoolStudentId = visionScreeningResult.getScreeningPlanSchoolStudentId();
                TwoTuple<ScreeningPlanSchoolStudent,String> schoolStudentStringTuple = planSchoolStudentMap.get(screeningPlanSchoolStudentId);
                return new ThreeTuple<>(schoolStudentStringTuple.getFirst(),tuple,schoolStudentStringTuple.getSecond());
            }).collect(Collectors.toList());

        }
        return null;

    }

    /**
     * 纳入统计数据
     */
    private List<VisionScreeningResult> getValidScreeningDataList(@NotNull Integer planId){
        List<VisionScreeningResult> visionScreeningResultList = getVisionScreeningResultList(planId,Boolean.FALSE);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)){
            Predicate<VisionScreeningResult> predicate = visionScreeningResult -> StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
            Predicate<VisionScreeningResult> cooperative = visionScreeningResult -> Objects.equals(StatUtil.isCooperative(visionScreeningResult),0);
            return visionScreeningResultList.stream().filter(predicate).filter(cooperative).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private List<TwoTuple<ScreeningPlanSchoolStudent,String>> getScreeningPlanSchoolStudentList(@NotNull Integer planId){
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningPlanId,planId);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(screeningPlanSchoolStudentList)){
            Set<Integer> gradeIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());
            List<SchoolGrade> schoolGradeList = schoolGradeService.getByIds(Lists.newArrayList(gradeIds));
            Map<Integer, SchoolGrade> schoolGradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
            return screeningPlanSchoolStudentList.stream().map(screeningPlanSchoolStudent->{
                SchoolGrade schoolGrade = schoolGradeMap.get(screeningPlanSchoolStudent.getGradeId());
                return TwoTuple.of(screeningPlanSchoolStudent,schoolGrade.getGradeCode());
            }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 复测数据
     */
    private List<TwoTuple<VisionScreeningResult,VisionScreeningResult>> getRescreenList(@NotNull Integer planId){
        List<VisionScreeningResult> visionScreeningResultList = getVisionScreeningResultList(planId,null);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)){
            Predicate<VisionScreeningResult> predicate = StatUtil::rescreenCompletedData;
            Predicate<VisionScreeningResult> cooperative = visionScreeningResult -> Objects.equals(StatUtil.isCooperative(visionScreeningResult),0);
            List<VisionScreeningResult> visionScreeningResults = visionScreeningResultList.stream().filter(predicate).filter(cooperative).collect(Collectors.toList());
            Map<String, List<VisionScreeningResult>> vsMap = visionScreeningResults.stream().collect(Collectors.groupingBy(vs -> getKey(vs.getScreeningType(), vs.getStudentId())));
            return vsMap.values().stream().map(list->{
                TwoTuple<VisionScreeningResult,VisionScreeningResult> tuple = new TwoTuple<>();
                for (VisionScreeningResult visionScreeningResult : list) {
                    if (visionScreeningResult.getIsDoubleScreen()) {
                        tuple.setFirst(visionScreeningResult);
                    }else {
                        tuple.setSecond(visionScreeningResult);
                    }
                }
                return tuple;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }


    private String getKey(Integer screeningType ,Integer studentId){
        return screeningType+"_"+studentId;
    }

    private List<VisionScreeningResult> getVisionScreeningResultList(@NotNull Integer planId,
                                                                    @NotNull Boolean isRescreen){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisionScreeningResult::getPlanId,planId);
        Optional.ofNullable(isRescreen).ifPresent(rescreen-> queryWrapper.eq(VisionScreeningResult::getIsDoubleScreen,rescreen));
        return visionScreeningResultService.list(queryWrapper);
    }

    static class BasicData {
        private BigDecimal leftCyl;
        private BigDecimal rightCyl;
        private BigDecimal leftSph;
        private BigDecimal rightSph;
        private BigDecimal rightNakedVision;
        private BigDecimal leftNakedVision;
        private BigDecimal leftCorrectVision;
        private BigDecimal rightCorrectVision;
        private Integer glassesType;
        private Boolean isWearingGlasses;
    }

    /**
     * 处理数据
     *
     * @param visionData
     */
    private static BasicData dealWithData(VisionDataDO visionData,ComputerOptometryDO computerOptometry) {
        BasicData basicData = new BasicData();
        Optional<VisionDataDO> visionDataDO = Optional.ofNullable(visionData);
        basicData.glassesType = visionDataDO.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
        basicData.isWearingGlasses = Optional.ofNullable(basicData.glassesType).map(g->g>0).orElse(null);
        basicData.leftNakedVision = visionDataDO.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getNakedVision).orElse(null);
        basicData.leftCorrectVision = visionDataDO.map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getCorrectedVision).orElse(null);
        basicData.rightNakedVision = visionDataDO.map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getNakedVision).orElse(null);
        basicData.rightCorrectVision = visionDataDO.map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getCorrectedVision).orElse(null);

        Optional<ComputerOptometryDO> computerOptometryDO = Optional.ofNullable(computerOptometry);
        basicData.leftCyl =  computerOptometryDO.map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).orElse(null);
        basicData.rightCyl = computerOptometryDO.map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).orElse(null);
        basicData.leftSph = computerOptometryDO.map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).orElse(null);
        basicData.rightSph = computerOptometryDO.map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).orElse(null);

        return basicData;

    }





}
