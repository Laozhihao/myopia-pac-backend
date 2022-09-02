package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FilterDataCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 学校答案实现
 *
 * @author hang.yuan 2022/8/25 10:39
 */
@Service
public class SchoolAnswerImpl extends AbstractUserAnswer {

    @Autowired
    private SchoolService schoolService;

    @Override
    public Integer getUserType() {
        return UserType.QUESTIONNAIRE_SCHOOL.getType();
    }

    @Override
    public List<UserQuestionRecord> filterData(FilterDataCondition filterDataCondition) {
        return getUserQuestionRecordList(filterDataCondition.getUserQuestionRecordList(),filterDataCondition.getDistrictId(),null);
    }

    @Override
    public List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO) {
        ExportCondition exportCondition = answerDataBO.getExportCondition();
        List<UserQuestionRecord> userQuestionRecordList = answerDataBO.getUserQuestionRecordList();
        return getUserQuestionRecordList(userQuestionRecordList,exportCondition.getDistrictId(),exportCondition.getSchoolId());

    }


    /**
     * 获取有效的用户问卷记录信息
     * @param userQuestionRecordList 用户问卷记录集合
     * @param districtId 地区ID
     * @param schoolId 学校ID
     */
    private List<UserQuestionRecord> getUserQuestionRecordList(List<UserQuestionRecord> userQuestionRecordList, Integer districtId,Integer schoolId) {
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }
        List<Integer> districtIdList = filterDistrict(districtId);

        Set<Integer> schoolIds = userQuestionRecordList.stream().map(UserQuestionRecord::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));

        Stream<School> schoolStream = schoolList.stream();

        if (Objects.nonNull(schoolId)) {
            schoolStream = schoolStream.filter(school -> Objects.equals(school.getId(), schoolId));
        }

        if (Objects.nonNull(districtIdList)) {
            schoolStream = schoolStream.filter(school -> districtIdList.contains(school.getDistrictId()));
        }
        List<Integer> schoolIdList = schoolStream.map(School::getId).collect(Collectors.toList());

        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> schoolIdList.contains(userQuestionRecord.getSchoolId()))
                .collect(Collectors.toList());

    }
}
