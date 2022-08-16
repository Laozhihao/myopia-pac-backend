package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.collection.CollUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(CurrentUser user) {
        IUserAnswerService userAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return userAnswerService.getUserQuestionnaire(user.getExQuestionnaireUserId());
    }

    /**
     * 保存问卷与qes之间关系和保存qes字段映射关系
     * @param questionnaireId 问卷ID
     * @param qesId qes管理ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionnaireQesField(Integer questionnaireId,Integer qesId){
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
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
                    String optionIdStr = CollUtil.join(optionIds, ",");
                    qesFieldMapping.setOptionId(optionIdStr);
                });
            }
            qesFieldMappingService.updateBatchById(qesFieldMappingList);
            Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
            questionnaire.setQesId(qesId);
            questionnaireService.updateById(questionnaire);
        }
    }
}
