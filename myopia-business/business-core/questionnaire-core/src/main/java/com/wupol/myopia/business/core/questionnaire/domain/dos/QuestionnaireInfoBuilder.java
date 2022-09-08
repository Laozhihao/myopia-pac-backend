package com.wupol.myopia.business.core.questionnaire.domain.dos;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷Rec数据信息
 *
 * @author hang.yuan 2022/8/19 17:03
 */
@Data
@Accessors(chain = true)
public class QuestionnaireInfoBuilder {

    private Questionnaire questionnaire;
    private List<Question> questionList;
    private List<QuestionnaireQuestion> questionnaireQuestionList;


    /**
     * 数据结构构建
     */
    public List<QuestionnaireQuestionDataBO> dataBuild(){
        if (!ObjectsUtil.allNotNull(questionnaire, questionnaireQuestionList, questionList)) {
            throw new BusinessException("QuestionnaireRecInfo构建失败，缺少关键参数");
        }
        List<QuestionnaireQuestionDataBO> questionDataBOList =Lists.newArrayList();
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        for (QuestionnaireQuestion questionnaireQuestion : questionnaireQuestionList) {
            setQuestionDataBOList(questionDataBOList, questionMap, questionnaireQuestion);
        }
        return questionDataBOList;
    }

    /**
     * 设置问卷问题rec数据结构集合
     * @param questionDataBOList 收集结果集合
     * @param questionMap 问题集合
     * @param questionnaireQuestion 问卷问题关系对象
     */
    private void setQuestionDataBOList(List<QuestionnaireQuestionDataBO> questionDataBOList, Map<Integer, Question> questionMap, QuestionnaireQuestion questionnaireQuestion) {
        Question question = questionMap.get(questionnaireQuestion.getQuestionId());
        QuestionnaireQuestionDataBO questionnaireQuestionDataBO = new QuestionnaireQuestionDataBO();
        questionnaireQuestionDataBO.setQuestion(question);
        questionnaireQuestionDataBO.setIsHidden(questionnaireQuestion.getIsHidden());
        if (Objects.isNull(question)){
            return;
        }
        setDataList(questionnaireQuestion, question, questionnaireQuestionDataBO);

        questionDataBOList.add(questionnaireQuestionDataBO);
    }

