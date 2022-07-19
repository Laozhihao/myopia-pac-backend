package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.questionnaire.service.QuestionnaireBizService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireBizService questionnaireBizService;

    @GetMapping("/getQuestionnaire/{questionnaireId}")
    public List<QuestionnaireInfoDTO> getQuestionnaire(@PathVariable("questionnaireId") Integer questionnaireId) {
        return questionnaireService.getQuestionnaire(questionnaireId);
    }

    @GetMapping("/getStudentQuestionnaire")
    public List<UserQuestionnaireResponseDTO> getStudentQuestionnaire() {
        return questionnaireBizService.getUserQuestionnaire();
    }
}
