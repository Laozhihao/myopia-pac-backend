package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 导出学校环境健康影响因素调查表
 *
 * @author hang.yuan 2022/7/18 11:25
 */
@Service
public class ExportSchoolEnvironmentService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportSchoolEnvironmentTemplate.xlsx")
    private Resource exportSchoolEnvironmentTemplate;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType();
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {

    }

    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {

    }
}
