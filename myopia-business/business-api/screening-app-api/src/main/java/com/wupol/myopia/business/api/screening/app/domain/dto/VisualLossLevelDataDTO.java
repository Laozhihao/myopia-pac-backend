package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.VisualLossLevelDataDO;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 盲及视力损害等级
 *
 * @Author HaoHao
 * @Date 2021/9/13
 **/
@Data
public class VisualLossLevelDataDTO implements Serializable {
    /**
     * 左：0~9 级
     */
    private Integer leftVisualLossLevel;

    /**
     * 右：0~9 级
     */
    private Integer rightVisualLossLevel;

    public static VisualLossLevelDataDTO getInstance(VisualLossLevelDataDO visualLossLevelDataDO) {
        VisualLossLevelDataDTO visualLossLevelDataDTO = new VisualLossLevelDataDTO();
        if (Objects.isNull(visualLossLevelDataDO)) {
            return visualLossLevelDataDTO;
        }
        VisualLossLevelDataDO.VisualLossLevelData leftEye = visualLossLevelDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            visualLossLevelDataDTO.setLeftVisualLossLevel(leftEye.getLevel());
        }
        VisualLossLevelDataDO.VisualLossLevelData rightEye = visualLossLevelDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            visualLossLevelDataDTO.setRightVisualLossLevel(rightEye.getLevel());
        }
        return visualLossLevelDataDTO;
    }

}
