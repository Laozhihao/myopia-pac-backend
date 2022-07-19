package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.QuestionBizService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 问卷管理
 *
 * @author Simple4H
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/questionnaire")
@Slf4j
public class SurveyController {

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionBizService questionBizService;

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

    @PostMapping("/question/save")
    public void saveQuestion(@RequestBody Question question) {
        questionBizService.saveQuestion(question);

    }
}
