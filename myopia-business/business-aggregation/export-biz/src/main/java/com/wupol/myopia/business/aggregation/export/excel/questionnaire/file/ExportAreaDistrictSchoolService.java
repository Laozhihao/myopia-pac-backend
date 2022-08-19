package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 导出地市及区（县）管理部门学校卫生工作调查表
 *
 * @author hang.yuan 2022/7/18 11:23
 */
@Service
public class ExportAreaDistrictSchoolService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportAreaDistrictSchoolTemplate.xlsx")
    private Resource exportAreaDistrictSchoolTemplate;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType();
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {

    }

    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {

    }
}
