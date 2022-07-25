package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireMainTitleEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 问卷
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireBizService {

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;


    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(CurrentUser user) {

        if (!user.isQuestionnaireStudentUser()) {
            // 不是筛查学生用户，则抛出异常
            throw new BusinessException("请确认身份");
        }
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(user.getQuestionnaireUserId());
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("获取信息异常");
        }

        List<QuestionnaireTypeEnum> typeList = QuestionnaireTypeEnum.getBySchoolAge(planStudent.getGradeType());
        if (CollectionUtils.isEmpty(typeList)) {
            return new ArrayList<>();
        }

        // 获取今年的问卷
        Map<Integer, Questionnaire> typeMap = questionnaireService.getByYearAndTypes(
                        typeList.stream().map(QuestionnaireTypeEnum::getType).collect(Collectors.toList()),
                        DateUtil.getYear(new Date())).stream()
                .collect(Collectors.toMap(Questionnaire::getType, Function.identity()));

        return typeList.stream().map(s -> {
            UserQuestionnaireResponseDTO responseDTO = new UserQuestionnaireResponseDTO();
            Questionnaire questionnaire = typeMap.get(s.getType());
            responseDTO.setId(questionnaire.getId());
            responseDTO.setTitle(s.getDesc());
            responseDTO.setMainTitle(QuestionnaireMainTitleEnum.getByType(s.getType()).getMainTitle());
            return responseDTO;
        }).collect(Collectors.toList());
    }
}
