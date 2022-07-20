package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import org.springframework.stereotype.Service;

/**
 * 按区域统计导出类型
 *
 * @author hang.yuan 2022/7/20 14:34
 */
@Service
public class DistrictStatisticsExportType implements ExportType {
    @Override
    public Integer getType() {
        return ExportTypeConst.DISTRICT_STATISTICS;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return null;
    }
}
