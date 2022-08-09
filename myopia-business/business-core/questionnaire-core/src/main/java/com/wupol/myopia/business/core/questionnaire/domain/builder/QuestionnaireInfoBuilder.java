package com.wupol.myopia.business.core.questionnaire.domain.builder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionAttribute;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionExtBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireAndQuestionBO;
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
    public static QuestionnaireInfoBO buildQuestionnaireInfoBO(Questionnaire questionnaire,
                                                               List<QuestionnaireQuestion> questionnaireQuestionList,
                                                               List<Question> questionList){

        QuestionnaireInfoBO questionnaireInfoBO = new QuestionnaireInfoBO();
        questionnaireInfoBO.setQuestionnaireId(questionnaire.getId());
        questionnaireInfoBO.setQuestionnaireName(questionnaire.getTitle());


        //问题
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        //问卷和问题关联
        Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.groupingBy(QuestionnaireQuestion::getPid));
        //父类
        List<QuestionnaireQuestion> questionPidList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> Objects.equals(questionnaireQuestion.getPid(), QuestionnaireConstant.PID)).collect(Collectors.toList());

        List<QuestionnaireInfoBO.QuestionBO> questionBOList = Lists.newArrayList();
        for (QuestionnaireQuestion questionnaireQuestion : questionPidList) {
            QuestionnaireInfoBO.QuestionBO questionBO = new QuestionnaireInfoBO.QuestionBO();
            Question question = questionMap.get(questionnaireQuestion.getQuestionId());
            questionBO.setQuestionnaireQuestionId(questionnaireQuestion.getId());
            questionBO.setQuestionId(question.getId());
            questionBO.setQuestionName(question.getTitle());
            questionBO.setQuestionSerialNumber(questionnaireQuestion.getSerialNumber());
            questionBO.setIsScore(Optional.ofNullable(question.getAttribute()).map(QuestionAttribute::getIsScore).orElse(Boolean.FALSE));
            questionBOList.add(questionBO);
        }

        setQuestionBO(questionBOList,questionnaireQuestionMap,questionMap);
        questionnaireInfoBO.setQuestionList(questionBOList);
        return questionnaireInfoBO;
    }

    /**
     * 递归设置问题
     * @param questionBOList 父类问题集合
     * @param questionnaireQuestionMap 问卷问题关联集合
     * @param questionMap 问题集合
     */
    private static void setQuestionBO(List<QuestionnaireInfoBO.QuestionBO> questionBOList,
                                      Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap,
                                      Map<Integer, Question> questionMap){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionBOList) {
            List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionMap.get(questionBO.getQuestionnaireQuestionId());
            if (CollectionUtil.isNotEmpty(questionnaireQuestions)){
                List<QuestionnaireInfoBO.QuestionBO> childList = Lists.newArrayList();
                for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestions) {
                    QuestionnaireInfoBO.QuestionBO child = new QuestionnaireInfoBO.QuestionBO();
                    Question question = questionMap.get(questionnaireQuestion.getQuestionId());
                    child.setQuestionnaireQuestionId(questionnaireQuestion.getId());
                    child.setQuestionId(question.getId());
                    child.setQuestionName(question.getTitle());
                    child.setQuestionSerialNumber(questionnaireQuestion.getSerialNumber());
                    child.setIsScore(Optional.ofNullable(question.getAttribute()).map(QuestionAttribute::getIsScore).orElse(Boolean.FALSE));
                    childList.add(child);
                }
                questionBO.setQuestionBOList(childList);
                setQuestionBO(childList,questionnaireQuestionMap,questionMap);
            }
        }
    }


    public static QuestionnaireAndQuestionBO buildQuestionnaireAndQuestionBO(Questionnaire questionnaire,
                                                                      List<QuestionnaireQuestion> questionnaireQuestionList,
                                                                      List<Question> questionList){

        QuestionnaireAndQuestionBO questionnaireAndQuestionBO = new QuestionnaireAndQuestionBO();
        questionnaireAndQuestionBO.setQuestionnaire(questionnaire);

        //问题
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        //问卷和问题关联
        Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.groupingBy(QuestionnaireQuestion::getPid));
        //父类
        List<QuestionnaireQuestion> questionPidList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> Objects.equals(questionnaireQuestion.getPid(), QuestionnaireConstant.PID)).collect(Collectors.toList());

        List<QuestionExtBO> questionBOList = Lists.newArrayList();
        for (QuestionnaireQuestion questionnaireQuestion : questionPidList) {
            QuestionExtBO questionExtBO = new QuestionExtBO();
            Question question = questionMap.get(questionnaireQuestion.getQuestionId());
            BeanUtil.copyProperties(question,questionExtBO);
            questionExtBO.setQuestionnaireQuestionId(questionnaireQuestion.getId());
            questionBOList.add(questionExtBO);
        }

        setQuestion(questionBOList,questionnaireQuestionMap,questionMap);
        questionnaireAndQuestionBO.setQuestionList(questionBOList);
        return questionnaireAndQuestionBO;
    }

    private static void setQuestion(List<QuestionExtBO> questionBOList,
                                      Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap,
                                      Map<Integer, Question> questionMap){
        for (QuestionExtBO questionExtBO : questionBOList) {
            List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionMap.get(questionExtBO.getQuestionnaireQuestionId());
            if (CollectionUtil.isNotEmpty(questionnaireQuestions)){
                List<QuestionExtBO> childList = Lists.newArrayList();
                for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestions) {
                    QuestionExtBO child = new QuestionExtBO();
                    Question question = questionMap.get(questionnaireQuestion.getQuestionId());
                    BeanUtil.copyProperties(question,child);
                    child.setQuestionnaireQuestionId(questionnaireQuestion.getId());
                    childList.add(child);
                }
                questionExtBO.setQuestionExtBOList(childList);
                setQuestion(childList,questionnaireQuestionMap,questionMap);
            }
        }
    }

    /**
     * 问题树展开
     * @param questionList 问题集合
     */
    public static List<QuestionExtBO> getQuestionBOList(List<QuestionExtBO> questionList){
        List<QuestionExtBO> questionExtBOList =Lists.newArrayList();
        if (CollectionUtils.isEmpty(questionList)){
            return questionExtBOList;
        }
        expandTree(questionList,questionExtBOList);
        return questionExtBOList;
    }

    private static void expandTree(List<QuestionExtBO> questionList, List<QuestionExtBO> questionExtBOList){
        for (QuestionExtBO questionExtBO : questionList) {
            questionExtBOList.add(questionExtBO);
            List<QuestionExtBO> childList = questionExtBO.getQuestionExtBOList();
            if (CollectionUtil.isNotEmpty(childList)){
                expandTree(childList,questionExtBOList);
            }
        }
    }
}
