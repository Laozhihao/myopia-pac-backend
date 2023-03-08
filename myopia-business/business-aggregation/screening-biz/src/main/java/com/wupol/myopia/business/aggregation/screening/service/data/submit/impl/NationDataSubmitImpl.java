package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import com.wupol.myopia.business.aggregation.screening.service.data.submit.IDataSubmitService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 国家数据表
 *
 * @author Simple4H
 */
@Service
public class NationDataSubmitImpl implements IDataSubmitService {

    private final static Integer SNO_INDEX = 3;

    @Override
    public Integer type() {
        return DataSubmitTypeEnum.NATION.getType();
    }

    @Override
    public List<?> getExportData(List<Map<Integer, String>> listMap,
                                 AtomicInteger success, AtomicInteger fail,
                                 Map<String, VisionScreeningResult> screeningData) {
        List<DataSubmitExportDTO> exportData = new ArrayList<>();
        listMap.forEach(s -> {
            DataSubmitExportDTO exportDTO = new DataSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, screeningData, s, exportDTO);
            exportData.add(exportDTO);
        });

        return exportData;
    }


    @Override
    public Class<?> getExportClass() {
        return DataSubmitExportDTO.class;
    }

    @Override
    public Function<Map<Integer, String>, String> getSnoFunction() {
        return s -> s.get(SNO_INDEX);
    }

    @Override
    public Integer getRemoveRows() {
        return DataSubmitTypeEnum.NATION.getRemoveRows();
    }


    /**
     * 获取原始数据
     */
    private void getOriginalInfo(Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        exportDTO.setGradeCode(s.get(0));
        exportDTO.setClassCode(s.get(1));
        exportDTO.setClassName(s.get(2));
        exportDTO.setStudentNo(s.get(3));
        exportDTO.setNation(s.get(4));
        exportDTO.setName(s.get(5));
        exportDTO.setGender(s.get(6));
        exportDTO.setBirthday(s.get(7));
        exportDTO.setAddress(s.get(8));
    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(s.get(SNO_INDEX));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setRightNakedVision(getNakedVision(EyeDataUtil.rightNakedVision(result)));
            exportDTO.setLeftNakedVision(getNakedVision(EyeDataUtil.leftNakedVision(result)));
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            BigDecimal rightAxial = EyeDataUtil.rightAxial(result);
            exportDTO.setRightAxial(Objects.isNull(rightAxial) ? "" : rightAxial.toString());
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
            BigDecimal leftAxial = EyeDataUtil.leftAxial(result);
            exportDTO.setLeftAxial(Objects.isNull(leftAxial) ? "" : leftAxial.toString());
            exportDTO.setIsOk(Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.code) ? "是" : "否");
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }

    /**
     * 处理裸眼视力
     *
     * @param nakedVision 裸眼视力
     * @return 裸眼视力
     */
    private String getNakedVision(BigDecimal nakedVision) {
        if (Objects.isNull(nakedVision)) {
            return StringUtils.EMPTY;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "3.0")) {
            return "9.0";
        }
        return nakedVision.setScale(1, RoundingMode.DOWN).toString();
    }

}
