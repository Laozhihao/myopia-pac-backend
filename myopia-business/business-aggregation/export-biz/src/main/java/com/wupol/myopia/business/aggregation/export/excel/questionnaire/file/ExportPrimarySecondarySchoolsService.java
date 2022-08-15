package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * 导出中小学校开展学校卫生工作情况调查表
 *
 * @author hang.yuan 2022/7/18 11:24
 */
@Service
public class ExportPrimarySecondarySchoolsService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportPrimarySecondarySchoolsTemplate.xlsx")
    private Resource exportPrimarySecondarySchoolsTemplate;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType();
    }


}
