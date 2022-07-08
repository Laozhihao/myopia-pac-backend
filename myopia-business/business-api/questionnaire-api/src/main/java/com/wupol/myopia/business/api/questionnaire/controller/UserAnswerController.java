package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/questionnaire/userAnswer")
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @GetMapping("list")
    public UserAnswerDTO getUserAnswerList(Integer userId, Integer questionnaireId) {
        return userAnswerService.getUserAnswerList(userId, questionnaireId);
    }


    @PostMapping("save")
    public void saveUserAnswer(@RequestBody UserAnswerDTO requestDTO) {
        userAnswerService.saveUserAnswer(requestDTO);
    }

}
