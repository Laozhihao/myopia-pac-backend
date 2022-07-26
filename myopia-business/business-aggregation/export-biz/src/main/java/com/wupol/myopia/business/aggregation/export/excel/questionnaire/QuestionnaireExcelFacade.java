package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
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
public class QuestionnaireExcelFacade {

    private static final String FILE_NAME="%s的%s的问卷数据.xlsx";

    @Autowired
    private List<QuestionnaireExcel> excelList;
    @Autowired
    private List<ExportType> typeList;
    @Autowired
    private SchoolService schoolService;

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

    public String getExcelFileName(String schoolId,Integer questionnaireType){
        School school = schoolService.getById(schoolId);
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
        return String.format(FILE_NAME,school.getName(),questionnaireTypeEnum.getDesc());
    }

}
