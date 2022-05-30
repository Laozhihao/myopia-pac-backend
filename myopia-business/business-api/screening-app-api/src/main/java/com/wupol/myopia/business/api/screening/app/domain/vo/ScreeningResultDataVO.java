package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.wupol.myopia.business.api.screening.app.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.OtherEyeDiseasesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;

import java.util.Objects;

/**
 * 所有筛查结果数据
 *
 * @Author HaoHao
 * @Date 2021/9/20
 **/
@Data
public class ScreeningResultDataVO {
    private MultiCheckDataDTO multiCheckData;
    private ComputerOptometryDTO computerOptometryData;
    private VisionDataDTO visionData;
    private BiometricDataDTO biometricData;
    private PupilOptometryDTO pupilOptometryData;
    private EyePressureDataDTO eyePressureData;
    private OtherEyeDiseasesDTO otherEyeDiseasesData;
    private HeightAndWeightDataDTO heightAndWeightData;
    private DeviationDTO deviationData;
    private SaprodontiaDTO saprodontiaData;
    private SpineDTO spineData;
    private BloodPressureDTO bloodPressureData;
    private DiseasesHistoryDTO diseasesHistoryData;
    private PrivacyDTO privacyData;

    public static ScreeningResultDataVO getInstance(VisionScreeningResult screeningResult) {
        if (Objects.isNull(screeningResult)) {
            return new ScreeningResultDataVO();
        }
        ScreeningResultDataVO screeningResultDataVO = new ScreeningResultDataVO();
        screeningResultDataVO.setMultiCheckData(MultiCheckDataDTO.getInstance(screeningResult.getOcularInspectionData(), screeningResult.getFundusData(), screeningResult.getSlitLampData(), screeningResult.getVisualLossLevelData()));
        screeningResultDataVO.setComputerOptometryData(ComputerOptometryDTO.getInstance(screeningResult.getComputerOptometry()));
        screeningResultDataVO.setVisionData(VisionDataDTO.getInstance(screeningResult.getVisionData()));
        screeningResultDataVO.setBiometricData(BiometricDataDTO.getInstance(screeningResult.getBiometricData()));
        screeningResultDataVO.setPupilOptometryData(PupilOptometryDTO.getInstance(screeningResult.getPupilOptometryData()));
        screeningResultDataVO.setEyePressureData(EyePressureDataDTO.getInstance(screeningResult.getEyePressureData()));
        screeningResultDataVO.setOtherEyeDiseasesData(OtherEyeDiseasesDTO.getInstance(screeningResult.getOtherEyeDiseases(), screeningResult.getSystemicDiseaseSymptom()));
        screeningResultDataVO.setHeightAndWeightData(HeightAndWeightDataDTO.getInstance(screeningResult.getHeightAndWeightData()));
        screeningResultDataVO.setDeviationData(DeviationDTO.getInstance(screeningResult.getDeviationData()));
        screeningResultDataVO.setSaprodontiaData(SaprodontiaDTO.getInstance(screeningResult.getSaprodontiaData()));
        screeningResultDataVO.setSpineData(SpineDTO.getInstance(screeningResult.getSpineData()));
        screeningResultDataVO.setBloodPressureData(BloodPressureDTO.getInstance(screeningResult.getBloodPressureData()));
        screeningResultDataVO.setDiseasesHistoryData(DiseasesHistoryDTO.getInstance(screeningResult.getDiseasesHistoryData()));
        screeningResultDataVO.setPrivacyData(PrivacyDTO.getInstance(screeningResult.getPrivacyData()));
        return screeningResultDataVO;
    }
}
