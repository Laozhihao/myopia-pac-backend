package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 问卷Excel导出门面
 *
 * @author hang.yuan 2022/7/18 11:03
 */
@Service
public class QuestionnaireExcelFactory {


    @Autowired
    private List<QuestionnaireExcel> excelList;
    @Autowired
    private List<ExportType> typeList;

    public Optional<QuestionnaireExcel> getQuestionnaireExcelService(Integer questionnaireType){
        return excelList.stream()
                .filter(service -> Objects.equals(service.getType(),questionnaireType))
                .findFirst();
    }

    public Optional<ExportType> getExportTypeService(Integer exportType){
        return typeList.stream()
                .filter(service -> Objects.equals(service.getType(),exportType))
                .findFirst();
    }

}
