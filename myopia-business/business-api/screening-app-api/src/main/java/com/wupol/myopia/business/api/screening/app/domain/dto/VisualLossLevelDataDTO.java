package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisualLossLevelDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 盲及视力损害等级
 *
 * @Author HaoHao
 * @Date 2021/9/13
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class VisualLossLevelDataDTO extends ScreeningResultBasicData implements Serializable {
    /**
     * 左：0~9 级
     */
    private Integer leftVisualLossLevel;

    /**
     * 右：0~9 级
     */
    private Integer rightVisualLossLevel;

    public static VisualLossLevelDataDTO getInstance(VisualLossLevelDataDO visualLossLevelDataDO) {
        if (Objects.isNull(visualLossLevelDataDO)) {
            return null;
        }
        VisualLossLevelDataDTO visualLossLevelDataDTO = new VisualLossLevelDataDTO();
        VisualLossLevelDataDO.VisualLossLevelData leftEye = visualLossLevelDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            visualLossLevelDataDTO.setLeftVisualLossLevel(leftEye.getLevel());
        }
        VisualLossLevelDataDO.VisualLossLevelData rightEye = visualLossLevelDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            visualLossLevelDataDTO.setRightVisualLossLevel(rightEye.getLevel());
        }
        visualLossLevelDataDTO.setIsCooperative(visualLossLevelDataDO.getIsCooperative());
        return visualLossLevelDataDTO;
    }


    public boolean isValid() {
        return ObjectUtils.anyNotNull(leftVisualLossLevel, rightVisualLossLevel);
    }

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        VisualLossLevelDataDO visualLossLevelDataDO = new VisualLossLevelDataDO();
        VisualLossLevelDataDO.VisualLossLevelData leftData = new VisualLossLevelDataDO.VisualLossLevelData();
        leftData.setLateriality(CommonConst.LEFT_EYE).setLevel(leftVisualLossLevel);
        VisualLossLevelDataDO.VisualLossLevelData rightData = new VisualLossLevelDataDO.VisualLossLevelData();
        rightData.setLateriality(CommonConst.RIGHT_EYE).setLevel(rightVisualLossLevel);
        visualLossLevelDataDO.setLeftEyeData(leftData).setRightEyeData(rightData).setIsCooperative(getIsCooperative());
        visualLossLevelDataDO.setCreateUserId(getCreateUserId());
        visualLossLevelDataDO.setUpdateTime(getUpdateTime());
        visionScreeningResult.setVisualLossLevelData(visualLossLevelDataDO);
        return visionScreeningResult;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_VISUAL_LOSS_LEVEL;
    }
}
