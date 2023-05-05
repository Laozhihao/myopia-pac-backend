package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import cn.hutool.core.date.DatePattern;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.base.util.ScreeningDataFormatUtils;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ShangHaiDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ShangHaiHealthCommissionSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 上海卫健委数据上报
 *
 * @author Simple4H
 */
@Service
public class ShangHaiHealthCommissionDataSubmitService implements IDataSubmitService {
    private final static Integer CREDENTIALS_INDEX = 6;

    private final static String DEFAULT_VALUE = "9";

    @Resource
    private CommonDataSubmitService commonDataSubmitService;

    @Override
    public Integer type() {
        return DataSubmitTypeEnum.SHANG_HAI_HEALTH_COMMISSION.getType();
    }

    @Override
    public List<?> getExportData(List<Map<Integer, String>> listMap, AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningData) {

        List<ShangHaiHealthCommissionSubmitExportDTO> exportData = new ArrayList<>();
        listMap.forEach(s -> {
            ShangHaiHealthCommissionSubmitExportDTO exportDTO = new ShangHaiHealthCommissionSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, screeningData, s, exportDTO);
            exportData.add(exportDTO);
        });
        return exportData;
    }

    @Override
    public Class<?> getExportClass() {
        return ShangHaiDataSubmitExportDTO.class;
    }

    @Override
    public Integer getRemoveRows() {
        return DataSubmitTypeEnum.SHANG_HAI_HEALTH_COMMISSION.getRemoveRows();
    }

    @Override
    public Map<String, VisionScreeningResult> getVisionScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId) {
        if (Objects.isNull(screeningPlanId)) {
            return commonDataSubmitService.getScreeningData(listMap, schoolId, getIdCardFunction());
        } else {
            return commonDataSubmitService.getScreeningData(listMap, schoolId, screeningPlanId, getIdCardFunction());
        }
    }

    private Function<Map<Integer, String>, String> getIdCardFunction() {
        return s -> s.get(CREDENTIALS_INDEX);
    }

    /**
     * 获取原始数据
     */
    private void getOriginalInfo(Map<Integer, String> s, ShangHaiHealthCommissionSubmitExportDTO exportDTO) {
        exportDTO.setSchoolName(s.get(0));
        exportDTO.setGradeName(s.get(1));
        exportDTO.setClassName(s.get(2));
        exportDTO.setStudentName(s.get(3));
        exportDTO.setGender(s.get(4));
        exportDTO.setBirthday(s.get(5));
        exportDTO.setIdCard(s.get(6));

    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, ShangHaiHealthCommissionSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(StringUtils.upperCase(s.get(CREDENTIALS_INDEX)));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setCheckData(DateUtil.format(result.getUpdateTime(), DatePattern.NORM_DATE_PATTERN));
            Integer glassesType = EyeDataUtil.glassesType(result);
            if (Objects.nonNull(glassesType)) {
                exportDTO.setIsGlasses(Objects.equals(glassesType, GlassesTypeEnum.NOT_WEARING.getCode()) ? "0" : "1");
                exportDTO.setGlassesType(glassesType.toString());
            }
            exportDTO.setRightCorrectedVisions(Optional.ofNullable(EyeDataUtil.rightCorrectedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
            exportDTO.setLeftCorrectedVisions(Optional.ofNullable(EyeDataUtil.leftCorrectedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
            exportDTO.setLeftNakedVisions(Optional.ofNullable(EyeDataUtil.leftNakedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
            exportDTO.setRightNakedVisions(Optional.ofNullable(EyeDataUtil.rightNakedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
            if (Objects.equals(GlassesTypeEnum.ORTHOKERATOLOGY.getCode(), glassesType)) {
                exportDTO.setLeftNakedVisions(Optional.ofNullable(EyeDataUtil.rightCorrectedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
                exportDTO.setRightNakedVisions(Optional.ofNullable(EyeDataUtil.leftCorrectedVision(result)).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(DEFAULT_VALUE));
            }
            exportDTO.setLeftSph(ScreeningDataFormatUtils.generateSingleSuffixDStrEmpty(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(ScreeningDataFormatUtils.generateSingleSuffixDStrEmpty(EyeDataUtil.leftCyl(result)));
            exportDTO.setLeftAxial(ScreeningDataFormatUtils.generateSingleEyeDegreeEmpty(EyeDataUtil.leftAxial(result)));
            exportDTO.setRightSph(ScreeningDataFormatUtils.generateSingleSuffixDStrEmpty(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(ScreeningDataFormatUtils.generateSingleSuffixDStrEmpty(EyeDataUtil.rightCyl(result)));
            exportDTO.setRightAxial(ScreeningDataFormatUtils.generateSingleEyeDegreeEmpty(EyeDataUtil.rightAxial(result)));

            success.incrementAndGet();
        } else {
            exportDTO.setLeftNakedVisions(DEFAULT_VALUE);
            exportDTO.setRightNakedVisions(DEFAULT_VALUE);
            fail.incrementAndGet();
        }
    }

    @Override
    public Boolean isXlsx() {
        return Boolean.FALSE;
    }
}
