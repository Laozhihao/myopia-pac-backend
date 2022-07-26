package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
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
        List<UserQuestionRecord> userQuestionRecordList = Lists.newArrayList();
        List<Integer> questionIdSortList = questionnaireFacade.getQuestionIdSort(excelDataConditionBO.getQuestionnaireId());
        if(!CollectionUtils.isEmpty(userQuestionRecordList)){

            List<Integer> recordIds = userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(), QuestionnaireStatusEnum.NOT_START.getCode()))
                    .map(UserQuestionRecord::getId).collect(Collectors.toList());

            List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);
            return getData(userAnswerList,questionIdSortList);
        }
        return null;
    }

    private List<String> getData(List<UserAnswer> userAnswerList,
                                 List<Integer> questionIdSortList){
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
     * 获取问卷记录数（有数据的问卷 状态进行中或者已完成）
     *
     * @param planId 筛查计划ID
     * @param questionnaireTypeList 问卷类型集合
     */
    public List<UserQuestionRecord> getQuestionnaireRecordList(Integer planId, List<Integer> questionnaireTypeList) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByPlanId(planId);
        if (!CollectionUtils.isEmpty(userQuestionRecordList)){
            return userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(),QuestionnaireStatusEnum.NOT_START.getCode()))
                    .filter(userQuestionRecord ->  questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public void baseProcess(List<UserQuestionRecord> baseInfoList) {
        if (CollectionUtils.isEmpty(baseInfoList)){
            return;
        }
        UserQuestionRecord userQuestionRecord = baseInfoList.get(0);
        List<Integer> questionIdSortList = questionnaireFacade.getQuestionIdSort(userQuestionRecord.getQuestionnaireId());

    }

}
