package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 学生答案实现
 *
 * @author hang.yuan 2022/8/25 10:38
 */
@Service
public class StudentAnswerImpl extends AbstractUserAnswer {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Override
    public Integer getUserType() {
        return UserType.QUESTIONNAIRE_STUDENT.getType();
    }

    @Override
    public List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO) {
        List<UserQuestionRecord> userQuestionRecordList = answerDataBO.getUserQuestionRecordList();
        List<Integer> gradeTypeList = answerDataBO.getGradeTypeList();
        ExportCondition exportCondition = answerDataBO.getExportCondition();
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }

        List<Integer> districtIdList = filterDistrict(exportCondition.getDistrictId());

        Set<Integer> planStudentIds = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));
        //年级过滤学生记录
        Stream<ScreeningPlanSchoolStudent> studentStream = planSchoolStudentList.stream()
                .filter(planSchoolStudent -> gradeTypeList.contains(planSchoolStudent.getGradeType()));

        if (Objects.nonNull(districtIdList)) {
            studentStream = studentStream.filter(planSchoolStudent -> districtIdList.contains(planSchoolStudent.getSchoolDistrictId()));
        }

        if (Objects.nonNull(exportCondition.getSchoolId())){
            userQuestionRecordList = userQuestionRecordList.stream().filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getSchoolId(),exportCondition.getSchoolId())).collect(Collectors.toList());
        }

        List<Integer> planStudentIdList = studentStream.map(ScreeningPlanSchoolStudent::getId)
                .collect(Collectors.toList());

        //根据计划学生过滤用户问卷记录
        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> planStudentIdList.contains(userQuestionRecord.getUserId()))
                .collect(Collectors.toList());
    }

}
