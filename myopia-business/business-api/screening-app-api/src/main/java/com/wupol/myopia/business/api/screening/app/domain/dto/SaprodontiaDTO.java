package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Description 龋齿
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SaprodontiaDTO extends ScreeningResultBasicData {
    /**
     * 上牙床
     */
    private List<SaprodontiaDataDO.SaprodontiaItem> above;

    /**
     * 下牙床
     */
    private List<SaprodontiaDataDO.SaprodontiaItem> underneath;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        SaprodontiaDataDO saprodontiaDataDO = new SaprodontiaDataDO();
        saprodontiaDataDO.setAbove(above);
        saprodontiaDataDO.setDiagnosis(super.getDiagnosis());
        saprodontiaDataDO.setCreateUserId(getCreateUserId());
        saprodontiaDataDO.setUnderneath(underneath);
        saprodontiaDataDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult.setSaprodontiaData(saprodontiaDataDO);
    }

    public boolean isValid() {
        // 暂时不需要验证，如果为空就是正常的
        return true;
    }

    public static SaprodontiaDTO getInstance(SaprodontiaDataDO saprodontiaDataDO) {
        if (Objects.isNull(saprodontiaDataDO)) {
            return null;
        }
        SaprodontiaDTO saprodontiaDTO = new SaprodontiaDTO();
        saprodontiaDTO.setAbove(saprodontiaDataDO.getAbove());
        saprodontiaDTO.setUnderneath(saprodontiaDataDO.getUnderneath());
        return saprodontiaDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_SAPRODONTIA;
    }
}
