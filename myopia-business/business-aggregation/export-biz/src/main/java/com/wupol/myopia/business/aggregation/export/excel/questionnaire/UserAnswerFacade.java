package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.AnswerConvertValueBuilder;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/7/21 19:50
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserAnswerFacade {

    private final SchoolService schoolService;
    private final DistrictService districtService;


    /**
     * 转换学生值
     * @param generateExcelDataList 生成excel数据集合
     */
    public List<GenerateExcelDataBO> convertStudentValue(List<GenerateExcelDataBO> generateExcelDataList){
        Set<Integer> schoolIds = generateExcelDataList.stream().map(GenerateExcelDataBO::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.listByIds(schoolIds);
        Map<Integer, List<String>> schoolDistrictMap =Maps.newHashMap();
        for (School school : schoolList) {
            schoolDistrictMap.put(school.getId(),getParseDistrict(school.getDistrictAreaCode()));
        }
        Map<Integer, School> schoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity(), (v1, v2) -> v2));
        return AnswerConvertValueBuilder.convertValue(generateExcelDataList,schoolMap,schoolDistrictMap);
    }

    /**
     * 转换政府值
     * @param generateExcelDataList 生成excel数据集合
     */
    public List<GenerateExcelDataBO> convertGovernmentValue(List<GenerateExcelDataBO> generateExcelDataList){
        List<String> governmentKeyList = generateExcelDataList.stream().map(GenerateExcelDataBO::getGovernmentKey).collect(Collectors.toList());
        Map<String, List<String>> governmentDistrictMap =Maps.newHashMap();
        for (String governmentKey : governmentKeyList) {
            governmentDistrictMap.put(governmentKey,getParseDistrict(Long.valueOf(governmentKey.split(StrUtil.UNDERLINE)[2])));
        }
        return AnswerConvertValueBuilder.convertGovernmentValue(generateExcelDataList,governmentDistrictMap);
    }

    /**
     * 解析地区数据
     * @param districtAreaCode 所属区/县行政区域编号
     */
    private List<String> getParseDistrict(Long districtAreaCode){
        if (Objects.isNull(districtAreaCode)){
            return Lists.newArrayList();
        }
        String code = districtAreaCode.toString();
        Map<Long,String> codeMap = Maps.newLinkedHashMap();
        codeMap.put(Long.parseLong(code.substring(0, 2))*10000000,"");
        codeMap.put(Long.parseLong(code.substring(0, 4))*100000,"");
        codeMap.put(Long.parseLong(code.substring(0, 6))*1000,"");
        codeMap.forEach((k,v)->{
            District district = districtService.getDistrictByCode(k, Boolean.FALSE);
            if (Objects.nonNull(district)){
                codeMap.put(k,district.getName());
            }
        });

        return Lists.newArrayList(codeMap.values());
    }
}
