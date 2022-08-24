package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷Rec数据信息
 *
 * @author hang.yuan 2022/8/19 17:03
 */
@Data
@Accessors(chain = true)
public class QuestionnaireRecInfoBuilder {

    private Questionnaire questionnaire;
    private List<Question> questionList;
    private List<QuestionnaireQuestion> questionnaireQuestionList;


    /**
     * 数据结构构建
     */
    public List<QuestionnaireQuestionRecDataBO> dataBuild(){
        if (!ObjectsUtil.allNotNull(questionnaire, questionnaireQuestionList, questionList)) {
            throw new BusinessException("QuestionnaireRecInfo构建失败，缺少关键参数");
        }
        List<QuestionnaireQuestionRecDataBO> questionRecDataBOList =Lists.newArrayList();
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestionList) {
            setQuestionRecDataBOList(questionRecDataBOList, questionMap, questionnaireQuestion);
        }
        return questionRecDataBOList;
    }

    /**
     * 设置问卷问题rec数据结构集合
     * @param questionRecDataBOList 收集结果集合
     * @param questionMap 问题集合
     * @param questionnaireQuestion 问卷问题关系对象
     */
    private void setQuestionRecDataBOList(List<QuestionnaireQuestionRecDataBO> questionRecDataBOList, Map<Integer, Question> questionMap, QuestionnaireQuestion questionnaireQuestion) {
        Question question = questionMap.get(questionnaireQuestion.getQuestionId());
        QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO = new QuestionnaireQuestionRecDataBO();
        questionnaireQuestionRecDataBO.setQuestion(question);
        questionnaireQuestionRecDataBO.setIsHidden(questionnaireQuestion.getIsHidden());

        if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)){
            questionnaireQuestionRecDataBO.setQuestionnaireRecDataBOList(getInputData(question, questionnaireQuestion));
        }
        if (Objects.equals(question.getType(),QuestionnaireConstant.RADIO)){
            questionnaireQuestionRecDataBO.setQuestionnaireRecDataBOList(getRadioOrCheckboxData(questionnaireQuestion, question,QuestionnaireConstant.RADIO_INPUT));
        }
        if (Objects.equals(question.getType(),QuestionnaireConstant.CHECKBOX)){
            questionnaireQuestionRecDataBO.setQuestionnaireRecDataBOList(getRadioOrCheckboxData(questionnaireQuestion, question,QuestionnaireConstant.CHECKBOX_INPUT));
        }
        questionRecDataBOList.add(questionnaireQuestionRecDataBO);
    }


    /**
     * 获取单选或者多选数据结构
     * @param questionnaireQuestion 问卷问题关系对象
     * @param question 问题对象
     * @param typeInput 单选或者多选Input类型
     */
    private List<QuestionnaireRecDataBO> getRadioOrCheckboxData(QuestionnaireQuestion questionnaireQuestion, Question question,String typeInput) {
        Map<String, QesDataDO> qesDataDoMap = questionnaireQuestion.getQesData().stream().collect(Collectors.toMap(QesDataDO::getOptionId, Function.identity()));
        List<QuestionnaireRecDataBO> radioOrCheckboxList = Lists.newArrayList();
        for (Option option : question.getOptions()) {
            QesDataDO qesDataDO = qesDataDoMap.get(option.getId());
            QuestionnaireRecDataBO questionnaireRecDataBO = buildRadioOrCheckboxData(questionnaireQuestion, qesDataDO,QuestionnaireConstant.RADIO);
            setRadioOrCheckboxInputData(typeInput, qesDataDoMap, option, questionnaireRecDataBO);
            radioOrCheckboxList.add(questionnaireRecDataBO);
        }
        return radioOrCheckboxList;
    }

    /**
     * 设置单选或者多选中Input类型数据结构
     * @param typeInput 单选或者多选Input类型
     * @param qesDataDoMap qes数据对象集合
     * @param option 选项对象
     * @param questionnaireRecDataBO 问卷rec数据结构信息
     */
    private void setRadioOrCheckboxInputData(String typeInput, Map<String, QesDataDO> qesDataDoMap, Option option, QuestionnaireRecDataBO questionnaireRecDataBO) {
        if (Objects.equals(option.getType(),typeInput)){
            List<QuestionnaireRecDataBO> radioOrCheckboxInputList = option.getOption()
                    .values().stream()
                    .map(value -> JSON.parseObject(JSON.toJSONString(value), InputOption.class))
                    .map(inputOption -> buildRadioOrCheckboxInputData(qesDataDoMap, inputOption,typeInput,questionnaireRecDataBO.getQuestionId()))
                    .collect(Collectors.toList());
            questionnaireRecDataBO.setQuestionnaireRecDataBOList(radioOrCheckboxInputList);
        }
    }

    /**
     * 构建单选或者多选数据结构
     * @param questionnaireQuestion 问卷问题关系对象
     * @param qesDataDO qes数据对象
     * @param type 选项类型
     */
    private QuestionnaireRecDataBO buildRadioOrCheckboxData(QuestionnaireQuestion questionnaireQuestion, QesDataDO qesDataDO,String type) {
        return new QuestionnaireRecDataBO()
                    .setQesField(qesDataDO.getQesField())
                    .setRecAnswer(qesDataDO.getQesSerialNumber())
                    .setDataType(QuestionnaireConstant.NUMBER)
                    .setRequired(questionnaireQuestion.getRequired())
                    .setIsHidden(questionnaireQuestion.getIsHidden())
                    .setOptionId(qesDataDO.getOptionId())
                    .setQuestionId(questionnaireQuestion.getQuestionId())
                    .setType(type);
    }

    /**
     * 构建单选或者多选中含Input数据结构
     * @param qesDataDoMap qes数据对象集合
     * @param inputOption input选项对象
     */
    private QuestionnaireRecDataBO buildRadioOrCheckboxInputData(Map<String, QesDataDO> qesDataDoMap, InputOption inputOption,String type,Integer questionId) {
        QesDataDO radioInputQes = qesDataDoMap.get(inputOption.getId());
        return new QuestionnaireRecDataBO()
                    .setQesField(radioInputQes.getQesField())
                    .setDataType(inputOption.getDataType())
                    .setRequired(inputOption.getRequired())
                    .setOptionId(inputOption.getId())
                    .setRange(inputOption.getRange())
                    .setLength(inputOption.getLength())
                    .setType(type)
                    .setQuestionId(questionId);
    }

    /**
     * 获取Input类型数据结构
     * @param question 问题对象
     * @param questionnaireQuestion 问卷问题关系对象
     */
    private List<QuestionnaireRecDataBO> getInputData(Question question,QuestionnaireQuestion questionnaireQuestion) {
        Option option = question.getOptions().get(0);
        Map<String, InputOption> inputOptionMap = option.getOption()
                .values().stream()
                .map(value -> JSON.parseObject(JSON.toJSONString(value), InputOption.class))
                .collect(Collectors.toMap(InputOption::getId, Function.identity()));
        return questionnaireQuestion.getQesData().stream()
                .map(qesDataDO -> buildInputDataBO(inputOptionMap,questionnaireQuestion,QuestionnaireConstant.INPUT, qesDataDO))
                .collect(Collectors.toList());
    }

    /**
     * 构建Input类型数据结构
     * @param inputOptionMap input选项对象集合
     * @param questionnaireQuestion 问卷问题关系对象
     * @param type 问题类型
     * @param qesDataDO qes数据对象
     */
    private QuestionnaireRecDataBO buildInputDataBO(Map<String, InputOption> inputOptionMap,QuestionnaireQuestion questionnaireQuestion,String type, QesDataDO qesDataDO) {
        InputOption inputOption = inputOptionMap.get(qesDataDO.getOptionId());
        return new QuestionnaireRecDataBO()
                    .setQesField(qesDataDO.getQesField())
                    .setOptionId(qesDataDO.getOptionId())
                    .setRequired(inputOption.getRequired())
                    .setDataType(inputOption.getDataType())
                    .setRange(inputOption.getRange())
                    .setLength(inputOption.getLength())
                    .setIsHidden(questionnaireQuestion.getIsHidden())
                    .setQuestionId(questionnaireQuestion.getQuestionId())
                    .setType(type);
    }
    /**
     * 构建问卷rec数据信息构建对象
     * @param questionnaireBaseInfo 问卷基本信息（问卷，问题，问卷问题关系）
     */
    public static QuestionnaireRecInfoBuilder build(ThreeTuple<Questionnaire, List<QuestionnaireQuestion>, List<Question>> questionnaireBaseInfo){
        return new QuestionnaireRecInfoBuilder()
                .setQuestionnaire(questionnaireBaseInfo.getFirst())
                .setQuestionnaireQuestionList(questionnaireBaseInfo.getSecond())
                .setQuestionList(questionnaireBaseInfo.getThird());
    }

}
