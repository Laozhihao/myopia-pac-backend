package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HeadBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireInfoBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
    private final SchoolService schoolService;

    private static final String FILE_NAME="%s的%s的问卷数据.xlsx";

    /**
     * 获取问卷信息（问卷+问题）
     * @param questionnaireId 问卷ID
     */
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

    /**
     * 构建问卷信息（问卷+问题）
     *
     * @param questionnaire 问卷
     * @param questionnaireQuestionList 问卷问题关联集合
     * @param questionList 问题集合
     */
    public QuestionnaireInfoBO buildQuestionnaireInfo(Questionnaire questionnaire,
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
            questionBOList.add(questionBO);
        }

        setQuestion(questionBOList,questionnaireQuestionMap,questionMap);
        questionnaireInfoBO.setQuestionList(questionBOList);
        return questionnaireInfoBO;
    }

    /**
     * 递归设置问题
     * @param questionBOList 父类问题集合
     * @param questionnaireQuestionMap 问卷问题关联集合
     * @param questionMap 问题集合
     */
    private void setQuestion(List<QuestionnaireInfoBO.QuestionBO> questionBOList,
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
                    childList.add(child);
                }
                questionBO.setQuestionBOList(childList);
                setQuestion(childList,questionnaireQuestionMap,questionMap);
            }
        }
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
            strList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,strList,headList,depth);
            }else {
                setHeadBOList(strList,questionBO.getQuestionId(),questionBO.getQuestionnaireQuestionId(),headList);
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
                .sorted(Comparator.comparing(HeadBO::getSort))
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
     */
    private void setHead(List<QuestionnaireInfoBO.QuestionBO> questionList ,List<String> list,List<HeadBO> lists,AtomicInteger depth){
        for (QuestionnaireInfoBO.QuestionBO questionBO : questionList) {
            List<String> cloneList = ObjectUtil.cloneByStream(list);
            cloneList.add(questionBO.getQuestionSerialNumber()+questionBO.getQuestionName());
            List<QuestionnaireInfoBO.QuestionBO> questionBOList = questionBO.getQuestionBOList();
            if (CollectionUtil.isNotEmpty(questionBOList)){
                setHead(questionBOList,cloneList,lists,depth);
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
    public List<Questionnaire> getLatestQuestionnaire(List<Integer> questionnaireTypeList){
        List<Questionnaire> questionnaireList = questionnaireService.getByTypes(questionnaireTypeList);
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
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getType());
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
     * 获取excel问卷名称
     *
     * @param schoolId 学校ID
     * @param questionnaireType 问卷类型
     */
    public String getExcelFileName(Integer schoolId,Integer questionnaireType){
        School school = schoolService.getById(schoolId);
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
        return String.format(FILE_NAME,school.getName(),questionnaireTypeEnum.getDesc());
    }
}
