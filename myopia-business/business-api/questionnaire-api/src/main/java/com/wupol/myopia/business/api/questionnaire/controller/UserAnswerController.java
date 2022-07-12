package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.questionnaire.service.UserAnswerBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/questionnaire/userAnswer")
public class UserAnswerController {

    @Autowired
    private UserAnswerBizService userAnswerBizService;

    @Autowired
    private UserAnswerService userAnswerService;


    @GetMapping("list/{questionnaireId}")
    public UserAnswerDTO getUserAnswerList(@PathVariable("questionnaireId") Integer questionnaireId) {
        return userAnswerService.getUserAnswerList(questionnaireId, CommonConst.USER_ID);
    }

    @PostMapping("save")
    public void saveUserAnswer(@RequestBody UserAnswerDTO requestDTO) {
        userAnswerBizService.saveUserAnswer(requestDTO, CommonConst.USER_ID);
    }

}
