package com.wupol.myopia.business.aggregation.export.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolTypeEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import com.wupol.myopia.business.core.questionnaire.util.AnswerUtil;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SaprodontiaStat;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CardInfoVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCommonDiseaseIdInfo;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 监测表rec数据业务
 *
 * @author hang.yuan 2022/8/25 20:55
 */
@UtilityClass
public class ArchiveRecDataBuilder {


    private static Map<String,String> nationMap = Maps.newHashMap();


    static {
        for (int i = 0; i < NationEnum.COMMON_NATION.size(); i++) {
            nationMap.put(NationEnum.COMMON_NATION.get(i).getName(),String.valueOf(i+1));
        }
    }

    /**
     * 获取监测表qes字段
     * @param schoolType 学校类型
     * @param commonDiseaseArchiveCardList 监测表数据集合
     */
    public List<List<QesFieldDataBO>> getDataList(Integer schoolType ,List<CommonDiseaseArchiveCard> commonDiseaseArchiveCardList){
        if (Objects.equals(schoolType, SchoolTypeEnum.KINDERGARTEN.getType())){
            return getKindergarten(commonDiseaseArchiveCardList);
        }
        if (Objects.equals(schoolType,SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType())){
            return getPrimaryAndSecondary(commonDiseaseArchiveCardList);
        }

        if (Objects.equals(schoolType,SchoolTypeEnum.UNIVERSITY.getType())){
            return getUniversity(commonDiseaseArchiveCardList);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取幼儿园
     * @param commonDiseaseArchiveCardList 常见病档案卡集合
     */
    private List<List<QesFieldDataBO>> getKindergarten(List<CommonDiseaseArchiveCard> commonDiseaseArchiveCardList){
         return commonDiseaseArchiveCardList.stream().map(commonDiseaseArchiveCard -> {
                    List<QesFieldDataBO> list = Lists.newArrayList();
                    list.addAll(setStudentCommonDiseaseIdInfo(commonDiseaseArchiveCard));
                    list.addAll(setStudentInfo(commonDiseaseArchiveCard));
                    list.addAll(setVisionData(commonDiseaseArchiveCard));
                    list.addAll(setComputerOptometry(commonDiseaseArchiveCard));
                    list.addAll(setOther(commonDiseaseArchiveCard));
                    return list;
                }).collect(Collectors.toList());
    }

    /**
     * 获取中小学
     * @param commonDiseaseArchiveCardList 常见病档案卡集合
     */
    private List<List<QesFieldDataBO>> getPrimaryAndSecondary(List<CommonDiseaseArchiveCard> commonDiseaseArchiveCardList){
        return commonDiseaseArchiveCardList.stream().map(commonDiseaseArchiveCard -> {
                    List<QesFieldDataBO> list = Lists.newArrayList();
                    list.addAll(setStudentCommonDiseaseIdInfo(commonDiseaseArchiveCard));
                    list.addAll(setStudentInfo(commonDiseaseArchiveCard));
                    list.addAll(setDiseasesHistoryData(commonDiseaseArchiveCard));
                    list.addAll(setVisionData(commonDiseaseArchiveCard));
                    list.addAll(setComputerOptometry(commonDiseaseArchiveCard));
                    list.addAll(setOther(commonDiseaseArchiveCard));
                    list.addAll(setSaprodontiaData(commonDiseaseArchiveCard));
                    list.addAll(setHeightAndWeightData(commonDiseaseArchiveCard));
                    list.addAll(setSpineDataDO(commonDiseaseArchiveCard));
                    list.addAll(setBloodPressureData(commonDiseaseArchiveCard));
                    list.addAll(setPrivacyData(commonDiseaseArchiveCard));
                    return list;
                }).collect(Collectors.toList());
    }

    /**
     * 获取大学
     * @param commonDiseaseArchiveCardList 常见病档案卡集合
     */
    private List<List<QesFieldDataBO>> getUniversity(List<CommonDiseaseArchiveCard> commonDiseaseArchiveCardList){
        return commonDiseaseArchiveCardList.stream().map(commonDiseaseArchiveCard -> {
                    List<QesFieldDataBO> list = Lists.newArrayList();
                    list.addAll(setStudentCommonDiseaseIdInfo(commonDiseaseArchiveCard));
                    list.addAll(setStudentInfo(commonDiseaseArchiveCard));
                    list.addAll(setDiseasesHistoryData(commonDiseaseArchiveCard));
                    list.addAll(setVisionData(commonDiseaseArchiveCard));
                    list.addAll(setOther(commonDiseaseArchiveCard));
                    list.addAll(setSaprodontiaData(commonDiseaseArchiveCard));
                    list.addAll(setHeightAndWeightData(commonDiseaseArchiveCard));
                    list.addAll(setBloodPressureData(commonDiseaseArchiveCard));
                    list.addAll(setPrivacyData(commonDiseaseArchiveCard));
                    return list;
                }).collect(Collectors.toList());
    }

    /**
     * 常见病ID信息
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setStudentCommonDiseaseIdInfo(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        StudentCommonDiseaseIdInfo commonDiseaseIdInfo = commonDiseaseArchiveCard.getCommonDiseaseIdInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("ID1",getValue(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCommonDiseaseId,StrUtil.EMPTY)));
        qesFieldDataBOList.add(new QesFieldDataBO("province",getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getProvinceCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("city", getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCityCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("district",getValueByInteger(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getAreaType)));
        qesFieldDataBOList.add(new QesFieldDataBO("county",getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getAreaCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("point",getValueByInteger(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getMonitorType)));
        qesFieldDataBOList.add(new QesFieldDataBO("school",getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getSchoolCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("grade",getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getGradeCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("num",getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getSchoolCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("ID2",getValue(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCommonDiseaseId,StrUtil.EMPTY)));
        return qesFieldDataBOList;
    }

    private <T>String getValueByString(T t, Function<T,String> function){
       return AnswerUtil.numberFormat(getValue(t,function,null));
    }
    private <T>String getValueByInteger(T t, Function<T,Integer> function){
        return AnswerUtil.numberFormat(getValue(t,function,null));
    }
    private <T,U> U getValue(T t, Function<T,U> function,U u){
        return Optional.ofNullable(t).map(function).orElse(u);
    }

    /**
     * 学生信息
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setStudentInfo(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        TwoTuple<String, String> tuple = getNationInfo(getValue(studentInfo,CardInfoVO::getNationDesc,null));
        qesFieldDataBOList.add(new QesFieldDataBO("gender",AnswerUtil.getGenderRecData(getValue(studentInfo,CardInfoVO::getGender,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("nation", tuple.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("nationother", tuple.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("birth", DateUtil.format(studentInfo.getBirthday(), QuestionnaireConstant.DATE_FORMAT)));
        qesFieldDataBOList.add(new QesFieldDataBO("examine",DateUtil.format(studentInfo.getScreeningDate(),QuestionnaireConstant.DATE_FORMAT)));
        return qesFieldDataBOList;
    }

    /**
     * 获取民族信息
     * @param nationDesc 民族
     */
    private TwoTuple<String,String> getNationInfo(String nationDesc){
        if (StrUtil.isBlank(nationDesc)){
            return TwoTuple.of(StrUtil.EMPTY,AnswerUtil.textFormat(StrUtil.EMPTY));
        }
        String nation = nationMap.get(nationDesc);
        if (Objects.nonNull(nation)){
            return TwoTuple.of(nation,AnswerUtil.textFormat(StrUtil.EMPTY));
        }
        return TwoTuple.of("8",AnswerUtil.textFormat(nationDesc));
    }

    /**
     * 视力数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setVisionData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        VisionDataDO visionData = commonDiseaseArchiveCard.getVisionData();
        VisionDataDO.VisionData leftEyeData = getValue(visionData,VisionDataDO::getLeftEyeData,null);
        VisionDataDO.VisionData rightEyeData = getValue(visionData,VisionDataDO::getRightEyeData,null);
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        ThreeTuple<String, String, String> tuple = getGlassType(getValue(rightEyeData, VisionDataDO.VisionData::getGlassesType,null),getValue(rightEyeData, VisionDataDO.VisionData::getOkDegree,null) , getValue(leftEyeData, VisionDataDO.VisionData::getOkDegree,null));
        qesFieldDataBOList.add(new QesFieldDataBO("glasstype", tuple.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKR", tuple.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKL",tuple.getThird()));
        qesFieldDataBOList.add(new QesFieldDataBO("visionR",getEyeDataValue(getValue(rightEyeData, VisionDataDO.VisionData::getNakedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("glassR",getEyeDataValue(getValue(rightEyeData, VisionDataDO.VisionData::getCorrectedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("visionL",getEyeDataValue(getValue(leftEyeData, VisionDataDO.VisionData::getNakedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("glassL",getEyeDataValue(getValue(leftEyeData, VisionDataDO.VisionData::getCorrectedVision,null))));
        return qesFieldDataBOList;
    }

    private ThreeTuple<String,String,String> getGlassType(Integer glassType, BigDecimal okr,BigDecimal okl){
        if (Objects.equals(GlassesTypeEnum.NOT_WEARING.getCode(),glassType)){
            return new ThreeTuple<>("4",StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.FRAME_GLASSES.getCode(),glassType)){
            return new ThreeTuple<>("1",StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.CONTACT_LENS.getCode(),glassType)){
            return new ThreeTuple<>("2",StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.ORTHOKERATOLOGY.getCode(),glassType)){

            return new ThreeTuple<>("3",getOkValue(okr),getOkValue(okl));
        }
        return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
    }

    private String getOkValue(BigDecimal num){
        num = getNumLessThan(num,"-30.00");
        num = getNumMoreThan(num,"0.00");
        return AnswerUtil.numberFormat(num,2);
    }

    private String getEyeDataValue(BigDecimal num){
        num = getNumLessThan(num,"3.3");
        num = getNumMoreThan(num,"5.6");
        return AnswerUtil.numberFormat(num,1);
    }

    /**
     * 屈光数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setComputerOptometry(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        ComputerOptometryDO computerOptometryData = commonDiseaseArchiveCard.getComputerOptometryData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = getValue(computerOptometryData,ComputerOptometryDO::getRightEyeData,null);
        ComputerOptometryDO.ComputerOptometry leftEyeData = getValue(computerOptometryData,ComputerOptometryDO::getLeftEyeData,null);
        BigDecimal rightSph = getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getSph, null);
        BigDecimal rightCyl = getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getCyl, null);
        BigDecimal rightAxial = getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getAxial, null);
        BigDecimal leftSph = getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getSph, null);
        BigDecimal leftCyl = getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getCyl, null);
        BigDecimal leftAxial = getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getAxial, null);
        ThreeTuple<String, String, String> rightConvertValue = getConvertValue(rightSph, rightCyl, rightAxial);
        ThreeTuple<String, String, String> leftConvertValue = getConvertValue(getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getSph,null), getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getCyl,null),getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getAxial,null));
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("spherR",getSpherValue(rightSph)));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinR",getCylinValue(rightCyl)));
        qesFieldDataBOList.add(new QesFieldDataBO("axisR",getAxisValue(rightAxial)));
        qesFieldDataBOList.add(new QesFieldDataBO("SER", AnswerUtil.numberFormat(StatUtil.getSphericalEquivalent(rightSph,rightCyl),3)));
        qesFieldDataBOList.add(new QesFieldDataBO("spherRT",rightConvertValue.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinRT",rightConvertValue.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("axisRT",rightConvertValue.getThird()));
        qesFieldDataBOList.add(new QesFieldDataBO("spherL",getSpherValue(leftSph)));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinL",getCylinValue(leftCyl)));
        qesFieldDataBOList.add(new QesFieldDataBO("axisL",getAxisValue(leftAxial)));
        qesFieldDataBOList.add(new QesFieldDataBO("SEL",AnswerUtil.numberFormat(StatUtil.getSphericalEquivalent(leftSph,leftCyl),3)));
        qesFieldDataBOList.add(new QesFieldDataBO("spherLT",leftConvertValue.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinLT",leftConvertValue.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("axisLT",leftConvertValue.getThird()));
        return qesFieldDataBOList;
    }


    private String getSpherValue(BigDecimal num){
        num = getNumLessThan(num,"-30.00");
        num = getNumMoreThan(num,"30.00");
        return AnswerUtil.numberFormat(num,2);
    }

    private String getCylinValue(BigDecimal num){
        num = getNumLessThan(num,"-15.00");
        num = getNumMoreThan(num,"15.00");
        return AnswerUtil.numberFormat(num,2);
    }

    private String getAxisValue(BigDecimal num){
        num = getNumLessThan(num,"0");
        num = getNumMoreThan(num,"180");
        return AnswerUtil.numberFormat(num,0);
    }

    private ThreeTuple<String,String,String> getConvertValue(BigDecimal spher,BigDecimal cylin, BigDecimal axis){
        if (ObjectsUtil.allNull(cylin,axis)){
            return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
        }

        if (Objects.nonNull(cylin) && BigDecimalUtil.lessThanAndEqual(cylin,"0")){
            return new ThreeTuple<>("999","999","999");
        }
        if (Objects.nonNull(cylin) && BigDecimalUtil.moreThan(cylin,"0") && Objects.nonNull(axis) &&  BigDecimalUtil.moreThan(axis,"90")){
            BigDecimal add = Optional.ofNullable(spher).orElse(new BigDecimal("0")).add(cylin);
            return new ThreeTuple<>(AnswerUtil.numberFormat(add,2),
                    AnswerUtil.numberFormat(cylin.multiply(new BigDecimal("-1")),2),
                    AnswerUtil.numberFormat(axis.subtract(new BigDecimal("90")),0));
        }

        if (Objects.nonNull(cylin) && BigDecimalUtil.moreThan(cylin,"0") && Objects.nonNull(axis) &&  BigDecimalUtil.lessThan(axis,"90")){
            BigDecimal add = Optional.ofNullable(spher).orElse(new BigDecimal("0")).add(cylin);
            return new ThreeTuple<>(AnswerUtil.numberFormat(add,2),
                    AnswerUtil.numberFormat(cylin.multiply(new BigDecimal("-1")),2),
                    AnswerUtil.numberFormat(axis.add(new BigDecimal("90")),0));
        }

        return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
    }

    /**
     * 其它数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setOther(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("NOTE",AnswerUtil.textFormat(null)));
        if (Objects.equals(SchoolTypeEnum.KINDERGARTEN.getType(),studentInfo.getSchoolType())){
            qesFieldDataBOList.add(new QesFieldDataBO("name",AnswerUtil.textFormat(null)));
        }else {
            qesFieldDataBOList.add(new QesFieldDataBO("name",AnswerUtil.textFormat(studentInfo.getName())));
        }
        qesFieldDataBOList.add(new QesFieldDataBO("date",DateUtil.format(Optional.ofNullable(studentInfo.getScreeningDate()).orElse(new Date()),QuestionnaireConstant.DATE_FORMAT)));
        return qesFieldDataBOList;
    }

    /**
     * 疾病史
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setDiseasesHistoryData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        List<String> diseasesHistoryData = commonDiseaseArchiveCard.getDiseasesHistoryData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q31",getValue(diseasesHistoryData,"肝炎")));
        qesFieldDataBOList.add(new QesFieldDataBO("q32",getValue(diseasesHistoryData,"肾炎")));
        qesFieldDataBOList.add(new QesFieldDataBO("q33",getValue(diseasesHistoryData,"心脏病")));
        qesFieldDataBOList.add(new QesFieldDataBO("q34",getValue(diseasesHistoryData,"高血压")));
        qesFieldDataBOList.add(new QesFieldDataBO("q35",getValue(diseasesHistoryData,"贫血")));
        qesFieldDataBOList.add(new QesFieldDataBO("q36",getValue(diseasesHistoryData,"糖尿病")));
        qesFieldDataBOList.add(new QesFieldDataBO("q37",getValue(diseasesHistoryData,"过敏性哮喘")));
        qesFieldDataBOList.add(new QesFieldDataBO("q38",getValue(diseasesHistoryData,"身体残疾")));
        return qesFieldDataBOList;
    }

    /**
     * 获取疾病史
     * @param diseasesHistoryData 疾病史
     * @param diseasesName 疾病名
     */
    private String getValue(List<String> diseasesHistoryData,String diseasesName){
        if (CollUtil.isEmpty(diseasesHistoryData)){
            diseasesHistoryData = Lists.newArrayList();
        }
        if (diseasesHistoryData.contains(diseasesName)){
            return "1";
        }
        return "2";
    }

    /**
     * 龋齿数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setSaprodontiaData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        SaprodontiaData saprodontiaData = commonDiseaseArchiveCard.getSaprodontiaData();
        SaprodontiaStat.StatItem deciduous = Optional.ofNullable(saprodontiaData).map(SaprodontiaData::getSaprodontiaStat).map(SaprodontiaStat::getDeciduous).orElse(null);
        SaprodontiaStat.StatItem permanent = Optional.ofNullable(saprodontiaData).map(SaprodontiaData::getSaprodontiaStat).map(SaprodontiaStat::getPermanent).orElse(null);
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q51",AnswerUtil.numberFormat(Optional.ofNullable(deciduous).map(SaprodontiaStat.StatItem::getDCount).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q52",AnswerUtil.numberFormat(Optional.ofNullable(deciduous).map(SaprodontiaStat.StatItem::getMCount).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q53",AnswerUtil.numberFormat(Optional.ofNullable(deciduous).map(SaprodontiaStat.StatItem::getFCount).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q54",AnswerUtil.numberFormat(Optional.ofNullable(permanent).map(SaprodontiaStat.StatItem::getDCount).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q55",AnswerUtil.numberFormat(Optional.ofNullable(deciduous).map(SaprodontiaStat.StatItem::getMCount).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q56",AnswerUtil.numberFormat(Optional.ofNullable(deciduous).map(SaprodontiaStat.StatItem::getFCount).orElse(null))));
        return qesFieldDataBOList;
    }

    /**
     * 身高体重数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setHeightAndWeightData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        HeightAndWeightDataDO heightAndWeightData = commonDiseaseArchiveCard.getHeightAndWeightData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q6",getHeightValue(heightAndWeightData.getHeight())));
        qesFieldDataBOList.add(new QesFieldDataBO("q7",getWeightValue(heightAndWeightData.getWeight())));
        return qesFieldDataBOList;
    }

    private String getHeightValue(BigDecimal num){
        num = getNumLessThan(num,"80");
        num = getNumMoreThan(num,"210");
        return AnswerUtil.numberFormat(num,1);
    }

    private String getWeightValue(BigDecimal num){
        num = getNumLessThan(num,"10");
        num = getNumMoreThan(num,"200");
        return AnswerUtil.numberFormat(num,1);
    }

    /**
     * 脊柱数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setSpineDataDO(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        SpineDataDO spineData = commonDiseaseArchiveCard.getSpineData();
        TwoTuple<String, String> chest = getSpineItemValue(Optional.ofNullable(spineData).map(SpineDataDO::getChest).orElse(null));
        TwoTuple<String, String> waist = getSpineItemValue(Optional.ofNullable(spineData).map(SpineDataDO::getWaist).orElse(null));
        TwoTuple<String, String> chestWaist = getSpineItemValue(Optional.ofNullable(spineData).map(SpineDataDO::getChestWaist).orElse(null));
        TwoTuple<String, String> entirety = getSpineItemValue(Optional.ofNullable(spineData).map(SpineDataDO::getEntirety).orElse(null));
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("qx2", chest.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx21",chest.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx3",chestWaist.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx31",chestWaist.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx4",waist.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx41",waist.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx1",entirety.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("qx11",entirety.getSecond()));
        return qesFieldDataBOList;
    }

    /**
     * 获取脊柱数据
     * @param spineItem 脊柱弯曲项
     */
    private TwoTuple<String,String> getSpineItemValue(SpineDataDO.SpineItem spineItem){
        Integer type = Optional.ofNullable(spineItem).map(SpineDataDO.SpineItem::getType).orElse(null);
        Integer level = Optional.ofNullable(spineItem).map(SpineDataDO.SpineItem::getLevel).orElse(null);
        return TwoTuple.of(AnswerUtil.numberFormat(type),AnswerUtil.numberFormat(level));
    }


    /**
     * 血压数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setBloodPressureData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        BloodPressureDataDO bloodPressureData = commonDiseaseArchiveCard.getBloodPressureData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q81",getSbpValue(Optional.ofNullable(bloodPressureData).map(BloodPressureDataDO::getSbp).orElse(null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q82",getDbpValue(Optional.ofNullable(bloodPressureData).map(BloodPressureDataDO::getDbp).orElse(null))));
        return qesFieldDataBOList;
    }

    private String getSbpValue(BigDecimal num){
        num = getNumLessThan(num,"0");
        num = getNumMoreThan(num,"300");
        return AnswerUtil.numberFormat(num,0);
    }
    private String getDbpValue(BigDecimal num){
        num = getNumLessThan(num,"0");
        num = getNumMoreThan(num,"200");
        return AnswerUtil.numberFormat(num,0);
    }

    /**
     * 隐私数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setPrivacyData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        PrivacyDataDO privacyData = commonDiseaseArchiveCard.getPrivacyData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();

        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        if (Objects.equals(GenderEnum.MALE.type,studentInfo.getGender())){
            TwoTuple<String, String> male = getPrivacyDataValue(privacyData);
            qesFieldDataBOList.add(new QesFieldDataBO("q91",StrUtil.EMPTY));
            qesFieldDataBOList.add(new QesFieldDataBO("q911",StrUtil.EMPTY));
            qesFieldDataBOList.add(new QesFieldDataBO("q92",male.getFirst()));
            qesFieldDataBOList.add(new QesFieldDataBO("q921",male.getSecond()));
        }else {
            TwoTuple<String, String> female = getPrivacyDataValue(privacyData);
            qesFieldDataBOList.add(new QesFieldDataBO("q91",female.getFirst()));
            qesFieldDataBOList.add(new QesFieldDataBO("q911",female.getSecond()));
            qesFieldDataBOList.add(new QesFieldDataBO("q92",StrUtil.EMPTY));
            qesFieldDataBOList.add(new QesFieldDataBO("q921",StrUtil.EMPTY));
        }

        return qesFieldDataBOList;
    }

    /**
     * 获取隐私数据
     * @param privacyData 隐私数据
     */
    private TwoTuple<String,String> getPrivacyDataValue(PrivacyDataDO privacyData){
        return Optional.ofNullable(privacyData).map(privacyDataDO -> {
            if (Objects.equals(privacyDataDO.getHasIncident(), Boolean.TRUE)) {
                return TwoTuple.of("2",AnswerUtil.numberFormat(privacyDataDO.getAge()));
            }
            return TwoTuple.of("1","");
        }).orElse(TwoTuple.of("1",""));
    }

    private static BigDecimal getNumLessThan(BigDecimal num,String value) {
        if (Objects.nonNull(num) && BigDecimalUtil.lessThan(num,value)){
            num = new BigDecimal(value);
        }
        return num;
    }
    private static BigDecimal getNumMoreThan(BigDecimal num,String value) {
        if (Objects.nonNull(num) && BigDecimalUtil.moreThan(num,value)){
            num = new BigDecimal(value);
        }
        return num;
    }


}
