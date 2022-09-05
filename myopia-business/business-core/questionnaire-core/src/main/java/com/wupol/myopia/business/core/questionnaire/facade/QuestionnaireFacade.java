package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.ListUtil;
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

    /**
     * 获取问卷信息（问卷+问题）
     * @param questionnaireId 问卷ID
     */
    public QuestionnaireInfoBO getQuestionnaireInfo(Integer questionnaireId){
        ThreeTuple<Questionnaire, List<QuestionnaireQuestion>, List<Question>> questionnaireBaseInfo = getQuestionnaireBaseInfo(questionnaireId);
        if (Objects.isNull(questionnaireBaseInfo)){
            return null;
        }
        return QuestionnaireInfoBuilder.buildQuestionnaireInfo(questionnaireBaseInfo.getFirst(),questionnaireBaseInfo.getSecond(),questionnaireBaseInfo.getThird());
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
     * 获取记分问题ID集合
     * @param questionnaireId 问卷ID
     */
    public List<Integer> getScoreQuestionIds(Integer questionnaireId){
        QuestionnaireInfoBO questionnaireInfo = getQuestionnaireInfo(questionnaireId);
        List<QuestionnaireInfoBO.QuestionBO> questionList = questionnaireInfo.getQuestionList();

        List<Integer> questionIds =Lists.newArrayList();

        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            Integer questionId = questionBO.getId();
            if (Objects.equals(questionBO.getIsScore(),Boolean.TRUE)){
                scoreMap.put(questionId,Lists.newArrayList(questionId));
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollUtil.isNotEmpty(questionBOList)){
                setScoreQuestion(questionBOList, questionId);
            }
            questionIds.addAll(scoreMap.getOrDefault(questionId, Lists.newArrayList()));
            scoreMap.put(questionId,Lists.newArrayList());
        }

        return questionIds;
    }



    /**
     * 获取问卷对应的qes字段集合
     * @param questionnaireIds 问卷ID集合
     */
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
     * 递归设置Excel表头数据
     * @param questionList 问题集合
     * @param questionId 问题ID
     */
    private void setScoreQuestion(List<QuestionnaireInfoBO.QuestionBO> questionList,Integer questionId){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            if (CollUtil.isNotEmpty(scoreMap.get(questionId))){
                scoreMap.get(questionId).add(questionBO.getId());
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollUtil.isNotEmpty(questionBOList)){
                setScoreQuestion(questionBOList,questionId);
            }
        }
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
