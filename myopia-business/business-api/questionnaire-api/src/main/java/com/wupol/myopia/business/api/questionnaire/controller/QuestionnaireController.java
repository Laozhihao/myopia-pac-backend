package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
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

    @GetMapping("list")
    public List<Questionnaire> questionnaireList(Integer year) {
        return questionnaireService.getQuestionnaireList(year);
    }

    @GetMapping("detail/{questionnaireId}")
    public Object getQuestionnaireDetail(@PathVariable("questionnaireId") Integer questionnaireId) {
        // TODO:
        return null;
    }

    @PostMapping("edit")
    public void editQuestionnaire(@RequestBody EditQuestionnaireRequestDTO requestDTO) {
        questionnaireService.editQuestionnaire(requestDTO);
    }


}
