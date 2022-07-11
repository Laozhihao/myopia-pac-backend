package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Simple4H
 * @Date 2022-07-07
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/userQuestionRecord")
public class UserQuestionRecordController extends BaseController<UserQuestionRecordService, UserQuestionRecord> {

}
