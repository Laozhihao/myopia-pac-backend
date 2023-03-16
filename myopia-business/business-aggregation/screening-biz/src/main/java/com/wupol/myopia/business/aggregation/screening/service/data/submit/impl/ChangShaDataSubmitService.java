package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.common.utils.util.ObjectUtil;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ChangShaDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    private SchoolStudentService schoolStudentService;

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
        VisionScreeningResult result = screeningResultMap.get(StringUtils.upperCase(s.get(CREDENTIALS_INDEX)));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setCheckDate(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            BigDecimal leftNakedVision = EyeDataUtil.leftNakedVision(result);
            BigDecimal rightNakedVision = EyeDataUtil.rightNakedVision(result);
            if (ObjectUtils.allNotNull(leftNakedVision, rightNakedVision)) {
                if (BigDecimalUtil.moreThanAndEqual(leftNakedVision, "5.0") && BigDecimalUtil.moreThanAndEqual(rightNakedVision, "5.0")) {
                    exportDTO.setEyeVisionDesc("正常");
                } else {
                    exportDTO.setEyeVisionDesc("异常");
                }
            } else {
                exportDTO.setEyeVisionDesc("未检测");
            }
            // 如果是OK镜，则填充矫正视力未裸眼视力
            if (Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.getCode())) {
                exportDTO.setRightNakedVisions(EyeDataUtil.correctedRightDataToStr(result));
                exportDTO.setLeftNakedVisions(EyeDataUtil.correctedLeftDataToStr(result));
            } else {
                exportDTO.setRightNakedVisions(EyeDataUtil.visionRightDataToStr(result));
                exportDTO.setLeftNakedVisions(EyeDataUtil.visionLeftDataToStr(result));
            }
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            exportDTO.setRightAxial(EyeDataUtil.computerRightAxial(result));
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
            exportDTO.setLeftAxial(EyeDataUtil.computerLeftAxial(result));
            exportDTO.setGlassesTypeDesc(EyeDataUtil.glassesTypeString(result));
            exportDTO.setRightCorrectedVisions(EyeDataUtil.correctedRightDataToStr(result));
            exportDTO.setLeftCorrectedVisions(EyeDataUtil.correctedLeftDataToStr(result));
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
        List<SchoolStudent> studentList = schoolStudentService.getByIdCardsOrPassports(credentialsLists, credentialsLists, schoolId).stream().filter(s -> Objects.equals(s.getSchoolId(), schoolId)).collect(Collectors.toList());

        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(studentList.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList()), schoolId);

        return studentList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .collect(Collectors.toMap(s -> StringUtils.upperCase(StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport()), s -> resultMap.getOrDefault(s.getStudentId(), new VisionScreeningResult())));
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
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .collect(Collectors.toMap(s -> StringUtils.upperCase(StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport()), s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }
}
