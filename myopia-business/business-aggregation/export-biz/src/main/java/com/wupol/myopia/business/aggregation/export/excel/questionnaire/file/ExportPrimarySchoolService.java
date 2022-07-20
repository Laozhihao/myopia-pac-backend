package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 导出学生健康状况及影响因素调查表（小学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Service
public class ExportPrimarySchoolService implements QuestionnaireExcel {

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType();
    }

    @Override
    public List<List<String>> getHead() {
        return null;
    }

    @Override
    public File generateExcelFile(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public void getExcelData(ExportCondition exportCondition, List dataList) {

    }
}
