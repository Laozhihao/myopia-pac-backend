package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 按区域统计导出类型
 *
 * @author hang.yuan 2022/7/20 14:34
 */
@Service
public class DistrictStatisticsExportType implements ExportType {

    @Autowired
    private DistrictService districtService;

    private static final String KEY = "%s的%s的问卷数据";
    private static final String FILE_EXPORT_EXCEL = "file:export:excel:districtStatistics:%s-%s-%s-%s";


    @Override
    public Integer getType() {
        return ExportTypeConst.DISTRICT_STATISTICS;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return getKey(exportCondition);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getKey(exportCondition);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(FILE_EXPORT_EXCEL,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getDistrictId(),
                exportCondition.getQuestionnaireType().get(0));
    }


    private String getKey(ExportCondition exportCondition){
        String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
        List<Integer> questionnaireType = exportCondition.getQuestionnaireType();
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType.get(0));
        return String.format(KEY,districtName,questionnaireTypeEnum.getDesc());
    }
}
