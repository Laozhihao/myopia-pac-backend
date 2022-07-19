package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireBizService {

    @Resource
    private QuestionnaireService questionnaireService;


    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire() {
        List<Integer> universityType = QuestionnaireTypeEnum.getUniversityType();

        // 获取今年的问卷
        Map<Integer, Questionnaire> typeMap = questionnaireService.getByYearAndTypes(universityType, DateUtil.getYear(new Date()))
                .stream().collect(Collectors.toMap(Questionnaire::getType, Function.identity()));

        return universityType.stream().map(s->{
            UserQuestionnaireResponseDTO responseDTO = new UserQuestionnaireResponseDTO();
            Questionnaire questionnaire = typeMap.get(s);
            responseDTO.setId(questionnaire.getId());
            responseDTO.setTitle(questionnaire.getTitle());
            return responseDTO;
        }).collect(Collectors.toList());
    }
}
