package com.wupol.myopia.business.api.school.management.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.api.school.management.constant.EyeTypeEnum;
import com.wupol.myopia.business.api.school.management.constant.SchoolConstant;
import com.wupol.myopia.business.api.school.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.api.school.management.domain.vo.*;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查计划相关构建
 *
 * @author hang.yuan 2022/9/14 17:17
 */
@UtilityClass
public class ScreeningPlanBuilder {

    /**
     * 构建筛查计划
     * @param screeningPlanDTO 筛查计划参数
     * @param currentUser 当前用户
     * @param districtId 区域ID
     */
    public ScreeningPlan buildScreeningPlan(ScreeningPlanDTO screeningPlanDTO, CurrentUser currentUser, Integer districtId) {
        return new ScreeningPlan()
                .setId(screeningPlanDTO.getId())
                .setSrcScreeningNoticeId(CommonConst.DEFAULT_ID)
                .setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setTitle(screeningPlanDTO.getTitle())
                .setContent(Optional.ofNullable(screeningPlanDTO.getContent()).orElse(StrUtil.EMPTY))
                .setStartTime(DateFormatUtil.parseDate(screeningPlanDTO.getStartTime(), SchoolConstant.START_TIME,DatePattern.NORM_DATETIME_PATTERN))
                .setEndTime(DateFormatUtil.parseDate(screeningPlanDTO.getEndTime(),SchoolConstant.END_TIME,DatePattern.NORM_DATETIME_PATTERN))
                .setGovDeptId(CommonConst.DEFAULT_ID)
                .setScreeningOrgId(currentUser.getOrgId())
                .setScreeningOrgType(ScreeningOrgTypeEnum.SCHOOL.getType())
                .setDistrictId(districtId)
                .setReleaseStatus(CommonConst.STATUS_NOT_RELEASE)
                .setCreateUserId(currentUser.getId())
                .setOperatorId(currentUser.getId())
                .setScreeningType(screeningPlanDTO.getScreeningType());
    }

    /**
     * 构建筛查计划学校
     * @param screeningPlanSchoolDb 数据库的筛查计划学校
     * @param school 学校对象
     */
    public ScreeningPlanSchool buildScreeningPlanSchool(ScreeningPlanSchool screeningPlanSchoolDb, School school,List<Integer> gradeIds) {

        if (Objects.nonNull(screeningPlanSchoolDb)){
            screeningPlanSchoolDb.setSchoolName(school.getName());
            screeningPlanSchoolDb.setScreeningGradeIds(CollUtil.join(gradeIds, StrUtil.COMMA));
            return screeningPlanSchoolDb;
        }

        return new ScreeningPlanSchool()
                .setScreeningOrgId(school.getId())
                .setSchoolId(school.getId())
                .setSchoolName(school.getName())
                .setScreeningGradeIds(CollUtil.join(gradeIds, StrUtil.COMMA));
    }

    /**
     * 筛查计划学校学生
     * @param schoolStudentList 选中的学生集合
     * @param school 学校
     * @param schoolGradeMap 年级集合
     * @param schoolClassMap 班级集合
     * @param screeningPlanSchoolStudentDbList 数据库的筛查学生集合
     */
    public TwoTuple<List<ScreeningPlanSchoolStudent>,List<Integer>> getScreeningPlanSchoolStudentList(List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, SchoolClass> schoolClassMap,
                                                                                                      List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList,boolean isAdd) {
        if (CollUtil.isEmpty(screeningPlanSchoolStudentDbList)){
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getScreeningPlanSchoolStudents(schoolStudentList, school, schoolGradeMap, schoolClassMap);
            return TwoTuple.of(screeningPlanSchoolStudentList, Lists.newArrayList());
        }else {
            Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentDbList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity()));
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList =Lists.newArrayList();
            List<Integer> addOrUpdateStudentIds=Lists.newArrayList();
            processAddAndUpdate(schoolStudentList, school, schoolGradeMap, schoolClassMap, planSchoolStudentMap, screeningPlanSchoolStudentList, addOrUpdateStudentIds);

