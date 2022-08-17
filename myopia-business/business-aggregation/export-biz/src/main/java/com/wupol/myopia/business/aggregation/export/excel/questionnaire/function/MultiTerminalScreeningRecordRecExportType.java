package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 多端筛查机构筛查记录rec导出类型
 *
 * @author hang.yuan 2022/7/20 14:35
 */
@Service
public class MultiTerminalScreeningRecordRecExportType implements ExportType {

    @Autowired
    private ExportTypeFacade exportTypeFacade;

    private static final String KEY = "%s的%s的问卷数据";
    private static final String FILE_EXPORT_EXCEL = "file:export:excel:multiTerminalOrgScreeningRecord:%s-%s-%s-%s";


    @Override
    public Integer getType() {
        return ExportTypeConst.MULTI_TERMINAL_ORG_SCREENING_RECORD_REC;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return exportTypeFacade.getSchoolKey(exportCondition,KEY);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return exportTypeFacade.getSchoolKey(exportCondition,KEY);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(FILE_EXPORT_EXCEL,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getQuestionnaireType().get(0));
    }

    @Override
    public Map<Integer, String> getQuestionnaireType() {
        return exportTypeFacade.getQuestionnaireType(getType());
    }

    @Override
    public void preProcess(ExportCondition exportCondition) {
        ExportTypeFacade.checkSchoolId(exportCondition);
    }
}
