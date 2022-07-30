package com.wupol.myopia.business.aggregation.export.excel.questionnaire.function;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 导出类型构建
 *
 * @author hang.yuan 2022/7/22 11:51
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExportTypeFacade {

    private final DistrictService districtService;
    private final SchoolService schoolService;
    private final ScreeningOrganizationService screeningOrganizationService;

    /**
     * 获取区域Key
     * @param exportCondition 导出条件
     */
    public String getDistrictKey(ExportCondition exportCondition,String key){
        String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
        return getKey(exportCondition, key, districtName);
    }

    /**
     * 获取区域学校Key
     * @param districtId 区域ID
     * @param key 格式化Key
     */
    public String getDistrictKey(Integer districtId,String key){
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        return String.format(key,districtName);
    }

    /**
     * 获取学校Key
     * @param exportCondition 导出条件
     */
    public String getSchoolKey(ExportCondition exportCondition,String key){
        School school = schoolService.getById(exportCondition.getSchoolId());
        if (Objects.isNull(school)){
            throw new BusinessException(String.format("学校不存在: id:%s",exportCondition.getSchoolId()));
        }
        return getKey(exportCondition, key, school.getName());
    }

    /**
     * 共同Key
     *
     * @param exportCondition 导出条件
     * @param key 模板key
     * @param name 填充模板变量
     */
    private String getKey(ExportCondition exportCondition, String key, String name) {
        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        Integer questionnaireType = questionnaireTypeList.get(0);
        String desc;
        if (Objects.equals(questionnaireType, QuestionnaireConstant.STUDENT_TYPE)) {
            desc = QuestionnaireConstant.STUDENT_TYPE_DESC;
        } else {
            QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
            desc = questionnaireTypeEnum.getDesc();
        }

        return String.format(key, name, desc);
    }

    /**
     * 获取机构或者学校Key
     *
     * @param exportCondition 导出条件
     * @param allKey 机构模板key
     * @param schoolKey 学校key
     */
    public String getOrgOrSchoolKey(ExportCondition exportCondition,String allKey,String schoolKey){
        Integer schoolId = exportCondition.getSchoolId();
        if (Objects.isNull(schoolId)){
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(exportCondition.getScreeningOrgId());
            return String.format(allKey,screeningOrganization.getName());
        }else {
            School school = schoolService.getById(schoolId);
            return String.format(schoolKey,school.getName());
        }
    }

    /**
     * 根据导出类型获取导出问卷类型
     *
     * @param exportType 导出类型
     */
    public Map<Integer,String> getQuestionnaireType(Integer exportType){
        Map<Integer,String> typeMap = Maps.newHashMap();
        switch (exportType){
            case 10:
            case 15:
                typeMap.put(QuestionnaireTypeEnum.VISION_SPINE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getDesc());
                typeMap.put(QuestionnaireConstant.STUDENT_TYPE,QuestionnaireConstant.STUDENT_TYPE_DESC);
                break;
            case 11:
            case 13:
                typeMap.put(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType(),QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getDesc());
                typeMap.put(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType(),QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getDesc());
                typeMap.put(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType(),QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getDesc());
                typeMap.put(QuestionnaireTypeEnum.VISION_SPINE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getDesc());
                typeMap.put(QuestionnaireConstant.STUDENT_TYPE,QuestionnaireConstant.STUDENT_TYPE_DESC);
                break;
            case 14:
                typeMap.put(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType(),QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getDesc());
                typeMap.put(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType(),QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getDesc());
                typeMap.put(QuestionnaireTypeEnum.VISION_SPINE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getDesc());
                typeMap.put(QuestionnaireConstant.STUDENT_TYPE,QuestionnaireConstant.STUDENT_TYPE_DESC);
                break;
            case 12:
            default:
                break;
        }
        return typeMap;
    }

    public List<String> getDistrictFolder(ExportCondition exportCondition, String fileName,String key){
        List<String> folderList = Lists.newArrayList();
        mkFolder(fileName, folderList, FileUtil.mainName(FileUtil.newFile(fileName)));
        //按地区时多生成一个学校文件夹
        if (Objects.nonNull(exportCondition.getDistrictId()) ){
            String districtSchoolKey = getDistrictKey(exportCondition.getDistrictId(), key);
            mkFolder(fileName, folderList, districtSchoolKey);
        }
        return folderList;
    }

    /**
     * 创建文件夹
     * @param filePath 文件路径
     * @param folderList 收集文件夹地址集合
     * @param fileName 文件夹名称
     */
    public static void mkFolder(String filePath, List<String> folderList, String fileName) {
        String fileSavePath = Paths.get(filePath, fileName).toString();
        if (!FileUtil.exist(fileSavePath)) {
            FileUtil.mkdir(fileSavePath);
        }
        folderList.add(fileSavePath);
    }
}
