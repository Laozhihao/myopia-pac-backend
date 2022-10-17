package com.wupol.myopia.business.aggregation.student.domain.builder;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.aggregation.student.constant.VisionScreeningConst;
import com.wupol.myopia.business.aggregation.student.domain.vo.VisionInfoVO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.MaskUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生业务
 *
 * @author hang.yuan 2022/9/29 13:09
 */
@UtilityClass
public class StudentBizBuilder {

    /**
     * 获取学生编号
     *
     * @param studentPlans 筛查学生计划
     * @return 编号
     */
    public List<Long> getScreeningCodesByPlan(List<ScreeningPlanSchoolStudent> studentPlans) {
        if (CollectionUtils.isEmpty(studentPlans)) {
            return Collections.emptyList();
        }
        return studentPlans.stream()
                .map(ScreeningPlanSchoolStudent::getScreeningCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取筛查计划机构唯一key
     * @param planId
     * @param screeningOrgId
     */
    public static String getPlanOrgKey(Integer planId,Integer screeningOrgId){
        return planId + StrUtil.UNDERLINE+screeningOrgId;
    }


    /**
     * 构建学生筛查档案
     *
     * @param screeningOrg
     * @param statMap
     * @param screeningPlanSchoolStudentMap
     * @param gender
     * @param result
     */
    public StudentScreeningResultItemsDTO builderStudentScreeningResultItemsDTO(TwoTuple<Map<Integer, ScreeningOrganization>, Map<Integer, School>> screeningOrg, Map<Integer, StatConclusion> statMap,
                                                                                Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap,
                                                                                Integer gender, VisionScreeningResultDTO result,Map<String, Integer> screeningOrgTypeMap) {
        StudentScreeningResultItemsDTO item = new StudentScreeningResultItemsDTO();
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentMap.getOrDefault(result.getScreeningPlanSchoolStudentId(), new ScreeningPlanSchoolStudent());
        StatConclusion statConclusion = statMap.getOrDefault(result.getId(), new StatConclusion());

        String screeningOrgName = StrUtil.EMPTY;
        Integer type = screeningOrgTypeMap.get(getPlanOrgKey(result.getPlanId(), result.getScreeningOrgId()));
        if (Objects.equals(type, ScreeningOrgTypeEnum.ORG.getType())){
            screeningOrgName = Optional.ofNullable(screeningOrg.getFirst().get(result.getScreeningOrgId())).map(ScreeningOrganization::getName).orElse(StrUtil.EMPTY);
        }else if (Objects.equals(type, ScreeningOrgTypeEnum.SCHOOL.getType())){
            screeningOrgName = Optional.ofNullable(screeningOrg.getSecond().get(result.getScreeningOrgId())).map(School::getName).orElse(StrUtil.EMPTY);
        }


        // 设置其他
        item.setScreeningTitle(result.getPlanTitle())
            .setScreeningDate(result.getUpdateTime())
            // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样的
            .setGlassesTypeDes(Optional.ofNullable(result.getVisionData()).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).map(WearingGlassesSituation::getType).orElse(null))
            .setResultId(result.getId())
            .setIsDoubleScreen(result.getIsDoubleScreen())
            .setOtherEyeDiseases(getOtherEyeDiseasesList(result))
            .setPlanId(result.getPlanId())
            .setHasScreening(ObjectUtils.anyNotNull(result.getVisionData(), result.getComputerOptometry(), result.getBiometricData(), result.getOtherEyeDiseases()))
            .setPlanStudentId(result.getScreeningPlanSchoolStudentId())
            .setScreeningCode(planSchoolStudent.getScreeningCode())
            .setClassId(planSchoolStudent.getClassId())
            // 设置预警、近视、远视、散光等级
            .setWarningLevel(statConclusion.getWarningLevel())
            .setMyopiaLevel(statConclusion.getMyopiaLevel())
            .setHyperopiaLevel(statConclusion.getHyperopiaLevel())
            .setAstigmatismLevel(statConclusion.getAstigmatismLevel())
            //筛查类型
            .setScreeningType(result.getScreeningType())
            //筛查机构名称
            .setScreeningOrgName(screeningOrgName)
            //设置学生性别
            .setGender(gender)
            //设置常见病ID
            .setCommonDiseasesCode(screeningPlanSchoolStudentMap.get(result.getScreeningPlanSchoolStudentId()).getCommonDiseaseId())
            .setReleaseStatus(result.getReleaseStatus());
        return item;
    }

    /**
     * 获取两眼别的病变
     *
     * @param visionScreeningResult 视力筛查结果
     * @return List<String>
     */
    public List<String> getOtherEyeDiseasesList(VisionScreeningResult visionScreeningResult) {
        List<String> emptyList = new ArrayList<>();
        OtherEyeDiseasesDO otherEyeDiseases = visionScreeningResult.getOtherEyeDiseases();
        if (Objects.isNull(otherEyeDiseases)) {
            return emptyList;
        }
        List<String> leftEyeDate = Objects.nonNull(otherEyeDiseases.getLeftEyeData()) ? otherEyeDiseases.getLeftEyeData().getEyeDiseases() : emptyList;
        List<String> rightEyeDate = Objects.nonNull(otherEyeDiseases.getRightEyeData()) ? otherEyeDiseases.getRightEyeData().getEyeDiseases() : emptyList;
        return ListUtils.sum(leftEyeDate, rightEyeDate);
    }

    /**
     * 设置学生筛查档案信息
     *
     * @param item
     * @param screeningInfoDTO
     * @param templateId
     */
    public void setStudentScreeningResultItemInfo(StudentScreeningResultItemsDTO item, ScreeningInfoDTO screeningInfoDTO, Integer templateId) {
        item.setDetails(screeningInfoDTO)
                .setTemplateId(templateId);
    }

    /**
     * 设置常见病信息
     * @param result
     * @return
     */
    public CommonDiseasesDTO getCommonDiseases(VisionScreeningResult result) {
        CommonDiseasesDTO commonDiseases = new  CommonDiseasesDTO();
        BeanUtils.copyProperties(result, commonDiseases);
        commonDiseases.setSaprodontiaStat(SaprodontiaStat.parseFromSaprodontiaDataDO(result.getSaprodontiaData()));
        return commonDiseases;
    }

    /**
     * 封装结果
     *
     * @param result 结果表
     * @return 详情列表
     */
    public List<StudentResultDetailsDTO> packageDTO(VisionScreeningResult result,Integer clientId) {

        // 设置左眼
        StudentResultDetailsDTO leftDetails = new StudentResultDetailsDTO();
        leftDetails.setLateriality(CommonConst.LEFT_EYE);
        //设置右眼
        StudentResultDetailsDTO rightDetails = new StudentResultDetailsDTO();
        rightDetails.setLateriality(CommonConst.RIGHT_EYE);

        if (Objects.nonNull(result.getVisionData())) {
            // 视力检查结果
            packageVisionResult(result.getVisionData(), leftDetails, rightDetails);
        }
        if (Objects.nonNull(result.getComputerOptometry())) {
            // 电脑验光
            packageComputerOptometryResult(result.getComputerOptometry(), leftDetails, rightDetails);
        }
        if (Objects.nonNull(result.getBiometricData())) {
            // 生物测量
            packageBiometricDataResult(result, leftDetails, rightDetails);
        }
        if (Objects.nonNull(result.getOtherEyeDiseases())) {
            // 眼部疾病
            packageOtherEyeDiseasesResult(result, leftDetails, rightDetails);
        }

        if (Objects.nonNull(result.getOtherEyeDiseases())) {
            // 眼部疾病
            packageOtherEyeDiseasesResult(result, leftDetails, rightDetails);
        }

        //处理学校学生数据
        if(Objects.equals(clientId, SystemCode.SCHOOL_CLIENT.getCode())){
            leftDetails.setClientId(clientId);
            rightDetails.setClientId(clientId);
            processSchoolClientData(leftDetails,rightDetails);
        }

        return Lists.newArrayList(rightDetails, leftDetails);
    }

    /**
     * 处理学校端的数据
     * @param leftDetails
     * @param rightDetails
     */
    private static void processSchoolClientData(StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        String formatStr = "度数：右眼：%s、左眼：%s";
        if (Objects.equals(leftDetails.getGlassesType(), WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY)){
            String leftOkDegree = getBigDecimalStr(BigDecimalUtil.getBigDecimalByFormat(leftDetails.getOkDegree(), 2));
            String rightOkDegree = getBigDecimalStr(BigDecimalUtil.getBigDecimalByFormat(rightDetails.getOkDegree(), 2));
            String okDegreeDesc = String.format(formatStr, rightOkDegree, leftOkDegree);
            rightDetails.setOkDegreeDesc(okDegreeDesc);
            leftDetails.setOkDegreeDesc(okDegreeDesc);
        }
    }

    private String getBigDecimalStr(BigDecimal  bigDecimal){
        if (Objects.isNull(bigDecimal)){
            return "--";
        }
        String value = bigDecimal.toString();
        return BigDecimalUtil.moreThan(bigDecimal,"0.00")? "+"+ value : value;
    }

    /**
     * 封装视力检查结果
     *
     * @param visionData   原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    public void packageVisionResult(VisionDataDO visionData, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼-视力检查结果
        VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
        leftDetails.setGlassesType(leftEyeData.getGlassesType());
        leftDetails.setGlassesTypeDes(WearingGlassesSituation.getType(leftEyeData.getGlassesType()));
        leftDetails.setCorrectedVision(leftEyeData.getCorrectedVision());
        leftDetails.setNakedVision(leftEyeData.getNakedVision());
        leftDetails.setOkDegree(leftEyeData.getOkDegree());

        // 右眼-视力检查结果
        VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
        rightDetails.setGlassesType(rightEyeData.getGlassesType());
        rightDetails.setGlassesTypeDes(WearingGlassesSituation.getType(rightEyeData.getGlassesType()));
        rightDetails.setCorrectedVision(rightEyeData.getCorrectedVision());
        rightDetails.setNakedVision(rightEyeData.getNakedVision());
        rightDetails.setOkDegree(rightEyeData.getOkDegree());
    }

    /**
     * 封装电脑验光
     *
     * @param computerOptometry 电脑验光筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    public void packageComputerOptometryResult(ComputerOptometryDO computerOptometry, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--电脑验光
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
        leftDetails.setAxial(leftEyeData.getAxial());
        leftDetails.setSe(StatUtil.getSphericalEquivalent(leftEyeData.getSph(), leftEyeData.getCyl()));
        leftDetails.setCyl(leftEyeData.getCyl());
        leftDetails.setSph(leftEyeData.getSph());

        // 左眼--电脑验光
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();
        rightDetails.setAxial(rightEyeData.getAxial());
        rightDetails.setSe(StatUtil.getSphericalEquivalent(rightEyeData.getSph(), rightEyeData.getCyl()));
        rightDetails.setCyl(rightEyeData.getCyl());
        rightDetails.setSph(rightEyeData.getSph());
    }

    /**
     * 封装生物测量结果
     *
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    public void packageBiometricDataResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--生物测量
        leftDetails.setAd(result.getBiometricData().getLeftEyeData().getAd());
        leftDetails.setAl(result.getBiometricData().getLeftEyeData().getAl());
        leftDetails.setCct(result.getBiometricData().getLeftEyeData().getCct());
        leftDetails.setLt(result.getBiometricData().getLeftEyeData().getLt());
        leftDetails.setWtw(result.getBiometricData().getLeftEyeData().getWtw());

        // 右眼--生物测量
        rightDetails.setAd(result.getBiometricData().getRightEyeData().getAd());
        rightDetails.setAl(result.getBiometricData().getRightEyeData().getAl());
        rightDetails.setCct(result.getBiometricData().getRightEyeData().getCct());
        rightDetails.setLt(result.getBiometricData().getRightEyeData().getLt());
        rightDetails.setWtw(result.getBiometricData().getRightEyeData().getWtw());
    }

    /**
     * 封装眼部疾病结果
     *
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageOtherEyeDiseasesResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--眼部疾病
        leftDetails.setEyeDiseases(getEyeDiseases(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases(), result.getSystemicDiseaseSymptom()));
        // 右眼--眼部疾病
        rightDetails.setEyeDiseases(getEyeDiseases(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases(), result.getSystemicDiseaseSymptom()));
    }

    /**
     * 获取疾病描述
     *
     * @param eyeDiseases            眼部疾病
     * @param systemicDiseaseSymptom 全身病
     * @return 疾病描述
     */
    private String getEyeDiseases(List<String> eyeDiseases, String systemicDiseaseSymptom) {
        if (CollectionUtils.isEmpty(eyeDiseases)) {
            return systemicDiseaseSymptom;
        }
        if (StringUtils.isEmpty(systemicDiseaseSymptom)) {
            return String.join(CommonConst.CH_COMMA, eyeDiseases);
        }
        return String.join(CommonConst.CH_COMMA, eyeDiseases) + CommonConst.CH_COMMA + systemicDiseaseSymptom;
    }

    /**
     * 设置学生基本信息
     *
     * @param studentInfo 学生
     * @return 学生档案卡基本信息
     */
    public CardInfoVO getCardInfo(StudentDTO studentInfo, ThreeTuple<String,String,String> districtInfo) {
        CardInfoVO cardInfoVO = new CardInfoVO();
        cardInfoVO.setName(studentInfo.getName());
        cardInfoVO.setBirthday(studentInfo.getBirthday());
        cardInfoVO.setIdCard(StringUtils.isNotBlank(studentInfo.getIdCard()) ? MaskUtil.maskIdCard(studentInfo.getIdCard()) : MaskUtil.maskPassport(studentInfo.getPassport()));
        cardInfoVO.setGender(studentInfo.getGender());
        cardInfoVO.setAge(DateUtil.ageOfNow(studentInfo.getBirthday()));
        cardInfoVO.setSno(studentInfo.getSno());
        cardInfoVO.setParentPhone(studentInfo.getParentPhone());
        cardInfoVO.setSchoolName(studentInfo.getSchoolName());
        cardInfoVO.setSchoolId(studentInfo.getSchoolId());
        cardInfoVO.setClassName(studentInfo.getClassName());
        cardInfoVO.setGradeName(studentInfo.getGradeName());
        cardInfoVO.setDistrictName(districtInfo.getFirst());
        cardInfoVO.setNation(studentInfo.getNation());
        cardInfoVO.setNationDesc(NationEnum.getName(studentInfo.getNation()));
        cardInfoVO.setPassport(studentInfo.getPassport());
        cardInfoVO.setSchoolType(SchoolAge.get(studentInfo.getGradeType()).getType());
        cardInfoVO.setCityDesc(districtInfo.getSecond());
        cardInfoVO.setAreaDesc(districtInfo.getThird());
        return cardInfoVO;
    }

    /**
     * 统计学生配合程度
     *
     * @param result 筛查结果
     * @return 统计
     */
    public Integer getCountNotCooperate(VisionScreeningResult result) {
        int total = 0;

        // 02
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(visionData.getIsCooperative())) {
            total++;
        }
        // 03
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        if (Objects.nonNull(computerOptometry) && VisionScreeningConst.IS_NOT_COOPERATE.equals(computerOptometry.getIsCooperative())) {
            total++;
        }
        // 05
        PupilOptometryDataDO pupilOptometryData = result.getPupilOptometryData();
        if (Objects.nonNull(pupilOptometryData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(pupilOptometryData.getIsCooperative())) {
            total++;
        }
        // 06
        BiometricDataDO biometricData = result.getBiometricData();
        if (Objects.nonNull(biometricData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(biometricData.getIsCooperative())) {
            total++;
        }
        // 07
        EyePressureDataDO eyePressureData = result.getEyePressureData();
        if (Objects.nonNull(eyePressureData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(eyePressureData.getIsCooperative())) {
            total++;
        }

        // 剩下三个特殊处理，只有一个有，就+1
        boolean spFlag = false;
        // 08
        FundusDataDO fundusData = result.getFundusData();
        if (Objects.nonNull(fundusData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(fundusData.getIsCooperative())) {
            spFlag = true;
        }
        // 04
        SlitLampDataDO slitLampData = result.getSlitLampData();
        if (Objects.nonNull(slitLampData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(slitLampData.getIsCooperative())) {
            spFlag = true;
        }
        // 01
        OcularInspectionDataDO ocularInspectionData = result.getOcularInspectionData();
        if (Objects.nonNull(ocularInspectionData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(ocularInspectionData.getIsCooperative())) {
            spFlag = true;
        }
        if (spFlag) {
            total++;
        }
        // 返回total
        return total;
    }

    /**
     * 青少年近视筛查结果
     *
     * @param result 筛查结果
     * @return 儿童青少年近视筛查结果记录表
     */
    public MyopiaScreeningResultCardDetail packageMyopiaScreeningResultCardDetail(VisionScreeningResult result, StudentCardResponseVO responseDTO){
        MyopiaScreeningResultCardDetail details = new MyopiaScreeningResultCardDetail();
        if (Objects.isNull(result)) {
            return details;
        }

        BeanUtils.copyProperties(result, details);

        details.setVisionResults(setVisionResult(result.getVisionData()));
        details.setRefractoryResults(setRefractoryResults(result.getComputerOptometry()));
        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        setClassType(result, details);

        String ageInfo = com.wupol.myopia.base.util.DateUtil.getAgeInfo(responseDTO.getInfo().getBirthday(),new Date());
        details.setAgeInfo(ageInfo);

        return details;
    }

    /**
     * 设置视力检查结果
     *
     * @param result 筛查结果
     * @return 视力检查结果List
     */
    public List<CardDetailsVO.VisionResult> setVisionResult(VisionDataDO result) {
        CardDetailsVO.VisionResult left = new CardDetailsVO.VisionResult();
        CardDetailsVO.VisionResult right = new CardDetailsVO.VisionResult();

        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);
        if (null != result) {
            // 左眼
            left.setCorrectedVision(result.getLeftEyeData().getCorrectedVision());
            left.setNakedVision(result.getLeftEyeData().getNakedVision());

            // 右眼
            right.setCorrectedVision(result.getRightEyeData().getCorrectedVision());
            right.setNakedVision(result.getRightEyeData().getNakedVision());
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置验光仪检查结果
     *
     * @param result 筛查结果
     * @return 验光仪检查结果列表
     */
    public List<CardDetailsVO.RefractoryResult> setRefractoryResults(ComputerOptometryDO result) {
        CardDetailsVO.RefractoryResult left = new CardDetailsVO.RefractoryResult();
        CardDetailsVO.RefractoryResult right = new CardDetailsVO.RefractoryResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);

        if (null != result) {
            // 左眼
            left.setAxial(result.getLeftEyeData().getAxial());
            left.setSph(result.getLeftEyeData().getSph());
            left.setCyl(result.getLeftEyeData().getCyl());

            // 右眼
            right.setAxial(result.getRightEyeData().getAxial());
            right.setSph(result.getRightEyeData().getSph());
            right.setCyl(result.getRightEyeData().getCyl());
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置戴镜类型
     * @param result
     * @param details
     */
    private void setClassType(VisionScreeningResult result, MyopiaScreeningResultCardDetail details) {
        CardDetailsVO.GlassesTypeObj glassesTypeObj = new CardDetailsVO.GlassesTypeObj();
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData)) {
            glassesTypeObj.setType(visionData.getLeftEyeData().getGlassesType());
            details.setGlassesTypeObj(glassesTypeObj);
        }
        details.setGlassesTypeObj(glassesTypeObj);
    }

    /**
     * 设置串镜检查结果
     *
     * @param result 数据
     * @param age    年龄
     * @return 串镜检查结果列表
     */
    public List<CardDetailsVO.CrossMirrorResult> setCrossMirrorResults(VisionScreeningResult result, Integer age) {
        CardDetailsVO.CrossMirrorResult left = new CardDetailsVO.CrossMirrorResult();
        CardDetailsVO.CrossMirrorResult right = new CardDetailsVO.CrossMirrorResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);

        if (null == result || null == result.getComputerOptometry()) {
            return Lists.newArrayList(right, left);
        }
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        VisionDataDO visionData = result.getVisionData();

        // 左眼
        if (Objects.nonNull(visionData) && Objects.nonNull(visionData.getLeftEyeData()) && Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getLeftEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getLeftEyeData().getSph(), computerOptometry.getLeftEyeData().getCyl(), visionData.getLeftEyeData().getNakedVision())) {
            left.setMyopia(StatUtil.isMyopia(computerOptometry.getLeftEyeData().getSph(), computerOptometry.getLeftEyeData().getCyl(), age, visionData.getLeftEyeData().getNakedVision()));
            left.setFarsightedness(StatUtil.isHyperopia(computerOptometry.getLeftEyeData().getSph().floatValue(), computerOptometry.getLeftEyeData().getCyl().floatValue(), age));
        }

        // 右眼
        if (Objects.nonNull(visionData) && Objects.nonNull(visionData.getRightEyeData()) && Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getRightEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getRightEyeData().getSph(), computerOptometry.getRightEyeData().getCyl(), visionData.getRightEyeData().getNakedVision())) {
            right.setMyopia(StatUtil.isMyopia(computerOptometry.getRightEyeData().getSph(), computerOptometry.getRightEyeData().getCyl(), age, visionData.getRightEyeData().getNakedVision()));
            right.setFarsightedness(StatUtil.isHyperopia(computerOptometry.getRightEyeData().getSph().floatValue(), computerOptometry.getRightEyeData().getCyl().floatValue(), age));
        }

        if (null != result.getOtherEyeDiseases() && !CollectionUtils.isEmpty(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases())) {
            left.setOther(true);
        }
        if (null != result.getOtherEyeDiseases() && !CollectionUtils.isEmpty(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases())) {
            right.setOther(true);
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置视力信息
     *
     * @param result 筛查结果
     * @param age    学生年龄
     * @return 档案卡视力详情
     */
    public CardDetailsVO packageCardDetail(VisionScreeningResult result, Integer age) {
        CardDetailsVO details = new CardDetailsVO();

        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        CardDetailsVO.GlassesTypeObj glassesTypeObj = new CardDetailsVO.GlassesTypeObj();
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData)) {
            glassesTypeObj.setType(visionData.getLeftEyeData().getGlassesType());
            details.setGlassesTypeObj(glassesTypeObj);
        }

        details.setVisionResults(StudentBizBuilder.setVisionResult(visionData));
        details.setRefractoryResults(StudentBizBuilder.setRefractoryResults(result.getComputerOptometry()));
        details.setCrossMirrorResults(StudentBizBuilder.setCrossMirrorResults(result, age));
        details.setEyeDiseasesResult(setEyeDiseasesResult(result.getOtherEyeDiseases()));
        return details;
    }

    /**
     * 其他眼部疾病
     *
     * @param result 其他眼部疾病
     * @return 其他眼病List
     */
    private List<CardDetailsVO.EyeDiseasesResult> setEyeDiseasesResult(OtherEyeDiseasesDO result) {
        CardDetailsVO.EyeDiseasesResult left = new CardDetailsVO.EyeDiseasesResult();
        CardDetailsVO.EyeDiseasesResult right = new CardDetailsVO.EyeDiseasesResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);
        if (null != result) {
            left.setEyeDiseases(result.getLeftEyeData().getEyeDiseases());
            right.setEyeDiseases(result.getRightEyeData().getEyeDiseases());
        } else {
            left.setEyeDiseases(new ArrayList<>());
            right.setEyeDiseases(new ArrayList<>());
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 获取海南学生档案卡基本信息
     * @param planSchoolStudent
     * @param cardInfoVO
     * @param districtInfo
     */
    public CardInfoVO getHeiNanCardInfo(ScreeningPlanSchoolStudent planSchoolStudent, CardInfoVO cardInfoVO,ThreeTuple<String, String, String> districtInfo) {
        cardInfoVO.setName(planSchoolStudent.getStudentName());
        cardInfoVO.setBirthday(planSchoolStudent.getBirthday());
        cardInfoVO.setIdCard(StringUtils.isNotBlank(planSchoolStudent.getIdCard()) ? MaskUtil.maskIdCard(planSchoolStudent.getIdCard()) : MaskUtil.maskPassport(planSchoolStudent.getPassport()));
        cardInfoVO.setGender(planSchoolStudent.getGender());
        cardInfoVO.setAge(DateUtil.ageOfNow(planSchoolStudent.getBirthday()));
        cardInfoVO.setSno(planSchoolStudent.getStudentNo());
        cardInfoVO.setParentPhone(planSchoolStudent.getParentPhone());
        cardInfoVO.setSchoolName(planSchoolStudent.getSchoolName());
        cardInfoVO.setSchoolId(planSchoolStudent.getSchoolId());
        cardInfoVO.setClassName(planSchoolStudent.getClassName());
        cardInfoVO.setGradeName(planSchoolStudent.getGradeName());
        cardInfoVO.setDistrictName(districtInfo.getFirst());
        cardInfoVO.setNation(planSchoolStudent.getNation());
        cardInfoVO.setNationDesc(NationEnum.getName(planSchoolStudent.getNation()));
        cardInfoVO.setPassport(planSchoolStudent.getPassport());
        cardInfoVO.setSchoolType(SchoolAge.get(planSchoolStudent.getGradeType()).type);
        cardInfoVO.setCityDesc(districtInfo.getSecond());
        cardInfoVO.setAreaDesc(districtInfo.getThird());
        return cardInfoVO;
    }

    /**
     * 封装海南档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param age                   年轻
     * @return HaiNanCardDetail
     */
    public HaiNanCardDetail packageHaiNanCardDetail(VisionScreeningResult visionScreeningResult, Integer age) {
        HaiNanCardDetail cardDetail = new HaiNanCardDetail();
        if (Objects.isNull(visionScreeningResult)) {
            return cardDetail;
        }
        BeanUtils.copyProperties(visionScreeningResult, cardDetail);
        cardDetail.setVisionDataDO(visionScreeningResult.getVisionData());
        cardDetail.setRemark(Objects.nonNull(visionScreeningResult.getFundusData()) ? visionScreeningResult.getFundusData().getRemark() : "");
        // 其他眼部疾病
        List<String> otherEyeDiseasesList = StudentBizBuilder.getOtherEyeDiseasesList(visionScreeningResult);

        // 其他眼病,过滤掉五种特殊情况
        cardDetail.setOtherEyeDiseases(ListUtils.subtract(otherEyeDiseasesList, eyeDiseases()));
        cardDetail.setResultOtherEyeDiseases(visionScreeningResult.getOtherEyeDiseases());
        cardDetail.setEyeDiseases(ListUtils.retainAll(eyeDiseases(), otherEyeDiseasesList));
        // 眼斜
        cardDetail.setSquint(getSquintList(otherEyeDiseasesList));
        // 设置是否近视、远视
        setMyopiaAndFarsightedness(visionScreeningResult, age, cardDetail);

        // 如果近视和远视，显示屈光不正
        if ((Objects.nonNull(cardDetail.getIsMyopia()) && cardDetail.getIsMyopia())
                || (Objects.nonNull(cardDetail.getIsHyperopia()) && cardDetail.getIsHyperopia())) {
            cardDetail.setIsRefractiveError(true);
        }
        return cardDetail;
    }

    /**
     * 设置屈光不正信息
     *
     * @param isRefractiveError
     * @param cardDetail
     * @param visionScreeningResult
     */
    public void setRefractiveErrorInfo(Boolean isRefractiveError, HaiNanCardDetail cardDetail,VisionScreeningResult visionScreeningResult) {
        // 其他眼部疾病
        List<String> otherEyeDiseasesList = StudentBizBuilder.getOtherEyeDiseasesList(visionScreeningResult);

        // 设置屈光不正信息
        // isRefractiveError为Null不展示
        if (Objects.nonNull(isRefractiveError)) {
            // 是否曲光不正
            cardDetail.setIsRefractiveError(isRefractiveError);
            // 是否正常
            boolean isNormal = CollectionUtils.isEmpty(otherEyeDiseasesList);
            cardDetail.setIsNormal(!isRefractiveError && isNormal);
            if (Objects.equals(cardDetail.getIsNormal(),Boolean.TRUE)) {
                // 正常就不显示近、远视
                cardDetail.setIsMyopia(null);
                cardDetail.setIsHyperopia(null);
            }
        }
    }

    /**
     * 五种特殊眼病
     *
     * @return List<String>
     */
    private List<String> eyeDiseases() {
        return Lists.newArrayList("眼球震颤", "弱视待排查", "高眼压", "青光眼待排", "大视杯");
    }

    /**
     * 获取斜视疾病
     *
     * @param otherEyeDiseasesList 其他眼病
     * @return 斜视疾病
     */
    private List<String> getSquintList(List<String> otherEyeDiseasesList) {
        if (CollectionUtils.isEmpty(otherEyeDiseasesList)) {
            return new ArrayList<>();
        }
        return ListUtils.retainAll(Lists.newArrayList("内显斜", "外显斜", "内隐斜", "外隐斜", "垂直斜视"), otherEyeDiseasesList);
    }

    /**
     * 设置是否近视、远视
     *
     * @param visionScreeningResult 数据
     * @param age                   年龄
     * @param cardDetail            档案卡
     */
    private void setMyopiaAndFarsightedness(VisionScreeningResult visionScreeningResult, Integer age, HaiNanCardDetail cardDetail) {
        ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
        VisionDataDO visionData = visionScreeningResult.getVisionData();

        if (ObjectsUtil.hasNull(computerOptometry, visionData)
                || !computerOptometry.valid()
                || !visionData.validNakedVision()) {
            return;
        }
        BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
        BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
        BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
        BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();

        BigDecimal leftNakedVision = visionData.getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = visionData.getRightEyeData().getNakedVision();
        // 是否近视
        Boolean leftMyopia = StatUtil.isMyopia(leftSph, leftCyl, age, leftNakedVision);
        Boolean rightMyopia = StatUtil.isMyopia(rightSph, rightCyl, age, rightNakedVision);
        cardDetail.setIsMyopia(StatUtil.getIsExist(leftMyopia,rightMyopia));

        // 是否远视
        Boolean leftHyperopia = StatUtil.isHyperopia(leftSph, leftCyl, age);
        Boolean rightHyperopia = StatUtil.isHyperopia(rightSph, rightCyl, age);
        cardDetail.setIsHyperopia(StatUtil.getIsExist(leftHyperopia,rightHyperopia));

    }

    /**
     * 获取是否屈光不正
     * @param cardDetail
     * @param visionScreeningResult
     * @param age
     * @param status
     */
    public Boolean getIsRefractiveError(HaiNanCardDetail cardDetail, VisionScreeningResult visionScreeningResult, Integer age, Integer status) {
        // 幼儿园判断
        VisionDataDO visionData = visionScreeningResult.getVisionData();
        if (status == 0) {
            OcularInspectionDataDO ocularInspectionData = visionScreeningResult.getOcularInspectionData();
            // 视力检查和33cm眼位都正常，为非屈光不正
            if (ObjectsUtil.allNotNull(visionData, ocularInspectionData)
                    && visionData.getDiagnosis().equals(AbstractDiagnosisResult.NORMAL)
                    && ocularInspectionData.getDiagnosis().equals(AbstractDiagnosisResult.NORMAL)) {
                return false;
            }
        }
        // 如果小瞳验光和屈光度数据都没有，则屈光正常
        PupilOptometryDataDO pupilOptometryData = visionScreeningResult.getPupilOptometryData();
        ComputerOptometryDO computerOptometryDO = visionScreeningResult.getComputerOptometry();
        if (ObjectsUtil.allNull(pupilOptometryData, computerOptometryDO)) {
            return null;
        }
        // 获取视力信息，优先取小瞳验光的数据
        TwoTuple<VisionInfoVO, VisionInfoVO> visionInfo = Objects.nonNull(pupilOptometryData) ?
                getVisionInfoByPupilOptometryData(pupilOptometryData, age, visionData) : getVisionInfoByComputerOptometryData(computerOptometryDO, age, visionData);
        VisionInfoVO leftEye = visionInfo.getFirst();
        VisionInfoVO rightEye = visionInfo.getSecond();
        // 是否屈光不正
        Boolean isRefractiveError = isRefractiveError(leftEye, rightEye);
        if (Objects.isNull(isRefractiveError)) {
            return isRefractiveError;
        }
        // 设置近视、远视、散光
        if (Objects.equals(isRefractiveError,Boolean.TRUE) && Objects.nonNull(leftEye)) {
            cardDetail.setLeftMyopiaInfo(leftEye.getMyopiaLevel());
            cardDetail.setLeftFarsightednessInfo(leftEye.getHyperopiaLevel());
            cardDetail.setLeftAstigmatismInfo(leftEye.getAstigmatism());
        }
        if (Objects.equals(isRefractiveError,Boolean.TRUE) && Objects.nonNull(rightEye)) {
            cardDetail.setRightMyopiaInfo(rightEye.getMyopiaLevel());
            cardDetail.setRightFarsightednessInfo(rightEye.getHyperopiaLevel());
            cardDetail.setRightAstigmatismInfo(rightEye.getAstigmatism());
        }

        return isRefractiveError;
    }

    /**
     * 获取近视情况
     *
     * @param pupilOptometryData 电脑验光数据
     * @param age                年龄
     * @param visionDataDO       视力检查结果
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByPupilOptometryData(PupilOptometryDataDO pupilOptometryData,
                                                                                   Integer age, VisionDataDO visionDataDO) {
        if (ObjectsUtil.hasNull(pupilOptometryData, visionDataDO)) {
            return new TwoTuple<>();
        }

        PupilOptometryDataDO.PupilOptometryData leftEyeData = pupilOptometryData.getLeftEyeData();
        PupilOptometryDataDO.PupilOptometryData rightEyeData = pupilOptometryData.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ? new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age, visionDataDO.getLeftEyeData().getNakedVision());
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ? new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age, visionDataDO.getRightEyeData().getNakedVision());
        return new TwoTuple<>(leftVision, rightVision);
    }

    /**
     * 获取近视预警级别
     *
     * @param sph         球镜
     * @param cyl         柱镜
     * @param nakedVision 裸眼视力
     * @return VisionInfoVO
     */
    private VisionInfoVO getMyopiaLevel(BigDecimal sph, BigDecimal cyl, Integer age, BigDecimal nakedVision) {
        VisionInfoVO visionInfoVO = new VisionInfoVO();
        if (ObjectsUtil.allNotNull(sph, cyl)) {
            // 近视
            MyopiaLevelEnum myopiaWarningLevel = null;
            if (Objects.nonNull(nakedVision)) {
                if ((age < 6 && nakedVision.compareTo(new BigDecimal("4.9")) < 0) || (age >= 6 && nakedVision.compareTo(new BigDecimal("5.0")) < 0)) {
                    myopiaWarningLevel = StatUtil.getMyopiaLevel(sph, cyl);
                }
            }
            // 远视
            HyperopiaLevelEnum farsightednessWarningLevel = StatUtil.getHyperopiaLevel(sph.floatValue(), cyl.floatValue(), age);
            visionInfoVO.setMyopiaLevel(Objects.nonNull(myopiaWarningLevel) ? myopiaWarningLevel.code : null);
            visionInfoVO.setHyperopiaLevel(Objects.nonNull(farsightednessWarningLevel) ? farsightednessWarningLevel.code : null);
        }
        // 散光
        visionInfoVO.setAstigmatism(Objects.nonNull(cyl) && cyl.abs().compareTo(new BigDecimal("0.5")) >= 0);
        return visionInfoVO;
    }

    /**
     * 获取近视情况
     *
     * @param computerOptometry 电脑验光数据
     * @param age               年龄
     * @param visionDataDO      视力检查结果
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByComputerOptometryData(ComputerOptometryDO computerOptometry,
                                                                                      Integer age, VisionDataDO visionDataDO) {
        if (Objects.isNull(computerOptometry) || Objects.isNull(visionDataDO)) {
            return new TwoTuple<>();
        }
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ? new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age, visionDataDO.getLeftEyeData().getNakedVision());
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ? new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age, visionDataDO.getRightEyeData().getNakedVision());
        return new TwoTuple<>(leftVision, rightVision);
    }

    /**
     * 是否屈光不正
     *
     * @param leftEye  左眼数据
     * @param rightEye 右眼数据
     * @return 是否屈光不正
     */
    private Boolean isRefractiveError(VisionInfoVO leftEye, VisionInfoVO rightEye) {
        if (ObjectsUtil.allNull(leftEye, rightEye)) {
            return null;
        }
        if ((Objects.nonNull(leftEye.getMyopiaLevel()) && leftEye.getMyopiaLevel() >= MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)
                || (Objects.nonNull(rightEye.getMyopiaLevel()) && rightEye.getMyopiaLevel() > MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)) {
            return true;
        }
        if ((Objects.nonNull(leftEye.getHyperopiaLevel()) && leftEye.getHyperopiaLevel() >= HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.code)
                || (Objects.nonNull(rightEye.getHyperopiaLevel()) && rightEye.getHyperopiaLevel() > HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.code)) {
            return true;
        }

        if (ObjectsUtil.allNotNull(leftEye.getAstigmatism(), rightEye.getAstigmatism()) && (leftEye.getAstigmatism() || rightEye.getAstigmatism())) {
            return true;
        }
        return null;
    }

    /**
     * 视力低下等级
     * @param isLowVision
     */
    public Integer getLowVision(Boolean isLowVision){
        return Optional.ofNullable(isLowVision)
                .map(vision-> Objects.equals(vision,Boolean.TRUE)?1:null)
                .orElse(null);
    }

}
