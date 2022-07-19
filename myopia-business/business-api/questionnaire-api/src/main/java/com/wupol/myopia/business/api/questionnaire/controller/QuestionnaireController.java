package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/getQuestionnaire/{questionnaireId}")
    public List<QuestionnaireInfoDTO> getQuestionnaire(@PathVariable("questionnaireId") Integer questionnaireId) {
        return questionnaireService.getQuestionnaire(questionnaireId);
    }
}
