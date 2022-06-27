package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseasesHistoryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * @Description 疾病史
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DiseasesHistoryDTO extends ScreeningResultBasicData {
    private List<String> diseases;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        DiseasesHistoryDO diseasesHistoryDO = new DiseasesHistoryDO();
        diseasesHistoryDO.setDiseases(diseases);
        diseasesHistoryDO.setDiagnosis(super.getDiagnosis());
        diseasesHistoryDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setDiseasesHistoryData(diseasesHistoryDO);
    }

    public boolean isValid() {
        // 暂时不需要验证，如果为空就是正常的
        return true;
    }

    public static DiseasesHistoryDTO getInstance(DiseasesHistoryDO diseasesDo) {
        if (Objects.isNull(diseasesDo)) {
            return null;
        }
        DiseasesHistoryDTO diseasesHistoryDTO = new DiseasesHistoryDTO();
        diseasesHistoryDTO.setDiseases(diseasesDo.getDiseases());
        return diseasesHistoryDTO;
    }
}
