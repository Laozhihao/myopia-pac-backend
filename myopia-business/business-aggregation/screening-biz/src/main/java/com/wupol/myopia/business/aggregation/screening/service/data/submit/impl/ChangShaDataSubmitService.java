package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ChangShaDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 长沙
 *
 * @author Simple4H
 */
@Service
public class ChangShaDataSubmitService implements IDataSubmitService {

    @Override
    public Integer type() {
        return 1;
    }

    @Override
    public List<?> getExportData(List<Map<Integer, String>> listMap, AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningData) {
        ChangShaDataSubmitExportDTO exportDTO = new ChangShaDataSubmitExportDTO();
        exportDTO.setSn("1");
        exportDTO.setSchoolName("1");
        exportDTO.setGradeName("1");
        exportDTO.setClassName("1");
        exportDTO.setStudentName("1");
        exportDTO.setGenderDesc("1");
        exportDTO.setIdCard("1");
        exportDTO.setStudentSno("1");
        exportDTO.setPhone("1");
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


        return Lists.newArrayList(exportDTO);
    }

    @Override
    public Class<?> getExportClass() {
        return ChangShaDataSubmitExportDTO.class;
    }

    @Override
    public Function<Map<Integer, String>, String> getSnoFunction() {
        return s -> s.get(7);
    }
}
