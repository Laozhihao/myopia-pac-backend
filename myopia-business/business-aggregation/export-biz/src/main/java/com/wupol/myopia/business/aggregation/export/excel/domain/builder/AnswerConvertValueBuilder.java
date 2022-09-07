package com.wupol.myopia.business.aggregation.export.excel.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.core.questionnaire.constant.DropSelectEnum;
import com.wupol.myopia.business.core.school.constant.AreaTypeEnum;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.MonitorTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 答案转换值
 *
 * @author hang.yuan 2022/9/2 20:13
 */
@UtilityClass
public class AnswerConvertValueBuilder {

    private static final String DISTRICT = "district";
    private static final String POINT = "point";
    private static final String PROVINCE = "province";
    private static final String CITY = "city";
    private static final String COUNTY = "county";
    private static final String A01 = "a01";
    private static final String A011 = "a011";
    private static final String SCHOOL_NAME = "schoolName";
    private static final String I1 = "i1";
    private static final String ID1 = "id1";

    private static final String DJ211 ="dj211";
    private static final String DJ221 ="dj221";
    private static final String DJ231 ="dj231";
    private static final String DJ241 ="dj241";
    private static final String DJ251 ="dj251";
    private static final String DY211 ="dy211";
    private static final String DY221 ="dy221";
    private static final String DY231 ="dy231";
    private static final String DY241 ="dy241";
    private static final String DY251 ="dy251";


    private static final String B511 ="b511";
    private static final String B521 ="b521";
    private static final String B531 ="b531";
    private static final String B541 ="b541";

    private static final String B512 ="b512";
    private static final String B522 ="b522";
    private static final String B532 ="b532";
    private static final String B542 ="b542";

    private static final String B514 ="b514";
    private static final String B524 ="b524";
    private static final String B534 ="b534";
    private static final String B544 ="b544";

    private static final String B515 ="b515";
    private static final String B525 ="b525";
    private static final String B535 ="b535";
    private static final String B545 ="b545";

    private static final String B516 ="b516";
    private static final String B526 ="b526";
    private static final String B536 ="b536";
    private static final String B546 ="b546";


    /**
     * 转换值
     * @param generateExcelDataList 生成excel数据集合
     * @param schoolMap 学校集合
     * @param schoolDistrictMap 学校地区集合
     */
    public List<GenerateExcelDataBO> convertValue(List<GenerateExcelDataBO> generateExcelDataList,
                                                  Map<Integer, School> schoolMap, Map<Integer, List<String>> schoolDistrictMap){
        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataList) {
            List<JSONObject> dataExcelList = generateExcelDataBO.getDataList();
            School school = schoolMap.get(generateExcelDataBO.getSchoolId());
            List<String> districtList = schoolDistrictMap.get(generateExcelDataBO.getSchoolId());
            JSONObject studentData = dataExcelList.get(0);
            if (studentData.containsKey(A01)) {
                dataExcelList.sort(Comparator.comparing(o -> o.getInteger(A01)));
            }
            for (JSONObject jsonObject : dataExcelList) {
                convertDistrictValue(districtList, jsonObject);
                convertSchoolValue(school, jsonObject);
                convertTeacherSelectValue(jsonObject);
                jsonObject.put(SCHOOL_NAME,school.getName());
            }
            generateExcelDataBO.setDataList(dataExcelList);
        }

