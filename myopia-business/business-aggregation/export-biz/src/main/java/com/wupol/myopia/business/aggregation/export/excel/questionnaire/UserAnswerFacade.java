package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FileNameCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.AnswerConvertValueBuilder;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
@Service
public class UserAnswerFacade {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private QuestionnaireFactory questionnaireFactory;


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

    /**
     * 生成学生excel
     * @param generateDataCondition 生成数据条件
     * @param fileNameCondition 生成文件名称条件
     * @param exportPrimarySchoolTemplate 导出excel模板
     * @param fileName 文件或者文件名
     */
    public void generateStudentExcel(GenerateDataCondition generateDataCondition,
                                     FileNameCondition fileNameCondition,
                                     Resource exportPrimarySchoolTemplate,
                                     String fileName) throws IOException {
        Answer answerService = questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());
        generateDataCondition.setFileType(QuestionnaireConstant.EXCEL_FILE);
        List<GenerateExcelDataBO> generateExcelDataBOList = answerService.getExcelData(generateDataCondition);
        if (CollUtil.isEmpty(generateExcelDataBOList)){
            return;
        }

        generateExcelDataBOList = convertStudentValue(generateExcelDataBOList);

        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataBOList) {
            fileNameCondition.setSchoolId(generateExcelDataBO.getSchoolId());
            String excelFileName = answerService.getFileName(fileNameCondition);
            String file = FileUtils.getFileSavePath(fileName, excelFileName);
            ExcelUtil.exportExcel(file, exportPrimarySchoolTemplate.getInputStream(),generateExcelDataBO.getDataList());
        }
    }

    /**
     * 生成学生rec
     * @param generateDataCondition 生成数据条件
     * @param fileNameCondition 生成文件名称条件
     * @param fileName 文件或者文件名
     */
    public void generateStudentRec(GenerateDataCondition generateDataCondition,
                                   FileNameCondition fileNameCondition,
                                   String fileName){
        Answer answerService = questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());
        generateDataCondition.setFileType(QuestionnaireConstant.REC_FILE);
        List<GenerateRecDataBO> generateRecDataBOList = answerService.getRecData(generateDataCondition);
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }
        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            fileNameCondition.setSchoolId(generateRecDataBO.getSchoolId());
            String recFileName = answerService.getFileName(fileNameCondition);
            answerService.exportRecFile(fileName, generateRecDataBO,recFileName);
        }
    }
}
