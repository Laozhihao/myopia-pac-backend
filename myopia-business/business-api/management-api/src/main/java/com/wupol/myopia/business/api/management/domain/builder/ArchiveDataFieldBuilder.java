package com.wupol.myopia.business.api.management.domain.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.common.utils.constant.SchoolTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 监测表字段数据
 *
 * @author hang.yuan 2022/8/25 19:55
 */
@UtilityClass
public class ArchiveDataFieldBuilder {

    private static final Map<Integer, List<QesArchiveField>> DATA_MAP;

    static {
        DATA_MAP = initDataMap();
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    protected class QesArchiveField{
        private String qesField;
        private String systemField;
    }

    public static Map<String,String> getArchiveDataMap(Integer schoolType){
        List<QesArchiveField> archiveFieldList = DATA_MAP.get(schoolType);
        return archiveFieldList.stream().collect(Collectors.toMap(QesArchiveField::getQesField, QesArchiveField::getSystemField));
    }

    private static Map<Integer, List<QesArchiveField>> initDataMap(){
        Map<Integer, List<QesArchiveField>> dataMap = Maps.newHashMap();
        dataMap.put(SchoolTypeEnum.KINDERGARTEN.getType(), getKindergarten());
        dataMap.put(SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType(), getPrimaryAndSecondary());
        dataMap.put(SchoolTypeEnum.UNIVERSITY.getType(), getUniversity());
        return dataMap;
    }

    private List<QesArchiveField> getKindergarten(){
        List<QesArchiveField> qesArchiveFieldList = Lists.newArrayList();
        setStudentCommonDiseaseIdInfo(qesArchiveFieldList);
        setVisionData(qesArchiveFieldList);
        setComputerOptometry(qesArchiveFieldList);
        qesArchiveFieldList.add(new QesArchiveField("NOTE",""));
        setNameData(qesArchiveFieldList);
        return qesArchiveFieldList;
    }

    private List<QesArchiveField> getPrimaryAndSecondary(){
        List<QesArchiveField> qesArchiveFieldList = Lists.newArrayList();
        setStudentCommonDiseaseIdInfo(qesArchiveFieldList);
        setDiseasesHistoryData(qesArchiveFieldList);
        setVisionData(qesArchiveFieldList);
        setComputerOptometry(qesArchiveFieldList);

        qesArchiveFieldList.add(new QesArchiveField("NOTE",""));
        qesArchiveFieldList.add(new QesArchiveField("q51",""));
        qesArchiveFieldList.add(new QesArchiveField("q52",""));
        qesArchiveFieldList.add(new QesArchiveField("q53",""));
        setSaprodontiaData(qesArchiveFieldList);

        qesArchiveFieldList.add(new QesArchiveField("q6",""));
        qesArchiveFieldList.add(new QesArchiveField("q7",""));
        qesArchiveFieldList.add(new QesArchiveField("qx2",""));
        qesArchiveFieldList.add(new QesArchiveField("qx21",""));
        qesArchiveFieldList.add(new QesArchiveField("qx3",""));
        qesArchiveFieldList.add(new QesArchiveField("qx31",""));
        qesArchiveFieldList.add(new QesArchiveField("qx4",""));
        qesArchiveFieldList.add(new QesArchiveField("qx41",""));
        qesArchiveFieldList.add(new QesArchiveField("qx1",""));
        qesArchiveFieldList.add(new QesArchiveField("qx11",""));
        setBloodPressureDataAndPrivacyData(qesArchiveFieldList);

        setNameData(qesArchiveFieldList);
        setDateData(qesArchiveFieldList);
        return qesArchiveFieldList;
    }

    private List<QesArchiveField> getUniversity(){
        List<QesArchiveField> qesArchiveFieldList = Lists.newArrayList();
        setStudentCommonDiseaseIdInfo(qesArchiveFieldList);
        setDiseasesHistoryData(qesArchiveFieldList);
        qesArchiveFieldList.add(new QesArchiveField("visionR",""));
        qesArchiveFieldList.add(new QesArchiveField("visionL",""));
        setSaprodontiaData(qesArchiveFieldList);
        qesArchiveFieldList.add(new QesArchiveField("q6",""));
        qesArchiveFieldList.add(new QesArchiveField("q7",""));
        setBloodPressureDataAndPrivacyData(qesArchiveFieldList);
        setNameData(qesArchiveFieldList);
        setDateData(qesArchiveFieldList);
        return qesArchiveFieldList;
    }


    private void setStudentCommonDiseaseIdInfo(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("ID1",""));
        qesArchiveFieldList.add(new QesArchiveField("province",""));
        qesArchiveFieldList.add(new QesArchiveField("city",""));
        qesArchiveFieldList.add(new QesArchiveField("district",""));
        qesArchiveFieldList.add(new QesArchiveField("county",""));
        qesArchiveFieldList.add(new QesArchiveField("point",""));
        qesArchiveFieldList.add(new QesArchiveField("school",""));
        qesArchiveFieldList.add(new QesArchiveField("grade",""));
        qesArchiveFieldList.add(new QesArchiveField("num",""));
        qesArchiveFieldList.add(new QesArchiveField("ID2",""));
        qesArchiveFieldList.add(new QesArchiveField("gender",""));
        qesArchiveFieldList.add(new QesArchiveField("nation",""));
        qesArchiveFieldList.add(new QesArchiveField("nationother",""));
        qesArchiveFieldList.add(new QesArchiveField("birth",""));
        qesArchiveFieldList.add(new QesArchiveField("examine",""));
    }


