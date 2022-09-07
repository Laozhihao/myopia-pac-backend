package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 问卷Excel导出门面
 *
 * @author hang.yuan 2022/7/18 11:03
 */
@Service
public class QuestionnaireFactory {


    @Autowired
    private List<QuestionnaireExcel> excelList;
    @Autowired
    private List<ExportType> typeList;
    @Autowired
    private List<Answer> answerList;

    /**
     * 获取问卷类型实例
     * @param questionnaireType 问卷类型
     */
    public QuestionnaireExcel getQuestionnaireExcelService(Integer questionnaireType){
        return excelList.stream()
                .filter(service -> Objects.equals(service.getType(),questionnaireType))
                .findFirst().orElseThrow(()->new BusinessException(String.format("不存在此类型实例,questionnaireType=%s",questionnaireType)));
    }

    /**
     * 获取导出类型实例
     * @param exportType 导出类型
     */
    public ExportType getExportTypeService(Integer exportType){
        return typeList.stream()
                .filter(service -> Objects.equals(service.getType(),exportType))
                .findFirst().orElseThrow(()->new BusinessException(String.format("不存在此类型实例,exportType=%s",exportType)));
    }

    /**
     * 获取用户类型实例
     * @param userType 用户类型
     */
    public Answer getAnswerService(Integer userType){
        return answerList.stream()
                .filter(service->Objects.equals(service.getUserType(),userType))
                .findFirst().orElseThrow(()->new BusinessException(String.format("不存在此类型实例,userType=%s",userType)));
    }

}
