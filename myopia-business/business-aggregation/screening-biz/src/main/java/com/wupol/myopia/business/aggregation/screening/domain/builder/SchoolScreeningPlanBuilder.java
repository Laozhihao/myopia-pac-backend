package com.wupol.myopia.business.aggregation.screening.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.screening.constant.EyeTypeEnum;
import com.wupol.myopia.business.aggregation.screening.constant.SchoolConstant;
import com.wupol.myopia.business.aggregation.screening.domain.dto.SchoolScreeningPlanDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.*;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 筛查计划相关构建
 *
 * @author hang.yuan 2022/9/14 17:17
 */
@UtilityClass
public class SchoolScreeningPlanBuilder {

    /**
     * 构建筛查计划
     * @param schoolScreeningPlanDTO 筛查计划参数
     * @param currentUser 当前用户
     * @param districtId 区域ID
     */
    public ScreeningPlan buildScreeningPlan(SchoolScreeningPlanDTO schoolScreeningPlanDTO, CurrentUser currentUser, Integer districtId) {
        return new ScreeningPlan()
                .setId(schoolScreeningPlanDTO.getId())
                .setSrcScreeningNoticeId(Optional.ofNullable(schoolScreeningPlanDTO.getScreeningNoticeId()).orElse(CommonConst.DEFAULT_ID))
                .setScreeningTaskId(Optional.ofNullable(schoolScreeningPlanDTO.getScreeningTaskId()).orElse(CommonConst.DEFAULT_ID))
                .setTitle(schoolScreeningPlanDTO.getTitle())
                .setContent(Optional.ofNullable(schoolScreeningPlanDTO.getContent()).orElse(StrUtil.EMPTY))
                .setStartTime(DateFormatUtil.parseDate(schoolScreeningPlanDTO.getStartTime(), SchoolConstant.START_TIME,DatePattern.NORM_DATETIME_PATTERN))
                .setEndTime(DateFormatUtil.parseDate(schoolScreeningPlanDTO.getEndTime(),SchoolConstant.END_TIME,DatePattern.NORM_DATETIME_PATTERN))
                .setGovDeptId(CommonConst.DEFAULT_ID)
                .setScreeningOrgId(currentUser.getOrgId())
                .setScreeningOrgType(ScreeningOrgTypeEnum.SCHOOL.getType())
                .setDistrictId(districtId)
                .setReleaseStatus(CommonConst.STATUS_NOT_RELEASE)
                .setCreateUserId(currentUser.getId())
                .setOperatorId(currentUser.getId())
                .setScreeningType(schoolScreeningPlanDTO.getScreeningType());
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
    private void setVisionDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        VisionDataDO visionData = studentScreeningResultDetail.getVisionData();
        List<VisionDataVO> visionDataVoList = Lists.newArrayList();
        if (Objects.isNull(visionData)){
            return;
        }

        Integer glassesType = Optional.ofNullable(visionData.getLeftEyeData()).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
        studentScreeningDetailVO.setGlassesType(glassesType);

        visionDataVoList.add(new VisionDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setNakedVision(getValueByBigDecimal(visionData.getLeftEyeData(), VisionDataDO.VisionData::getNakedVision,SchoolConstant.SCALE_1,Boolean.FALSE))
                .setCorrectedVision(getValueByBigDecimal(visionData.getLeftEyeData(), VisionDataDO.VisionData::getCorrectedVision,SchoolConstant.SCALE_1,Boolean.FALSE)));
        visionDataVoList.add(new VisionDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setNakedVision(getValueByBigDecimal(visionData.getRightEyeData(), VisionDataDO.VisionData::getNakedVision,SchoolConstant.SCALE_1,Boolean.FALSE))
                .setCorrectedVision(getValueByBigDecimal(visionData.getRightEyeData(), VisionDataDO.VisionData::getCorrectedVision,SchoolConstant.SCALE_1,Boolean.FALSE)));

        studentScreeningDetailVO.setVisionData(visionDataVoList);
    }


