package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.questionnaire.domain.SchoolListResponseDTO;
import com.wupol.myopia.business.api.questionnaire.service.UserAnswerBizService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author Simple4H
 */
@ResponseResultBody
@RestController
@RequestMapping("/questionnaire/userAnswer")
public class UserAnswerController {

    @Resource
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

    /**
     * 保存用户答案
     */
    @PostMapping("save")
    public Boolean saveUserAnswer(@RequestBody UserAnswerDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.saveUserAnswer(requestDTO, user);
    }

    /**
     * 是否完成问卷
     */
    @GetMapping("isFinish")
    public Boolean userAnswerIsFinish() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.userAnswerIsFinish(user);
    }

    /**
     * 获取用户状态
     */
    @GetMapping("getUserStatus")
    public void getUserStatus() {
        CurrentUserUtil.getCurrentUser();
    }

    /**
     * 获取学校名称
     *
     * @return 学校名称
     */
    @GetMapping("getSchoolName")
    public ApiResult<String> getSchoolName() {
        return ApiResult.success(userAnswerBizService.getSchoolName(CurrentUserUtil.getCurrentUser()));
    }

    /**
     * 问卷是否完成
     *
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    @GetMapping("isFinish/{questionnaireId}")
    public Boolean questionnaireIsFinish(@PathVariable("questionnaireId") Integer questionnaireId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.questionnaireIsFinish(questionnaireId, user);
    }

    /**
     * 获取学校
     */
    @GetMapping("schoolList")
    public List<SchoolListResponseDTO> getSchoolList(String name) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getSchoolList(name, currentUser);
    }

    /**
     * 政府获取行政区域
     *
     * @return List<District>
     *
     * @throws IOException IOException
     */
    @GetMapping("gov/getDistrict")
    public List<District> getDistrict() throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getDistrict(user);
    }

    /**
     * 获取学校信息
     *
     * @return SchoolListResponseDTO
     */
    @GetMapping("getSchoolInfo")
    public SchoolListResponseDTO getSchoolInfo() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return userAnswerBizService.getSchoolInfo(currentUser);
    }

}