            List<Integer> dbStudentIds=Lists.newArrayList();
            if (Objects.equals(Boolean.FALSE,isAdd)){
                //删除
                dbStudentIds.addAll(planSchoolStudentMap.keySet());
                dbStudentIds.removeAll(addOrUpdateStudentIds);
            }
            return TwoTuple.of(screeningPlanSchoolStudentList,dbStudentIds) ;
        }
    }

    /**
     *  处理新增和更新数据
     *
     * @param schoolStudentList 学生集合
     * @param school 学校信息
     * @param schoolGradeMap 年级集合
     * @param schoolClassMap 班级集合
     * @param planSchoolStudentMap 筛查计划学校学生集合
     * @param screeningPlanSchoolStudentList 新增和更新筛查计划学校学生集合
     * @param addOrUpdateStudentIds 新增和更新学生ID集合
     */
    private static void processAddAndUpdate(List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap,
                                            Map<Integer, SchoolClass> schoolClassMap, Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap,
                                            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList, List<Integer> addOrUpdateStudentIds) {
        if (CollUtil.isEmpty(schoolStudentList)){
            return;
        }
        //新增或更新
        schoolStudentList.forEach(schoolStudent -> {
            addOrUpdateStudentIds.add(schoolStudent.getStudentId());
            SchoolGrade schoolGrade = schoolGradeMap.get(schoolStudent.getGradeId());
            SchoolClass schoolClass = schoolClassMap.get(schoolStudent.getClassId());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planSchoolStudentMap.get(schoolStudent.getStudentId());
            if (Objects.isNull(screeningPlanSchoolStudent)){
                screeningPlanSchoolStudentList.add(buildScreeningPlanSchoolStudent(schoolStudent, school, schoolGrade, schoolClass));
            }else {
                updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent,school,schoolStudent,schoolGrade,schoolClass);
                screeningPlanSchoolStudentList.add(screeningPlanSchoolStudent);
            }
        });
    }

    /**
     * 获取筛查计划学生
     * @param schoolStudentList 学校学生信息
     * @param school 学校信息
     * @param schoolGradeMap 年级信息
     * @param schoolClassMap 班级信息
     */
    private static List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudents(List<SchoolStudent> schoolStudentList, School school, Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, SchoolClass> schoolClassMap) {
        return schoolStudentList.stream().map(schoolStudent -> {
                    SchoolGrade schoolGrade = schoolGradeMap.get(schoolStudent.getGradeId());
                    SchoolClass schoolClass = schoolClassMap.get(schoolStudent.getClassId());
                    return buildScreeningPlanSchoolStudent(schoolStudent, school, schoolGrade, schoolClass);
                }).collect(Collectors.toList());
    }

    /**
     * 更新筛查计划学校学生
     * @param screeningPlanSchoolStudent 筛查计划学校学生
     * @param school 学校信息
     * @param schoolStudent 学生信息
     * @param schoolGrade 年级集合
     * @param schoolClass 班级集合
     */
    private void updateScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent,School school, SchoolStudent schoolStudent, SchoolGrade schoolGrade, SchoolClass schoolClass) {
        setStudentChangeData(screeningPlanSchoolStudent, school, schoolStudent);
        if (Objects.isNull(screeningPlanSchoolStudent.getScreeningCode())){
            screeningPlanSchoolStudent.setScreeningCode(ScreeningCodeGenerator.nextId());
        }
        if (Objects.nonNull(schoolGrade)){
            screeningPlanSchoolStudent.setGradeName(schoolGrade.getName());
        }
        if (Objects.nonNull(schoolClass)){
            screeningPlanSchoolStudent.setClassName(schoolClass.getName());
        }
    }

    /**
     * 设置学生变动数据
     * @param screeningPlanSchoolStudent 筛查计划学校学生对象
     * @param school 学校对象
     * @param schoolStudent 学生对象
     */
    private void setStudentChangeData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, School school, SchoolStudent schoolStudent) {
        screeningPlanSchoolStudent
                .setGradeId(schoolStudent.getGradeId())
                .setClassId(schoolStudent.getClassId())
                .setPlanDistrictId(school.getDistrictId())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolName(school.getName())
                .setGradeType(schoolStudent.getGradeType())
                .setIdCard(schoolStudent.getIdCard())
                .setBirthday(schoolStudent.getBirthday())
                .setGender(schoolStudent.getGender())
                .setStudentAge(DateUtil.ageOfNow(schoolStudent.getBirthday()))
                .setStudentSituation(SerializationUtil.serializeWithoutException(schoolStudent))
                .setStudentName(schoolStudent.getName())
                .setProvinceCode(schoolStudent.getProvinceCode())
                .setCityCode(schoolStudent.getCityCode())
                .setAreaCode(schoolStudent.getAreaCode())
                .setTownCode(schoolStudent.getTownCode())
                .setAddress(schoolStudent.getAddress())
                .setParentPhone(schoolStudent.getParentPhone())
                .setNation(schoolStudent.getNation())
                .setPassport(schoolStudent.getPassport());
    }

    /**
     * 构建筛查计划学校学生
     * @param schoolStudent 学生信息
     * @param school 学校信息
     * @param schoolGrade 年级集合
     * @param schoolClass 班级集合
     */
    private ScreeningPlanSchoolStudent buildScreeningPlanSchoolStudent(SchoolStudent schoolStudent,School school,SchoolGrade schoolGrade,SchoolClass schoolClass){
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent()
                .setSrcScreeningNoticeId(CommonConst.DEFAULT_ID)
                .setScreeningTaskId(CommonConst.DEFAULT_ID)
                .setScreeningOrgId(school.getId())
                .setSchoolId(school.getId())
                .setStudentId(schoolStudent.getStudentId())
                .setStudentNo(schoolStudent.getSno())
                .setArtificial(ArtificialStatusConstant.NON_ARTIFICIAL);
        updateScreeningPlanSchoolStudent(screeningPlanSchoolStudent,school,schoolStudent,schoolGrade,schoolClass);
        return screeningPlanSchoolStudent;
    }

    /**
     * 获取学生筛查详情
     * @param studentScreeningResultDetail 学生筛查结果详情
     */
    public static StudentScreeningDetailVO getStudentScreeningDetailVO(VisionScreeningResultDTO studentScreeningResultDetail) {
        StudentScreeningDetailVO studentScreeningDetailVO = new StudentScreeningDetailVO();

        setVisionDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setComputerOptometryDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setOtherDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setHeightAndWeightDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setBiometricDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setPupilOptometryDataVO(studentScreeningResultDetail, studentScreeningDetailVO);
        setEyePressureDataVO(studentScreeningResultDetail, studentScreeningDetailVO);

        return studentScreeningDetailVO;
    }

    /**
     * 视力数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setVisionDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        VisionDataDO visionData = studentScreeningResultDetail.getVisionData();
        List<VisionDataVO> visionDataVoList = Lists.newArrayList();
        if (Objects.isNull(visionData)){
            visionDataVoList.add(new VisionDataVO()
                    .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                    .setNakedVision(SchoolConstant.NO_DATA)
                    .setCorrectedVision(SchoolConstant.NO_DATA));
             visionDataVoList.add(new VisionDataVO()
                    .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                    .setNakedVision(SchoolConstant.NO_DATA)
                    .setCorrectedVision(SchoolConstant.NO_DATA));
            studentScreeningDetailVO.setVisionData(visionDataVoList);
            return;
        }

        Integer glassesType = Optional.ofNullable(visionData.getLeftEyeData()).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
        studentScreeningDetailVO.setGlassesType(glassesType);

        visionDataVoList.add(new VisionDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setNakedVision(getValueByBigDecimal(visionData.getLeftEyeData(), VisionDataDO.VisionData::getNakedVision))
                .setCorrectedVision(getValueByBigDecimal(visionData.getLeftEyeData(), VisionDataDO.VisionData::getCorrectedVision)));
        visionDataVoList.add(new VisionDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setNakedVision(getValueByBigDecimal(visionData.getRightEyeData(), VisionDataDO.VisionData::getNakedVision))
                .setCorrectedVision(getValueByBigDecimal(visionData.getRightEyeData(), VisionDataDO.VisionData::getCorrectedVision)));

        studentScreeningDetailVO.setVisionData(visionDataVoList);
    }


    /**
     * 电脑验光
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setComputerOptometryDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        ComputerOptometryDO computerOptometry = studentScreeningResultDetail.getComputerOptometry();
        List<ComputerOptometryDataVO> computerOptometryDataVoList = Lists.newArrayList();
        if (Objects.isNull(computerOptometry)){
            computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                            .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                            .setAxial(SchoolConstant.NO_DATA)
                            .setSph(SchoolConstant.NO_DATA)
                            .setCyl(SchoolConstant.NO_DATA));
            computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                    .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                    .setAxial(SchoolConstant.NO_DATA)
                    .setSph(SchoolConstant.NO_DATA)
                    .setCyl(SchoolConstant.NO_DATA));
            studentScreeningDetailVO.setComputerOptometryData(computerOptometryDataVoList);
            return;
        }

        computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setAxial(getComputerOptometryDataValue(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getAxial))
                .setSph(getComputerOptometryDataValue(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getSph))
                .setCyl(getComputerOptometryDataValue(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getCyl)));
        computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setAxial(getComputerOptometryDataValue(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getAxial))
                .setSph(getComputerOptometryDataValue(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getSph))
                .setCyl(getComputerOptometryDataValue(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getCyl)));

        studentScreeningDetailVO.setComputerOptometryData(computerOptometryDataVoList);
    }


    /**
     * 其它
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setOtherDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        List<OtherDataVO> otherDataVoList = Lists.newArrayList(new OtherDataVO(EyeTypeEnum.LEFT_EYE.getCode()),new OtherDataVO(EyeTypeEnum.RIGHT_EYE.getCode()));
        setSlitLampData(studentScreeningResultDetail, otherDataVoList);
        setOcularInspectionData(studentScreeningResultDetail, otherDataVoList);
        setFundusData(studentScreeningResultDetail, otherDataVoList);
        setOtherEyeDiseases(studentScreeningResultDetail, otherDataVoList);
        studentScreeningDetailVO.setOtherData(otherDataVoList);
    }

    /**
     * 设置其他眼病数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param otherDataVoList 其它数据
     */
    private static void setOtherEyeDiseases(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList) {
        OtherEyeDiseasesDO otherEyeDiseases = studentScreeningResultDetail.getOtherEyeDiseases();
        if (Objects.isNull(otherEyeDiseases)){
            otherDataVoList.get(0).setOtherEyeDiseases(SchoolConstant.NO_DATA);
            otherDataVoList.get(1).setOtherEyeDiseases(SchoolConstant.NO_DATA);
        }else {
            otherDataVoList.get(0).setOtherEyeDiseases(getValueByStringList(otherEyeDiseases.getLeftEyeData(), OtherEyeDiseasesDO.OtherEyeDiseases::getEyeDiseases));
            otherDataVoList.get(1).setOtherEyeDiseases(getValueByStringList(otherEyeDiseases.getRightEyeData(), OtherEyeDiseasesDO.OtherEyeDiseases::getEyeDiseases));
        }
    }

    /**
     * 设置眼底数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param otherDataVoList 其它数据
     */
    private static void setFundusData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList) {
        FundusDataDO fundusData = studentScreeningResultDetail.getFundusData();
        if (Objects.isNull(fundusData)){
            otherDataVoList.get(0).setFundus(SchoolConstant.NO_DATA);
            otherDataVoList.get(1).setFundus(SchoolConstant.NO_DATA);
        }else {
            otherDataVoList.get(0).setFundus(getValueByInteger(fundusData.getLeftEyeData(), FundusDataDO.FundusData::getHasAbnormal));
            otherDataVoList.get(1).setFundus(getValueByInteger(fundusData.getRightEyeData(), FundusDataDO.FundusData::getHasAbnormal));
        }
    }

    /**
     * 设置眼位数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param otherDataVoList 其它数据
     */
    private static void setOcularInspectionData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList) {
        OcularInspectionDataDO ocularInspectionData = studentScreeningResultDetail.getOcularInspectionData();
        if (Objects.isNull(ocularInspectionData)){
            otherDataVoList.get(0).setOcularInspection(SchoolConstant.NO_DATA);
            otherDataVoList.get(1).setOcularInspection(SchoolConstant.NO_DATA);
        }else {
            String value = getValueByBoolean(ocularInspectionData, AbstractDiagnosisResult::isNormal);
            otherDataVoList.get(0).setOcularInspection(value);
            otherDataVoList.get(1).setOcularInspection(value);
        }
    }

    /**
     * 设置裂隙灯检查数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param otherDataVoList 其它数据
     */
    private static void setSlitLampData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList) {
        SlitLampDataDO slitLampData = studentScreeningResultDetail.getSlitLampData();
        if (Objects.isNull(slitLampData)){
            otherDataVoList.get(0).setSlitLamp(SchoolConstant.NO_DATA);
            otherDataVoList.get(1).setSlitLamp(SchoolConstant.NO_DATA);
        }else {
            otherDataVoList.get(0).setSlitLamp(getValueByBoolean(slitLampData.getLeftEyeData(), AbstractDiagnosisResult::isNormal));
            otherDataVoList.get(0).setSlitLamp(getValueByBoolean(slitLampData.getRightEyeData(), AbstractDiagnosisResult::isNormal));
        }
    }


    private static <T>String getValueByBigDecimal(T data, Function<T,BigDecimal> function){
        return Optional.ofNullable(data).map(function).map(BigDecimal::toString).orElse(SchoolConstant.NO_DATA);
    }

    private static <T>String getValueByString(T data, Function<T,String> function){
        return Optional.ofNullable(data).map(function).orElse(SchoolConstant.NO_DATA);
    }

    private static <T>String getComputerOptometryDataValue(T data,Function<T,BigDecimal> function){
        return Optional.ofNullable(data).map(function).map(BigDecimal::toString).orElse(SchoolConstant.NO_DATA);
    }

    private <T>String getValueByBoolean(T data,Function<T,Boolean> function){
        return Optional.ofNullable(data).map(function).map(b-> Objects.equals(Boolean.TRUE,b)?"正常":"异常").orElse(SchoolConstant.NO_DATA);
    }

    private <T>String getValueByInteger(T data,Function<T,Integer> function){
        return Optional.ofNullable(data).map(function).map(b-> Objects.equals(AbstractDiagnosisResult.NORMAL,b)?"正常":"异常").orElse(SchoolConstant.NO_DATA);
    }

    private <T>String getValueByStringList(T data,Function<T,List<String>> function){
        return Optional.ofNullable(data).map(function).map(b-> CollUtil.join(b,",")).orElse(SchoolConstant.NO_DATA);
    }

    /**
     * 身高体重
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setHeightAndWeightDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        HeightAndWeightDataDO heightAndWeightData = studentScreeningResultDetail.getHeightAndWeightData();
        HeightAndWeightDataVO heightAndWeightDataVO = new HeightAndWeightDataVO();
        if (Objects.isNull(heightAndWeightData)){
            heightAndWeightDataVO.setHeight(SchoolConstant.NO_DATA);
            heightAndWeightDataVO.setWeight(SchoolConstant.NO_DATA);
            studentScreeningDetailVO.setHeightAndWeightData(heightAndWeightDataVO);
            return;
        }

        heightAndWeightDataVO.setHeight(getValueByBigDecimal(heightAndWeightData, HeightAndWeightDataDO::getHeight));
        heightAndWeightDataVO.setWeight(getValueByBigDecimal(heightAndWeightData, HeightAndWeightDataDO::getWeight));
        studentScreeningDetailVO.setHeightAndWeightData(heightAndWeightDataVO);
    }


    /**
     * 生物测量
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setBiometricDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        BiometricDataDO biometricData = studentScreeningResultDetail.getBiometricData();
        List<BiometricDataVO> biometricDataVoList = Lists.newArrayList();
        if (Objects.isNull(biometricData)){
            BiometricDataVO leftEyeData = new BiometricDataVO()
                    .setK1(SchoolConstant.NO_DATA).setK2(SchoolConstant.NO_DATA).setAst(SchoolConstant.NO_DATA)
                    .setPd(SchoolConstant.NO_DATA).setWtw(SchoolConstant.NO_DATA).setAl(SchoolConstant.NO_DATA)
                    .setCct(SchoolConstant.NO_DATA).setAd(SchoolConstant.NO_DATA).setLt(SchoolConstant.NO_DATA)
                    .setVt(SchoolConstant.NO_DATA);
            biometricDataVoList.add(leftEyeData.setEyeType(EyeTypeEnum.LEFT_EYE.getCode()));
            BiometricDataVO rightEyeData = ObjectUtil.cloneByStream(leftEyeData);
            biometricDataVoList.add(rightEyeData.setEyeType(EyeTypeEnum.RIGHT_EYE.getCode()));
            studentScreeningDetailVO.setBiometricData(biometricDataVoList);
            return;
        }

        biometricDataVoList.add(new BiometricDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setK1(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getK1))
                .setK2(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getK2))
                .setAst(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getAst))
                .setPd(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getPd))
                .setWtw(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getWtw))
                .setAl(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getAl))
                .setCct(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getCct))
                .setAd(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getAd))
                .setLt(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getLt))
                .setVt(getValueByString(biometricData.getLeftEyeData(), BiometricDataDO.BiometricData::getVt)));
        biometricDataVoList.add(new BiometricDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setK1(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getK1))
                .setK2(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getK2))
                .setAst(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getAst))
                .setPd(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getPd))
                .setWtw(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getWtw))
                .setAl(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getAl))
                .setCct(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getCct))
                .setAd(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getAd))
                .setLt(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getLt))
                .setVt(getValueByString(biometricData.getRightEyeData(), BiometricDataDO.BiometricData::getVt)));

        studentScreeningDetailVO.setBiometricData(biometricDataVoList);
    }

    /**
     * 小瞳验光
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setPupilOptometryDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        PupilOptometryDataDO pupilOptometryData = studentScreeningResultDetail.getPupilOptometryData();
        List<PupilOptometryDataVO> pupilOptometryDataVoList = Lists.newArrayList();
        if (Objects.isNull(pupilOptometryData)){
            pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                    .setAxial(SchoolConstant.NO_DATA)
                    .setSph(SchoolConstant.NO_DATA)
                    .setCyl(SchoolConstant.NO_DATA)
                    .setEyeType(EyeTypeEnum.LEFT_EYE.getCode()));
            pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                    .setAxial(SchoolConstant.NO_DATA)
                    .setSph(SchoolConstant.NO_DATA)
                    .setCyl(SchoolConstant.NO_DATA)
                    .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode()));
            studentScreeningDetailVO.setPupilOptometryData(pupilOptometryDataVoList);
            return;
        }

        pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setAxial(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getAxial))
                .setSph(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getSph))
                .setCyl(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getCyl)));
        pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setAxial(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getAxial))
                .setSph(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getSph))
                .setCyl(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getCyl)));

        studentScreeningDetailVO.setPupilOptometryData(pupilOptometryDataVoList);
    }

    /**
     * 眼压
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private static void setEyePressureDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        EyePressureDataDO eyePressureData = studentScreeningResultDetail.getEyePressureData();
        List<EyePressureDataVO> eyePressureDataVoList = Lists.newArrayList();
        if (Objects.isNull(eyePressureData)){
            eyePressureDataVoList.add(new EyePressureDataVO()
                    .setPressure(SchoolConstant.NO_DATA)
                    .setEyeType(EyeTypeEnum.LEFT_EYE.getCode()));
            eyePressureDataVoList.add(new EyePressureDataVO()
                    .setPressure(SchoolConstant.NO_DATA)
                    .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode()));
            studentScreeningDetailVO.setEyePressureData(eyePressureDataVoList);
            return;
        }

        eyePressureDataVoList.add(new EyePressureDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setPressure(getValueByBigDecimal(eyePressureData.getLeftEyeData(), EyePressureDataDO.EyePressureData::getPressure)));
        eyePressureDataVoList.add(new EyePressureDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setPressure(getValueByBigDecimal(eyePressureData.getRightEyeData(), EyePressureDataDO.EyePressureData::getPressure)));
        studentScreeningDetailVO.setEyePressureData(eyePressureDataVoList);
    }
}
