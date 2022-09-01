package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
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
    private final QesFieldMappingService qesFieldMappingService;
    private final QuestionnaireQesService questionnaireQesService;

    private Map<Integer,List<Integer>> scoreMap = Maps.newConcurrentMap();

    private static final String TOTAL_SCORE="总分";

    /**
     * 获取问卷信息（问卷+问题）
     * @param questionnaireId 问卷ID
     */
    public QuestionnaireInfoBO getQuestionnaireInfo(Integer questionnaireId){
        ThreeTuple<Questionnaire, List<QuestionnaireQuestion>, List<Question>> questionnaireBaseInfo = getQuestionnaireBaseInfo(questionnaireId);
        if (Objects.isNull(questionnaireBaseInfo)){
            return null;
        }
//        return com.wupol.myopia.business.core.questionnaire.domain.builder.QuestionnaireInfoBuilder.buildQuestionnaireInfo(questionnaireBaseInfo.getFirst(),questionnaireBaseInfo.getSecond(),questionnaireBaseInfo.getThird());
        return null;
    }

    /**
     * 获取问卷数据构建结构
     * @param questionnaireIds 问卷ID集合
     */
    public List<QuestionnaireQuestionDataBO> getDataBuildList(List<Integer> questionnaireIds){
        List<QuestionnaireQuestionDataBO> dataBuildList = Lists.newArrayList();
        if (CollUtil.isEmpty(questionnaireIds)){
            return dataBuildList;
        }
        questionnaireIds.forEach(questionnaireId-> dataBuildList.addAll(getQuestionnaireRecInfoBO(questionnaireId).dataBuild()));
        return dataBuildList;
    }

    /**
     * 获取问卷rec数据信息
     * @param questionnaireId 问卷ID
     */
    public QuestionnaireInfoBuilder getQuestionnaireRecInfoBO(Integer questionnaireId){
        ThreeTuple<Questionnaire, List<QuestionnaireQuestion>, List<Question>> questionnaireBaseInfo = getQuestionnaireBaseInfo(questionnaireId);
        if (Objects.isNull(questionnaireBaseInfo)){
            return null;
        }
        return QuestionnaireInfoBuilder.build(questionnaireBaseInfo);
    }

    /**
     * 获取问卷基础信息（问卷信息，题目信息，问卷题目关系）
     * @param questionnaireId 问卷ID
     */
    private ThreeTuple<Questionnaire,List<QuestionnaireQuestion>,List<Question>> getQuestionnaireBaseInfo(Integer questionnaireId){
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollUtil.isEmpty(questionnaireQuestionList)){
            return null;
        }
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        List<Integer> questionIds = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questionList = questionService.listByIds(questionIds);
        return new ThreeTuple<>(questionnaire,questionnaireQuestionList,questionList);
    }


    /**
     * 获取头信息对象集合
     * @param questionnaireId 问卷ID
     * @param depth 问题深度
     */
    private List<HeadBO> getHeadBO(Integer questionnaireId,AtomicInteger depth){
        List<HeadBO> headList =Lists.newArrayList();
        QuestionnaireInfoBO questionnaireInfo = getQuestionnaireInfo(questionnaireId);

        List<QuestionnaireInfoBO.QuestionBO> questionList = questionnaireInfo.getQuestionList();
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> strList=Lists.newArrayList();
            strList.add(questionBO.getQuestionSerialNumber()+getTitle(questionBO.getTitle()));
            List<String> scoreList=Lists.newArrayList();
            if (Objects.equals(questionBO.getIsScore(),Boolean.TRUE)){
                scoreList =Lists.newArrayList();
                scoreList.add(questionBO.getQuestionSerialNumber()+getTitle(questionBO.getTitle()));
                scoreMap.put(questionnaireId,Lists.newArrayList(questionBO.getId()));
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,strList,headList,depth,scoreList,questionnaireId);
            }else {
                setHeadBOList(strList,questionBO.getId(),questionBO.getQuestionnaireQuestionId(),headList);
            }
            if (CollUtil.isNotEmpty(scoreList)){
                scoreList.add(TOTAL_SCORE);
                setHeadBOList(scoreList,-1,null,headList);
            }
        }
        return headList;
    }

    public List<String> excelHeadInfo(Integer questionnaireId){
        QuestionnaireInfoBO questionnaireInfo = getQuestionnaireInfo(questionnaireId);
        List<QuestionnaireInfoBO.QuestionBO> questionList = questionnaireInfo.getQuestionList();
        List<ExcelInfoBO> excelInfoBOList = Lists.newArrayList();
        printInfo(questionList, excelInfoBOList);

        List<String> excelDataFieldList = Lists.newArrayList();
        for (ExcelInfoBO excelInfoBO : excelInfoBOList) {
            setData(excelInfoBO,excelDataFieldList);
        }
        return excelDataFieldList;
    }

    private void setData(ExcelInfoBO excelInfoBO, List<String> excelDataFieldList){
        QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.getType(excelInfoBO.getType());
        List<String> qesFields = excelInfoBO.getQesFields();
        switch (questionTypeEnum){
            case INPUT:
            case RADIO:
            case CHECKBOX:
                excelDataFieldList.addAll(getExcelDataList(qesFields));
                break;
            case RADIO_INPUT:
            case CHECKBOX_INPUT:
                excelDataFieldList.add(setDataInfo(qesFields.get(0)));
                break;
            case TITLE:
            default:
                break;
        }
    }

    private List<String> getExcelDataList(List<String> qesFields){
        if (CollUtil.isEmpty(qesFields)){
            return Lists.newArrayList();
        }
        List<String> headList =Lists.newArrayList();
        for (int i = 0; i < qesFields.size(); i++) {
            headList.add(setDataInfo(qesFields.get(i)));
        }
        return headList;
    }
    private String setDataInfo(String qesField){
        return String.format("{.%s}",qesField);
    }

    private void printInfo(List<QuestionnaireInfoBO.QuestionBO> questionList,List<ExcelInfoBO> excelInfoBOList) {
        if (CollUtil.isEmpty(questionList)) {
            return;
        }

        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            ExcelInfoBO excelInfoBO = new ExcelInfoBO();
            excelInfoBO.setType(questionBO.getType());
            QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.getType(questionBO.getType());
            if (Objects.nonNull(questionTypeEnum)){
                switch (questionTypeEnum){
                    case INPUT:
                        inputData(questionBO,excelInfoBO);
                        break;
                    case RADIO:
                    case CHECKBOX:
                        radioAndCheckboxData(questionBO,excelInfoBO);
                        break;
                    case RADIO_INPUT:
                    case CHECKBOX_INPUT:
                        radioAndCheckboxInputData(questionBO,excelInfoBO);
                        break;
                    case TITLE:
                    default:
                        break;
                }
            }
            excelInfoBOList.add(excelInfoBO);
            printInfo(questionBO.getQuestionBOList(), excelInfoBOList);
        }
    }

    /**
     * 输入框
     * @param questionBO 问题对象
     * @param excelInfoBO excel信息对象
     */
    private void inputData(QuestionnaireInfoBO.QuestionBO questionBO,ExcelInfoBO excelInfoBO) {
        List<QesDataDO> qesDataList = questionBO.getQesData();
        if (CollUtil.isNotEmpty(qesDataList)){
            List<String> qesFields = qesDataList.stream().map(QesDataDO::getQesField).distinct().collect(Collectors.toList());
            excelInfoBO.setQesFields(qesFields);
        }

        Option option = questionBO.getOptions().get(0);
        if (Objects.nonNull(option)){
            JSONObject options = option.getOption();
            if (Objects.nonNull(options)){
                List<String> optionIds = options.values().stream().map(value -> {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(value), JSONObject.class);
                    return jsonObject.getString("id");
                }).collect(Collectors.toList());
                excelInfoBO.setOptionIds(optionIds);
            }

        }

    }

    /**
     * 单选或者多选加输入框
     * @param questionBO 问题对象
     * @param excelInfoBO excel信息对象
     */
    private void radioAndCheckboxInputData(QuestionnaireInfoBO.QuestionBO questionBO,ExcelInfoBO excelInfoBO) {

        List<QesDataDO> qesDataList = questionBO.getQesData();
        if (CollUtil.isNotEmpty(qesDataList)){
            List<String> qesFields = qesDataList.stream().map(QesDataDO::getQesField).distinct().collect(Collectors.toList());
            excelInfoBO.setQesFields(qesFields);
        }

        List<Option> optionList = questionBO.getOptions();
        if (CollUtil.isNotEmpty(optionList)){
            List<String> optionIds = optionList.stream().map(Option::getId).collect(Collectors.toList());
            excelInfoBO.setOptionIds(optionIds);

            Map<String,List<QesDataDO>> subInputMap = Maps.newHashMap();
            for (Option option : optionList) {
                if (Objects.isNull(option.getOption()) || option.getOption().isEmpty()){
                    continue;
                }

                JSONObject options = option.getOption();
                if (Objects.nonNull(options)){
                    List<QesDataDO> qesDataDOList = options.values().stream().map(value -> {
                        QesDataDO qesDataDO = new QesDataDO();
                        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(value), JSONObject.class);
                        qesDataDO.setOptionId(jsonObject.getString("id"));
                        return qesDataDO;
                    }).collect(Collectors.toList());
                    excelInfoBO.setOptionIds(optionIds);
                    subInputMap.put(option.getId(),qesDataDOList);
                }

            }
            excelInfoBO.setSubInputMap(subInputMap);
        }
    }

    /**
     * 单选或者多选
     * @param questionBO 问题对象
     * @param excelInfoBO excel信息对象
     */
    private void radioAndCheckboxData(QuestionnaireInfoBO.QuestionBO questionBO,ExcelInfoBO excelInfoBO) {
        List<QesDataDO> qesDataList = questionBO.getQesData();
        if (CollUtil.isNotEmpty(qesDataList)){
            List<String> qesFields = qesDataList.stream().map(QesDataDO::getQesField).distinct().collect(Collectors.toList());
            excelInfoBO.setQesFields(qesFields);
        }

        List<Option> optionList = questionBO.getOptions();
        if (CollUtil.isNotEmpty(optionList)){
            List<String> optionIds = optionList.stream().map(Option::getId).collect(Collectors.toList());
            excelInfoBO.setOptionIds(optionIds);
        }
    }


    /**
     * 问题中标题部分去除
     * @param title 问题标题
     */
    private static String getTitle(String title){
        if (StrUtil.isNotBlank(title) && title.contains("||")){
            String[] split = title.split("\\|\\|");
            return split[1];
        }
        return title;
    }

    /**
     * 获取Excel表头信息
     * @param questionnaireIds 问卷ID集合
     */
    public List<List<String>> getHead(List<Integer> questionnaireIds){
        List<HeadBO> headBOList = getHeadList(questionnaireIds);
        return headBOList.stream()
                .map(HeadBO::getQuestionHead)
                .collect(Collectors.toList());
    }

    /**
     * 获取Excel表头信息的顺序
     * @param questionnaireIds 问卷ID集合
     */
    public List<Integer> getQuestionIdSort(List<Integer> questionnaireIds){
        List<HeadBO> headBOList = getHeadList(questionnaireIds);
        return headBOList.stream()
                .map(HeadBO::getLastQuestionId)
                .collect(Collectors.toList());
    }

    public List<QesFieldMapping> getQesFieldMappingList(List<Integer> questionnaireIds){
        List<Questionnaire> questionnaireList = questionnaireService.listByIds(questionnaireIds);
        if (CollUtil.isEmpty(questionnaireList)){
            return Lists.newArrayList();
        }
        List<String> qesIdStrList = questionnaireList.stream().map(Questionnaire::getQesId).filter(Objects::nonNull).collect(Collectors.toList());
        if (qesIdStrList.size() != questionnaireIds.size()){
            throw new BusinessException(String.format("未上传qes文件,问卷名称:%s",CollUtil.join(questionnaireList.stream().map(Questionnaire::getTitle).collect(Collectors.toList()), ",")));
        }

        List<List<Integer>> qesIdList =Lists.newArrayList();
        for (Questionnaire questionnaire : questionnaireList) {
            String qesId = questionnaire.getQesId();
            qesIdList.add(Arrays.stream(qesId.split(StrUtil.COMMA)).map(Integer::valueOf).collect(Collectors.toList()));
        }
        List<Integer> intersectionList = ListUtil.getIntersection(qesIdList);

        if (CollUtil.isEmpty(intersectionList)){
            return Lists.newArrayList();
        }
        return qesFieldMappingService.listByQesId(intersectionList.get(0));
    }


    /**
     * 获取Excel表头信息对象集合
     * @param questionnaireIds 问卷ID集合
     */
    private List<HeadBO> getHeadList(List<Integer> questionnaireIds) {
        List<List<HeadBO>> headList = Lists.newArrayList();
        AtomicInteger depth = new AtomicInteger(0);
        questionnaireIds.forEach(questionnaireId -> {
            List<HeadBO> headBOList = getHeadBO(questionnaireId, depth);
            headList.add(headBOList);
        });
        List<HeadBO> headBOList = Lists.newArrayList();
        for (List<HeadBO> list : headList) {
            list.forEach(headBO -> {
                headBO.setDepth(depth.get());
                headBOList.add(headBO);
            });
        }
        return headBOList;
    }

    /**
     * 递归设置Excel表头数据
     * @param questionList 问题集合
     * @param list 表头数据集合
     * @param lists 收集表头信息集合
     * @param depth 深度
     * @param scoreList 记分项
     * @param questionnaireId 问卷ID
     */
    private void setHead(List<QuestionnaireInfoBO.QuestionBO> questionList,List<String> list,
                         List<HeadBO> lists,AtomicInteger depth,List<String> scoreList,
                         Integer questionnaireId){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> cloneList = ObjectUtil.cloneByStream(list);
            cloneList.add(questionBO.getQuestionSerialNumber()+getTitle(questionBO.getTitle()));
            if (CollUtil.isNotEmpty(scoreMap.get(questionnaireId))){
                scoreMap.get(questionnaireId).add(questionBO.getId());
            }

            if (Objects.equals(questionBO.getIsScore(),Boolean.TRUE)){
                scoreList.add(questionBO.getQuestionSerialNumber()+getTitle(questionBO.getTitle()));
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,cloneList,lists,depth,scoreList,questionnaireId);
            }else{
                depth.set(CollUtil.max(Lists.newArrayList(depth.get(),cloneList.size())));
                setHeadBOList(cloneList,questionBO.getId(),questionBO.getQuestionnaireQuestionId(),lists);
            }
        }
    }

    /**
     * 设Excel头信息对象
     * @param cloneList 头信息集合
     * @param lastQuestionId 最深的问题ID
     * @param sort 排序
     * @param lists 收集表头信息集合
     */
    private void setHeadBOList(List<String> cloneList,Integer lastQuestionId,Integer sort ,List<HeadBO> lists){
        lists.add(new HeadBO()
                .setQuestionDepthList(cloneList)
                .setLastQuestionId(lastQuestionId)
                .setSort(sort));
    }

    /**
     * 获取最新问卷ID集合
     *
     * @return 问卷集合
     */
    public List<Questionnaire> getLatestQuestionnaire(QuestionnaireTypeEnum questionnaireTypeEnum,String exportFile){
        List<Integer> questionnaireTypeList = getQuestionnaireTypeList(questionnaireTypeEnum,exportFile);
        if (CollUtil.isEmpty(questionnaireTypeList)){
            return Lists.newArrayList();
        }
        List<Questionnaire> questionnaireList = questionnaireService.getByTypes(questionnaireTypeList);
        if (CollUtil.isEmpty(questionnaireList)){
            return Lists.newArrayList();
        }
        return questionnaireList;
    }

    /**
     * 获取完整问卷
     *
     * @param questionnaireTypeEnum 问卷类型
     */
    public List<Integer> getQuestionnaireTypeList(QuestionnaireTypeEnum questionnaireTypeEnum,String exportFile){
        switch (questionnaireTypeEnum){
            case AREA_DISTRICT_SCHOOL:
                return QuestionnaireConstant.getAreaDistrictSchool();
            case PRIMARY_SECONDARY_SCHOOLS:
                return QuestionnaireConstant.getPrimarySecondarySchool();
            case PRIMARY_SCHOOL:
                return QuestionnaireConstant.getPrimarySchool(exportFile);
            case MIDDLE_SCHOOL:
                return QuestionnaireConstant.getMiddleSchool(exportFile);
            case UNIVERSITY_SCHOOL:
                return QuestionnaireConstant.getUniversitySchool();
            case VISION_SPINE:
                return QuestionnaireConstant.getVisionSpine();
            case SCHOOL_ENVIRONMENT:
                return QuestionnaireConstant.getSchoolEnvironment();
            default:
                break;
        }
        return Lists.newArrayList();
    }


    /**
     * 获取隐藏问题集合
     *
     * @param questionnaireId 问卷ID
     */
    public List<HideQuestionDataBO> getHideQuestionnaireQuestion(Integer questionnaireId) {
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        questionnaireQuestionList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> Objects.equals(Boolean.TRUE,questionnaireQuestion.getIsHidden())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream()
                    .map(QuestionnaireQuestion::getQuestionId)
                    .collect(Collectors.toList());
            Map<Integer, QuestionnaireQuestion> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.toMap(QuestionnaireQuestion::getQuestionId, Function.identity()));
            List<Question> questionList = questionService.listByIds(questionIds);
            CollUtil.sort(questionList,Comparator.comparing(Question::getId));

           return questionList.stream().map(question -> {
               HideQuestionDataBO hideQuestionDataBO = new HideQuestionDataBO(question.getId());
               QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionMap.get(question.getId());
               if (Objects.nonNull(questionnaireQuestion)){
                   hideQuestionDataBO.setQesData(questionnaireQuestion.getQesData());
               }
               return hideQuestionDataBO;
           }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 隐藏问题处理
     * @param questionnaireId 问卷ID
     */
    public List<HideQuestionRecDataBO> getHideQuestionnaireQuestionRec(Integer questionnaireId) {
        if (Objects.isNull(questionnaireId)){
            return Lists.newArrayList();
        }
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        questionnaireQuestionList = questionnaireQuestionList.stream()
                .filter(questionnaireQuestion -> Objects.equals(Boolean.TRUE,questionnaireQuestion.getIsHidden()))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream()
                    .map(QuestionnaireQuestion::getQuestionId)
                    .collect(Collectors.toList());

            Map<Integer, QuestionnaireQuestion> questionnaireQuestionMap = questionnaireQuestionList.stream().collect(Collectors.toMap(QuestionnaireQuestion::getQuestionId, Function.identity(),(v1,v2)->v2));
            List<Question> questionList = questionService.listByIds(questionIds);
            CollUtil.sort(questionList,Comparator.comparing(Question::getId));

            return questionList.stream()
                    .map(question -> buildHideQuestionRecDataBO(questionnaireQuestionMap, question))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 构建隐藏问题
     * @param questionnaireQuestionMap 问卷问题关系集合
     * @param question 问题对象
     */
    private HideQuestionRecDataBO buildHideQuestionRecDataBO(Map<Integer, QuestionnaireQuestion> questionnaireQuestionMap, Question question) {
        HideQuestionRecDataBO hideQuestionDataBO = new HideQuestionRecDataBO(question.getId(),question.getType());
        QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionMap.get(question.getId());
        if (Objects.nonNull(questionnaireQuestion)){
            List<HideQuestionRecDataBO.QesDataBO> collect = questionnaireQuestion.getQesData()
                    .stream()
                    .map(qesDataDO -> new HideQuestionRecDataBO.QesDataBO(qesDataDO.getQesField(), qesDataDO.getQesSerialNumber()))
                    .collect(Collectors.toList());
            hideQuestionDataBO.setQesData(collect);
        }
        return hideQuestionDataBO;
    }


    /**
     * 获取计算分值问题ID集合
     *
     * @param questionnaireIds 问卷ID
     */
    public List<Integer> getScoreQuestionIds(List<Integer> questionnaireIds) {
        List<Integer> questionIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(questionnaireIds)){
            questionnaireIds.forEach(id-> Optional.ofNullable(scoreMap.get(id)).ifPresent(questionIds::addAll));
        }
        return questionIds;
    }

    /**
     * 移除计算分值问题ID集合
     *
     * @param questionnaireIds 问卷ID集合
     */
    public void removeScoreQuestionId(List<Integer> questionnaireIds) {
        if (CollUtil.isNotEmpty(questionnaireIds)){
            questionnaireIds.forEach(id-> scoreMap.put(id,Lists.newArrayList()));
        }
    }

    /**
     * 获取qes文件ID
     * @param qesId qes管理ID
     */
    public Integer getQesFileId(Integer qesId){
        QuestionnaireQes questionnaireQes = questionnaireQesService.getById(qesId);
        return questionnaireQes.getQesFileId();
    }

}
