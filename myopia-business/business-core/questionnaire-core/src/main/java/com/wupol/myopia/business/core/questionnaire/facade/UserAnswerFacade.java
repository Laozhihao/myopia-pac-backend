package com.wupol.myopia.business.core.questionnaire.facade;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/7/21 19:50
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserAnswerFacade {

    private final UserQuestionRecordService userQuestionRecordService;

    public List getExcelData(ExcelDataConditionBO excelDataConditionBO) {

        return null;
    }

    public List<UserQuestionRecord> getQuestionnaireList(Integer planId, Integer questionnaireType) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByPlanId(planId);
        if (!CollectionUtils.isEmpty(userQuestionRecordList)){
            return userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(),0))
                    .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getQuestionnaireType(), questionnaireType))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
