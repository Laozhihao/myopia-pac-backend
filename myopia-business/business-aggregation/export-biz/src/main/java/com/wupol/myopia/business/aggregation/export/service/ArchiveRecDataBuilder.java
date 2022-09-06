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
import java.util.stream.Collectors;

/**
 * 监测表rec数据业务
 *
 * @author hang.yuan 2022/8/25 20:55
 */
@UtilityClass
public class ArchiveRecDataBuilder {


    private static Map<String,String> nationMap = Maps.newHashMap();

    private static final String COMMA_CH = "，";
    private static final String NUM_STR_0 = "0";
    private static final String NUM_RANGE_0 = "0.00";
    private static final String NUM_NEGATIVE_1 = "-1";
    private static final String NUM_STR_1 = "1";
    private static final String NUM_STR_2 = "2";
    private static final String NUM_STR_3 = "3";
    private static final String NUM_POINT_33 = "3.3";
    private static final String NUM_STR_4 = "4";
    private static final String NUM_POINT_56 = "5.6";
    private static final String NUM_STR_8 = "8";
    private static final String NUM_STR_10 = "10";
    private static final String NUM_STR_15 = "15.00";
    private static final String NUM_NEGATIVE_15 = "-15.00";
    private static final String NUM_STR_30 = "30.00";
    private static final String NUM_NEGATIVE_30 = "-30.00";
    private static final String NUM_STR_80 = "80";
    private static final String NUM_STR_90 = "90";
    private static final String NUM_STR_180 = "180";
    private static final String NUM_STR_200 = "200";
    private static final String NUM_STR_210= "210";
    private static final String NUM_STR_300= "300";
    private static final String NUM_STR_999 = "999";

    private static final Integer NUM_0 = 0;
    private static final Integer NUM_1 = 1;
    private static final Integer NUM_2 = 2;


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
        qesFieldDataBOList.add(new QesFieldDataBO("ID1",AnswerUtil.getValue(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCommonDiseaseId,StrUtil.EMPTY)));
        qesFieldDataBOList.add(new QesFieldDataBO("province",AnswerUtil.getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getProvinceCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("city", AnswerUtil.getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCityCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("district",AnswerUtil.getValueByInteger(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getAreaType)));
        qesFieldDataBOList.add(new QesFieldDataBO("county",AnswerUtil.getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getAreaCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("point",AnswerUtil.getValueByInteger(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getMonitorType)));
        qesFieldDataBOList.add(new QesFieldDataBO("school",AnswerUtil.getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getSchoolCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("grade",AnswerUtil.getValueByString(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getGradeCode)));
        qesFieldDataBOList.add(new QesFieldDataBO("num",getNum(AnswerUtil.getValue(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCommonDiseaseId,StrUtil.EMPTY))));
        qesFieldDataBOList.add(new QesFieldDataBO("ID2",AnswerUtil.getValue(commonDiseaseIdInfo,StudentCommonDiseaseIdInfo::getCommonDiseaseId,StrUtil.EMPTY)));
        return qesFieldDataBOList;
    }

    /**
     * 获取编码（常见病ID的后四位）
     * @param commonDiseaseId 常见病ID
     */
    private String getNum(String commonDiseaseId){
        if (StrUtil.isBlank(commonDiseaseId)){
            return StrUtil.EMPTY;
        }
        return commonDiseaseId.substring(12, 16);
    }


    /**
     * 学生信息
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setStudentInfo(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        TwoTuple<String, String> tuple = getNationInfo(AnswerUtil.getValue(studentInfo,CardInfoVO::getNationDesc,null));
        qesFieldDataBOList.add(new QesFieldDataBO("gender",AnswerUtil.getGenderRecData(AnswerUtil.getValue(studentInfo,CardInfoVO::getGender,null))));
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
        return TwoTuple.of(NUM_STR_8,AnswerUtil.textFormat(nationDesc));
    }

    /**
     * 视力数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setVisionData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        VisionDataDO visionData = commonDiseaseArchiveCard.getVisionData();
        VisionDataDO.VisionData leftEyeData = AnswerUtil.getValue(visionData,VisionDataDO::getLeftEyeData,null);
        VisionDataDO.VisionData rightEyeData = AnswerUtil.getValue(visionData,VisionDataDO::getRightEyeData,null);
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        ThreeTuple<String, String, String> tuple = getGlassType(AnswerUtil.getValue(rightEyeData, VisionDataDO.VisionData::getGlassesType,null),AnswerUtil.getValue(rightEyeData, VisionDataDO.VisionData::getOkDegree,null) , AnswerUtil.getValue(leftEyeData, VisionDataDO.VisionData::getOkDegree,null));
        qesFieldDataBOList.add(new QesFieldDataBO("glasstype", tuple.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKR", tuple.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKL",tuple.getThird()));
        qesFieldDataBOList.add(new QesFieldDataBO("visionR",getEyeDataValue(AnswerUtil.getValue(rightEyeData, VisionDataDO.VisionData::getNakedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("glassR",getEyeDataValue(AnswerUtil.getValue(rightEyeData, VisionDataDO.VisionData::getCorrectedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("visionL",getEyeDataValue(AnswerUtil.getValue(leftEyeData, VisionDataDO.VisionData::getNakedVision,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("glassL",getEyeDataValue(AnswerUtil.getValue(leftEyeData, VisionDataDO.VisionData::getCorrectedVision,null))));
        return qesFieldDataBOList;
    }

    /**
     * 根据戴镜类型获取值
     * @param glassType 戴镜类型
     * @param okr ok右值
     * @param okl ok左值
     */
    private ThreeTuple<String,String,String> getGlassType(Integer glassType, BigDecimal okr,BigDecimal okl){
        if (Objects.equals(GlassesTypeEnum.NOT_WEARING.getCode(),glassType)){
            return new ThreeTuple<>(NUM_STR_4,StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.FRAME_GLASSES.getCode(),glassType)){
            return new ThreeTuple<>(NUM_STR_1,StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.CONTACT_LENS.getCode(),glassType)){
            return new ThreeTuple<>(NUM_STR_2,StrUtil.EMPTY,StrUtil.EMPTY);
        }
        if (Objects.equals(GlassesTypeEnum.ORTHOKERATOLOGY.getCode(),glassType)){

            return new ThreeTuple<>(NUM_STR_3,getOkValue(okr),getOkValue(okl));
        }
        return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
    }

    /**
     * 获取ok值
     * @param num ok值
     */
    private String getOkValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_NEGATIVE_30);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_RANGE_0);
        return AnswerUtil.numberFormat(num,NUM_2);
    }

