package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import org.springframework.stereotype.Service;

/**
 * 多端学校筛查记录导出类型
 *
 * @author hang.yuan 2022/7/20 14:35
 */
@Service
public class MultiTerminalScreeningRecordExportType implements ExportType {
    @Override
    public Integer getType() {
        return ExportTypeConst.MULTI_TERMINAL_SCHOOL_SCREENING_RECORD;
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
