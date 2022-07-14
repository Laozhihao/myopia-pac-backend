package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.QuestionAreaDTO;
import com.wupol.myopia.business.api.management.domain.dto.QuestionSearchDTO;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.api.management.service.ManagerQuestionnaireService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


/**
 * 问卷管理
 *
 * @Author xz
 * @Date 2022/7/6 15:20
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/questionnaire")
@Slf4j
public class ManagerQuestionnaireController {
    @Autowired
    private ManagerQuestionnaireService managerQuestionnaireService;

    /**
     * 获得当前登录人的筛查任务
     *
     * @return
     */
    @GetMapping("/task")
    public List<QuestionTaskVO> getQuestionTask() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return managerQuestionnaireService.getQuestionTaskByUnitId(user);
    }

    /**
     * 获得当前登录人的地区树（且在当前任务下）
     *
     * @return
     */
    @GetMapping("/areas")
    public QuestionAreaDTO getQuestionTaskAreas(Integer taskId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return managerQuestionnaireService.getQuestionTaskAreas(taskId, user);
    }

    /**
     * 学校填写情况
     *
     * @return
     */
    @GetMapping("/school")
    public QuestionSchoolVO getQuestionSchool(@RequestParam(value = "taskId", required = false, defaultValue = "") Integer taskId, @RequestParam(value = "areaId", required = false, defaultValue = "") Integer areaId) throws IOException {
        return managerQuestionnaireService.getQuestionSchool(taskId, areaId);
    }

    /**
     * 待办填写的问卷情况
     *
     * @return
     */
    @GetMapping("/backlog")
    public List<QuestionBacklogVO> getQuestionBacklog(@RequestParam(value = "taskId", required = false, defaultValue = "") Integer taskId, @RequestParam(value = "areaId", required = false, defaultValue = "") Integer areaId) throws IOException {
        return managerQuestionnaireService.getQuestionBacklog(taskId, areaId);
    }


    /**
     * 问卷待办填写情况
     *
     * @return
     */
    @GetMapping("/schools/list")
    public IPage<QuestionSchoolRecordVO> getQuestionSchoolList(QuestionSearchDTO questionSearchDTO) throws IOException {
        return managerQuestionnaireService.getQuestionSchoolList(questionSearchDTO);
    }

    /**
     * 问卷待办填写情况
     *
     * @return
     */
    @GetMapping("/backlog/list")
    public IPage<QuestionBacklogRecordVO> getQuestionBacklogList(QuestionSearchDTO questionSearchDTO) throws IOException {
        return managerQuestionnaireService.getQuestionBacklogList(questionSearchDTO);
    }
}
