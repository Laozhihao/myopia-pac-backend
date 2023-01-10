package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/4 18:08
 */
@Data
public class StatBaseDTO {

    private List<StatConclusion> firstScreen;
    private List<StatConclusion> valid;
    private List<Integer> WaitingRepairIds;

    public StatBaseDTO(List<StatConclusion> statConclusions) {
        if (Objects.isNull(statConclusions)) {
            statConclusions = new ArrayList<>();
        }
        firstScreen = statConclusions.stream().filter(x -> Boolean.FALSE.equals(x.getIsRescreen()))
                        .collect(Collectors.toList());
        valid = firstScreen.stream().filter(x -> Boolean.TRUE.equals(x.getIsValid()))
                .collect(Collectors.toList());
    }

    /**
     * 获取需要修复的数据的原始数据id集
     * @return
     */
    public List<Integer> getWaitingRepairResultIds() {
        return valid.stream().map(StatConclusion::getResultId).collect(Collectors.toList());
    }

    public void dataRepair(Map<Integer, VisionScreeningResult> resultMap) {
        // 重新计算是否近视、矫正情况
        valid.forEach(stat -> {
            if (Objects.isNull(stat.getIsMyopia())) {
                stat.setIsMyopia(isMyopia(resultMap.get(stat.getResultId())));
            }
            stat.setVisionCorrection(setVisionCorrection(stat, resultMap.get(stat.getResultId())));
        });
    }

    private Boolean isMyopia(VisionScreeningResult result) {
        if (Objects.isNull(result)) {
            return false;
        }

        Boolean leftMyopia = StatUtil.isMyopia(EyeDataUtil.leftSph(result), EyeDataUtil.leftSph(result), EyeDataUtil.leftNakedVision(result));
        Boolean rightMyopia = StatUtil.isMyopia(EyeDataUtil.rightSph(result), EyeDataUtil.rightSph(result), EyeDataUtil.rightNakedVision(result));
        if (ObjectsUtil.allNull(leftMyopia, rightMyopia)) {
            return false;
        }
        return StatUtil.getIsExist(leftMyopia, rightMyopia);
    }

    private Integer setVisionCorrection(StatConclusion statConclusion, VisionScreeningResult result) {
        if (Objects.isNull(result)) {
            return null;
        }
        if (Objects.equals(statConclusion.getIsMyopia(), Boolean.TRUE)) {
            if (Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.NOT_WEARING.getCode())) {
                return VisionCorrection.UNCORRECTED.getCode();
            }
            if (BigDecimalUtil.lessThan(EyeDataUtil.leftCorrectedVision(result), "4.9") || BigDecimalUtil.lessThan(EyeDataUtil.rightCorrectedVision(result), "4.9")) {
                return VisionCorrection.UNDER_CORRECTED.getCode();
            }
        }
        return null;
    }

}
