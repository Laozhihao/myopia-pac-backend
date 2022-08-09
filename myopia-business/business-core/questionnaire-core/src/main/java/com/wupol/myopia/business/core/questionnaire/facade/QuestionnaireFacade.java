package com.wupol.myopia.business.core.questionnaire.facade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.builder.QuestionnaireInfoBuilder;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HeadBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireAndQuestionBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireInfoBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private Map<Integer,List<Integer>> scoreMap = Maps.newConcurrentMap();

    private static final String TOTAL_SCORE="总分";

    /**
     * 获取问卷信息（问卷+问题）
     * @param questionnaireId 问卷ID
     */
    public QuestionnaireInfoBO getQuestionnaireInfoBO(Integer questionnaireId){
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollectionUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questionList = questionService.listByIds(questionIds);
            return QuestionnaireInfoBuilder.buildQuestionnaireInfoBO(questionnaire,questionnaireQuestionList,questionList);
        }
        return null;
    }

    public QuestionnaireAndQuestionBO getQuestionnaireInfo(Integer questionnaireId){
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollectionUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questionList = questionService.listByIds(questionIds);
            return QuestionnaireInfoBuilder.buildQuestionnaireAndQuestionBO(questionnaire,questionnaireQuestionList,questionList);
        }
        return null;
    }




    /**
     * 获取头信息对象集合
     * @param questionnaireId 问卷ID
     * @param depth 问题深度
     */
    private List<HeadBO> getHeadBO(Integer questionnaireId,AtomicInteger depth){
        List<HeadBO> headList =Lists.newArrayList();
        QuestionnaireInfoBO questionnaireInfo = getQuestionnaireInfoBO(questionnaireId);

        List<QuestionnaireInfoBO.QuestionBO> questionList = questionnaireInfo.getQuestionList();
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> strList=Lists.newArrayList();
            strList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            List<String> scoreList=Lists.newArrayList();
            if (Objects.equals(questionBO.getIsScore(),Boolean.TRUE)){
                scoreList =Lists.newArrayList();
                scoreList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
                scoreMap.put(questionnaireId,Lists.newArrayList(questionBO.getQuestionId()));
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,strList,headList,depth,scoreList,questionnaireId);
            }else {
                setHeadBOList(strList,questionBO.getQuestionId(),questionBO.getQuestionnaireQuestionId(),headList);
            }
            if (!CollectionUtils.isEmpty(scoreList)){
                scoreList.add(TOTAL_SCORE);
                setHeadBOList(scoreList,-1,null,headList);
            }
        }
        return headList;
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
            cloneList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            if (CollectionUtil.isNotEmpty(scoreMap.get(questionnaireId))){
                scoreMap.get(questionnaireId).add(questionBO.getQuestionId());
            }

            if (Objects.equals(questionBO.getIsScore(),Boolean.TRUE)){
                scoreList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            }
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,cloneList,lists,depth,scoreList,questionnaireId);
            }else{
                depth.set(CollectionUtil.max(Lists.newArrayList(depth.get(),cloneList.size())));
                setHeadBOList(cloneList,questionBO.getQuestionId(),questionBO.getQuestionnaireQuestionId(),lists);
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
    public List<Questionnaire> getLatestQuestionnaire(QuestionnaireTypeEnum questionnaireTypeEnum){
        List<Questionnaire> questionnaireList = questionnaireService.getByTypes(getQuestionnaireTypeList(questionnaireTypeEnum));
        if (CollectionUtils.isEmpty(questionnaireList)){
            return Lists.newArrayList();
        }
        return questionnaireList;
    }

    /**
     * 获取完整问卷
     *
     * @param questionnaireTypeEnum 问卷类型
     */
    public List<Integer> getQuestionnaireTypeList(QuestionnaireTypeEnum questionnaireTypeEnum){
        switch (questionnaireTypeEnum){
            case AREA_DISTRICT_SCHOOL:
                return getAreaDistrictSchool();
            case PRIMARY_SECONDARY_SCHOOLS:
                return getPrimarySecondarySchool();
            case PRIMARY_SCHOOL:
                return getPrimarySchool();
            case MIDDLE_SCHOOL:
                return getMiddleSchool();
            case UNIVERSITY_SCHOOL:
                return getUniversitySchool();
            case VISION_SPINE:
                return getVisionSpine();
            case SCHOOL_ENVIRONMENT:
                return getSchoolEnvironment();
            default:
                break;
        }
        return Lists.newArrayList();
    }

    /**
     * 获取省、地市及区（县）管理部门学校卫生工作调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getAreaDistrictSchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType());
    }

    /**
     * 获取中小学校开展学校卫生工作情况调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getPrimarySecondarySchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType());
    }

    /**
     * 获取学生健康状况及影响因素调查表（小学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getPrimarySchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType());
    }

    /**
     * 获取学生健康状况及影响因素调查表（中学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getMiddleSchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType());
    }

    /**
     * 获取学生健康状况及影响因素调查表（大学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getUniversitySchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType());
    }

    /**
     * 获取学生视力不良及脊柱弯曲异常影响因素专项调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getVisionSpine(){
        return Lists.newArrayList(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getType());
    }


    /**
     * 获取学校环境健康影响因素调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getSchoolEnvironment(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType());
    }


    /**
     * 获取隐藏问题集合
     *
     * @param questionnaireId 问卷ID
     */
    public List<HideQuestionDataBO> getHideQuestionnaireQuestion(Integer questionnaireId) {
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        questionnaireQuestionList = questionnaireQuestionList.stream().filter(questionnaireQuestion -> StrUtil.isBlank(questionnaireQuestion.getSerialNumber())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream()
                    .map(QuestionnaireQuestion::getQuestionId)
                    .collect(Collectors.toList());
            List<Question> questionList = questionService.listByIds(questionIds);
            CollectionUtil.sort(questionList,Comparator.comparing(Question::getId));

           return questionList.stream().map(question -> new HideQuestionDataBO(question.getId())).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }




    /**
     * 获取计算分值问题ID集合
     *
     * @param questionnaireIds 问卷ID
     */
    public List<Integer> getScoreQuestionIds(List<Integer> questionnaireIds) {
        List<Integer> questionIds = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(questionnaireIds)){
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
        if (CollectionUtil.isNotEmpty(questionnaireIds)){
            questionnaireIds.forEach(id-> scoreMap.put(id,Lists.newArrayList()));
        }
    }

}
