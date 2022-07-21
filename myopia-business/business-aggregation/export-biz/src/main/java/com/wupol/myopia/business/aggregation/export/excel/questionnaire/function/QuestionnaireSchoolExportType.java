package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 问卷管理页面学校导出类型
 *
 * @author hang.yuan 2022/7/20 14:33
 */
@Service
public class QuestionnaireSchoolExportType implements ExportType {

    @Autowired
    private SchoolService schoolService;

    private static final String KEY = "%s的%s的问卷数据";
    private static final String FILE_EXPORT_EXCEL = "file:export:excel:questionnaireSchool:%s-%s-%s-%s";


    @Override
    public Integer getType() {
        return ExportTypeConst.QUESTIONNAIRE_SCHOOL;
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
                exportCondition.getSchoolId(),
                exportCondition.getQuestionnaireType().get(0));
    }

    private String getKey(ExportCondition exportCondition){
        School school = schoolService.getById(exportCondition.getSchoolId());
        List<Integer> questionnaireType = exportCondition.getQuestionnaireType();
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType.get(0));
        return String.format(KEY,school.getName(),questionnaireTypeEnum.getDesc());
    }
}
