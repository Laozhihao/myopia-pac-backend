package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ChangShaDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
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
    public Function<Map<Integer, String>, String> getSnoFunction() {
        return s -> s.get(SNO_INDEX);
    }

    @Override
    public Integer getRemoveRows() {
        return DataSubmitTypeEnum.CHANG_SHA.getRemoveRows();
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
            exportDTO.setCheckDate("1");
            exportDTO.setEyeVisionDesc("1");
            exportDTO.setRightNakedVisions("1");
            exportDTO.setLeftNakedVisions("1");
            exportDTO.setRightSph("1");
            exportDTO.setRightCyl("1");
            exportDTO.setRightAxial("1");
            exportDTO.setLeftSph("1");
            exportDTO.setLeftCyl("1");
            exportDTO.setLeftAxial("1");
            exportDTO.setGlassesTypeDesc("1");
            exportDTO.setRightCorrectedVisions("1");
            exportDTO.setLeftCorrectedVisions("1");
            exportDTO.setCheckType("1");
            exportDTO.setLeftCj("1");
            exportDTO.setRightCj("1");
            exportDTO.setRightSphMydriasis("1");
            exportDTO.setRightCylMydriasis("1");
            exportDTO.setRightAxialMydriasis("1");
            exportDTO.setLeftSphMydriasis("1");
            exportDTO.setLeftCylMydriasis("1");
            exportDTO.setLeftAxialMydriasis("1");
            exportDTO.setRightBiometricK1("1");
            exportDTO.setRightBiometricK1Axis("1");
            exportDTO.setRightBiometricK2("1");
            exportDTO.setRightBiometricK2Axis("1");
            exportDTO.setLeftBiometricK1("1");
            exportDTO.setLeftBiometricK1Axis("1");
            exportDTO.setLeftBiometricK2("1");
            exportDTO.setLeftBiometricK2Axis("1");
            exportDTO.setRightBiometricAL("1");
            exportDTO.setLeftBiometricAL("1");
            exportDTO.setRightEyePressureDate("1");
            exportDTO.setLeftEyePressureDate("1");
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }
}
