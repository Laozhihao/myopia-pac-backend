package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 政府答案实现
 *
 * @author hang.yuan 2022/8/25 10:40
 */
@Service
public class GovernmentAnswerImpl extends AbstractUserAnswer {

    @Autowired
    private  GovDeptService govDeptService;
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

        Set<Integer> govIds = userQuestionRecordList.stream().map(UserQuestionRecord::getGovId).collect(Collectors.toSet());
        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(govIds));
        Stream<GovDept> govDeptStream = govDeptList.stream();
        if (Objects.nonNull(districtIdList)){
            govDeptStream = govDeptStream.filter(govDept -> districtIdList.contains(govDept.getDistrictId()));
        }
        List<Integer> govDeptIdList = govDeptStream.map(GovDept::getId).collect(Collectors.toList());

        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> govDeptIdList.contains(userQuestionRecord.getUserId()))
                .collect(Collectors.toList());
    }



}
