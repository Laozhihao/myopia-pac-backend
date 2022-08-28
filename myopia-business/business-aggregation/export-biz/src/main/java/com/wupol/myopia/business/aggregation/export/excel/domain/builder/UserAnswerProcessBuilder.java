package com.wupol.myopia.business.aggregation.export.excel.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.*;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.util.AnswerUtil;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import com.wupol.myopia.rec.domain.RecExportDTO;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户答案处理
 *
 * @author hang.yuan 2022/8/28 17:32
 */
@UtilityClass
public class UserAnswerProcessBuilder {

    /**
     * 构建导出条件
     *
     * @param generateRecDataBO 生成rec数据
     * @param recFileName 问卷类型
     */
    public RecExportDTO buildRecExportDTO(GenerateRecDataBO generateRecDataBO, String recFileName) {
        RecExportDTO recExportDTO = new RecExportDTO();
        recExportDTO.setQesUrl(generateRecDataBO.getQesUrl());
        recExportDTO.setDataList(generateRecDataBO.getDataList());
        recExportDTO.setRecName(recFileName);
        return recExportDTO;
    }

    /**
     * rec导出工具生成的压缩包文件，解压及移动
     * @param recZip 压缩包路径
     * @param epiDataPath 基础路径
     * @param recFolderName rec文件夹名称
     */
    public void recFileMove(String recZip,String epiDataPath, String recFolderName){
        ZipUtil.unzip(recZip,epiDataPath);
        recFolderName = Paths.get(epiDataPath, recFolderName).toString();
        File[] files = FileUtil.newFile(recFolderName).listFiles();
        if (ArrayUtil.isEmpty(files)){
            return;
        }
        for (File file : files) {
            FileUtil.move(file,FileUtil.newFile(epiDataPath),true);
        }
        FileUtil.del(recZip);
        FileUtil.del(recFolderName);
    }


    /**
     * 获取导出REC的数据
     *
     * @param userQuestionnaireAnswerBOList 用户问卷记录集合
     * @param dataBuildList                 问题Rec数据结构集合
     * @param qesFieldList                  有序问卷字段
     */
    public Map<String, List<QuestionnaireRecDataBO>> getRecData(List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList,
                                                                List<QuestionnaireQuestionRecDataBO> dataBuildList,
                                                                List<String> qesFieldList) {

        Map<String, List<QuestionnaireRecDataBO>> dataMap = Maps.newHashMap();

        //学校里的每个学生或者用户
        for (UserQuestionnaireAnswerBO userQuestionnaireAnswerBO : userQuestionnaireAnswerBOList) {

            List<QuestionnaireRecDataBO> dataList = Lists.newArrayList();
            Map<Integer, Map<String, OptionAnswer>> questionAnswerMap = userQuestionnaireAnswerBO.getAnswerMap();
            //每个学生或者用户完成数据
            for (QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO : dataBuildList) {
                if (Objects.equals(Boolean.TRUE, questionnaireQuestionRecDataBO.getIsHidden())) {
                    //隐藏问题
                    hideQuestionDataProcess(dataList, questionnaireQuestionRecDataBO, userQuestionnaireAnswerBO);
                    continue;
                }
                processAnswerData(dataList, questionAnswerMap, questionnaireQuestionRecDataBO);
            }
            Map<String, QuestionnaireRecDataBO> studentDataMap = dataList.stream().collect(Collectors.toMap(questionnaireRecDataBO -> AnswerUtil.getQesFieldStr(questionnaireRecDataBO.getQesField()), Function.identity()));
            List<QuestionnaireRecDataBO> collect = qesFieldList.stream().map(studentDataMap::get).collect(Collectors.toList());
            dataMap.put(userQuestionnaireAnswerBO.getUserKey(), collect);
        }
        return dataMap;
    }

