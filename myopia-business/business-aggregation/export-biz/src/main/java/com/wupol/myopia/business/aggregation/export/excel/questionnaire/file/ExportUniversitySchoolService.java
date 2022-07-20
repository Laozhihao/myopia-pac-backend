package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出学生健康状况及影响因素调查表（大学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Service
public class ExportUniversitySchoolService implements QuestionnaireExcel {
    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType();
    }

    @Override
    public List<List<String>> getHead() {
        return null;
    }
}