    /**
     * 电脑验光
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private void setComputerOptometryDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        ComputerOptometryDO computerOptometry = studentScreeningResultDetail.getComputerOptometry();
        List<ComputerOptometryDataVO> computerOptometryDataVoList = Lists.newArrayList();
        if (Objects.isNull(computerOptometry)){
            return;
        }

        computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setAxial(getValueByBigDecimal(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getAxial,SchoolConstant.SCALE_0,Boolean.FALSE))
                .setSph(getValueByBigDecimal(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getSph,SchoolConstant.SCALE_2,Boolean.TRUE))
                .setCyl(getValueByBigDecimal(computerOptometry.getLeftEyeData(), ComputerOptometryDO.ComputerOptometry::getCyl,SchoolConstant.SCALE_2,Boolean.TRUE)));
        computerOptometryDataVoList.add(new ComputerOptometryDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setAxial(getValueByBigDecimal(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getAxial,SchoolConstant.SCALE_0,Boolean.FALSE))
                .setSph(getValueByBigDecimal(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getSph,SchoolConstant.SCALE_2,Boolean.TRUE))
                .setCyl(getValueByBigDecimal(computerOptometry.getRightEyeData(), ComputerOptometryDO.ComputerOptometry::getCyl,SchoolConstant.SCALE_2,Boolean.TRUE)));

        studentScreeningDetailVO.setComputerOptometryData(computerOptometryDataVoList);
    }


    /**
     * 其它
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private void setOtherDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        List<OtherDataVO> otherDataVoList = Lists.newArrayList(new OtherDataVO(EyeTypeEnum.LEFT_EYE.getCode()),new OtherDataVO(EyeTypeEnum.RIGHT_EYE.getCode()));
        List<Boolean> otherList = Lists.newArrayList();
        setSlitLampData(studentScreeningResultDetail, otherDataVoList,otherList);
        setOcularInspectionData(studentScreeningResultDetail, otherDataVoList,otherList);
        setFundusData(studentScreeningResultDetail, otherDataVoList,otherList);
        setOtherEyeDiseases(studentScreeningResultDetail, otherDataVoList,otherList);

        if (Objects.equals(otherList.size(),4)){
            return;
        }
        studentScreeningDetailVO.setOtherData(otherDataVoList);
    }

    /**
     * 设置其他眼病数据
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param otherDataVoList 其它数据
     */
    private void setOtherEyeDiseases(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList,List<Boolean> otherList) {
        OtherEyeDiseasesDO otherEyeDiseases = studentScreeningResultDetail.getOtherEyeDiseases();
        if (Objects.isNull(otherEyeDiseases) || Objects.equals(otherEyeDiseases.isNull(),Boolean.TRUE)){
            otherList.add(Boolean.TRUE);
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
    private void setFundusData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList,List<Boolean> otherList) {
        FundusDataDO fundusData = studentScreeningResultDetail.getFundusData();
        if (Objects.isNull(fundusData)){
            otherList.add(Boolean.TRUE);
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
    private void setOcularInspectionData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList,List<Boolean> otherList) {
        OcularInspectionDataDO ocularInspectionData = studentScreeningResultDetail.getOcularInspectionData();
        if (Objects.isNull(ocularInspectionData)){
            otherList.add(Boolean.TRUE);
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
    private void setSlitLampData(VisionScreeningResultDTO studentScreeningResultDetail, List<OtherDataVO> otherDataVoList,List<Boolean> otherList) {
        SlitLampDataDO slitLampData = studentScreeningResultDetail.getSlitLampData();
        if (Objects.isNull(slitLampData)){
            otherList.add(Boolean.TRUE);
            otherDataVoList.get(0).setSlitLamp(SchoolConstant.NO_DATA);
            otherDataVoList.get(1).setSlitLamp(SchoolConstant.NO_DATA);
        }else {
            otherDataVoList.get(0).setSlitLamp(getValueByBoolean(slitLampData.getLeftEyeData(), AbstractDiagnosisResult::isNormal));
            otherDataVoList.get(1).setSlitLamp(getValueByBoolean(slitLampData.getRightEyeData(), AbstractDiagnosisResult::isNormal));
        }
    }

    private <T>String getValueByBigDecimal(T data, Function<T,BigDecimal> function,int scale,Boolean symbol){
        return Optional.ofNullable(data).map(function).map(bigDecimal -> {
            BigDecimal decimalByFormat = BigDecimalUtil.getBigDecimalByFormat(bigDecimal, scale);
            if (Objects.isNull(decimalByFormat)){
                return null;
            }else {
                if (Objects.equals(symbol,Boolean.TRUE)){
                    return BigDecimalUtil.moreThan(decimalByFormat,SchoolConstant.ZERO)?SchoolConstant.POSITIVE_SYMBOL+decimalByFormat.toString():decimalByFormat.toString();
                }
                return decimalByFormat.toString();
            }
        }).orElse(SchoolConstant.NO_DATA);
    }

    private <T> String getValueByString(T data, Function<T, String> function) {
        return Optional.ofNullable(data).map(function).orElse(SchoolConstant.NO_DATA);
    }

    private <T> String getValueByBoolean(T data, Function<T, Boolean> function) {
        return Optional.ofNullable(data).map(function).map(b -> Objects.equals(Boolean.TRUE, b) ? "正常" : "异常").orElse(SchoolConstant.NO_DATA);
    }

    private <T> String getValueByInteger(T data, Function<T, Integer> function) {
        return Optional.ofNullable(data).map(function).map(b -> Objects.equals(AbstractDiagnosisResult.NORMAL, b) ? "正常" : "异常").orElse(SchoolConstant.NO_DATA);
    }

    private <T> String getValueByStringList(T data, Function<T, List<String>> function) {
        return Optional.ofNullable(data).map(function).map(b -> CollUtil.join(b, ",")).orElse(SchoolConstant.NO_DATA);
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
            return;
        }

        heightAndWeightDataVO.setHeight(getValueByBigDecimal(heightAndWeightData, HeightAndWeightDataDO::getHeight,SchoolConstant.SCALE_1,Boolean.FALSE));
        heightAndWeightDataVO.setWeight(getValueByBigDecimal(heightAndWeightData, HeightAndWeightDataDO::getWeight,SchoolConstant.SCALE_1,Boolean.FALSE));
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
    private void setPupilOptometryDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        PupilOptometryDataDO pupilOptometryData = studentScreeningResultDetail.getPupilOptometryData();
        List<PupilOptometryDataVO> pupilOptometryDataVoList = Lists.newArrayList();
        if (Objects.isNull(pupilOptometryData)){
            return;
        }

        pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setAxial(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getAxial,SchoolConstant.SCALE_0,Boolean.FALSE))
                .setSph(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getSph,SchoolConstant.SCALE_2,Boolean.TRUE))
                .setCyl(getValueByBigDecimal(pupilOptometryData.getLeftEyeData(), PupilOptometryDataDO.PupilOptometryData::getCyl,SchoolConstant.SCALE_2,Boolean.TRUE)));
        pupilOptometryDataVoList.add(new PupilOptometryDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setAxial(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getAxial,SchoolConstant.SCALE_0,Boolean.FALSE))
                .setSph(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getSph,SchoolConstant.SCALE_2,Boolean.TRUE))
                .setCyl(getValueByBigDecimal(pupilOptometryData.getRightEyeData(), PupilOptometryDataDO.PupilOptometryData::getCyl,SchoolConstant.SCALE_2,Boolean.TRUE)));

        studentScreeningDetailVO.setPupilOptometryData(pupilOptometryDataVoList);
    }

    /**
     * 眼压
     * @param studentScreeningResultDetail 学生筛查结果详情（数据库）
     * @param studentScreeningDetailVO 学生筛查结果详情（响应数据）
     */
    private void setEyePressureDataVO(VisionScreeningResultDTO studentScreeningResultDetail, StudentScreeningDetailVO studentScreeningDetailVO) {
        EyePressureDataDO eyePressureData = studentScreeningResultDetail.getEyePressureData();
        List<EyePressureDataVO> eyePressureDataVoList = Lists.newArrayList();
        if (Objects.isNull(eyePressureData)){
            return;
        }

        eyePressureDataVoList.add(new EyePressureDataVO()
                .setEyeType(EyeTypeEnum.LEFT_EYE.getCode())
                .setPressure(getValueByBigDecimal(eyePressureData.getLeftEyeData(), EyePressureDataDO.EyePressureData::getPressure,SchoolConstant.SCALE_0,Boolean.FALSE)));
        eyePressureDataVoList.add(new EyePressureDataVO()
                .setEyeType(EyeTypeEnum.RIGHT_EYE.getCode())
                .setPressure(getValueByBigDecimal(eyePressureData.getRightEyeData(), EyePressureDataDO.EyePressureData::getPressure,SchoolConstant.SCALE_0,Boolean.FALSE)));
        studentScreeningDetailVO.setEyePressureData(eyePressureDataVoList);
    }
}
