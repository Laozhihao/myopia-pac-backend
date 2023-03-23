package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ShangHaiDataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 上海数据上报
 *
 * @author Simple4H
 */
@Service
public class ShangHaiDataSubmitService implements IDataSubmitService {
    private final static Integer CREDENTIALS_INDEX = 9;

    private final static String DEFAULT_VALUE = "9";

    @Resource
    private CommonDataSubmitService commonDataSubmitService;

    @Override
    public Integer type() {
        return DataSubmitTypeEnum.SHANG_HAI.getType();
    }

    @Override
    public List<?> getExportData(List<Map<Integer, String>> listMap, AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningData) {

        List<ShangHaiDataSubmitExportDTO> exportData = new ArrayList<>();
        listMap.forEach(s -> {
            ShangHaiDataSubmitExportDTO exportDTO = new ShangHaiDataSubmitExportDTO();
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
        return DataSubmitTypeEnum.SHANG_HAI.getRemoveRows();
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
    private void getOriginalInfo(Map<Integer, String> s, ShangHaiDataSubmitExportDTO exportDTO) {
        exportDTO.setGradeCodeSn(s.get(0));
        exportDTO.setClassNo(s.get(1));
        exportDTO.setClassName(s.get(2));
        exportDTO.setStudentNo(s.get(3));
        exportDTO.setNationDesc(s.get(4));
        exportDTO.setStudentName(s.get(5));
        exportDTO.setGender(s.get(6));
        exportDTO.setBirthday(s.get(7));
        exportDTO.setAddress(s.get(8));
        exportDTO.setIdCard(s.get(9));
    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, ShangHaiDataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(StringUtils.upperCase(s.get(CREDENTIALS_INDEX)));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {


            exportDTO.setLeftNakedVisions(Optional.ofNullable(EyeDataUtil.leftNakedVision(result)).map(BigDecimal::toString).orElse(DEFAULT_VALUE));
            exportDTO.setRightNakedVisions(Optional.ofNullable(EyeDataUtil.rightNakedVision(result)).map(BigDecimal::toString).orElse(DEFAULT_VALUE));
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            exportDTO.setLeftAxial(EyeDataUtil.computerLeftAxial(result));
            exportDTO.setRightAxial(EyeDataUtil.computerRightAxial(result));
            if (Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.getCode())) {
                exportDTO.setIsOkGlasses("0");
            } else {
                exportDTO.setIsOkGlasses(DEFAULT_VALUE);
            }
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
