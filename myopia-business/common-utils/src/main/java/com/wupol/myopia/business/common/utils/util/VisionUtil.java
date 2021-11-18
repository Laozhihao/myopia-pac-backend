package com.wupol.myopia.business.common.utils.util;

import com.wupol.myopia.business.common.utils.constant.AstigmatismLevelEnum;
import com.wupol.myopia.business.common.utils.constant.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.HyperopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

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
    public String getVisionSummary(Integer glassesType, Integer myopiaLevel, Integer hyperopiaLevel, Integer astigmatismLevel) {
        List<String> resultList = new LinkedList<>();
        if (Objects.nonNull(glassesType)) {
            resultList.add(GlassesTypeEnum.getDescByCode(glassesType));
        }
        // 近视
        if (!MyopiaLevelEnum.ZERO.code.equals(myopiaLevel)) {
            resultList.add(MyopiaLevelEnum.getDesc(myopiaLevel));
        }
        // 远视
        if (!HyperopiaLevelEnum.ZERO.code.equals(hyperopiaLevel)) {
            resultList.add(HyperopiaLevelEnum.getDesc(hyperopiaLevel));
        }
        // 散光
        if (!AstigmatismLevelEnum.ZERO.code.equals(astigmatismLevel)) {
            resultList.add(AstigmatismLevelEnum.getDesc(astigmatismLevel));
        }
        return resultList.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }
}
