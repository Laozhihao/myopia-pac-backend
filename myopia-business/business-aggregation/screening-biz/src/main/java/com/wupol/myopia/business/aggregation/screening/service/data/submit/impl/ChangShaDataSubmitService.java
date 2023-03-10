package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ChangShaDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 长沙
 *
 * @author Simple4H
 */
@Service
public class ChangShaDataSubmitService implements IDataSubmitService {

    private final static Integer CREDENTIALS_INDEX = 6;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private StudentService studentService;

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
        if (Objects.isNull(screeningPlanId)) {
            return getScreeningData(listMap, schoolId);
        } else {
            return getScreeningData(listMap, schoolId, screeningPlanId);
        }
    }

    private Function<Map<Integer, String>, String> getIdCardFunction() {
        return s -> s.get(CREDENTIALS_INDEX);
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
        VisionScreeningResult result = screeningResultMap.get(s.get(CREDENTIALS_INDEX));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setCheckDate(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            exportDTO.setEyeVisionDesc("--");
            exportDTO.setRightNakedVisions(EyeDataUtil.visionRightDataToStr(result));
            exportDTO.setLeftNakedVisions(EyeDataUtil.visionLeftDataToStr(result));
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            exportDTO.setRightAxial(EyeDataUtil.computerRightAxial(result));
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
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

    /**
     * 通过证件号获取筛查信息
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId) {
        List<String> credentialsLists = listMap.stream().map(getIdCardFunction()).collect(Collectors.toList());
        List<Student> studentList = studentService.getByIdCardsOrPassports(credentialsLists, credentialsLists).stream().filter(s -> Objects.equals(s.getSchoolId(), schoolId)).collect(Collectors.toList());

        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(studentList.stream().map(Student::getId).collect(Collectors.toList()), schoolId);

        return studentList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .filter(ListUtil.distinctByKey(Student::getIdCard))
                .filter(ListUtil.distinctByKey(Student::getPassport))
                .collect(Collectors.toMap(s -> StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport(), s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }

    /**
     * 通过证件号在筛查计划中获取筛查数据
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId) {

        List<String> credentialsList = listMap.stream().map(getIdCardFunction()).collect(Collectors.toList());
        // 筛查计划中学生数据查询
        List<PlanStudentInfoDTO> planStudentList = screeningPlanSchoolStudentService.getByCredentials(schoolId, screeningPlanId, credentialsList, credentialsList);
        // 根据学生id查询筛查信息
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getFirstMap(planStudentList.stream().map(PlanStudentInfoDTO::getId).collect(Collectors.toList()), schoolId, screeningPlanId);

        return planStudentList.stream()
                .filter(ListUtil.distinctByKey(PlanStudentInfoDTO::getIdCard))
                .filter(ListUtil.distinctByKey(PlanStudentInfoDTO::getPassport))
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .collect(Collectors.toMap(s -> StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport(), s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }
}