    private void setDataList(QuestionnaireQuestion questionnaireQuestion, Question question, QuestionnaireQuestionDataBO questionnaireQuestionDataBO) {
        if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)){
            questionnaireQuestionDataBO.setQuestionnaireDataBOList(getInputData(question, questionnaireQuestion));
        }
        if (Objects.equals(question.getType(),QuestionnaireConstant.RADIO)){
            questionnaireQuestionDataBO.setQuestionnaireDataBOList(getRadioOrCheckboxData(questionnaireQuestion, question,QuestionnaireConstant.RADIO_INPUT));
        }
        if (Objects.equals(question.getType(),QuestionnaireConstant.CHECKBOX)){
            questionnaireQuestionDataBO.setQuestionnaireDataBOList(getRadioOrCheckboxData(questionnaireQuestion, question,QuestionnaireConstant.CHECKBOX_INPUT));
        }
    }


    /**
     * 获取单选或者多选数据结构
     * @param questionnaireQuestion 问卷问题关系对象
     * @param question 问题对象
     * @param typeInput 单选或者多选Input类型
     */
    private List<QuestionnaireDataBO> getRadioOrCheckboxData(QuestionnaireQuestion questionnaireQuestion, Question question, String typeInput) {
        Map<String, QesDataDO> qesDataDoMap = questionnaireQuestion.getQesData().stream().collect(Collectors.toMap(QesDataDO::getOptionId, Function.identity()));
        List<QuestionnaireDataBO> radioOrCheckboxList = Lists.newArrayList();
        for (Option option : question.getOptions()) {
            QesDataDO qesDataDO = qesDataDoMap.get(option.getId());
            QuestionnaireDataBO questionnaireDataBO = buildRadioOrCheckboxData(questionnaireQuestion, qesDataDO,QuestionnaireConstant.RADIO);
            questionnaireDataBO.setExcelAnswer(option.getText());
            setRadioOrCheckboxInputData(typeInput, qesDataDoMap, option, questionnaireDataBO);
            radioOrCheckboxList.add(questionnaireDataBO);
        }
        return radioOrCheckboxList;
    }

    /**
     * 设置单选或者多选中Input类型数据结构
     * @param typeInput 单选或者多选Input类型
     * @param qesDataDoMap qes数据对象集合
     * @param option 选项对象
     * @param questionnaireDataBO 问卷rec数据结构信息
     */
    private void setRadioOrCheckboxInputData(String typeInput, Map<String, QesDataDO> qesDataDoMap, Option option, QuestionnaireDataBO questionnaireDataBO) {
        if (Objects.equals(option.getType(),typeInput)){
            String excelAnswer = questionnaireDataBO.getExcelAnswer();
            List<QuestionnaireDataBO> radioOrCheckboxInputList =Lists.newArrayList();

            for (Map.Entry<String, Object> entry : option.getOption().entrySet()) {
                InputOption inputOption = JSON.parseObject(JSON.toJSONString(entry.getValue()), InputOption.class);
                QesDataDO radioInputQes = qesDataDoMap.get(inputOption.getId());
                if (Objects.nonNull(radioInputQes)){
                    excelAnswer = excelAnswer.replace(String.format(QuestionnaireConstant.PLACEHOLDER, entry.getKey()), String.format(QuestionnaireConstant.PLACEHOLDER,radioInputQes.getQesField()));
                }
                QuestionnaireDataBO dataBO = buildRadioOrCheckboxInputData(qesDataDoMap, inputOption, typeInput, questionnaireDataBO.getQuestionId());
                if (Objects.isNull(dataBO)){
                    continue;
                }
                radioOrCheckboxInputList.add(dataBO);
            }
            questionnaireDataBO.setExcelAnswer(excelAnswer);
            questionnaireDataBO.setQuestionnaireDataBOList(radioOrCheckboxInputList);
        }
    }

    /**
     * 构建单选或者多选数据结构
     * @param questionnaireQuestion 问卷问题关系对象
     * @param qesDataDO qes数据对象
     * @param type 选项类型
     */
    private QuestionnaireDataBO buildRadioOrCheckboxData(QuestionnaireQuestion questionnaireQuestion, QesDataDO qesDataDO, String type) {
        return new QuestionnaireDataBO()
                    .setQesField(qesDataDO.getQesField())
                    .setRecAnswer(qesDataDO.getQesSerialNumber())
                    .setShowSerialNumber(qesDataDO.getShowSerialNumber())
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
    private QuestionnaireDataBO buildRadioOrCheckboxInputData(Map<String, QesDataDO> qesDataDoMap, InputOption inputOption, String type, Integer questionId) {
        QesDataDO radioInputQes = qesDataDoMap.get(inputOption.getId());
        if (Objects.isNull(radioInputQes)){
            return null;
        }
        return new QuestionnaireDataBO()
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
    private List<QuestionnaireDataBO> getInputData(Question question, QuestionnaireQuestion questionnaireQuestion) {
        Option option = question.getOptions().get(0);

        Map<String, InputOption> inputOptionMap = option.getOption()
                .values().stream()
                .map(value -> JSON.parseObject(JSON.toJSONString(value), InputOption.class))
                .collect(Collectors.toMap(InputOption::getId, Function.identity(),(v1,v2)->v2));
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
    private QuestionnaireDataBO buildInputDataBO(Map<String, InputOption> inputOptionMap, QuestionnaireQuestion questionnaireQuestion, String type, QesDataDO qesDataDO) {
        InputOption inputOption = inputOptionMap.get(qesDataDO.getOptionId());
        return new QuestionnaireDataBO()
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
    public static QuestionnaireInfoBuilder build(ThreeTuple<Questionnaire, List<QuestionnaireQuestion>, List<Question>> questionnaireBaseInfo){
        return new QuestionnaireInfoBuilder()
                .setQuestionnaire(questionnaireBaseInfo.getFirst())
                .setQuestionnaireQuestionList(questionnaireBaseInfo.getSecond())
                .setQuestionList(questionnaireBaseInfo.getThird());
    }

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
    private static QuestionnaireInfoBO.QuestionBO buildQuestionBO(QuestionnaireQuestion questionnaireQuestion, Question question) {
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
