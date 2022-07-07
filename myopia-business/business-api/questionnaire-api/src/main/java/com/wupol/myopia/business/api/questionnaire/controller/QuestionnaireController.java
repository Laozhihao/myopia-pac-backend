package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController extends BaseController<QuestionnaireService, Questionnaire> {


}
