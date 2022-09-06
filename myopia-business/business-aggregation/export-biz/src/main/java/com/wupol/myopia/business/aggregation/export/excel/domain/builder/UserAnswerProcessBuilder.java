package com.wupol.myopia.business.aggregation.export.excel.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.*;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireQuestionDataBO;
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
    public Map<String, List<QuestionnaireDataBO>> getRecData(List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList,
                                                             List<QuestionnaireQuestionDataBO> dataBuildList,
                                                             List<String> qesFieldList) {

        Map<String, List<QuestionnaireDataBO>> dataMap = Maps.newHashMap();

        //学校里的每个学生或者用户
        for (UserQuestionnaireAnswerBO userQuestionnaireAnswerBO : userQuestionnaireAnswerBOList) {

            List<QuestionnaireDataBO> dataList = Lists.newArrayList();
            Map<Integer, Map<String, OptionAnswer>> questionAnswerMap = userQuestionnaireAnswerBO.getAnswerMap();
            //每个学生或者用户完成数据
            for (QuestionnaireQuestionDataBO questionnaireQuestionDataBO : dataBuildList) {
                if (Objects.equals(Boolean.TRUE, questionnaireQuestionDataBO.getIsHidden())) {
                    //隐藏问题
                    hideQuestionDataProcess(dataList, questionnaireQuestionDataBO, userQuestionnaireAnswerBO);
                    continue;
                }
                processAnswerData(dataList, questionAnswerMap, questionnaireQuestionDataBO);
            }
            Map<String, QuestionnaireDataBO> studentDataMap = dataList.stream().collect(Collectors.toMap(questionnaireRecDataBO -> AnswerUtil.getQesFieldStr(questionnaireRecDataBO.getQesField()), Function.identity(),(v1, v2)->v2));
            List<QuestionnaireDataBO> collect = qesFieldList.stream().map(studentDataMap::get).collect(Collectors.toList());
            dataMap.put(userQuestionnaireAnswerBO.getUserKey(), collect);
        }
        return dataMap;
    }


    /**
     * 隐藏问题处理
     *
     * @param dataList                       结果集合
     * @param questionnaireQuestionDataBO 问卷问题rec数据结构对象
     * @param userQuestionnaireAnswerBO      用户问卷答案对象
     */
    private void hideQuestionDataProcess(List<QuestionnaireDataBO> dataList,
                                         QuestionnaireQuestionDataBO questionnaireQuestionDataBO,
                                         UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        Question question = questionnaireQuestionDataBO.getQuestion();
        List<QuestionnaireDataBO> questionnaireDataBOList = questionnaireQuestionDataBO.getQuestionnaireDataBOList();
        Map<String, List<QuestionnaireDataBO>> questionnaireRecDataMap = questionnaireDataBOList.stream().collect(Collectors.groupingBy(QuestionnaireDataBO::getQesField));

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
    private void getHideInputData(List<QuestionnaireDataBO> dataList, Map<String, QesFieldDataBO> qesFieldDataBOMap, List<QuestionnaireDataBO> recDataList) {
        if (CollUtil.isEmpty(recDataList)){
            return;
        }
        for (QuestionnaireDataBO recDataBO : recDataList) {
            QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataBO);
            String answer = Optional.ofNullable(qesFieldDataBOMap.get(questionnaireDataBO.getQesField())).map(qesFieldDataBO -> Optional.ofNullable(qesFieldDataBO.getRecAnswer()).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                if (Objects.equals(questionnaireDataBO.getQesField(), "ID1") || Objects.equals(questionnaireDataBO.getQesField(), "ID2")) {
                    questionnaireDataBO.setRecAnswer(answer);
                } else {
                    questionnaireDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireDataBO.getRange()));
                }
            }
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                if (Objects.equals(questionnaireDataBO.getQesField(), "date")) {
                    questionnaireDataBO.setRecAnswer(answer);
                } else {
                    questionnaireDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
                }
            }
            dataList.add(questionnaireDataBO);
        }
    }

    /**
     * 获取隐藏单选数据
     *
     * @param dataList               结果集合
     * @param qesFieldDataBoMap      qes字段数据集合
     * @param recDataBO 问卷Rec数据信息
     */
    private void getHideRadio(List<QuestionnaireDataBO> dataList,
                              Map<String, QesFieldDataBO> qesFieldDataBoMap,
                              QuestionnaireDataBO recDataBO) {
        if (Objects.isNull(recDataBO)){
            return;
        }
        QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataBO);
        QesFieldDataBO qesFieldDataBO = qesFieldDataBoMap.get(questionnaireDataBO.getQesField());
        if (Objects.isNull(qesFieldDataBO)) {
            return;
        }

        questionnaireDataBO.setRecAnswer(qesFieldDataBO.getRecAnswer());
        dataList.add(questionnaireDataBO);
        //单选或者多选Input
        getHideRadioInputData(dataList, qesFieldDataBoMap, questionnaireDataBO);
    }

    /**
     * 获取隐藏单选输入数据类型数据
     * @param dataList 结果集合
     * @param qesFieldDataBOMap qes字段数据集合
     * @param questionnaireDataBO  rec数据对象
     */
    private void getHideRadioInputData(List<QuestionnaireDataBO> dataList,
                                       Map<String, QesFieldDataBO> qesFieldDataBOMap,
                                       QuestionnaireDataBO questionnaireDataBO) {
        List<QuestionnaireDataBO> questionnaireDataBOList = questionnaireDataBO.getQuestionnaireDataBOList();
        if (CollUtil.isEmpty(questionnaireDataBOList)) {
            return;
        }
        getHideInputData(dataList, qesFieldDataBOMap, questionnaireDataBOList);
    }



    /**
     * 处理答案数据
     *
     * @param dataList                       结果集合
     * @param questionAnswerMap              问题集合
     * @param questionnaireQuestionDataBO 问卷问题Rec数据信息
     */
    private void processAnswerData(List<QuestionnaireDataBO> dataList, Map<Integer, Map<String, OptionAnswer>> questionAnswerMap, QuestionnaireQuestionDataBO questionnaireQuestionDataBO) {
        Question question = questionnaireQuestionDataBO.getQuestion();
        Map<String, OptionAnswer> answerMap = questionAnswerMap.getOrDefault(question.getId(), Maps.newHashMap());
        List<QuestionnaireDataBO> recDataList = questionnaireQuestionDataBO.getQuestionnaireDataBOList();

        if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)) {
            getInputData(dataList, answerMap, recDataList,QuestionnaireConstant.INPUT);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.RADIO)) {
            answerMap.putAll(questionAnswerMap.getOrDefault(-1,Maps.newHashMap()));
            getRadioData(dataList, answerMap, recDataList);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.CHECKBOX)) {
            answerMap.putAll(questionAnswerMap.getOrDefault(-1,Maps.newHashMap()));
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
    private void getInputData(List<QuestionnaireDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireDataBO> recDataList,String type) {
        for (QuestionnaireDataBO recDataBO : recDataList) {
            QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataBO);
            OptionAnswer optionAnswer = answerMap.get(questionnaireDataBO.getOptionId());
            String answer = Optional.ofNullable(optionAnswer).map(OptionAnswer::getValue).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                questionnaireDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireDataBO.getRange()));
            }
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
            }
            if (Objects.equals(questionnaireDataBO.getDataType(),QuestionnaireConstant.SELECT)){
                questionnaireDataBO.setRecAnswer(answer);
            }
            questionnaireDataBO.setType(type);
            dataList.add(questionnaireDataBO);
        }
    }

    /**
     * 获取单选数据
     *
     * @param dataList               结果集合
     * @param answerMap      答案集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getRadioData(List<QuestionnaireDataBO> dataList,
                              Map<String, OptionAnswer> answerMap,
                              List<QuestionnaireDataBO> recDataList) {
        if (CollUtil.isEmpty(recDataList)){
            return;
        }
        List<QuestionnaireDataBO> inputList = recDataList.stream().map(QuestionnaireDataBO::getQuestionnaireDataBOList).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
        QuestionnaireDataBO questionnaireDataBO = getQuestionnaireRecDataBO(recDataList, answerMap);
        if (!Objects.equals(questionnaireDataBO.getQesField(),QuestionnaireConstant.QM)){
            questionnaireDataBO.setType(QuestionnaireConstant.RADIO);
            dataList.add(questionnaireDataBO);
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
    private QuestionnaireDataBO getQuestionnaireRecDataBO(List<QuestionnaireDataBO> recDataList,
                                                          Map<String, OptionAnswer> answerMap) {
        //初始化单选值
        List<QuestionnaireDataBO> result = Lists.newArrayList();

        for (QuestionnaireDataBO questionnaireDataBO : recDataList) {
            OptionAnswer optionAnswer = answerMap.get(questionnaireDataBO.getOptionId());
            if (Objects.isNull(optionAnswer)) {
                continue;
            }
            result.add(questionnaireDataBO);
        }
        if (CollUtil.isEmpty(result)) {
            QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataList.get(0));
            questionnaireDataBO.setRecAnswer(StrUtil.EMPTY);
            questionnaireDataBO.setExcelAnswer(StrUtil.EMPTY);
            return questionnaireDataBO;
        }
        return result.get(0);
    }

    /**
     * 获取单选中输入框类型的数据
     * @param dataList 结果集合
     * @param answerMap 问题集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getRadioInputData(List<QuestionnaireDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireDataBO> recDataList) {

        for (QuestionnaireDataBO recDataBO : recDataList) {
            QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataBO);
            OptionAnswer optionAnswer = answerMap.get(questionnaireDataBO.getOptionId());
            String answer = Optional.ofNullable(optionAnswer).map(OptionAnswer::getValue).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                questionnaireDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireDataBO.getRange()));
            }
            if (Objects.equals(questionnaireDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
            }
            if (Objects.equals(questionnaireDataBO.getDataType(),QuestionnaireConstant.SELECT)){
                questionnaireDataBO.setRecAnswer(answer);
            }

            if (Objects.nonNull(optionAnswer) && StrUtil.isNotBlank(answer) ){
                dataList.add(questionnaireDataBO);
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
    private void getCheckboxData(List<QuestionnaireDataBO> dataList,
                                 Map<String, OptionAnswer> answerMap,
                                 QuestionnaireDataBO recDataBO) {
        QuestionnaireDataBO questionnaireDataBO = ObjectUtil.cloneByStream(recDataBO);
        OptionAnswer optionAnswer = answerMap.get(questionnaireDataBO.getOptionId());
        if (Objects.isNull(optionAnswer)) {
            questionnaireDataBO.setRecAnswer("2");
        }else {
            questionnaireDataBO.setRecAnswer("1");
        }
        //多选Input
        getCheckboxInputData(dataList, answerMap, questionnaireDataBO);
    }


    /**
     * 获取单元或者多选Input类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireDataBO 问卷Rec数据信息
     */
    private void getCheckboxInputData(List<QuestionnaireDataBO> dataList,
                                      Map<String, OptionAnswer> answerMap,
                                      QuestionnaireDataBO questionnaireDataBO) {
        List<QuestionnaireDataBO> questionnaireDataBOList = questionnaireDataBO.getQuestionnaireDataBOList();
        if (CollUtil.isEmpty(questionnaireDataBOList)) {
            questionnaireDataBO.setType(QuestionnaireConstant.CHECKBOX);
            dataList.add(questionnaireDataBO);
            return;
        }

        if (!Objects.equals(questionnaireDataBO.getQesField(),QuestionnaireConstant.QM)){
            dataList.add(questionnaireDataBO);
        }
        getInputData(dataList, answerMap, questionnaireDataBOList,QuestionnaireConstant.CHECKBOX_INPUT);
    }

    /**
     * 构建导出rec数据实体
     * @param qesFieldList qes字段集合
     * @param qesUrl qes url地址
     * @param schoolId 学校ID
     * @param answersMap 答案集合
     */
    public GenerateRecDataBO buildGenerateRecDataBO(List<String> qesFieldList, String qesUrl, Integer schoolId, Map<String, List<QuestionnaireDataBO>> answersMap) {
        List<String> dataTxt = getDataTxtList(qesFieldList, answersMap);
        return new GenerateRecDataBO(schoolId, qesUrl, dataTxt);
    }

    /**
     * 构建导出EXCEL数据实体
     * @param schoolId 学校ID
     * @param answersMap 答案集合
     */
    public GenerateExcelDataBO buildGenerateExcelDataBO(Integer schoolId, Map<String, List<QuestionnaireDataBO>> answersMap,List<Integer> questionIds) {
        return new GenerateExcelDataBO(schoolId, getDataExcelList(answersMap,questionIds));
    }

    /**
     * 构建政府导出rec数据实体
     * @param qesFieldList qes字段集合
     * @param qesUrl qes url地址
     * @param governmentKey 政府唯一key
     * @param answersMap 答案集合
     */
    public GenerateRecDataBO buildGovernmentGenerateRecDataBO(List<String> qesFieldList, String qesUrl, String governmentKey, Map<String, List<QuestionnaireDataBO>> answersMap) {
        List<String> dataTxt = getDataTxtList(qesFieldList, answersMap);
        return new GenerateRecDataBO(governmentKey, qesUrl, dataTxt);
    }

    /**
     * 构建政府导出Excel数据实体
     * @param governmentKey 政府唯一key
     * @param answersMap 答案集合
     */
    public GenerateExcelDataBO buildGovernmentGenerateExcelDataBO(String governmentKey, Map<String, List<QuestionnaireDataBO>> answersMap) {
        List<JSONObject> dataTxt = getDataExcelList(answersMap,Lists.newArrayList());
        return new GenerateExcelDataBO(governmentKey, dataTxt);
    }

    /**
     * 获取生成txt文件的数据
     * @param qesFieldList qes字段集合
     * @param answersMap 答案集合
     */
    private static List<String> getDataTxtList(List<String> qesFieldList, Map<String, List<QuestionnaireDataBO>> answersMap) {
        List<List<String>> dataList = new ArrayList<>();
        answersMap.forEach((userKey, answerList) -> dataList.add(answerList.stream()
                .map(answer -> Optional.ofNullable(answer)
                        .map(questionnaireRecDataBO -> Optional.ofNullable(questionnaireRecDataBO.getRecAnswer()).orElse(StrUtil.EMPTY))
                        .orElse(StrUtil.EMPTY))
                .collect(Collectors.toList())));
        return EpiDataUtil.mergeDataTxt(qesFieldList, dataList);
    }

    /**
     * 获取生成Excel文件的数据
     * @param answersMap 答案集合
     * @param questionIds 记分问题集合
     */
    private static List<JSONObject> getDataExcelList(Map<String, List<QuestionnaireDataBO>> answersMap,List<Integer> questionIds) {
        List<JSONObject> dataList = new ArrayList<>();
        answersMap.forEach((userKey, answerList) -> setDataList(dataList, answerList,questionIds));
        return dataList;
    }

    /**
     * 设置数据集合
     * @param dataList 数据集合
     * @param answerList 答案集合
     * @param questionIds 记分问题集合
     */
    private static void setDataList(List<JSONObject> dataList, List<QuestionnaireDataBO> answerList,List<Integer> questionIds) {
        Map<Integer, List<QuestionnaireDataBO>> questionAnswerMap = answerList.stream().filter(Objects::nonNull).filter(questionnaireDataBO -> Objects.nonNull(questionnaireDataBO.getQuestionId())).collect(Collectors.groupingBy(QuestionnaireDataBO::getQuestionId));
        JSONObject data = new JSONObject();
        List<QuestionnaireDataBO> questionnaireDataList =Lists.newArrayList();
        questionAnswerMap.forEach((questionId,questionAnswerList)-> {
            setData(data, questionAnswerList);
            if (questionIds.contains(questionId)){
                questionnaireDataList.addAll(questionAnswerList);
            }
        });
        calculateScore(questionnaireDataList,data);
        dataList.add(data);
    }

    /**
     * 计算分数
     * @param questionnaireDataList 问卷数据集合
     * @param data 数据
     */
    private void calculateScore(List<QuestionnaireDataBO> questionnaireDataList,JSONObject data){
        if (CollUtil.isEmpty(questionnaireDataList)){
            return;
        }
        int sum = questionnaireDataList.stream()
                .map(QuestionnaireDataBO::getRecAnswer)
                .filter(answer -> Objects.nonNull(answer) && StrUtil.isNotBlank(answer))
                .mapToInt(Integer::parseInt).sum();
        data.put("total",sum);
    }

    /**
     * 设置数据
     * @param data 数据
     * @param questionAnswerList 答案集合
     */
    private static void setData(JSONObject data, List<QuestionnaireDataBO> questionAnswerList) {
        QuestionnaireDataBO questionnaireDataBO = questionAnswerList.get(0);
        if (Objects.equals(questionnaireDataBO.getType(), QuestionnaireConstant.INPUT)){
            setDataValue(data, questionAnswerList);
        }

        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.RADIO_INPUT)){
            setDataValue(data, questionAnswerList);
        }

        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.CHECKBOX_INPUT)){
            setDataValue(data, questionAnswerList);
        }

        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.RADIO)){
            setRadioDataValue(data, questionAnswerList, questionnaireDataBO);

        }
        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.CHECKBOX)){
            setCheckboxDataValue(data, questionAnswerList);

        }
    }

    /**
     * 设置多选值
     * @param data 数据
     * @param questionAnswerList 问题答案集合
     */
    private static void setCheckboxDataValue(JSONObject data, List<QuestionnaireDataBO> questionAnswerList) {
        List<String> answerDataList = getCheckboxDataList(questionAnswerList);
        if(CollUtil.isNotEmpty(answerDataList)){
            String answerValue = getCheckboxInputToString(questionAnswerList, answerDataList);
            for (QuestionnaireDataBO dataBO : questionAnswerList) {
                data.put(dataBO.getQesField().toLowerCase(), answerValue);
            }
        }
    }

    /**
     * 设置单选值
     * @param data 数据
     * @param questionAnswerList 问题答案集合
     */
    private static void setRadioDataValue(JSONObject data, List<QuestionnaireDataBO> questionAnswerList, QuestionnaireDataBO questionnaireDataBO) {
        String answerValue = Optional.ofNullable(questionnaireDataBO.getExcelAnswer()).orElse(StrUtil.EMPTY);
        boolean isCheckBox = false;
        for (QuestionnaireDataBO dataBO : questionAnswerList) {
            String answer = getAnswer(dataBO.getRecAnswer());
            if (Objects.equals(dataBO.getType(), QuestionnaireConstant.RADIO_INPUT)){
                answerValue = answerValue.replace(String.format(QuestionnaireConstant.PLACEHOLDER, dataBO.getQesField()), answer);
            }
            if (Objects.equals(dataBO.getType(),QuestionnaireConstant.CHECKBOX_INPUT)){
                isCheckBox = true;
                if (Objects.equals(questionnaireDataBO.getRecAnswer(),"2")){
                    answerValue = StrUtil.EMPTY;
                }else {
                    answerValue = answerValue.replace(String.format(QuestionnaireConstant.PLACEHOLDER, dataBO.getQesField()), answer);
                }
            }
        }
        data.put(questionnaireDataBO.getQesField().toLowerCase(), answerValue);

        if (Objects.equals(isCheckBox,Boolean.TRUE)){
            List<String> checkboxDataList = getCheckboxDataList(questionAnswerList);
            setCheckboxInputValue(data, questionAnswerList, questionnaireDataBO, checkboxDataList);
        }
    }

    /**
     * 数据设置值
     * @param data 数据
     * @param questionAnswerList 问题答案集合
     */
    private static void setDataValue(JSONObject data, List<QuestionnaireDataBO> questionAnswerList) {
        for (QuestionnaireDataBO dataBo : questionAnswerList) {
            data.put(dataBo.getQesField().toLowerCase(), getAnswer(dataBo.getRecAnswer()));
        }
    }

    /**
     * 设置多选加输入框值
     * @param data 数据
     * @param questionAnswerList 问题答案集合
     * @param questionnaireDataBO 问卷数据
     * @param checkboxDataList 多选选项集合
     */
    private static void setCheckboxInputValue(JSONObject data, List<QuestionnaireDataBO> questionAnswerList, QuestionnaireDataBO questionnaireDataBO, List<String> checkboxDataList) {
        if(CollUtil.isNotEmpty(checkboxDataList)){
            String checkboxValue = getCheckboxInputToString(questionAnswerList, checkboxDataList);
            if (Objects.equals(questionnaireDataBO.getQesField(),"c101")){
                data.put(questionnaireDataBO.getQesField().toLowerCase(), checkboxValue);
            }
            if (Objects.equals(questionnaireDataBO.getQesField(),"c201")){
                data.put(questionnaireDataBO.getQesField().toLowerCase(), checkboxValue);
            }
        }
    }

    /**
     * 获取多选加输入框的值转换
     * @param questionAnswerList 问题答案集合
     * @param checkboxDataList 多选数据选项集合
     */
    private static String getCheckboxInputToString(List<QuestionnaireDataBO> questionAnswerList, List<String> checkboxDataList) {
        String checkboxValue = CollUtil.join(checkboxDataList, "、");
        for (QuestionnaireDataBO dataBO : questionAnswerList) {
            if (Objects.equals(dataBO.getType(), QuestionnaireConstant.CHECKBOX_INPUT)) {
                checkboxValue = checkboxValue.replace(String.format(QuestionnaireConstant.PLACEHOLDER, dataBO.getQesField()), getAnswer(dataBO.getRecAnswer()));
            }
        }
        return checkboxValue;
    }

    /**
     * 获取多选数据集合
     * @param questionAnswerList 问题答案集合
     */
    private static List<String> getCheckboxDataList(List<QuestionnaireDataBO> questionAnswerList) {
        List<String> answerDataList = Lists.newArrayList();
        for (QuestionnaireDataBO dataBO : questionAnswerList) {
            if (Objects.equals(dataBO.getRecAnswer(),"2")){
                continue;
            }
            answerDataList.add(Optional.ofNullable(dataBO.getExcelAnswer()).orElse(StrUtil.EMPTY));
        }
        return answerDataList.stream().filter(answerData->!Objects.equals(answerData,StrUtil.EMPTY)).collect(Collectors.toList());
    }

    private String getAnswer(String answerStr){
        return Optional.ofNullable(answerStr)
                .map(answer-> answer.replace("\"",StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
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
