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
import com.wupol.myopia.business.core.school.constant.AreaTypeEnum;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.MonitorTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
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
            getInputData(dataList, answerMap, recDataList);
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
    private void getInputData(List<QuestionnaireDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireDataBO> recDataList) {
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
            questionnaireDataBO.setType(QuestionnaireConstant.INPUT);
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
            dataList.add(questionnaireDataBO);
            return;
        }

        if (!Objects.equals(questionnaireDataBO.getQesField(),QuestionnaireConstant.QM)){
            questionnaireDataBO.setType(QuestionnaireConstant.CHECKBOX);
            dataList.add(questionnaireDataBO);
        }
        getInputData(dataList, answerMap, questionnaireDataBOList);
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

    public GenerateExcelDataBO buildGenerateExcelDataBO(Integer schoolId, Map<String, List<QuestionnaireDataBO>> answersMap) {
        return new GenerateExcelDataBO(schoolId, getDataExcelList(answersMap));
    }

    /**
     * 转换值
     * @param generateExcelDataList 生成excel数据集合
     * @param schoolMap 学校集合
     * @param schoolDistrictMap 学校地区集合
     */
    public List<GenerateExcelDataBO> convertValue(List<GenerateExcelDataBO> generateExcelDataList,
                                                  Map<Integer, School> schoolMap,Map<Integer, List<String>> schoolDistrictMap){
        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataList) {
            List<JSONObject> dataExcelList = generateExcelDataBO.getDataList();
            School school = schoolMap.get(generateExcelDataBO.getSchoolId());
            List<String> districtList = schoolDistrictMap.get(generateExcelDataBO.getSchoolId());
            dataExcelList.sort(Comparator.comparing(o -> o.getInteger("a01")));
            for (JSONObject jsonObject : dataExcelList) {
                convertValue(school, districtList, jsonObject);
                jsonObject.put("schoolName",school.getName());
            }
            generateExcelDataBO.setDataList(dataExcelList);
        }

        return generateExcelDataList;
    }

    private static void convertValue(School school, List<String> districtList, JSONObject jsonObject) {
        if (jsonObject.containsKey("province")){
            jsonObject.put("province",getDistrictName(districtList,0));
        }
        if (jsonObject.containsKey("city")){
            jsonObject.put("city",getDistrictName(districtList,1));
        }
        if (jsonObject.containsKey("county")){
            jsonObject.put("county",getDistrictName(districtList,2));
        }
        if (jsonObject.containsKey("district")){
            jsonObject.put("district", Optional.ofNullable(school.getAreaType()).map(type-> Optional.ofNullable(AreaTypeEnum.get(type)).map(AreaTypeEnum::getName).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY));
        }
        if (jsonObject.containsKey("point")){
            jsonObject.put("point",Optional.ofNullable(school.getMonitorType()).map(type-> Optional.ofNullable(MonitorTypeEnum.get(type)).map(MonitorTypeEnum::getName).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY));
        }
        if (jsonObject.containsKey("a01")){
            String gradeCode = jsonObject.getString("a01");
            gradeCode = gradeCode.length()==1?"0"+gradeCode:gradeCode;
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            jsonObject.put("a01",gradeCodeEnum.getName());
        }
    }

    /**
     * 获取地区名称
     * @param districtList 区域名称集合
     * @param index 下标
     */
    private static String getDistrictName(List<String> districtList ,Integer index){
        return CollUtil.isNotEmpty(districtList) ? districtList.get(index):StrUtil.EMPTY;
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
        List<JSONObject> dataTxt = getDataExcelList(answersMap);
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
     */
    private static List<JSONObject> getDataExcelList(Map<String, List<QuestionnaireDataBO>> answersMap) {
        List<JSONObject> dataList = new ArrayList<>();
        answersMap.forEach((userKey, answerList) -> setDataList(dataList, answerList));
        return dataList;
    }

    private static void setDataList(List<JSONObject> dataList, List<QuestionnaireDataBO> answerList) {
        Map<Integer, List<QuestionnaireDataBO>> questionAnswerMap = answerList.stream().filter(Objects::nonNull).filter(questionnaireDataBO -> Objects.nonNull(questionnaireDataBO.getQuestionId())).collect(Collectors.groupingBy(QuestionnaireDataBO::getQuestionId));
        JSONObject data = new JSONObject();
        questionAnswerMap.forEach((questionId,questionAnswerList)-> setData(data, questionAnswerList));
        dataList.add(data);
    }

    private static void setData(JSONObject data, List<QuestionnaireDataBO> questionAnswerList) {
        String  placeholder = "-{%s}";
        QuestionnaireDataBO questionnaireDataBO = questionAnswerList.get(0);
        if (Objects.equals(questionnaireDataBO.getType(), QuestionnaireConstant.INPUT)){
            for (QuestionnaireDataBO dataBo : questionAnswerList) {
                data.put(dataBo.getQesField().toLowerCase(),Optional.ofNullable(dataBo.getRecAnswer()).orElse(StrUtil.EMPTY));
            }
        }
        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.RADIO)){
            String answerValue = getAnswerValue(questionnaireDataBO.getShowSerialNumber(), questionnaireDataBO.getExcelAnswer());
            for (QuestionnaireDataBO dataBO : questionAnswerList) {
                if (Objects.equals(dataBO.getType(),QuestionnaireConstant.RADIO_INPUT) && Objects.nonNull(answerValue)){
                    answerValue = answerValue.replace(String.format(placeholder, dataBO.getQesField()), Optional.ofNullable(dataBO.getRecAnswer()).orElse(StrUtil.EMPTY));
                }
            }
            data.put(questionnaireDataBO.getQesField().toLowerCase(), answerValue);
        }
        if (Objects.equals(questionnaireDataBO.getType(),QuestionnaireConstant.CHECKBOX)){

            List<String> answerDataList = Lists.newArrayList();
            for (QuestionnaireDataBO dataBO : questionAnswerList) {
                if (!Objects.equals(dataBO.getRecAnswer(),"1")){
                    continue;
                }
                answerDataList.add(getAnswerValue(dataBO.getShowSerialNumber(),dataBO.getExcelAnswer()));
            }
            if(CollUtil.isNotEmpty(answerDataList)){
                String answerData = CollUtil.join(answerDataList, " ");
                questionAnswerList.forEach(dataBO -> data.put(dataBO.getQesField().toLowerCase(),answerData));
            }
        }
    }


    private String getAnswerValue(String showNum,String answer){
        answer = Optional.ofNullable(answer).orElse(StrUtil.EMPTY);
        if (StrUtil.isBlank(answer)){
            return answer;
        }
        showNum = Optional.ofNullable(showNum).orElse(StrUtil.EMPTY);
        return showNum+answer;
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
