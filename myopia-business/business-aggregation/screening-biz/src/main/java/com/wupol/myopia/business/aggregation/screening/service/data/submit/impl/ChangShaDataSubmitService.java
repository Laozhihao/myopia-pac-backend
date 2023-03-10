package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ChangShaDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 长沙
 *
 * @author Simple4H
 */
@Service
public class ChangShaDataSubmitService implements IDataSubmitService {

    private final static Integer SNO_INDEX = 7;

    @Override
    public Integer type() {
        return DataSubmitTypeEnum.CHANG_SHA.getType();
    }

    @Override
    public List<?> getExportData(List<Map<Integer, String>> listMap, AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningData) {

        List<ChangShaDataSubmitExportDTO> exportData = new ArrayList<>();
        listMap.forEach(s -> {
            ChangShaDataSubmitExportDTO exportDTO = new ChangShaDataSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, screeningData, s, exportDTO);
            exportData.add(exportDTO);
        });

        return exportData;
    }

    @Override
    public Class<?> getExportClass() {
        return ChangShaDataSubmitExportDTO.class;
    }

    @Override
    public Integer getRemoveRows() {
        return DataSubmitTypeEnum.CHANG_SHA.getRemoveRows();
    }

    @Override
    public Map<String, VisionScreeningResult> getVisionScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId) {
        return null;
    }

    /**
     * 获取原始数据
     */
    private void getOriginalInfo(Map<Integer, String> s, ChangShaDataSubmitExportDTO exportDTO) {
        exportDTO.setSn(s.get(0));
        exportDTO.setSchoolName(s.get(1));
        exportDTO.setGradeName(s.get(2));
        exportDTO.setClassName(s.get(3));
        exportDTO.setStudentName(s.get(4));
        exportDTO.setGenderDesc(s.get(5));
        exportDTO.setIdCard(s.get(6));
        exportDTO.setStudentSno(s.get(7));
        exportDTO.setPhone(s.get(8));
    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, ChangShaDataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(s.get(SNO_INDEX));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setCheckDate(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            exportDTO.setEyeVisionDesc("--");
            exportDTO.setRightNakedVisions(EyeDataUtil.visionRightDataToStr(result));
            exportDTO.setLeftNakedVisions(EyeDataUtil.visionLeftDataToStr(result));
            exportDTO.setRightSph(EyeDataUtil.computerRightSphNULL(result));
            exportDTO.setRightCyl(EyeDataUtil.computerRightCyl(result));
            exportDTO.setRightAxial(EyeDataUtil.computerRightAxial(result));
            exportDTO.setLeftSph(EyeDataUtil.computerLeftSph(result));
            exportDTO.setLeftCyl(EyeDataUtil.computerLeftCyl(result));
            exportDTO.setLeftAxial(EyeDataUtil.computerLeftAxial(result));
            exportDTO.setGlassesTypeDesc(EyeDataUtil.glassesTypeString(result));
            exportDTO.setRightCorrectedVisions(EyeDataUtil.correctedRightDataToStr(result));
            exportDTO.setLeftCorrectedVisions(EyeDataUtil.correctedLeftDataToStr(result));
            exportDTO.setCheckType("--");
            exportDTO.setLeftCj("--");
            exportDTO.setRightCj("--");
            exportDTO.setRightSphMydriasis("--");
            exportDTO.setRightCylMydriasis("--");
            exportDTO.setRightAxialMydriasis("--");
            exportDTO.setLeftSphMydriasis("--");
            exportDTO.setLeftCylMydriasis("--");
            exportDTO.setLeftAxialMydriasis("--");
            exportDTO.setRightBiometricK1(EyeDataUtil.biometricRightK1(result));
            exportDTO.setRightBiometricK1Axis(EyeDataUtil.biometricRightK1Axis(result));
            exportDTO.setRightBiometricK2(EyeDataUtil.biometricRightK2(result));
            exportDTO.setRightBiometricK2Axis(EyeDataUtil.biometricRightK2Axis(result));
            exportDTO.setLeftBiometricK1(EyeDataUtil.biometricLeftK1(result));
            exportDTO.setLeftBiometricK1Axis(EyeDataUtil.biometricLeftK1Axis(result));
            exportDTO.setLeftBiometricK2(EyeDataUtil.biometricLeftK2(result));
            exportDTO.setLeftBiometricK2Axis(EyeDataUtil.biometricLeftK2Axis(result));
            exportDTO.setRightBiometricAL(EyeDataUtil.biometricRightAl(result));
            exportDTO.setLeftBiometricAL(EyeDataUtil.biometricLeftAl(result));
            exportDTO.setRightEyePressureDate(EyeDataUtil.rightEyePressure(result));
            exportDTO.setLeftEyePressureDate(EyeDataUtil.leftEyePressure(result));
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }
}
