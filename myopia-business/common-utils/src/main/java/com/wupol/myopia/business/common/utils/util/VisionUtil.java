package com.wupol.myopia.business.common.utils.util;

import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 视力处理相关工具
 *
 * @Author HaoHao
 * @Date 2021/10/27
 **/
@UtilityClass
public class VisionUtil {

    /**
     * 获取视力的情况概述
     *
     * @param glassesType
     * @param myopiaLevel
     * @param hyperopiaLevel
     * @param astigmatismLevel
     * @return java.lang.String
     **/
    public String getVisionSummary(Integer glassesType, Integer myopiaLevel, Integer hyperopiaLevel, Integer astigmatismLevel,Integer screeningMyopia,Integer lowVision) {
        List<String> resultList = new LinkedList<>();
        if (Objects.nonNull(glassesType)) {
            resultList.add(GlassesTypeEnum.getDescByCode(glassesType));
        }
        if (Objects.nonNull(lowVision)){
            resultList.add(LowVisionLevelEnum.getDescByCode(lowVision));
        }
        //筛查性近视
        if (Objects.equals(MyopiaLevelEnum.SCREENING_MYOPIA.code,screeningMyopia)) {
            resultList.add(MyopiaLevelEnum.getDescByCode(screeningMyopia));
        }
        // 近视
        if (!MyopiaLevelEnum.ZERO.code.equals(myopiaLevel)) {
            resultList.add(MyopiaLevelEnum.getDescByCode(myopiaLevel));
        }
        // 远视
        if (!HyperopiaLevelEnum.ZERO.code.equals(hyperopiaLevel)) {
            resultList.add(HyperopiaLevelEnum.getDescByCode(hyperopiaLevel));
        }
        // 散光
        if (!AstigmatismLevelEnum.ZERO.code.equals(astigmatismLevel)) {
            resultList.add(AstigmatismLevelEnum.getDescByCode(astigmatismLevel));
        }
        return resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }

    /**
     * 获取视力情况
     *
     * @param glassesType
     * @param lowVision
     */
    public String getVisionSituation(Integer glassesType, Integer gradeType, Integer lowVision) {
        List<String> resultList = new LinkedList<>();
        if (Objects.nonNull(glassesType)) {
            resultList.add(WearingGlassesSituation.getType(glassesType));
        }
        if (LowVisionLevelEnum.lowVisionLevelCodeList().contains(lowVision)){
            if (SchoolAge.checkKindergarten(gradeType)){
                resultList.add("视力低常");
            }else {
                resultList.add("视力低下");
            }
        }
        if (Objects.equals(lowVision,LowVisionLevelEnum.ZERO.getCode())){
            resultList.add("正常");
        }
        return resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }

    /**
     * 获取屈光情况 (小学及以上)
     *
     * @param myopiaLevel
     * @param hyperopiaLevel
     * @param astigmatismLevel
     * @param screeningMyopia
     */
    public String getRefractionSituation(Integer myopiaLevel, Integer hyperopiaLevel, Integer astigmatismLevel, Integer screeningMyopia) {
        List<String> resultList = new LinkedList<>();

        //筛查性近视
        if (Objects.equals(MyopiaLevelEnum.SCREENING_MYOPIA.code, screeningMyopia)) {
            resultList.add(MyopiaLevelEnum.getDescByCode(screeningMyopia));
        }
        // 近视
        if (!MyopiaLevelEnum.ZERO.code.equals(myopiaLevel)) {
            resultList.add(MyopiaLevelEnum.getDescByCode(myopiaLevel));
        }
        // 远视
        if (!HyperopiaLevelEnum.ZERO.code.equals(hyperopiaLevel)) {
            resultList.add(HyperopiaLevelEnum.getDescByCode(hyperopiaLevel));
        }
        // 散光
        if (!AstigmatismLevelEnum.ZERO.code.equals(astigmatismLevel)) {
            resultList.add(AstigmatismLevelEnum.getDescByCode(astigmatismLevel));
        }
        if (CollectionUtils.isEmpty(resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()))) {
            resultList.add("正常");
        }

        return resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }

    /**
     * 获取屈光情况 (幼儿园)
     * @param isAnisometropia
     * @param isRefractiveError
     * @param visionLabel
     */
    public String getRefractionSituation(Boolean isAnisometropia, Boolean isRefractiveError, Integer visionLabel) {
        List<String> resultList = new LinkedList<>();

        //远视储备不足
        if (Objects.equals(visionLabel, WarningLevel.ZERO_SP.getCode())) {
            resultList.add("远视储备不足");
        }
        //屈光不正
        if (Objects.equals(isRefractiveError, Boolean.TRUE)) {
            resultList.add("屈光不正");
        }
        //屈光参差
        if (Objects.equals(isAnisometropia, Boolean.TRUE)) {
            resultList.add("屈光参差");
        }
        if (CollectionUtils.isEmpty(resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()))) {
            resultList.add("正常");
        }

        return resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }
}
