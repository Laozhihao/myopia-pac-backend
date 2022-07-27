package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.QuestionBizService;
import com.wupol.myopia.business.api.management.service.QuestionnaireQuestionBizService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.*;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
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
    private QuestionService questionService;

    @Resource
    private QuestionBizService questionBizService;

    @Resource
    private QuestionnaireQuestionBizService questionnaireQuestionBizService;

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;


    @GetMapping("list")
    public List<Questionnaire> questionnaireList(Integer year) {
        return questionnaireService.getQuestionnaireList(year);
    }

    @GetMapping("detail/{questionnaireId}")
    public QuestionnaireResponseDTO getQuestionnaireDetail(@PathVariable("questionnaireId") Integer questionnaireId) {
        return questionnaireService.getDetailByQuestionnaireId(questionnaireId);
    }

    @PostMapping("edit")
    public void editQuestionnaire(@RequestBody EditQuestionnaireRequestDTO requestDTO) {
        questionnaireService.editQuestionnaire(requestDTO);
    }

    @PostMapping("/question/save")
    public void saveQuestion(@RequestBody Question question) {
        questionBizService.saveQuestion(question);
    }

    @GetMapping("/question/search")
    public List<Question> searchQuestion(SearchQuestionRequestDTO requestDTO) {
        return questionService.searchQuestion(requestDTO.getName(), requestDTO.getIsTitle());
    }

    /**
     * 获取逻辑题目
     */
    @GetMapping("logic/list")
    public List<QuestionnaireQuestion> logicList(Integer questionnaireId) {
        return questionnaireQuestionService.logicList(questionnaireId);
    }

    @PostMapping("logic/edit")
    public void editLogic(@RequestBody LogicEditRequestDTO requestDTO) {
        questionnaireQuestionBizService.editLogic(requestDTO);
    }

    @GetMapping("logic/findQuestion")
    public List<LogicFindQuestionResponseDTO> logicFindQuestion(Integer questionnaireId, String serialNumber, Integer questionId) {
        return questionnaireQuestionBizService.logicFindQuestion(questionnaireId, serialNumber, questionId);
    }

    @PostMapping("logic/deleted")
    public void editDeleted(@RequestBody LogicDeletedRequestDTO requestDTO) {
        questionnaireQuestionBizService.editDeleted(requestDTO);
    }

    @GetMapping("all")
    public Object getAll() {
        return questionBizService.getAllOptionIds();
    }
}
