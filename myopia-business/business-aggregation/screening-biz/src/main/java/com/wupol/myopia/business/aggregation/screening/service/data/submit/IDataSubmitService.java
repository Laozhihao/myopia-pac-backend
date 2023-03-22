package com.wupol.myopia.business.aggregation.screening.service.data.submit;

import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据上报
 *
 * @author Simple4H
 */
public interface IDataSubmitService {

    Integer type();

    List<?> getExportData(List<Map<Integer, String>> listMap, AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningData);

    Class<?> getExportClass();

    Integer getRemoveRows();

    Map<String, VisionScreeningResult> getVisionScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId);

    default Boolean isXlsx() {
        return Boolean.TRUE;
    }
}