    /**
     * 获取视力数据值
     * @param num 视力值
     */
    private String getEyeDataValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_POINT_33);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_POINT_56);
        return AnswerUtil.numberFormat(num,NUM_1);
    }

    /**
     * 屈光数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setComputerOptometry(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        ComputerOptometryDO computerOptometryData = commonDiseaseArchiveCard.getComputerOptometryData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = AnswerUtil.getValue(computerOptometryData,ComputerOptometryDO::getRightEyeData,null);
        ComputerOptometryDO.ComputerOptometry leftEyeData = AnswerUtil.getValue(computerOptometryData,ComputerOptometryDO::getLeftEyeData,null);
        BigDecimal rightSph = AnswerUtil.getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getSph, null);
        BigDecimal rightCyl = AnswerUtil.getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getCyl, null);
        BigDecimal rightAxial = AnswerUtil.getValue(rightEyeData, ComputerOptometryDO.ComputerOptometry::getAxial, null);
        BigDecimal leftSph = AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getSph, null);
        BigDecimal leftCyl = AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getCyl, null);
        BigDecimal leftAxial = AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getAxial, null);
        ThreeTuple<String, String, String> rightConvertValue = getConvertValue(rightSph, rightCyl, rightAxial);
        ThreeTuple<String, String, String> leftConvertValue = getConvertValue(AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getSph,null), AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getCyl,null),AnswerUtil.getValue(leftEyeData, ComputerOptometryDO.ComputerOptometry::getAxial,null));
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

    /**
     * 获取球镜值
     * @param num 球镜值
     */
    private String getSpherValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_NEGATIVE_30);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_30);
        return AnswerUtil.numberFormat(num,NUM_2);
    }

    /**
     * 获取柱镜值
     * @param num 柱镜值
     */
    private String getCylinValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_NEGATIVE_15);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_15);
        return AnswerUtil.numberFormat(num,NUM_2);
    }

    /**
     * 获取轴位值
     * @param num 轴位值
     */
    private String getAxisValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_STR_0);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_180);
        return AnswerUtil.numberFormat(num,NUM_0);
    }

    /**
     * 获取转换值
     * @param spher 球镜值
     * @param cylin 柱镜值
     * @param axis 轴位值
     */
    private ThreeTuple<String,String,String> getConvertValue(BigDecimal spher,BigDecimal cylin, BigDecimal axis){
        if (ObjectsUtil.allNull(cylin,axis)){
            return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
        }

        if (Objects.nonNull(cylin) && BigDecimalUtil.lessThanAndEqual(cylin,NUM_STR_0)){
            return new ThreeTuple<>(NUM_STR_999,NUM_STR_999,NUM_STR_999);
        }
        if (Objects.nonNull(cylin) && BigDecimalUtil.moreThan(cylin,NUM_STR_0) && Objects.nonNull(axis) &&  BigDecimalUtil.moreThan(axis,"90")){
            BigDecimal add = Optional.ofNullable(spher).orElse(new BigDecimal(NUM_STR_0)).add(cylin);
            return new ThreeTuple<>(AnswerUtil.numberFormat(add,2),
                    AnswerUtil.numberFormat(cylin.multiply(new BigDecimal(NUM_NEGATIVE_1)),NUM_2),
                    AnswerUtil.numberFormat(axis.subtract(new BigDecimal(NUM_STR_90)),NUM_0));
        }

        if (Objects.nonNull(cylin) && BigDecimalUtil.moreThan(cylin,NUM_STR_0) && Objects.nonNull(axis) &&  BigDecimalUtil.lessThan(axis,"90")){
            BigDecimal add = Optional.ofNullable(spher).orElse(new BigDecimal(NUM_STR_0)).add(cylin);
            return new ThreeTuple<>(AnswerUtil.numberFormat(add,2),
                    AnswerUtil.numberFormat(cylin.multiply(new BigDecimal(NUM_NEGATIVE_1)),NUM_2),
                    AnswerUtil.numberFormat(axis.add(new BigDecimal(NUM_STR_90)),NUM_0));
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
        qesFieldDataBOList.add(new QesFieldDataBO("NOTE",getNoteValue(commonDiseaseArchiveCard.getOtherEyeDiseases())));
        if (Objects.equals(SchoolTypeEnum.KINDERGARTEN.getType(),studentInfo.getSchoolType())){
            qesFieldDataBOList.add(new QesFieldDataBO("name",AnswerUtil.textFormat(null)));
        }else {
            qesFieldDataBOList.add(new QesFieldDataBO("name",AnswerUtil.textFormat(studentInfo.getName())));
        }
        qesFieldDataBOList.add(new QesFieldDataBO("date",DateUtil.format(Optional.ofNullable(studentInfo.getScreeningDate()).orElse(new Date()),QuestionnaireConstant.DATE_FORMAT)));
        return qesFieldDataBOList;
    }

    /**
     * 获取其它注意事项
     * @param otherEyeDiseases 其它眼疾
     */
    private String getNoteValue(OtherEyeDiseasesDO otherEyeDiseases){
        if (Objects.isNull(otherEyeDiseases)){
            return AnswerUtil.textFormat(null);
        }
        OtherEyeDiseasesDO.OtherEyeDiseases leftEyeData = AnswerUtil.getValue(otherEyeDiseases, OtherEyeDiseasesDO::getLeftEyeData, null);
        OtherEyeDiseasesDO.OtherEyeDiseases rightEyeData = AnswerUtil.getValue(otherEyeDiseases, OtherEyeDiseasesDO::getRightEyeData, null);
        List<String> leftEyeDiseases = AnswerUtil.getValue(leftEyeData, OtherEyeDiseasesDO.OtherEyeDiseases::getEyeDiseases, null);
        List<String> rightEyeDiseases = AnswerUtil.getValue(rightEyeData, OtherEyeDiseasesDO.OtherEyeDiseases::getEyeDiseases, null);


        if (CollUtil.isEmpty(leftEyeDiseases) && CollUtil.isNotEmpty(rightEyeDiseases)){
            return AnswerUtil.textFormat(CollUtil.join(rightEyeDiseases,COMMA_CH));
        }

        if (CollUtil.isNotEmpty(leftEyeDiseases) && CollUtil.isEmpty(rightEyeDiseases)){
            return AnswerUtil.textFormat(CollUtil.join(leftEyeDiseases,COMMA_CH));
        }

        if (CollUtil.isEmpty(leftEyeDiseases) && CollUtil.isEmpty(rightEyeDiseases)){
            return AnswerUtil.textFormat(null);
        }
        leftEyeDiseases.removeAll(rightEyeDiseases);
        leftEyeDiseases.addAll(rightEyeDiseases);

        return AnswerUtil.textFormat(CollUtil.join(leftEyeDiseases,COMMA_CH));
    }

    /**
     * 疾病史
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setDiseasesHistoryData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        List<String> diseasesHistoryData = commonDiseaseArchiveCard.getDiseasesHistoryData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q31",getDiseasesValue(diseasesHistoryData,"肝炎")));
        qesFieldDataBOList.add(new QesFieldDataBO("q32",getDiseasesValue(diseasesHistoryData,"肾炎")));
        qesFieldDataBOList.add(new QesFieldDataBO("q33",getDiseasesValue(diseasesHistoryData,"心脏病")));
        qesFieldDataBOList.add(new QesFieldDataBO("q34",getDiseasesValue(diseasesHistoryData,"高血压")));
        qesFieldDataBOList.add(new QesFieldDataBO("q35",getDiseasesValue(diseasesHistoryData,"贫血")));
        qesFieldDataBOList.add(new QesFieldDataBO("q36",getDiseasesValue(diseasesHistoryData,"糖尿病")));
        qesFieldDataBOList.add(new QesFieldDataBO("q37",getDiseasesValue(diseasesHistoryData,"过敏性哮喘")));
        qesFieldDataBOList.add(new QesFieldDataBO("q38",getDiseasesValue(diseasesHistoryData,"身体残疾")));
        return qesFieldDataBOList;
    }

    /**
     * 获取疾病史
     * @param diseasesHistoryData 疾病史
     * @param diseasesName 疾病名
     */
    private String getDiseasesValue(List<String> diseasesHistoryData,String diseasesName){
        if (CollUtil.isEmpty(diseasesHistoryData)){
            diseasesHistoryData = Lists.newArrayList();
        }
        if (diseasesHistoryData.contains(diseasesName)){
            return NUM_STR_1;
        }
        return NUM_STR_2;
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
        qesFieldDataBOList.add(new QesFieldDataBO("q51",AnswerUtil.getValueByInteger(deciduous, SaprodontiaStat.StatItem::getDCount)));
        qesFieldDataBOList.add(new QesFieldDataBO("q52",AnswerUtil.getValueByInteger(deciduous, SaprodontiaStat.StatItem::getMCount)));
        qesFieldDataBOList.add(new QesFieldDataBO("q53",AnswerUtil.getValueByInteger(deciduous, SaprodontiaStat.StatItem::getFCount)));
        qesFieldDataBOList.add(new QesFieldDataBO("q54",AnswerUtil.getValueByInteger(permanent, SaprodontiaStat.StatItem::getDCount)));
        qesFieldDataBOList.add(new QesFieldDataBO("q55",AnswerUtil.getValueByInteger(permanent, SaprodontiaStat.StatItem::getMCount)));
        qesFieldDataBOList.add(new QesFieldDataBO("q56",AnswerUtil.getValueByInteger(permanent, SaprodontiaStat.StatItem::getFCount)));
        return qesFieldDataBOList;
    }

    /**
     * 身高体重数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setHeightAndWeightData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        HeightAndWeightDataDO heightAndWeightData = commonDiseaseArchiveCard.getHeightAndWeightData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();

        qesFieldDataBOList.add(new QesFieldDataBO("q6",getHeightValue(AnswerUtil.getValue(heightAndWeightData,HeightAndWeightDataDO::getHeight,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q7",getWeightValue(AnswerUtil.getValue(heightAndWeightData,HeightAndWeightDataDO::getWeight,null))));
        return qesFieldDataBOList;
    }

    /**
     * 获取身高值
     * @param num 身高值
     */
    private String getHeightValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_STR_80);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_210);
        return AnswerUtil.numberFormat(num,NUM_1);
    }

    /**
     * 获取体重值
     * @param num 体重值
     */
    private String getWeightValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_STR_10);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_200);
        return AnswerUtil.numberFormat(num,NUM_1);
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
        Integer type = AnswerUtil.getValue(spineItem, SpineDataDO.SpineItem::getType,null);
        Integer level = AnswerUtil.getValue(spineItem, SpineDataDO.SpineItem::getLevel,null);
        return TwoTuple.of(AnswerUtil.numberFormat(type),AnswerUtil.numberFormat(level));
    }


    /**
     * 血压数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setBloodPressureData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        BloodPressureDataDO bloodPressureData = commonDiseaseArchiveCard.getBloodPressureData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q81",getSbpValue(AnswerUtil.getValue(bloodPressureData,BloodPressureDataDO::getSbp,null))));
        qesFieldDataBOList.add(new QesFieldDataBO("q82",getDbpValue(AnswerUtil.getValue(bloodPressureData,BloodPressureDataDO::getDbp,null))));
        return qesFieldDataBOList;
    }

    /**
     * 获取收缩压
     * @param num 收缩压值
     */
    private String getSbpValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_STR_0);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_300);
        return AnswerUtil.numberFormat(num,NUM_0);
    }

    /**
     * 获取舒张压值
     * @param num 舒张压值
     */
    private String getDbpValue(BigDecimal num){
        num = BigDecimalUtil.getNumLessThan(num,NUM_STR_0);
        num = BigDecimalUtil.getNumMoreThan(num,NUM_STR_200);
        return AnswerUtil.numberFormat(num,NUM_0);
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
                return TwoTuple.of(NUM_STR_2,AnswerUtil.numberFormat(privacyDataDO.getAge()));
            }
            return TwoTuple.of(NUM_STR_1,StrUtil.EMPTY);
        }).orElse(TwoTuple.of(NUM_STR_1,StrUtil.EMPTY));
    }

}