    /**
     * 隐藏问题处理
     *
     * @param dataList                       结果集合
     * @param questionnaireQuestionRecDataBO 问卷问题rec数据结构对象
     * @param userQuestionnaireAnswerBO      用户问卷答案对象
     */
    private void hideQuestionDataProcess(List<QuestionnaireRecDataBO> dataList,
                                         QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO,
                                         UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        Question question = questionnaireQuestionRecDataBO.getQuestion();
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireQuestionRecDataBO.getQuestionnaireRecDataBOList();
        Map<String, List<QuestionnaireRecDataBO>> questionnaireRecDataMap = questionnaireRecDataBOList.stream().collect(Collectors.groupingBy(QuestionnaireRecDataBO::getQesField));

        List<QesFieldDataBO> qesFieldDataBOList = userQuestionnaireAnswerBO.getQesFieldDataBOList();
        if (CollUtil.isEmpty(qesFieldDataBOList)){
            return;
        }
        Map<String, QesFieldDataBO> qesFieldDataBoMap = qesFieldDataBOList.stream().collect(Collectors.toMap(QesFieldDataBO::getQesField, Function.identity()));
        questionnaireRecDataMap.forEach((qesField, recDataList) -> {
            if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)) {
                getHideInputData(dataList, qesFieldDataBoMap, recDataList);
            }
            if (Objects.equals(question.getType(), QuestionnaireConstant.RADIO)) {
                getHideRadio(dataList, qesFieldDataBoMap, recDataList.get(0));
            }
        });
    }

    /**
     * 获取隐藏Input类型数据
     *
     * @param dataList          结果集合
     * @param qesFieldDataBOMap qes字段数据集合
     * @param recDataList       问卷Rec数据信息集合
     */
    private void getHideInputData(List<QuestionnaireRecDataBO> dataList, Map<String, QesFieldDataBO> qesFieldDataBOMap, List<QuestionnaireRecDataBO> recDataList) {
        if (CollUtil.isEmpty(recDataList)){
            return;
        }
        for (QuestionnaireRecDataBO recDataBO : recDataList) {
            QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataBO);
            String answer = Optional.ofNullable(qesFieldDataBOMap.get(questionnaireRecDataBO.getQesField())).map(qesFieldDataBO -> Optional.ofNullable(qesFieldDataBO.getRecAnswer()).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                if (Objects.equals(questionnaireRecDataBO.getQesField(), "ID1") || Objects.equals(questionnaireRecDataBO.getQesField(), "ID2")) {
                    questionnaireRecDataBO.setRecAnswer(answer);
                } else {
                    questionnaireRecDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireRecDataBO.getRange()));
                }
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                if (Objects.equals(questionnaireRecDataBO.getQesField(), "date")) {
                    questionnaireRecDataBO.setRecAnswer(answer);
                } else {
                    questionnaireRecDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
                }
            }
            dataList.add(questionnaireRecDataBO);
        }
    }

    /**
     * 获取隐藏单选数据
     *
     * @param dataList               结果集合
     * @param qesFieldDataBoMap      qes字段数据集合
     * @param recDataBO 问卷Rec数据信息
     */
    private void getHideRadio(List<QuestionnaireRecDataBO> dataList,
                              Map<String, QesFieldDataBO> qesFieldDataBoMap,
                              QuestionnaireRecDataBO recDataBO) {
        if (Objects.isNull(recDataBO)){
            return;
        }
        QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataBO);
        QesFieldDataBO qesFieldDataBO = qesFieldDataBoMap.get(questionnaireRecDataBO.getQesField());
        if (Objects.isNull(qesFieldDataBO)) {
            return;
        }

        questionnaireRecDataBO.setRecAnswer(qesFieldDataBO.getRecAnswer());
        dataList.add(questionnaireRecDataBO);
        //单选或者多选Input
        getHideRadioInputData(dataList, qesFieldDataBoMap, questionnaireRecDataBO);
    }

    /**
     * 获取隐藏单选输入数据类型数据
     * @param dataList 结果集合
     * @param qesFieldDataBOMap qes字段数据集合
     * @param questionnaireRecDataBO  rec数据对象
     */
    private void getHideRadioInputData(List<QuestionnaireRecDataBO> dataList,
                                       Map<String, QesFieldDataBO> qesFieldDataBOMap,
                                       QuestionnaireRecDataBO questionnaireRecDataBO) {
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireRecDataBO.getQuestionnaireRecDataBOList();
        if (CollUtil.isEmpty(questionnaireRecDataBOList)) {
            return;
        }
        getHideInputData(dataList, qesFieldDataBOMap, questionnaireRecDataBOList);
    }



    /**
     * 处理答案数据
     *
     * @param dataList                       结果集合
     * @param questionAnswerMap              问题集合
     * @param questionnaireQuestionRecDataBO 问卷问题Rec数据信息
     */
    private void processAnswerData(List<QuestionnaireRecDataBO> dataList, Map<Integer, Map<String, OptionAnswer>> questionAnswerMap, QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO) {
        Question question = questionnaireQuestionRecDataBO.getQuestion();
        Map<String, OptionAnswer> answerMap = questionAnswerMap.getOrDefault(question.getId(), Maps.newHashMap());
        List<QuestionnaireRecDataBO> recDataList = questionnaireQuestionRecDataBO.getQuestionnaireRecDataBOList();

        if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)) {
            getInputData(dataList, answerMap, recDataList);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.RADIO)) {
            answerMap.putAll(questionAnswerMap.getOrDefault(-1,Maps.newHashMap()));
            getRadioData(dataList, answerMap, recDataList);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.CHECKBOX)) {
            recDataList.forEach(questionnaireRecDataBO -> getCheckboxData(dataList, answerMap, questionnaireRecDataBO));
        }
    }

    /**
     * 获取Input类型数据
     *
     * @param dataList    结果集合
     * @param answerMap   问题集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getInputData(List<QuestionnaireRecDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireRecDataBO> recDataList) {
        for (QuestionnaireRecDataBO recDataBO : recDataList) {
            QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataBO);
            OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
            String answer = Optional.ofNullable(optionAnswer).map(OptionAnswer::getValue).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireRecDataBO.getRange()));
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
            }
            dataList.add(questionnaireRecDataBO);
        }
    }

    /**
     * 获取单选数据
     *
     * @param dataList               结果集合
     * @param answerMap      答案集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getRadioData(List<QuestionnaireRecDataBO> dataList,
                              Map<String, OptionAnswer> answerMap,
                              List<QuestionnaireRecDataBO> recDataList) {
        if (CollUtil.isEmpty(recDataList)){
            return;
        }
        List<QuestionnaireRecDataBO> inputList = recDataList.stream().map(QuestionnaireRecDataBO::getQuestionnaireRecDataBOList).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
        QuestionnaireRecDataBO questionnaireRecDataBO = getQuestionnaireRecDataBO(recDataList, answerMap);
        if (!Objects.equals(questionnaireRecDataBO.getQesField(),QuestionnaireConstant.QM)){
            dataList.add(questionnaireRecDataBO);
        }
        if (CollUtil.isEmpty(inputList)) {
            return;
        }
        getRadioInputData(dataList, answerMap, inputList);
    }

    /**
     * 获取单选的问卷Rec数据信息
     *
     * @param recDataList 问卷Rec数据信息集合
     * @param answerMap   答案集合
     */
    private QuestionnaireRecDataBO getQuestionnaireRecDataBO(List<QuestionnaireRecDataBO> recDataList,
                                                             Map<String, OptionAnswer> answerMap) {
        //初始化单选值
        List<QuestionnaireRecDataBO> result = Lists.newArrayList();

        for (QuestionnaireRecDataBO questionnaireRecDataBO : recDataList) {
            OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
            if (Objects.isNull(optionAnswer)) {
                continue;
            }
            result.add(questionnaireRecDataBO);
        }
        if (CollUtil.isEmpty(result)) {
            QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataList.get(0));
            questionnaireRecDataBO.setRecAnswer(StrUtil.EMPTY);
            return questionnaireRecDataBO;
        }
        return result.get(0);
    }

    /**
     * 获取单选中输入框类型的数据
     * @param dataList 结果集合
     * @param answerMap 问题集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getRadioInputData(List<QuestionnaireRecDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireRecDataBO> recDataList) {

        for (QuestionnaireRecDataBO recDataBO : recDataList) {
            QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataBO);
            OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
            String answer = Optional.ofNullable(optionAnswer).map(OptionAnswer::getValue).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireRecDataBO.getRange()));
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(),QuestionnaireConstant.SELECT)){
                questionnaireRecDataBO.setRecAnswer(answer);
            }

            if (Objects.nonNull(optionAnswer) && StrUtil.isNotBlank(answer) ){
                dataList.add(questionnaireRecDataBO);
            }
        }
    }

    /**
     * 获取单元或者多选类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param recDataBO 问卷Rec数据信息
     */
    private void getCheckboxData(List<QuestionnaireRecDataBO> dataList,
                                 Map<String, OptionAnswer> answerMap,
                                 QuestionnaireRecDataBO recDataBO) {
        QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataBO);
        OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
        if (Objects.isNull(optionAnswer)) {
            questionnaireRecDataBO.setRecAnswer("2");
        }else {
            questionnaireRecDataBO.setRecAnswer("1");
        }
        //多选Input
        getCheckboxInputData(dataList, answerMap, questionnaireRecDataBO);
    }


    /**
     * 获取单元或者多选Input类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     */
    private void getCheckboxInputData(List<QuestionnaireRecDataBO> dataList,
                                      Map<String, OptionAnswer> answerMap,
                                      QuestionnaireRecDataBO questionnaireRecDataBO) {
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireRecDataBO.getQuestionnaireRecDataBOList();
        if (CollUtil.isEmpty(questionnaireRecDataBOList)) {
            dataList.add(questionnaireRecDataBO);
            return;
        }

        if (!Objects.equals(questionnaireRecDataBO.getQesField(),QuestionnaireConstant.QM)){
            questionnaireRecDataBO.setQuestionnaireRecDataBOList(null);
            dataList.add(questionnaireRecDataBO);
        }
        getInputData(dataList, answerMap, questionnaireRecDataBOList);
    }

    /**
     * 构建导出rec数据实体
     * @param qesFieldList qes字段集合
     * @param qesUrl qes url地址
     * @param schoolId 学校ID
     * @param answersMap 答案集合
     */
    public GenerateRecDataBO buildGenerateRecDataBO(List<String> qesFieldList, String qesUrl, Integer schoolId, Map<String, List<QuestionnaireRecDataBO>> answersMap) {
        List<String> dataTxt = getDataTxtList(qesFieldList, answersMap);
        return new GenerateRecDataBO(schoolId, qesUrl, dataTxt);
    }

    /**
     * 构建政府导出rec数据实体
     * @param qesFieldList qes字段集合
     * @param qesUrl qes url地址
     * @param governmentKey 政府唯一key
     * @param answersMap 答案集合
     */
    public GenerateRecDataBO buildGovernmentGenerateRecDataBO(List<String> qesFieldList, String qesUrl, String governmentKey, Map<String, List<QuestionnaireRecDataBO>> answersMap) {
        List<String> dataTxt = getDataTxtList(qesFieldList, answersMap);
        return new GenerateRecDataBO(governmentKey, qesUrl, dataTxt);
    }

    /**
     * 获取生成txt文件的数据
     * @param qesFieldList qes字段集合
     * @param answersMap 答案集合
     */
    private static List<String> getDataTxtList(List<String> qesFieldList, Map<String, List<QuestionnaireRecDataBO>> answersMap) {
        List<List<String>> dataList = new ArrayList<>();
        answersMap.forEach((userKey, answerList) -> dataList.add(answerList.stream()
                .map(answer -> Optional.ofNullable(answer)
                        .map(questionnaireRecDataBO -> Optional.ofNullable(questionnaireRecDataBO.getRecAnswer()).orElse(StrUtil.EMPTY))
                        .orElse(StrUtil.EMPTY))
                .collect(Collectors.toList())));
        return EpiDataUtil.mergeDataTxt(qesFieldList, dataList);
    }



    /**
     * 构建获取答案数据条件
     * @param userQuestionRecordList 用户问卷记录集合
     * @param generateDataCondition 生成数据条件
     */
    public AnswerDataBO buildAnswerData(List<UserQuestionRecord> userQuestionRecordList, GenerateDataCondition generateDataCondition){
        return new AnswerDataBO()
                .setExportCondition(generateDataCondition.getExportCondition())
                .setUserQuestionRecordList(userQuestionRecordList)
                .setGradeTypeList(generateDataCondition.getGradeTypeList())
                .setQuestionnaireTypeEnum(generateDataCondition.getMainBodyType());
    }

}
