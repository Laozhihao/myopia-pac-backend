package com.wupol.myopia.business.aggregation.export.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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


    static {
        for (int i = 0; i < NationEnum.COMMON_NATION.size(); i++) {
            nationMap.put(NationEnum.COMMON_NATION.get(i).getName(),String.valueOf(i+1));
        }
    }


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
        qesFieldDataBOList.add(new QesFieldDataBO("ID1",commonDiseaseIdInfo.getCommonDiseaseId()));
        qesFieldDataBOList.add(new QesFieldDataBO("province",AnswerUtil.numberFormat(commonDiseaseIdInfo.getProvinceCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("city", AnswerUtil.numberFormat(commonDiseaseIdInfo.getCityCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("district",AnswerUtil.numberFormat(commonDiseaseIdInfo.getAreaType())));
        qesFieldDataBOList.add(new QesFieldDataBO("county",AnswerUtil.numberFormat(commonDiseaseIdInfo.getAreaCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("point",AnswerUtil.numberFormat(commonDiseaseIdInfo.getMonitorType())));
        qesFieldDataBOList.add(new QesFieldDataBO("school",AnswerUtil.numberFormat(commonDiseaseIdInfo.getSchoolCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("grade",AnswerUtil.numberFormat(commonDiseaseIdInfo.getGradeCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("num",AnswerUtil.numberFormat(commonDiseaseIdInfo.getSchoolCode())));
        qesFieldDataBOList.add(new QesFieldDataBO("ID2",commonDiseaseIdInfo.getCommonDiseaseId()));
        return qesFieldDataBOList;
    }

    /**
     * 学生信息
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setStudentInfo(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        TwoTuple<String, String> tuple = getNationInfo(studentInfo.getNationDesc());
        qesFieldDataBOList.add(new QesFieldDataBO("gender",AnswerUtil.getGenderRecData(studentInfo.getGender())));
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
        VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
        VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        ThreeTuple<String, String, String> tuple = getGlassType(rightEyeData.getGlassesType(), rightEyeData.getOkDegree(), leftEyeData.getOkDegree());
        qesFieldDataBOList.add(new QesFieldDataBO("glasstype", tuple.getFirst()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKR", tuple.getSecond()));
        qesFieldDataBOList.add(new QesFieldDataBO("OKL",tuple.getThird()));
        qesFieldDataBOList.add(new QesFieldDataBO("visionR",AnswerUtil.numberFormat(rightEyeData.getNakedVision(),1)));
        qesFieldDataBOList.add(new QesFieldDataBO("glassR",AnswerUtil.numberFormat(rightEyeData.getCorrectedVision(),1)));
        qesFieldDataBOList.add(new QesFieldDataBO("visionL",AnswerUtil.numberFormat(leftEyeData.getNakedVision(),1)));
        qesFieldDataBOList.add(new QesFieldDataBO("glassL",AnswerUtil.numberFormat(leftEyeData.getCorrectedVision(),1)));
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
            return new ThreeTuple<>("3",AnswerUtil.numberFormat(okr,2),AnswerUtil.numberFormat(okl,2));
        }
        return new ThreeTuple<>(StrUtil.EMPTY,StrUtil.EMPTY,StrUtil.EMPTY);
    }

    /**
     * 屈光数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setComputerOptometry(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        ComputerOptometryDO computerOptometryData = commonDiseaseArchiveCard.getComputerOptometryData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometryData.getRightEyeData();
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometryData.getLeftEyeData();

        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("spherR",AnswerUtil.numberFormat(rightEyeData.getSph(),2)));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinR",AnswerUtil.numberFormat(rightEyeData.getCyl(),2)));
        qesFieldDataBOList.add(new QesFieldDataBO("axisR",AnswerUtil.numberFormat(rightEyeData.getAxial(),0)));
        qesFieldDataBOList.add(new QesFieldDataBO("SER", AnswerUtil.numberFormat(StatUtil.getSphericalEquivalent(rightEyeData.getSph(),rightEyeData.getCyl()),3)));
        qesFieldDataBOList.add(new QesFieldDataBO("spherRT",StrUtil.EMPTY));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinRT",StrUtil.EMPTY));
        qesFieldDataBOList.add(new QesFieldDataBO("axisRT",StrUtil.EMPTY));
        qesFieldDataBOList.add(new QesFieldDataBO("spherL",AnswerUtil.numberFormat(leftEyeData.getSph(),2)));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinL",AnswerUtil.numberFormat(leftEyeData.getCyl(),2)));
        qesFieldDataBOList.add(new QesFieldDataBO("axisL",AnswerUtil.numberFormat(leftEyeData.getAxial(),0)));
        qesFieldDataBOList.add(new QesFieldDataBO("SEL",AnswerUtil.numberFormat(StatUtil.getSphericalEquivalent(leftEyeData.getSph(),leftEyeData.getCyl()),3)));
        qesFieldDataBOList.add(new QesFieldDataBO("spherLT",StrUtil.EMPTY));
        qesFieldDataBOList.add(new QesFieldDataBO("cylinLT",StrUtil.EMPTY));
        qesFieldDataBOList.add(new QesFieldDataBO("axisLT",StrUtil.EMPTY));
        return qesFieldDataBOList;
    }

    /**
     * 其它数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setOther(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        CardInfoVO studentInfo = commonDiseaseArchiveCard.getStudentInfo();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("NOTE",AnswerUtil.textFormat(null)));
        if (Objects.equals(SchoolAge.KINDERGARTEN.code,studentInfo.getSchoolType())){
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
        SaprodontiaStat.StatItem deciduous = saprodontiaData.getSaprodontiaStat().getDeciduous();
        SaprodontiaStat.StatItem permanent = saprodontiaData.getSaprodontiaStat().getPermanent();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q51",AnswerUtil.numberFormat(deciduous.getDCount())));
        qesFieldDataBOList.add(new QesFieldDataBO("q52",AnswerUtil.numberFormat(deciduous.getMCount())));
        qesFieldDataBOList.add(new QesFieldDataBO("q53",AnswerUtil.numberFormat(deciduous.getFCount())));
        qesFieldDataBOList.add(new QesFieldDataBO("q54",AnswerUtil.numberFormat(permanent.getDCount())));
        qesFieldDataBOList.add(new QesFieldDataBO("q55",AnswerUtil.numberFormat(permanent.getDCount())));
        qesFieldDataBOList.add(new QesFieldDataBO("q56",AnswerUtil.numberFormat(permanent.getDCount())));
        return qesFieldDataBOList;
    }

    /**
     * 身高体重数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setHeightAndWeightData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        HeightAndWeightDataDO heightAndWeightData = commonDiseaseArchiveCard.getHeightAndWeightData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q6",AnswerUtil.numberFormat(heightAndWeightData.getHeight(),1)));
        qesFieldDataBOList.add(new QesFieldDataBO("q7",AnswerUtil.numberFormat(heightAndWeightData.getWeight(),1)));
        return qesFieldDataBOList;
    }

    /**
     * 脊柱数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setSpineDataDO(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        SpineDataDO spineData = commonDiseaseArchiveCard.getSpineData();
        TwoTuple<String, String> chest = getSpineItemValue(spineData.getChest());
        TwoTuple<String, String> waist = getSpineItemValue(spineData.getWaist());
        TwoTuple<String, String> chestWaist = getSpineItemValue(spineData.getChestWaist());
        TwoTuple<String, String> entirety = getSpineItemValue(spineData.getEntirety());
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
        return TwoTuple.of(AnswerUtil.numberFormat(spineItem.getType()),AnswerUtil.numberFormat(spineItem.getLevel()));
    }


    /**
     * 血压数据
     * @param commonDiseaseArchiveCard 常见病档案卡
     */
    private List<QesFieldDataBO> setBloodPressureData(CommonDiseaseArchiveCard commonDiseaseArchiveCard){
        BloodPressureDataDO bloodPressureData = commonDiseaseArchiveCard.getBloodPressureData();
        List<QesFieldDataBO> qesFieldDataBOList = Lists.newArrayList();
        qesFieldDataBOList.add(new QesFieldDataBO("q81",AnswerUtil.numberFormat(bloodPressureData.getSbp(),0)));
        qesFieldDataBOList.add(new QesFieldDataBO("q82",AnswerUtil.numberFormat(bloodPressureData.getDbp(),0)));
        return qesFieldDataBOList;
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
            return TwoTuple.of("1",AnswerUtil.textFormat(null));
        }).orElse(TwoTuple.of("1",AnswerUtil.textFormat(null)));
    }
}
