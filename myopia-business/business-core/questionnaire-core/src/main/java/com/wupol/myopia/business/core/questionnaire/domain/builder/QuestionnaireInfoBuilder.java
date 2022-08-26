package com.wupol.myopia.business.core.questionnaire.domain.builder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionAttribute;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionExtBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireInfoBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷信息构建类
 *
 * @author hang.yuan 2022/8/8 15:14
 */
@UtilityClass
public class QuestionnaireInfoBuilder {


    /**
     * 构建问卷信息（问卷+问题）
     *
     * @param questionnaire 问卷
     * @param questionnaireQuestionList 问卷问题关联集合
     * @param questionList 问题集合
     */
    public static QuestionnaireInfoBO buildQuestionnaireInfo(Questionnaire questionnaire,
                                                             List<QuestionnaireQuestion> questionnaireQuestionList,
                                                             List<Question> questionList){

        QuestionnaireInfoBO questionnaireInfoBO = new QuestionnaireInfoBO();
        questionnaireInfoBO.setQuestionnaire(questionnaire);

        //问题
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        //问卷和问题关联
        Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.groupingBy(QuestionnaireQuestion::getPid));

        //父类
        List<QuestionnaireQuestion> questionPidList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> Objects.equals(questionnaireQuestion.getPid(), QuestionnaireConstant.PID)).collect(Collectors.toList());

        List<QuestionnaireInfoBO.QuestionBO> questionBOList = Lists.newArrayList();
        for (QuestionnaireQuestion questionnaireQuestion : questionPidList) {
            Question question = questionMap.get(questionnaireQuestion.getQuestionId());
            questionBOList.add(buildQuestionBO(questionnaireQuestion, question));
        }

        setQuestion(questionBOList,questionnaireQuestionMap,questionMap);
        questionnaireInfoBO.setQuestionList(questionBOList);
        return questionnaireInfoBO;
    }

    /**
     * 构建QuestionBO对象
     * @param questionnaireQuestion 问卷问题关联对象
     * @param question 问题对象
     */
    private QuestionnaireInfoBO.QuestionBO buildQuestionBO(QuestionnaireQuestion questionnaireQuestion, Question question) {
        QuestionnaireInfoBO.QuestionBO questionBO = new QuestionnaireInfoBO.QuestionBO();
        BeanUtil.copyProperties(question,questionBO);
        questionBO.setQuestionnaireQuestionId(questionnaireQuestion.getId());
        questionBO.setQuestionSerialNumber(questionnaireQuestion.getSerialNumber());
        List<QesDataDO> qesDataList = questionnaireQuestion.getQesData();
        if (CollUtil.isNotEmpty(qesDataList)){
            questionBO.setQesData(qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), QuestionnaireConstant.QM)).collect(Collectors.toList()));
        }
        questionBO.setIsScore(Optional.ofNullable(question.getAttribute()).map(QuestionAttribute::getIsScore).orElse(Boolean.FALSE));
        return questionBO;
    }


    /**
     * 递归设置问题
     * @param questionBOList 父类问题集合
     * @param questionnaireQuestionMap 问卷问题关联集合
     * @param questionMap 问题集合
     */
    private static void setQuestion(List<QuestionnaireInfoBO.QuestionBO> questionBOList,
                                      Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap,
                                      Map<Integer, Question> questionMap){
        for (QuestionnaireInfoBO.QuestionBO questionBO  : questionBOList) {
            List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionMap.get(questionBO.getQuestionnaireQuestionId());
            if (CollUtil.isEmpty(questionnaireQuestions)){
                continue;
            }
            List<QuestionnaireInfoBO.QuestionBO> childList = Lists.newArrayList();
            for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestions) {
                Question question = questionMap.get(questionnaireQuestion.getQuestionId());
                childList.add(buildQuestionBO(questionnaireQuestion, question));
            }
            questionBO.setQuestionBOList(childList);
            setQuestion(childList,questionnaireQuestionMap,questionMap);
        }
    }
}
