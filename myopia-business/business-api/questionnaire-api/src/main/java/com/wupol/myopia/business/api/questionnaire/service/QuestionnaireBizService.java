package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.QesFieldMapping;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QesFieldMappingService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireBizService {

    @Resource
    private UserAnswerFactory userAnswerFactory;
    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;
    @Resource
    private QesFieldMappingService qesFieldMappingService;
    @Resource
    private QuestionnaireService questionnaireService;


    /**
     * 获取用户问卷
     *
     * @param user 用户
     *
     * @return 问卷
     */
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(CurrentUser user) {
        IUserAnswerService userAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return userAnswerService.getUserQuestionnaire(user.getExQuestionnaireUserId());
    }

    /**
     * 保存问卷与qes之间关系和保存qes字段映射关系
     * @param questionnaireIds 问卷ID集合
     * @param qesId qes管理ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionnaireQesField(List<Integer> questionnaireIds,Integer qesId){
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireIds(questionnaireIds);
        if (CollUtil.isNotEmpty(questionnaireQuestionList)){
            Map<String, List<QesDataDO>> qesDataMap = questionnaireQuestionList.stream()
                    .filter(questionBO -> CollUtil.isNotEmpty(questionBO.getQesData()))
                    .flatMap(questionBO -> questionBO.getQesData().stream())
                    .collect(Collectors.groupingBy(QesDataDO::getQesField));

            List<QesFieldMapping> qesFieldMappingList = qesFieldMappingService.listByQesId(qesId);
            if (CollUtil.isEmpty(qesFieldMappingList)){
                return;
            }
            for (QesFieldMapping qesFieldMapping : qesFieldMappingList) {
                Optional.ofNullable(qesDataMap.get(qesFieldMapping.getQesField())).ifPresent(qesDataDOList -> {
                    List<String> optionIds = qesDataDOList.stream().map(QesDataDO::getOptionId).collect(Collectors.toList());
                    String optionIdStr = CollUtil.join(optionIds, StrUtil.COMMA);
                    qesFieldMapping.setOptionId(optionIdStr);
                });
            }
            qesFieldMappingService.updateBatchById(qesFieldMappingList);
            List<Questionnaire> questionnaireList = questionnaireService.listByIds(questionnaireIds);
            questionnaireList.forEach(questionnaire -> {
                questionnaire.setQesId(getQesId(questionnaire.getQesId(),qesId));
                questionnaire.setUpdateTime(new Date());
            });
            questionnaireService.updateBatchById(questionnaireList);
        }
    }

    /**
     * 获取qes管理ID
     * @param dbQesId 数据库qes管理ID
     * @param newQesId 新的qes管理ID
     */
    private String getQesId(String dbQesId,Integer newQesId){
        //数据库的值为空
        if (StrUtil.isBlank(dbQesId)){
            return newQesId.toString();
        }
        //数据的值不为空 新增新值
        Set<String> qesIds = Sets.newHashSet(dbQesId.split(StrUtil.COMMA));
        qesIds.add(newQesId.toString());
        return CollUtil.join(qesIds, StrUtil.COMMA);
    }
}