        return generateExcelDataList;
    }

    /**
     * 政府值转换
     * @param generateExcelDataList 生成excel数据集合
     * @param governmentDistrictMap 地区集合
     */
    public List<GenerateExcelDataBO> convertGovernmentValue(List<GenerateExcelDataBO> generateExcelDataList,Map<String, List<String>> governmentDistrictMap){
        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataList) {
            List<JSONObject> dataExcelList = generateExcelDataBO.getDataList();
            List<String> districtList = governmentDistrictMap.get(generateExcelDataBO.getGovernmentKey());
            for (JSONObject jsonObject : dataExcelList) {
                convertDistrictValue(districtList, jsonObject);
                convertGovernmentValue(jsonObject);
                convertSelectValue(jsonObject);
            }
            generateExcelDataBO.setDataList(dataExcelList);
        }

        return generateExcelDataList;
    }


    /**
     * 学校值转换
     * @param school 学校对象
     * @param jsonObject 转换对象
     */
    private static void convertSchoolValue(School school, JSONObject jsonObject) {
        jsonObject.computeIfPresent(DISTRICT,(k,v)->getDistrict(school.getAreaType()));
        jsonObject.computeIfPresent(POINT,(k,v)->getPoint(school.getMonitorType()));
        jsonObject.putIfAbsent(I1, 0);
        if (jsonObject.containsKey(A01)){
            String gradeCode = jsonObject.getString(A01);
            gradeCode = gradeCode.length()==1?"0"+gradeCode:gradeCode;
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            jsonObject.put(A01,gradeCodeEnum.getName());
        }
        if (jsonObject.containsKey(A011) && jsonObject.containsKey(ID1)){
            String id = jsonObject.getString(ID1);
            jsonObject.put(A011,id.substring(12,16));
        }
    }

    /**
     * 政府值转换
     * @param jsonObject 转换对象
     */
    private static void convertGovernmentValue(JSONObject jsonObject) {
        jsonObject.computeIfPresent(DISTRICT,(k,v)->getDistrict(jsonObject.getInteger(k)));
        jsonObject.computeIfPresent(POINT,(k,v)->getPoint(jsonObject.getInteger(k)));
    }


    /**
     * 下拉框值转换（甲乙、丙病）
     * @param jsonObject 转换对象
     */
    private static void convertSelectValue(JSONObject jsonObject) {
        BiFunction<String, Object, String> abFunction = (k, v) -> getAbDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(DJ211, abFunction);
        jsonObject.computeIfPresent(DJ221, abFunction);
        jsonObject.computeIfPresent(DJ231, abFunction);
        jsonObject.computeIfPresent(DJ241, abFunction);
        jsonObject.computeIfPresent(DJ251, abFunction);

        BiFunction<String, Object, String> cFunction = (k, v) -> getCDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(DY211, cFunction);
        jsonObject.computeIfPresent(DY221, cFunction);
        jsonObject.computeIfPresent(DY231, cFunction);
        jsonObject.computeIfPresent(DY241, cFunction);
        jsonObject.computeIfPresent(DY251, cFunction);
    }

    /**
     * 教师表格下拉框值转换
     * @param jsonObject 转换对象
     */
    private static void convertTeacherSelectValue(JSONObject jsonObject) {
        BiFunction<String, Object, String> teacherType = (k, v) -> getTeacherTypeDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(B511, teacherType);
        jsonObject.computeIfPresent(B521, teacherType);
        jsonObject.computeIfPresent(B531, teacherType);
        jsonObject.computeIfPresent(B541, teacherType);

        BiFunction<String, Object, String> workType = (k, v) -> getWorkTypeDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(B512, workType);
        jsonObject.computeIfPresent(B522, workType);
        jsonObject.computeIfPresent(B532, workType);
        jsonObject.computeIfPresent(B542, workType);

        BiFunction<String, Object, String> educationType = (k, v) -> getEducationTypeDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(B514, educationType);
        jsonObject.computeIfPresent(B524, educationType);
        jsonObject.computeIfPresent(B534, educationType);
        jsonObject.computeIfPresent(B544, educationType);

        BiFunction<String, Object, String> jobTitle = (k, v) -> getJobTitleDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(B515, jobTitle);
        jsonObject.computeIfPresent(B525, jobTitle);
        jsonObject.computeIfPresent(B535, jobTitle);
        jsonObject.computeIfPresent(B545, jobTitle);

        BiFunction<String, Object, String> qc = (k, v) -> getQcDropSelectName(jsonObject.getString(k));
        jsonObject.computeIfPresent(B516, qc);
        jsonObject.computeIfPresent(B526, qc);
        jsonObject.computeIfPresent(B536, qc);
        jsonObject.computeIfPresent(B546, qc);
    }

    /**
     * 甲乙病下拉框值转换
     * @param value 值
     */
    private String getAbDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getAbDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 丙病下拉框值转换
     * @param value 值
     */
    private String getCDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getCDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 教师-类型下拉框值转换
     * @param value 值
     */
    private String getTeacherTypeDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getTeacherTypeDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 教师-专/兼职下拉框值转换
     * @param value 值
     */
    private String getWorkTypeDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getWorkTypeDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 教师-学历下拉框值转换
     * @param value 值
     */
    private String getEducationTypeDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getEducationTypeDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 教师-职称下拉框值转换
     * @param value 值
     */
    private String getJobTitleDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getJobTitleDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 教师-执业资格证书下拉框值转换
     * @param value 值
     */
    private String getQcDropSelectName(String value){
        return Optional.ofNullable(value)
                .map(code-> Optional.ofNullable(DropSelectEnum.getQcDropSelect(code))
                        .map(DropSelectEnum::getLabel).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }

    /**
     * 获取片区值
     * @param district 片区值
     */
    private String getDistrict(Integer district){
        return Optional.ofNullable(district)
                .map(type-> Optional.ofNullable(AreaTypeEnum.get(type))
                        .map(AreaTypeEnum::getName).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }
    /**
     * 获取监测点值
     * @param point 监测点值
     */
    private String getPoint(Integer point){
        return Optional.ofNullable(point)
                .map(type-> Optional.ofNullable(MonitorTypeEnum.get(type))
                        .map(MonitorTypeEnum::getName).orElse(StrUtil.EMPTY))
                .orElse(StrUtil.EMPTY);
    }
    /**
     * 区域值转换
     * @param districtList 区域集合
     * @param jsonObject 转换对象
     */
    private static void convertDistrictValue(List<String> districtList, JSONObject jsonObject) {
        jsonObject.computeIfPresent(PROVINCE, (k,v)->getDistrictName(districtList,0));
        jsonObject.computeIfPresent(CITY, (k,v)->getDistrictName(districtList,1));
        jsonObject.computeIfPresent(COUNTY, (k,v)->getDistrictName(districtList,2));
    }


    /**
     * 获取地区名称
     * @param districtList 区域名称集合
     * @param index 下标
     */
    private static String getDistrictName(List<String> districtList ,Integer index){
        return CollUtil.isNotEmpty(districtList) ? districtList.get(index):StrUtil.EMPTY;
    }

}
