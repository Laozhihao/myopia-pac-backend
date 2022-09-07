package com.wupol.myopia.business.aggregation.export.excel.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.UserQuestionnaireAnswerBO;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.util.AnswerUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
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

    /**
     * 用户问卷记录集合
     */
    private List<UserQuestionRecord> userQuestionRecordList;
    /**
     * 用户问卷记录对应答案集合
     */
    private Map<Integer, List<UserAnswer>> userAnswerMap;
    /**
     * 隐藏问题集合
     */
    private List<HideQuestionRecDataBO> hideQuestionDataBOList;
    /**
     * 筛查计划下学生信息集合
     */
    private Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap;
    /**
     * 学校集合
     */
    private Map<Integer, School> schoolMap;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 问卷类型
     */
    private QuestionnaireTypeEnum questionnaireTypeEnum;


    /**
     * 构建用户问卷答案集合
     */
    public List<UserQuestionnaireAnswerBO> dataBuild(){
        if (!ObjectsUtil.allNotNull(userQuestionRecordList, userAnswerMap,userType,questionnaireTypeEnum)
                || (Objects.equals(userType,UserType.QUESTIONNAIRE_STUDENT.getType()) && Objects.isNull(planSchoolStudentMap))
                || (Objects.equals(userType,UserType.QUESTIONNAIRE_SCHOOL.getType()) && Objects.isNull(schoolMap))) {
            throw new BusinessException("UserQuestionnaireAnswerInfo构建失败，缺少关键参数");
        }

        List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = Lists.newArrayList();

        //问卷系统学生端处理
        if (Objects.equals(userType, UserType.QUESTIONNAIRE_STUDENT.getType())){
            //学生ID对应的问卷记录信息集合
            Map<Integer, List<UserQuestionRecord>> studentMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));
            studentMap.forEach((studentId,recordList)-> userQuestionnaireAnswerBOList.add(processStudentData(studentId,recordList)));
        }

        //问卷系统学校端处理
        if (Objects.equals(userType,UserType.QUESTIONNAIRE_SCHOOL.getType())){
            setSchoolData(userQuestionnaireAnswerBOList,UserType.QUESTIONNAIRE_SCHOOL.getType());
        }

        //问卷系统政府端处理
        if (Objects.equals(userType,UserType.QUESTIONNAIRE_GOVERNMENT.getType())){
            if (Objects.equals(questionnaireTypeEnum,QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)){

                Map<String, List<UserQuestionRecord>> governmentMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(userQuestionRecord -> getGovernmentKey(userQuestionRecord.getUserType(),userQuestionRecord.getGovId(),userQuestionRecord.getDistrictCode())));
                governmentMap.forEach((key,recordList)-> userQuestionnaireAnswerBOList.add(processGovernmentData(recordList)));

            }else if (Objects.equals(questionnaireTypeEnum,QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT)){

                setSchoolData(userQuestionnaireAnswerBOList,UserType.QUESTIONNAIRE_GOVERNMENT.getType());
            }
        }
        return userQuestionnaireAnswerBOList;
    }

    private void setSchoolData(List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList,Integer userType) {
        //学校ID对应的问卷记录信息集合
        Map<Integer, List<UserQuestionRecord>> schoolDataMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
        schoolDataMap.forEach((schoolId, recordList) -> userQuestionnaireAnswerBOList.add(processSchoolData(schoolId, recordList,userType)));
    }

    /**
     * 处理政府数据
     * @param recordList 问卷记录集合
     */
    private UserQuestionnaireAnswerBO processGovernmentData(List<UserQuestionRecord> recordList) {
        Date fillDate = recordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());
        UserQuestionnaireAnswerBO userQuestionnaireAnswerBO = new UserQuestionnaireAnswerBO();
        for (UserQuestionRecord userQuestionRecord : recordList) {
            if (Objects.equals(userQuestionRecord.getRecordType(),1)){
                continue;
            }
            userQuestionnaireAnswerBO.setUserId(userQuestionRecord.getUserId());
            userQuestionnaireAnswerBO.setUserType(userQuestionRecord.getUserType());
            userQuestionnaireAnswerBO.setSchoolId(userQuestionRecord.getSchoolId());
            List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecord.getId());
            //处理学生隐藏数据
            hideQuestionRecDataGovernmentProcess(userAnswers, fillDate,userQuestionnaireAnswerBO);
            //处理非隐藏数据
            questionRecDataProcess(userAnswers, userQuestionnaireAnswerBO);
        }
        return userQuestionnaireAnswerBO;
    }

    public static String getGovernmentKey(Integer userType,Integer govId,Long districtCode){
        return userType+StrUtil.UNDERLINE+govId+StrUtil.UNDERLINE+districtCode;
    }

    /**
     * 处理学校数据
     * @param schoolId 学校ID
     * @param recordList 问卷记录集合
     * @param userType 用户类型
     */
    private UserQuestionnaireAnswerBO processSchoolData(Integer schoolId, List<UserQuestionRecord> recordList,Integer userType){
        UserQuestionnaireAnswerBO userQuestionnaireAnswerBO = new UserQuestionnaireAnswerBO();
        Date fillDate = recordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());
        for (UserQuestionRecord userQuestionRecord : recordList) {
            if (Objects.equals(userQuestionRecord.getRecordType(),1)){
                continue;
            }
            userQuestionnaireAnswerBO.setUserId(userQuestionRecord.getUserId());
            userQuestionnaireAnswerBO.setUserType(userQuestionRecord.getUserType());
            userQuestionnaireAnswerBO.setSchoolId(userQuestionRecord.getSchoolId());
            List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecord.getId());
            //处理学校隐藏数据
            hideSchoolQuestionRecDataProcess(schoolId, fillDate,userQuestionnaireAnswerBO,userAnswers,userType);

            //处理非隐藏数据
            questionRecDataProcess(userAnswers, userQuestionnaireAnswerBO);
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
            userQuestionnaireAnswerBO.setStudentId(userQuestionRecord.getStudentId());
            if (Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())
                    || Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType())){
                //处理学生隐藏数据
                hideQuestionRecDataProcess(studentId, fillDate,userQuestionnaireAnswerBO);
            }
            //处理非隐藏数据
            List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecord.getId());
            questionRecDataProcess(userAnswers, userQuestionnaireAnswerBO);
        }

        return userQuestionnaireAnswerBO;
    }


    /**
     * 处理非隐藏数据
     * @param userAnswers 用户答案集合
     * @param userQuestionnaireAnswerBO 用户问卷答案
     */
    private void questionRecDataProcess(List<UserAnswer> userAnswers, UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        if (CollUtil.isNotEmpty(userAnswers)){
            Map<Integer, List<OptionAnswer>> questionAnswerMap = getQuestionAnswerMap(userAnswers);
            if (Objects.isNull(userQuestionnaireAnswerBO.getQuestionAnswerMap())) {
                userQuestionnaireAnswerBO.setQuestionAnswerMap(questionAnswerMap);
            }else {
                userQuestionnaireAnswerBO.getQuestionAnswerMap().putAll(questionAnswerMap);
            }
        }
    }

    /**
     * 问卷对应的答案集合
     * @param userAnswers 用户问题集合
     */
    private Map<Integer, List<OptionAnswer>> getQuestionAnswerMap(List<UserAnswer> userAnswers) {
        Map<Integer, List<UserAnswer>> questionUserAnswerMap  = userAnswers.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
        Map<Integer, List<OptionAnswer>> questionAnswerMap = Maps.newHashMap();
        questionUserAnswerMap.forEach((questionId,list)->questionAnswerMap.put(questionId,getRecAnswerData(list)) );
        return questionAnswerMap;
    }


    /**
     * 获取答案数据
     * @param userAnswerList 用户答案数据集合
     */
    private List<OptionAnswer> getRecAnswerData(List<UserAnswer> userAnswerList){

        return userAnswerList.stream().flatMap(answer -> {
            if (Objects.equals(answer.getType(),"teacherTable")
                    || Objects.equals(answer.getType(),"diseaseTable")){
                String tableJson = answer.getTableJson();
                JSONObject jsonObject = JSON.parseObject(tableJson);
                return jsonObject.entrySet().stream().map(entry->{
                    OptionAnswer optionAnswer = new OptionAnswer();
                    optionAnswer.setOptionId(entry.getKey());
                    optionAnswer.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(StrUtil.EMPTY));
                    return optionAnswer;
                });
            }
            if (Objects.equals(answer.getType(),"classTable")
                    || Objects.equals(answer.getType(),"classTable2")){
                String tableJson = answer.getTableJson();

                JSONObject jsonObject = JSON.parseObject(tableJson);
                return jsonObject.values().stream()
                        .map(classObj -> JSON.parseObject(JSON.toJSONString(classObj)))
                        .flatMap(classData -> classData.entrySet().stream()
                                .map(entry -> {
                                    OptionAnswer optionAnswer = new OptionAnswer();
                                    optionAnswer.setOptionId(entry.getKey());
                                    optionAnswer.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(StrUtil.EMPTY));
                                    return optionAnswer;
                                })
                        );
            }
            List<OptionAnswer> answerList = JSON.parseArray(JSON.toJSONString(answer.getAnswer()), OptionAnswer.class);
            return answerList.stream();

        }).collect(Collectors.toList());

    }

    /**
     * 学校填写的隐藏数据处理
     * @param schoolId 学校ID
     * @param fillDate 填写日期
     * @param userQuestionnaireAnswerBO 用户问卷答案
     */
    public void hideSchoolQuestionRecDataProcess(Integer schoolId, Date fillDate,UserQuestionnaireAnswerBO userQuestionnaireAnswerBO,List<UserAnswer> userAnswers,Integer userType) {
        if (CollUtil.isEmpty(hideQuestionDataBOList)){
            return;
        }
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();

        String schoolNo = getSchoolNo(schoolId,userAnswers,userType);

        for (HideQuestionRecDataBO hideQuestionDataBO : hideQuestionDataBOList) {
            QesFieldDataBO recAnswerDataBO = new QesFieldDataBO();
            List<HideQuestionRecDataBO.QesDataBO> qesDataList = hideQuestionDataBO.getQesData();
            qesDataList = qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), QuestionnaireConstant.QM)).collect(Collectors.toList());
            if (CollUtil.isEmpty(qesDataList)) {
                continue;
            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.INPUT)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                recAnswerDataBO.setQesField(qesDataBO.getQesField());
                setSchoolQesFieldData(fillDate, schoolNo, recAnswerDataBO, qesDataBO);

            }
            qesFieldDataBOList.add(recAnswerDataBO);
        }
        userQuestionnaireAnswerBO.setQesFieldDataBOList(qesFieldDataBOList);
    }

    private String getSchoolNo(Integer schoolId, List<UserAnswer> userAnswers, Integer userType) {
        if (Objects.equals(userType,UserType.QUESTIONNAIRE_SCHOOL.getType())){
            School school = schoolMap.get(schoolId);
            if (Objects.isNull(school)){
                return StrUtil.EMPTY;
            }
            return school.getSchoolNo();
        }

        if (Objects.equals(userType,UserType.QUESTIONNAIRE_GOVERNMENT.getType())){
            if (CollUtil.isEmpty(userAnswers)){
                return StrUtil.EMPTY;
            }
            Map<Integer, List<OptionAnswer>> questionAnswerMap = getQuestionAnswerMap(userAnswers);
            return questionAnswerMap.values().stream().flatMap(Collection::stream)
                    .filter(optionAnswer -> Objects.equals(optionAnswer.getQesField(), "ID2"))
                    .map(OptionAnswer::getValue)
                    .findFirst().orElse(StrUtil.EMPTY);
        }

        return StrUtil.EMPTY;
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
            qesDataList = qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), QuestionnaireConstant.QM)).collect(Collectors.toList());
            if (CollUtil.isEmpty(qesDataList)) {
                continue;
            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.INPUT)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                recAnswerDataBO.setQesField(qesDataBO.getQesField());
                setStudentQesFieldData(fillDate, commonDiseaseId, recAnswerDataBO, qesDataBO);

            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.RADIO)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                if (Objects.equals(qesDataBO.getQesField(), "a02")) {
                    recAnswerDataBO.setQesField(qesDataBO.getQesField());
                    recAnswerDataBO.setRecAnswer(AnswerUtil.getGenderRecData(screeningPlanSchoolStudent.getGender()));
                }
            }
            qesFieldDataBOList.add(recAnswerDataBO);
        }
        userQuestionnaireAnswerBO.setQesFieldDataBOList(qesFieldDataBOList);
    }

    /**
     * 政府填写的隐藏数据处理
     * @param userAnswers 用户答案
     * @param fillDate 填写日期
     * @param userQuestionnaireAnswerBO 用户问卷答案
     */
    public void hideQuestionRecDataGovernmentProcess(List<UserAnswer> userAnswers, Date fillDate,UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        if (CollUtil.isEmpty(hideQuestionDataBOList)){
            return;
        }
        String id2=StrUtil.EMPTY;
        if (CollUtil.isNotEmpty(userAnswers)){
            Map<Integer, List<OptionAnswer>> questionAnswerMap = getQuestionAnswerMap(userAnswers);
            id2 = questionAnswerMap.values().stream().flatMap(Collection::stream)
                    .filter(optionAnswer -> Objects.equals(optionAnswer.getQesField(), "ID2"))
                    .map(OptionAnswer::getValue)
                    .findFirst().orElse(StrUtil.EMPTY);
        }

        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();

        for (HideQuestionRecDataBO hideQuestionDataBO : hideQuestionDataBOList) {
            QesFieldDataBO recAnswerDataBO = new QesFieldDataBO();
            List<HideQuestionRecDataBO.QesDataBO> qesDataList = hideQuestionDataBO.getQesData();
            qesDataList = qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), QuestionnaireConstant.QM)).collect(Collectors.toList());
            if (CollUtil.isEmpty(qesDataList)) {
                continue;
            }
            if (Objects.equals(hideQuestionDataBO.getType(), QuestionnaireConstant.INPUT)) {
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                recAnswerDataBO.setQesField(qesDataBO.getQesField());
                setGovernmentQesFieldData(fillDate, id2, recAnswerDataBO, qesDataBO);

            }
            qesFieldDataBOList.add(recAnswerDataBO);
        }
        userQuestionnaireAnswerBO.setQesFieldDataBOList(qesFieldDataBOList);
    }


    /**
     * 政府设置qes字段数据
     * @param fillDate 日期
     * @param id2 常见病ID
     * @param qesFieldDataBO qes字段数据对象
     * @param qesDataBO qes数据对象
     */
    private void setGovernmentQesFieldData(Date fillDate, String id2, QesFieldDataBO qesFieldDataBO, HideQuestionRecDataBO.QesDataBO qesDataBO) {
        switch (qesDataBO.getQesField()) {
            case "ID1":
            case "ID2":
                qesFieldDataBO.setRecAnswer(id2);
                break;
            case "date":
                qesFieldDataBO.setRecAnswer(DateUtil.format(fillDate, QuestionnaireConstant.DATE_FORMAT));
                break;
            default:
                break;
        }
    }

    /**
     * 学校设置qes字段数据
     * @param fillDate 日期
     * @param commonDiseaseId 常见病ID
     * @param qesFieldDataBO qes字段数据对象
     * @param qesDataBO qes数据对象
     */
    private void setStudentQesFieldData(Date fillDate, String commonDiseaseId, QesFieldDataBO qesFieldDataBO, HideQuestionRecDataBO.QesDataBO qesDataBO) {

        if (Objects.equals(qesDataBO.getQesField(),"a01")){
            qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(commonDiseaseId, 10, 12));
            return;
        }
        if (Objects.equals(qesDataBO.getQesField(),"a011")){
            qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(commonDiseaseId, 12, 16));
            return;
        }
        setSchoolQesFieldData(fillDate,commonDiseaseId,qesFieldDataBO,qesDataBO);

    }

    /**
     * 学校qes字段数据
     * @param fillDate 填写时间
     * @param schoolNo 学生编码
     * @param qesFieldDataBO qes字段对应答案
     * @param qesDataBO qes数据
     */
    private void setSchoolQesFieldData(Date fillDate, String schoolNo, QesFieldDataBO qesFieldDataBO, HideQuestionRecDataBO.QesDataBO qesDataBO) {
        switch (qesDataBO.getQesField()) {
            case "province":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 0, 2));
                break;
            case "city":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 2, 4));
                break;
            case "district":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 4, 5));
                break;
            case "county":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 5, 7));
                break;
            case "point":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 7, 8));
                break;
            case "school":
                qesFieldDataBO.setRecAnswer(AnswerUtil.getValue(schoolNo, 8, 10));
                break;
            case "ID1":
            case "ID2":
                qesFieldDataBO.setRecAnswer(schoolNo);
                break;
            case "date":
                qesFieldDataBO.setRecAnswer(DateUtil.format(fillDate, QuestionnaireConstant.DATE_FORMAT));
                break;
            default:
                break;
        }
    }

}
