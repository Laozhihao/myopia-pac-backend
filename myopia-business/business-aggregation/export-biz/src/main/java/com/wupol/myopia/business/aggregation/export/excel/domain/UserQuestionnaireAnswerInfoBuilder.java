package com.wupol.myopia.business.aggregation.export.excel.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户问卷答案信息构建
 *
 * @author hang.yuan 2022/8/20 17:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuestionnaireAnswerInfoBuilder {

    private List<UserQuestionRecord> userQuestionRecordList;
    private Map<Integer, List<UserAnswer>> userAnswerMap;
    private List<HideQuestionRecDataBO> hideQuestionDataBOList;
    private Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap;
    private Integer userType;

    private List<Integer> schoolType = Lists.newArrayList(
                                        QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType(),
                                        QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType(),
                                        QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType());


    public List<UserQuestionnaireAnswerBO> dataBuild(){
        if (!ObjectsUtil.allNotNull(userQuestionRecordList, userAnswerMap,planSchoolStudentMap,userType)) {
            throw new BusinessException("UserQuestionnaireAnswerInfo构建失败，缺少关键参数");
        }

        List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = Lists.newArrayList();

        if (Objects.equals(userType, UserType.QUESTIONNAIRE_STUDENT.getType())){
            //学生ID对应的问卷记录信息集合
            Map<Integer, List<UserQuestionRecord>> studentMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));
            studentMap.forEach((studentId,recordList)-> userQuestionnaireAnswerBOList.add(processStudentData(studentId,recordList)));
        }

        if (Objects.equals(userType,UserType.QUESTIONNAIRE_SCHOOL.getType())
                || Objects.equals(userType,UserType.QUESTIONNAIRE_GOVERNMENT.getType())){

            //学生ID对应的问卷记录信息集合
            Map<Integer, List<UserQuestionRecord>> schoolMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
            schoolMap.forEach((schoolId,recordList)-> userQuestionnaireAnswerBOList.add(processSchoolData(schoolId,recordList)));
        }

        return userQuestionnaireAnswerBOList;
    }


    private UserQuestionnaireAnswerBO processSchoolData(Integer schoolId, List<UserQuestionRecord> userQuestionRecordList){
        UserQuestionnaireAnswerBO userQuestionnaireAnswerBO = new UserQuestionnaireAnswerBO();
        Date fillDate = userQuestionRecordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());
        for (UserQuestionRecord userQuestionRecord : userQuestionRecordList) {
            userQuestionnaireAnswerBO.setUserId(userQuestionRecord.getUserId());
            userQuestionnaireAnswerBO.setUserType(userQuestionRecord.getUserType());
            userQuestionnaireAnswerBO.setStudentId(userQuestionRecord.getSchoolId());
            if (schoolType.contains(userQuestionRecord.getQuestionnaireType())){
                //处理隐藏数据（学生和学校数据）
                hideSchoolQuestionRecDataProcess(schoolId, fillDate,userQuestionnaireAnswerBO);
            }
            //处理非隐藏数据
            questionRecDataProcess(userAnswerMap, userQuestionnaireAnswerBO, userQuestionRecord.getId());
        }
        return userQuestionnaireAnswerBO;
    }

    /**
     * 处理每个学生数据
     * @param studentId 学生ID
     * @param userQuestionRecordList 用户问卷记录集合
     */
    private UserQuestionnaireAnswerBO processStudentData(Integer studentId, List<UserQuestionRecord> userQuestionRecordList){
        UserQuestionnaireAnswerBO userQuestionnaireAnswerBO = new UserQuestionnaireAnswerBO();
        Date fillDate = userQuestionRecordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());
        for (UserQuestionRecord userQuestionRecord : userQuestionRecordList) {
            userQuestionnaireAnswerBO.setUserId(userQuestionRecord.getUserId());
            userQuestionnaireAnswerBO.setUserType(userQuestionRecord.getUserType());
            userQuestionnaireAnswerBO.setStudentId(userQuestionRecord.getSchoolId());
            if (Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())
                    || Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType())){
                //处理隐藏数据（学生和学校数据）
                hideQuestionRecDataProcess(studentId, fillDate,userQuestionnaireAnswerBO);
            }
            //处理非隐藏数据
            questionRecDataProcess(userAnswerMap, userQuestionnaireAnswerBO, userQuestionRecord.getId());
        }

        return userQuestionnaireAnswerBO;
    }


    /**
     * 处理非隐藏数据
     * @param userAnswerMap 用户答案集合
     * @param userQuestionnaireAnswerBO 用户问卷答案
     * @param userQuestionRecordId 用户问卷记录ID
     */
    private void questionRecDataProcess(Map<Integer, List<UserAnswer>> userAnswerMap, UserQuestionnaireAnswerBO userQuestionnaireAnswerBO, Integer userQuestionRecordId) {
        List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecordId);
        if (CollUtil.isNotEmpty(userAnswers)){
            Map<Integer, List<UserAnswer>> questionUserAnswerMap  = userAnswers.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
            Map<Integer, List<OptionAnswer>> questionAnswerMap = Maps.newHashMap();
            questionUserAnswerMap.forEach((questionId,list)->questionAnswerMap.put(questionId,getRecAnswerData(list)) );
            if (Objects.isNull(userQuestionnaireAnswerBO.getQuestionAnswerMap())) {
                userQuestionnaireAnswerBO.setQuestionAnswerMap(questionAnswerMap);
            }else {
                userQuestionnaireAnswerBO.getQuestionAnswerMap().putAll(questionAnswerMap);
            }
        }
    }


    /**
     * 获取答案数据
     * @param userAnswerList 用户答案数据集合
     */
    private List<OptionAnswer> getRecAnswerData(List<UserAnswer> userAnswerList){
        return userAnswerList.stream().flatMap(answer -> {
            List<OptionAnswer> answerList = JSON.parseArray(JSON.toJSONString(answer.getAnswer()), OptionAnswer.class);
            return answerList.stream();
        }).collect(Collectors.toList());

    }
    public void hideSchoolQuestionRecDataProcess(Integer schoolId, Date fillDate,UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {

    }

    /**
     * 隐藏问卷数据处理
     *
     * @param studentId              学生ID
     * @param fillDate               填写日期
     */
    public void hideQuestionRecDataProcess(Integer studentId, Date fillDate,UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        if (CollUtil.isEmpty(hideQuestionDataBOList)){
            return;
        }
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planSchoolStudentMap.get(studentId);
        String commonDiseaseId = screeningPlanSchoolStudent.getCommonDiseaseId();

        for (HideQuestionRecDataBO hideQuestionDataBO : hideQuestionDataBOList) {
            QesFieldDataBO recAnswerDataBO = new QesFieldDataBO();
            List<HideQuestionRecDataBO.QesDataBO> qesDataList = hideQuestionDataBO.getQesData();
            qesDataList = qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), "QM")).collect(Collectors.toList());
            if (CollUtil.isEmpty(qesDataList)) {
                continue;
            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.INPUT)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                recAnswerDataBO.setQesField(qesDataBO.getQesField());
                setQesFieldData(fillDate, commonDiseaseId, recAnswerDataBO, qesDataBO);

            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.RADIO)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                if (Objects.equals(qesDataBO.getQesField(), "a02")) {
                    recAnswerDataBO.setQesField(qesDataBO.getQesField());
                    recAnswerDataBO.setRecAnswer(UserAnswerBuilder.getGenderRecData(screeningPlanSchoolStudent.getGender()));
                }
            }
            qesFieldDataBOList.add(recAnswerDataBO);
        }
        userQuestionnaireAnswerBO.setQesFieldDataBOList(qesFieldDataBOList);
    }

    /**
     * 设置qes字段数据
     * @param fillDate 日期
     * @param commonDiseaseId 常见病ID
     * @param qesFieldDataBO qes字段数据对象
     * @param qesDataBO qes数据对象
     */
    private void setQesFieldData(Date fillDate, String commonDiseaseId, QesFieldDataBO qesFieldDataBO, HideQuestionRecDataBO.QesDataBO qesDataBO) {
        switch (qesDataBO.getQesField()) {
            case "province":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 0, 2));
                break;
            case "city":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 2, 4));
                break;
            case "district":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 4, 5));
                break;
            case "county":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 5, 7));
                break;
            case "point":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 7, 8));
                break;
            case "school":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 8, 10));
                break;
            case "a01":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 10, 12));
                break;
            case "a011":
                qesFieldDataBO.setRecAnswer(UserAnswerBuilder.getValue(commonDiseaseId, 12, 16));
                break;
            case "ID1":
            case "ID2":
                qesFieldDataBO.setRecAnswer(commonDiseaseId);
                break;
            case "date":
                qesFieldDataBO.setRecAnswer(DateUtil.format(fillDate, "yyyy/MM/dd"));
                break;
            default:
                break;
        }
    }


}
