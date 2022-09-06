package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FilterDataCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
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
 * 政府答案实现
 *
 * @author hang.yuan 2022/8/25 10:40
 */
@Service
public class GovernmentAnswerImpl extends AbstractUserAnswer {

    @Autowired
    private SchoolService schoolService;

    private QuestionnaireTypeEnum questionnaireTypeEnum;

    @Override
    public Integer getUserType() {
        return UserType.QUESTIONNAIRE_GOVERNMENT.getType();
    }

    @Override
    public List<UserQuestionRecord> filterData(FilterDataCondition filterDataCondition) {
        return getUserQuestionRecordList(filterDataCondition.getUserQuestionRecordList(),filterDataCondition.getDistrictId(),filterDataCondition.getQuestionnaireTypeEnum());
    }

    @Override
    public List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO) {
        List<UserQuestionRecord> userQuestionRecordList = answerDataBO.getUserQuestionRecordList();
        ExportCondition exportCondition = answerDataBO.getExportCondition();
        return getUserQuestionRecordList(userQuestionRecordList,exportCondition.getDistrictId(),answerDataBO.getQuestionnaireTypeEnum());
    }


    @Override
    protected List<Integer> filterDistrict(Integer districtId) {

        List<District> districtList = super.getDistrictList(districtId);
        List<Integer> districtIdList = super.getDistrictIds(districtList);
        if (Objects.equals(questionnaireTypeEnum,QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)){
            if (Objects.isNull(districtIdList)){
                return districtIdList;
            }
            return districtList.stream().map(district -> district.getCode().toString().substring(0, 6)).map(Integer::valueOf).distinct().collect(Collectors.toList());
        }
        return districtIdList;
    }

    /**
     * 获取有效的用户问卷记录信息
     * @param userQuestionRecordList 用户问卷记录集合
     * @param districtId 地区ID
     * @param questionnaireType 问卷类型
     */
    private List<UserQuestionRecord> getUserQuestionRecordList(List<UserQuestionRecord> userQuestionRecordList,Integer districtId,QuestionnaireTypeEnum questionnaireType) {
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }
        Stream<UserQuestionRecord> userQuestionRecordStream = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getUserType(), UserType.QUESTIONNAIRE_GOVERNMENT.getType()));

        if (Objects.equals(questionnaireType, QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)){
            questionnaireTypeEnum = QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL;
            List<Integer> districtIdList = filterDistrict(districtId);
            if (CollUtil.isNotEmpty(districtIdList)){
                userQuestionRecordStream = userQuestionRecordStream.filter(userQuestionRecord -> {
                    if (Objects.isNull(userQuestionRecord)){
                        return false;
                    }
                    return districtIdList.contains(Integer.valueOf(userQuestionRecord.getDistrictCode().toString().substring(0,6)));
                });
            }
        }else if (Objects.equals(questionnaireType, QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT)){
            questionnaireTypeEnum = QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT;
            List<Integer> districtIdList = filterDistrict(districtId);
            Set<Integer> schoolIds = userQuestionRecordList.stream().map(UserQuestionRecord::getSchoolId).collect(Collectors.toSet());
            List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));
            Stream<School> schoolStream = schoolList.stream();
            if (CollUtil.isNotEmpty(districtIdList)) {
                schoolStream = schoolStream.filter(school -> districtIdList.contains(school.getDistrictId()));
            }
            List<Integer> schoolIdList = schoolStream.map(School::getId).collect(Collectors.toList());
            userQuestionRecordStream = userQuestionRecordStream.filter(userQuestionRecord -> schoolIdList.contains(userQuestionRecord.getSchoolId()));
        }

        return userQuestionRecordStream.collect(Collectors.toList());
    }
}
