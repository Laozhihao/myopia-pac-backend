package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.QuestionSearchDTO;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.api.management.service.QuestionnaireService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
public class QuestionnaireController {
    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Autowired
    private DistrictService districtService;

    /**
     * 获得当前登录人的筛查任务
     *
     * @return
     */
    @GetMapping("/task")
    public List<QuestionTaskVO> getQuestionTask() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return questionnaireService.getQuestionTaskByUnitId(user.getOrgId());
    }

    /**
     * 获得当前登录人的地区树（且在当前任务下）
     *
     * @return
     */
    @GetMapping("/areas")
    public List<District> getQuestionTaskAreas(Integer taskId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return questionnaireService.getQuestionTaskAreas(taskId, user);
    }

    /**
     * 学校填写情况
     *
     *
     * @return
     */
    @GetMapping("/school")
    public QuestionSchoolVO getQuestionSchool(Integer taskId,Integer areaId) {
        QuestionSchoolVO questionSchoolVO = new QuestionSchoolVO();
        questionSchoolVO.setSchoolUnfinished(10);
        questionSchoolVO.setSchoolAmount(20);
        questionSchoolVO.setStudentEnvironmentAmount(10);
        questionSchoolVO.setStudentEnvironmentUnfinished(20);
        questionSchoolVO.setStudentSpecialAmount(6);
        questionSchoolVO.setStudentSpecialUnfinished(60);
        return questionSchoolVO;
    }

    /**
     * 待办填写的问卷情况
     *
     * @return
     */
    @GetMapping("/backlog")
    public List<QuestionBacklogVO> getQuestionBacklog(Integer taskId,Integer areaId) {
        QuestionBacklogVO questionSchoolVO = new QuestionBacklogVO();
        questionSchoolVO.setQuestionnaireId(1);
        questionSchoolVO.setQuestionnaireTitle("xxssp");
        questionSchoolVO.setAmount(12);
        questionSchoolVO.setUnfinished(8);
        QuestionBacklogVO questionSchoolVO2 = new QuestionBacklogVO();
        questionSchoolVO.setQuestionnaireId(1);
        questionSchoolVO.setQuestionnaireTitle("xxssp12312");
        questionSchoolVO.setAmount(10);
        questionSchoolVO.setUnfinished(5);
        ArrayList<QuestionBacklogVO> ls = Lists.newArrayList();
        ls.add(questionSchoolVO);
        ls.add(questionSchoolVO2);
        return ls;
    }


    /**
     * 问卷待办填写情况
     *
     * @return
     */
    @GetMapping("/schools/list")
    public JSONObject getQuestionSchoolList(QuestionSearchDTO questionSearchDTO) {
        List<QuestionSchoolRecordVO> questionSchoolRecordVOS = Lists.newArrayList();
        for (int i = 0; i < 25; i++) {
            QuestionSchoolRecordVO vo = new QuestionSchoolRecordVO();
            vo.setAreaId(RandomUtil.randomInt(100));
            vo.setAreaName(districtService.getById(vo.getAreaId()).getName());
            vo.setSchoolId(RandomUtil.randomInt(188));
            vo.setSchoolSurveyStatus(RandomUtil.randomInt(3));
            vo.setStudentEnvironmentSurveyStatus(RandomUtil.randomInt(3));
            vo.setStudentSpecialSurveyStatus(RandomUtil.randomInt(3));
            vo.setSchoolName("xxx的假学校");
            vo.setOrgId(RandomUtil.randomInt(89));
            vo.setOrgName("xxx的假机构");
            questionSchoolRecordVOS.add(vo);
        }
        JSONObject ret = new JSONObject();
        ret.put("total",25);
        ret.put("size",questionSearchDTO.getSize());
        ret.put("current",questionSearchDTO.getPage());
        ret.put("pages",25 % questionSearchDTO.getSize()>0?25 % questionSearchDTO.getSize()+1:25 % questionSearchDTO.getSize());
        ret.put("records",questionSchoolRecordVOS);
        return ret;
    }

    /**
     * 问卷待办填写情况
     *
     * @return
     */
    @GetMapping("/backlog/list")
    public JSONObject getQuestionBacklogList(QuestionSearchDTO questionSearchDTO) {
        List<QuestionBacklogRecordVO> questionSchoolRecordVOS = Lists.newArrayList();
        for (int i = 0; i < 25; i++) {
            QuestionBacklogRecordVO vo = new QuestionBacklogRecordVO();
            vo.setAreaId(RandomUtil.randomInt(100));
            vo.setAreaName(districtService.getById(vo.getAreaId()).getName());
            vo.setSchoolId(RandomUtil.randomInt(188));
            vo.setEnvironmentalStatus(RandomUtil.randomInt(3));
            vo.setEnvironmentalId(RandomUtil.randomInt());
            vo.setSchoolName("xxx的假学校");
            vo.setOrgId(RandomUtil.randomInt(89));
            vo.setOrgName("xxx的假机构");
            questionSchoolRecordVOS.add(vo);
        }
        JSONObject ret = new JSONObject();
        ret.put("total",25);
        ret.put("size",questionSearchDTO.getSize());
        ret.put("current",questionSearchDTO.getPage());
        ret.put("pages",25 % questionSearchDTO.getSize()>0?25 % questionSearchDTO.getSize()+1:25 % questionSearchDTO.getSize());
        ret.put("records",questionSchoolRecordVOS);
        return ret;
    }
}
