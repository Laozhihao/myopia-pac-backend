package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/7/21 19:50
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserAnswerFacade {

    private final UserQuestionRecordService userQuestionRecordService;
    private final UserAnswerService userAnswerService;
    private final QuestionService questionService;
    private final QuestionnaireFacade questionnaireFacade;

    public List getExcelData(ExcelDataConditionBO excelDataConditionBO) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getList(excelDataConditionBO);
        List<Integer> questionIdSortList = questionnaireFacade.getQuestionIdSort(excelDataConditionBO.getQuestionnaireId());
        if(!CollectionUtils.isEmpty(userQuestionRecordList)){
            List<Integer> recordIds = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toList());
            List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);
            return getData(userAnswerList,questionIdSortList);
        }
        return null;
    }

    private List<String> getData(List<UserAnswer> userAnswerList,List<Integer> questionIdSortList){
        Map<Integer, List<UserAnswer>> userAnswerMap = userAnswerList.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
        List<Integer> questionIds = userAnswerList.stream().map(UserAnswer::getQuestionId).collect(Collectors.toList());
        List<Question> questionList = questionService.listByIds(questionIds);
        Map<Integer, String> questionTypeMap = questionList.stream().collect(Collectors.toMap(Question::getId, Question::getType));

        List<String> dataList= Lists.newArrayList();
        for (Integer questionId : questionIdSortList) {
            String type = questionTypeMap.get(questionId);
            List<UserAnswer> userAnswers = userAnswerMap.get(questionId);
            StringBuilder text= new StringBuilder(StrUtil.EMPTY);
            for (UserAnswer userAnswer : userAnswers) {
                List<String> textList = userAnswer.getAnswer().stream().map(OptionAnswer::getText).collect(Collectors.toList());
                text.append(CollectionUtil.join(textList, " "));
            }
            dataList.add(text.toString());
        }
        return dataList;
    }

    /**
     * 获取问卷记录计划
     *
     * @param planId 筛查计划ID
     * @param questionnaireType 问卷类型
     */
    public List<UserQuestionRecord> getQuestionnaireRecordList(Integer planId, Integer questionnaireType) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByPlanId(planId);
        if (!CollectionUtils.isEmpty(userQuestionRecordList)){
            return userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(),0))
                    .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getQuestionnaireType(), questionnaireType))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
