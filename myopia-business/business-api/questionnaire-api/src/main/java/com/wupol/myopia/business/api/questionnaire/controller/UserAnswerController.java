package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.questionnaire.service.UserAnswerBizService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
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


    /**
     * 获取用户答案
     *
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    @GetMapping("list/{questionnaireId}")
    public UserAnswerDTO getUserAnswerList(@PathVariable("questionnaireId") Integer questionnaireId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getUserAnswerList(questionnaireId, user);
    }

    @PostMapping("save")
    public Boolean saveUserAnswer(@RequestBody UserAnswerDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.saveUserAnswer(requestDTO, user);
    }

    @GetMapping("isFinish")
    public Boolean userAnswerIsFinish() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.userAnswerIsFinish(user);
    }

    @GetMapping
    public void getUserStatus() {
        CurrentUserUtil.getCurrentUser();
    }

}
