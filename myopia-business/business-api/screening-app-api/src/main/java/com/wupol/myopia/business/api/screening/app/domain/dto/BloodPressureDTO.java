package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description 血压
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class BloodPressureDTO  extends ScreeningResultBasicData {
    /**
     * 舒张压
     */
    private BigDecimal dbp;

    /**
     * 收缩压
     */
    private BigDecimal sbp;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        BloodPressureDataDO bloodPressureDataDO = new BloodPressureDataDO();
        bloodPressureDataDO.setDbp(dbp);
        bloodPressureDataDO.setSbp(sbp);
        bloodPressureDataDO.setDiagnosis(super.getDiagnosis());
        bloodPressureDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setBloodPressureData(bloodPressureDataDO);
    }

    public boolean isValid() {
        // 暂时不需要验证，如果为空就是正常的
        return true;
    }

    public static BloodPressureDTO getInstance(BloodPressureDataDO bloodPressureDataDO) {
        if (Objects.isNull(bloodPressureDataDO)) {
            return null;
        }
        BloodPressureDTO bloodPressureDTO = new BloodPressureDTO();
        bloodPressureDTO.setDbp(bloodPressureDataDO.getDbp());
        bloodPressureDTO.setSbp(bloodPressureDataDO.getSbp());
        return bloodPressureDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_BLOOD_PRESSURE;
    }
}
