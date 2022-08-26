package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 政府答案实现
 *
 * @author hang.yuan 2022/8/25 10:40
 */
@Service
public class GovernmentAnswerImpl extends AbstractUserAnswer {

    @Override
    public Integer getUserType() {
        return UserType.QUESTIONNAIRE_GOVERNMENT.getType();
    }

    @Override
    public List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO) {
        List<UserQuestionRecord> userQuestionRecordList = answerDataBO.getUserQuestionRecordList();
        ExportCondition exportCondition = answerDataBO.getExportCondition();
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }

        List<Integer> districtIdList = filterDistrict(exportCondition.getDistrictId());
        Stream<UserQuestionRecord> stream = userQuestionRecordList.stream();
        if (Objects.nonNull(districtIdList)) {
            stream = stream.filter(userQuestionRecord -> districtIdList.contains(userQuestionRecord.getDistrictId()));
        }

        return stream.collect(Collectors.toList());
    }



}
