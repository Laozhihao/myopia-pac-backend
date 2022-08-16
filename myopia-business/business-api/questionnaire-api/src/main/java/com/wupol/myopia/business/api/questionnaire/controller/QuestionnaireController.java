package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.questionnaire.service.QuestionnaireBizService;
import com.wupol.myopia.business.core.questionnaire.constant.DropSelectEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.DropSelect;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Simple4H
 */
@ResponseResultBody
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireBizService questionnaireBizService;

    @GetMapping("/getQuestionnaire/{questionnaireId}")
    public List<QuestionnaireInfoDTO> getQuestionnaire(@PathVariable("questionnaireId") Integer questionnaireId) {
        return questionnaireService.getQuestionnaire(questionnaireId);
    }

    /**
     * 获取学生问卷
     *
     * @return 学生问卷
     */
    @GetMapping("/getStudentQuestionnaire")
    public List<UserQuestionnaireResponseDTO> getStudentQuestionnaire() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return questionnaireBizService.getUserQuestionnaire(user);
    }

    /**
     * 通过key获取下拉值
     *
     * @param key key
     *
     * @return List<DropSelect>
     */
    @GetMapping("/getDropSelect/{key}")
    public List<DropSelect> getDropSelectKey(@PathVariable("key") String key) {
        return DropSelectEnum.getSelect(key);
    }


    /**
     * 问卷关联qes字段映射
     * @param questionnaireId 问卷ID
     * @param qesId qes管理ID
     */
    @GetMapping("addQesFieldMapping")
    public void addQesFieldMapping(@RequestParam Integer questionnaireId, @RequestParam Integer qesId){
        questionnaireBizService.saveQuestionnaireQesField(questionnaireId,qesId);
    }
}
