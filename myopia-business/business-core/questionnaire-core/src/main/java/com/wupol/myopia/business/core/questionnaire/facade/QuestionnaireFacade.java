package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.domain.dto.HeadBO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷
 *
 * @author hang.yuan 2022/7/20 16:40
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class QuestionnaireFacade {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireQuestionService questionnaireQuestionService;
    private final QuestionService questionService;

    private static final Integer PID = -1;
    private static AtomicInteger depth = new AtomicInteger(0);

    public QuestionnaireInfoBO getQuestionnaireInfo(Integer questionnaireId){
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollectionUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questionList = questionService.listByIds(questionIds);
            return buildQuestionnaireInfo(questionnaire,questionnaireQuestionList,questionList);
        }
        return null;
    }

    public QuestionnaireInfoBO buildQuestionnaireInfo(Questionnaire questionnaire,
                                                                        List<QuestionnaireQuestion> questionnaireQuestionList,
                                                                        List<Question> questionList){

        QuestionnaireInfoBO questionnaireInfoBO = new QuestionnaireInfoBO();
        questionnaireInfoBO.setQuestionnaireId(questionnaire.getId());
        questionnaireInfoBO.setQuestionnaireName(questionnaire.getTitle());

        //问题
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        //问卷和问题关连
        Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.groupingBy(QuestionnaireQuestion::getPid));
        //父类
        List<QuestionnaireQuestion> questionPidList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> Objects.equals(questionnaireQuestion.getPid(), PID)).collect(Collectors.toList());

        List<QuestionnaireInfoBO.QuestionBO> questionBOList = Lists.newArrayList();
        for (QuestionnaireQuestion questionnaireQuestion : questionPidList) {
            QuestionnaireInfoBO.QuestionBO questionBO = new QuestionnaireInfoBO.QuestionBO();
            Question question = questionMap.get(questionnaireQuestion.getQuestionId());
            questionBO.setQuestionId(question.getId());
            questionBO.setQuestionName(question.getTitle());
            questionBO.setQuestionSerialNumber(questionnaireQuestion.getSerialNumber());
            questionBOList.add(questionBO);
        }

        setQuestion(questionBOList,questionnaireQuestionMap,questionMap);
        questionnaireInfoBO.setQuestionList(questionBOList);
        return questionnaireInfoBO;
    }

    private void setQuestion(List<QuestionnaireInfoBO.QuestionBO> questionBOList,
                             Map<Integer, List<QuestionnaireQuestion>> questionnaireQuestionMap,
                             Map<Integer, Question> questionMap){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionBOList) {
            List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionMap.get(questionBO.getQuestionId());
            if (CollectionUtil.isNotEmpty(questionnaireQuestions)){
                List<QuestionnaireInfoBO.QuestionBO> childList = Lists.newArrayList();
                for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestions) {
                    QuestionnaireInfoBO.QuestionBO child = new QuestionnaireInfoBO.QuestionBO();
                    Question question = questionMap.get(questionnaireQuestion.getQuestionId());
                    child.setQuestionId(question.getId());
                    child.setQuestionName(question.getTitle());
                    child.setQuestionSerialNumber(questionnaireQuestion.getSerialNumber());
                    childList.add(child);
                }
                questionBO.setQuestionBOList(childList);
                setQuestion(childList,questionnaireQuestionMap,questionMap);
            }
        }
    }

    private List<HeadBO> getHeadBO(Integer questionnaireId){
        List<HeadBO> headList =Lists.newArrayList();
        QuestionnaireInfoBO questionnaireInfo = getQuestionnaireInfo(questionnaireId);

        List<QuestionnaireInfoBO.QuestionBO> questionList = questionnaireInfo.getQuestionList();
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> strList=Lists.newArrayList();
            strList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,strList,headList);
            }
        }
        return headList;
    }

    /**
     * 获取表头数据
     * @param questionnaireId 问卷ID
     */
    public List<List<String>> getHead(Integer questionnaireId){
        List<HeadBO> headBOList = getHeadBO(questionnaireId);
        return headBOList.stream()
                .map(HeadBO::getQuestionHead)
                .collect(Collectors.toList());
    }

    /**
     * 获取表头数据的ID的顺序
     * @param questionnaireId 问卷ID
     */
    public List<Integer> getQuestionIdSort(Integer questionnaireId){
        List<HeadBO> headBOList = getHeadBO(questionnaireId);
        return headBOList.stream()
                .sorted(Comparator.comparing(HeadBO::getSort))
                .map(HeadBO::getLastQuestionId)
                .collect(Collectors.toList());
    }

    private void setHead(List<QuestionnaireInfoBO.QuestionBO> questionList ,List<String> list,List<HeadBO> lists){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> cloneList = ObjectUtil.cloneByStream(list);
            cloneList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,cloneList,lists);
            }else{
                Integer depthValue = CollectionUtil.max(Lists.newArrayList(depth.get(),cloneList.size()));
                depth.set(depthValue);
                HeadBO headBO = new HeadBO()
                        .setDepth(depth.get())
                        .setQuestionDepthList(cloneList)
                        .setLastQuestionId(questionBO.getQuestionId());
                lists.add(headBO);
            }
        }
    }

    public List<Integer> getLatestQuestionnaireIds(){
        List<Questionnaire> questionnaireList = questionnaireService.getLatestData();
        return questionnaireList.stream().map(Questionnaire::getId).collect(Collectors.toList());
    }
}
