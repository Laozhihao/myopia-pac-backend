package com.wupol.myopia.business.api.questionnaire.controller;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.dto.Attribute;
import com.wupol.myopia.business.core.questionnaire.domain.dto.Options;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController extends BaseController<QuestionnaireService, Questionnaire> {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionnaireService questionnaireService;

    @GetMapping()
    public void test() {
        Question question = new Question();
        question.setType("radio");
        question.setTitle("单选");

        Attribute attribute = new Attribute();
        attribute.setRequired(false);
        attribute.setStatistics(true);
        attribute.setIsTitle(false);
        question.setAttribute(attribute);
        Options options = new Options();
        options.setId(1);
        options.setDataType(1);
        options.setSerialNumber("1-1");
        options.setSystemSerialNumber("1-1");
        options.setText("选项1");
        options.setExclusive(false);

        Options options2 = new Options();
        options2.setId(2);
        options2.setDataType(1);
        options2.setText("选项2");
        options2.setExclusive(false);
        question.setOptions(Lists.newArrayList(options, options2));
        question.setSerialNumber("1-1");
        questionService.save(question);
    }

    @GetMapping("a")
    public Question test2() {
        return questionService.getByQuestionId(1);
    }

    @GetMapping("getSurvey")
    public Object test3() {
        return questionnaireService.getQuestionnaireResponseById(1);
    }

}
