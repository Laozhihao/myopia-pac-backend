package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SpineDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @Description 脊柱
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SpineDTO extends ScreeningResultBasicData {
    /**
     * 胸部
     */
    private SpineDataDO.SpineItem chest;

    /**
     * 腰部
     */
    private SpineDataDO.SpineItem waist;

    /**
     * 胸腰
     */
    private SpineDataDO.SpineItem chestWaist;

    /**
     * 前后弯曲
     */
    private SpineDataDO.SpineItem entirety;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        SpineDataDO spineDataDO = new SpineDataDO();
        spineDataDO.setChest(chest);
        spineDataDO.setWaist(waist);
        spineDataDO.setChestWaist(chestWaist);
        spineDataDO.setEntirety(entirety);
        return visionScreeningResult.setSpineData(spineDataDO);
    }

    public boolean isValid() {
        // 如果类型不为1 则必选程度
        if (chest.getType() != 1 && Objects.isNull(chest.getLevel())) {
            return false;
        }
        if (waist.getType() != 1 && Objects.isNull(waist.getLevel())) {
            return false;
        }
        if (chestWaist.getType() != 1 && Objects.isNull(chestWaist.getLevel())) {
            return false;
        }
        return entirety.getType() == 1 || !Objects.isNull(entirety.getLevel());
    }

    public static SpineDTO getInstance(SpineDataDO spineDataDO) {
        if (Objects.isNull(spineDataDO)) {
            return null;
        }
        SpineDTO spineDTO = new SpineDTO();
        spineDTO.setChest(spineDataDO.getChest());
        spineDTO.setWaist(spineDataDO.getWaist());
        spineDTO.setEntirety(spineDataDO.getEntirety());
        spineDTO.setChestWaist(spineDataDO.getChestWaist());
        return spineDTO;
    }
}