    private void setVisionData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("glasstype",""));
        qesArchiveFieldList.add(new QesArchiveField("OKR",""));
        qesArchiveFieldList.add(new QesArchiveField("OKL",""));
        qesArchiveFieldList.add(new QesArchiveField("visionR",""));
        qesArchiveFieldList.add(new QesArchiveField("glassR",""));
        qesArchiveFieldList.add(new QesArchiveField("visionL",""));
        qesArchiveFieldList.add(new QesArchiveField("glassL",""));
    }

    private void setComputerOptometry(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("spherR",""));
        qesArchiveFieldList.add(new QesArchiveField("cylinR",""));
        qesArchiveFieldList.add(new QesArchiveField("axisR",""));
        qesArchiveFieldList.add(new QesArchiveField("SER",""));
        qesArchiveFieldList.add(new QesArchiveField("spherRT",""));
        qesArchiveFieldList.add(new QesArchiveField("cylinRT",""));
        qesArchiveFieldList.add(new QesArchiveField("axisRT",""));
        qesArchiveFieldList.add(new QesArchiveField("spherL",""));
        qesArchiveFieldList.add(new QesArchiveField("cylinL",""));
        qesArchiveFieldList.add(new QesArchiveField("axisL",""));
        qesArchiveFieldList.add(new QesArchiveField("SEL",""));
        qesArchiveFieldList.add(new QesArchiveField("spherLT",""));
        qesArchiveFieldList.add(new QesArchiveField("cylinLT",""));
        qesArchiveFieldList.add(new QesArchiveField("axisLT",""));
    }

    private void setDiseasesHistoryData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("q31",""));
        qesArchiveFieldList.add(new QesArchiveField("q32",""));
        qesArchiveFieldList.add(new QesArchiveField("q33",""));
        qesArchiveFieldList.add(new QesArchiveField("q34",""));
        qesArchiveFieldList.add(new QesArchiveField("q35",""));
        qesArchiveFieldList.add(new QesArchiveField("q36",""));
        qesArchiveFieldList.add(new QesArchiveField("q37",""));
        qesArchiveFieldList.add(new QesArchiveField("q38",""));
    }

    private void setNameData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("name",""));
    }

    private void setDateData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("date",""));
    }

    private void setSaprodontiaData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("q54",""));
        qesArchiveFieldList.add(new QesArchiveField("q55",""));
        qesArchiveFieldList.add(new QesArchiveField("q56",""));
    }

    private void setBloodPressureDataAndPrivacyData(List<QesArchiveField> qesArchiveFieldList){
        qesArchiveFieldList.add(new QesArchiveField("q81",""));
        qesArchiveFieldList.add(new QesArchiveField("q82",""));
        qesArchiveFieldList.add(new QesArchiveField("q91",""));
        qesArchiveFieldList.add(new QesArchiveField("q911",""));
        qesArchiveFieldList.add(new QesArchiveField("q92",""));
        qesArchiveFieldList.add(new QesArchiveField("q921",""));
    }

}
