package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.config.ScreeningDataFactory;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.constants.ScreeningTypeConst;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2021/12/30/17:13
 * @Description: 帮新谋重构的代码，不知道是干啥用 ps:筛查计划-筛查数据导出
 */
@Log4j2
@Service("exportPlanStudentDataExcelService")
public class ExportPlanStudentDataExcelService extends BaseExportExcelFileService {
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private ScreeningNoticeService screeningNoticeService;


    @Override
    public List<StatConclusionExportDTO> getExcelData(ExportCondition exportCondition) {
        //这个地方有问题
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        Integer notificationId = exportCondition.getNotificationId();

        if (ExportTypeConst.District.equals(exportCondition.getExportType())) {
            Integer districtId = exportCondition.getDistrictId();
            List<Integer> childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            return statConclusionService.getExportVoByScreeningNoticeIdAndDistrictIds(notificationId, childDistrictIds);
        }

        List<StatConclusionExportDTO> statConclusionExportDTOs = statConclusionService.selectExportVoBySPlanIdAndSOrgIdAndSChoolIdAndGradeNameAndClassanme(screeningPlanId, screeningOrgId, schoolId, gradeId, classId);
        statConclusionExportDTOs.forEach(vo ->
                vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));

        return statConclusionExportDTOs;
    }

    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        IScreeningDataService screeningDataService = ScreeningDataFactory.getScreeningDataService(getScreeningType(exportCondition));
        return screeningDataService.getExportClass();
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        String suffix = StringUtils.EMPTY;
        Integer screeningType = getScreeningType(exportCondition);
        if (ScreeningTypeConst.VISION.equals(screeningType)) {
            suffix = "【视力数据】";
        }
        if (ScreeningTypeConst.COMMON_DISEASE.equals(screeningType)) {
            suffix = "【常见病数据】";
        }
        return getFileNameTitle(exportCondition) + suffix;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getPackageFileName(exportCondition);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer userId = exportCondition.getApplyExportFileUserId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        return String.format(RedisConstant.FILE_EXPORT_PLAN_SCREENING_DATA, screeningPlanId, screeningOrgId, schoolId, userId, gradeId, classId);
    }

    @Override
    public File generateExcelFile(String filePath, List data, ExportCondition exportCondition) {

        List<StatConclusionExportDTO> statConclusionExportDTOs = data;
        Map<Integer, List<StatConclusionExportDTO>> schoolMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolId));
        Map<Integer, List<StatConclusionExportDTO>> gradeMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getGradeId));

        Integer exportType = exportCondition.getExportType();
        Integer screeningType = screeningPlanService.getById(statConclusionExportDTOs.get(0).getPlanId()).getScreeningType();
        IScreeningDataService screeningDataService = ScreeningDataFactory.getScreeningDataService(screeningType);


        // 按计划维度导出
        if (ExportTypeConst.PLAN.equals(exportType)) {
            schoolMap.forEach((key, value) -> {
                School school = schoolService.getById(key);
                String path = Paths.get(filePath, getFileNameTitle(exportCondition)).toString();
                makerExcel(path, String.format(PLAN_STUDENT_FILE_NAME, school.getName()), screeningDataService.generateExportData(value), screeningDataService.getExportClass());
            });
        }

        // 学校维度导出
        if (ExportTypeConst.SCHOOL.equals(exportType)) {
            School school = schoolService.getById(exportCondition.getSchoolId());
            //先导出整个学校数据
            String folder = Paths.get(filePath, String.format(PLAN_STUDENT_FILE_NAME, school.getName())).toString();
            makerExcel(folder, String.format(PLAN_STUDENT_FILE_NAME, school.getName()), screeningDataService.generateExportData(schoolMap.get(exportCondition.getSchoolId())), screeningDataService.getExportClass());
            //再导出年级数据
            gradeMap.forEach((key, value) -> {
                SchoolGrade grade = schoolGradeService.getById(key);
                excelGraderAndClassData(key, value, String.format(PLAN_STUDENT_FILE_NAME, grade.getName()), folder, school);
            });
        }

        // 年级维度导出
        if (ExportTypeConst.GRADE.equals(exportType)) {
            School school = schoolService.getById(exportCondition.getSchoolId());
            //导出年级数据
            gradeMap.forEach((key, value) -> {
                SchoolGrade grade = schoolGradeService.getById(key);
                excelGraderAndClassData(key, value, String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName()), filePath, school);
            });
        }

        // 班级、区域维度导出
        if (ExportTypeConst.CLASS.equals(exportType) || ExportTypeConst.District.equals(exportType)) {
            return makerExcel(filePath, getFileNameTitle(exportCondition), screeningDataService.generateExportData(data), screeningDataService.getExportClass());
        }
        return null;
    }


    public void excelGraderAndClassData(Integer key, List<StatConclusionExportDTO> value, String gradeFolder, String filePath, School school) {

        Integer screeningType = screeningPlanService.getById(value.get(0).getPlanId()).getScreeningType();
        IScreeningDataService screeningDataService = ScreeningDataFactory.getScreeningDataService(screeningType);

        SchoolGrade grade = schoolGradeService.getById(key);
        String path = Paths.get(filePath, gradeFolder).toString();
        makerExcel(path, gradeFolder, screeningDataService.generateExportData(value), screeningDataService.getExportClass());
        //再导出该年级的班级数据
        Map<Integer, List<StatConclusionExportDTO>> collect = value.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getClassId));
        collect.forEach((classKey, classValue) -> {
            SchoolClass schoolClass = schoolClassService.getById(classKey);
            makerExcel(path, String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName() + schoolClass.getName()), screeningDataService.generateExportData(classValue), screeningDataService.getExportClass());
        });
    }

    public File makerExcel(String folder, String filePath, List data, Class clazz) {
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        try {
            return ExcelUtil.exportListToExcelWithFolder(folder, filePath, data, mergeStrategy, clazz);
        } catch (IOException e) {
            log.error("【Excel生成异常】{}", data, e);
        }
        return null;
    }


    /**
     * 获取文件同步导出文件名称
     *
     * @param exportCondition
     * @return
     */
    private String getFileNameTitle(ExportCondition exportCondition) {
        Integer exportType = exportCondition.getExportType();
        School school = schoolService.getById(exportCondition.getSchoolId());
        SchoolGrade grade = schoolGradeService.getById(exportCondition.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());
        Integer districtId = exportCondition.getDistrictId();
        String screeningOrg = screeningOrganizationService.getNameById(exportCondition.getScreeningOrgId());

        if (ExportTypeConst.PLAN.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, screeningOrg);
        }
        if (ExportTypeConst.SCHOOL.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, school.getName());
        }
        if (ExportTypeConst.GRADE.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName());
        }
        if (ExportTypeConst.CLASS.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName() + schoolClass.getName());
        }
        if (ExportTypeConst.District.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, districtService.getById(districtId).getName());
        }
        return "";
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Integer exportType = exportCondition.getExportType();
        if (ExportTypeConst.District.equals(exportType)) {
            return;
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(exportCondition.getPlanId());
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("筛查计划不存在");
        }
    }

    /**
     * 获取压缩包名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getPackageFileName(ExportCondition exportCondition) {
        Integer exportType = exportCondition.getExportType();
        Integer districtId = exportCondition.getDistrictId();
        School school = schoolService.getById(exportCondition.getSchoolId());
        SchoolGrade grade = schoolGradeService.getById(exportCondition.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());

        if (ExportTypeConst.PLAN.equals(exportType)) {
            String screeningOrgName = screeningOrganizationService.getNameById(exportCondition.getScreeningOrgId());
            return String.format(SCREENING_ORG_EXCEL_FILE_NAME, screeningOrgName);
        }
        if (ExportTypeConst.SCHOOL.equals(exportType)) {
            return String.format(SCHOOL_EXCEL_FILE_NAME, school.getName());
        }
        if (ExportTypeConst.GRADE.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName());
        }
        if (ExportTypeConst.CLASS.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, school.getName() + grade.getName() + schoolClass.getName());
        }
        if (ExportTypeConst.District.equals(exportType)) {
            return String.format(PLAN_STUDENT_FILE_NAME, districtService.getById(districtId).getName());
        }
        return "";
    }

    @Override
    public Boolean isPackage() {
        return true;
    }

    /**
     * 获取筛查类型
     *
     * @param exportCondition 条件
     * @return 筛查类型
     */
    private Integer getScreeningType(ExportCondition exportCondition) {
        Integer screeningType;
        // 如果是区域筛查导出的，取通知的screeningType
        if (ExportTypeConst.District.equals(exportCondition.getExportType())) {
            screeningType = screeningNoticeService.getById(exportCondition.getNotificationId()).getScreeningType();
        } else {
            screeningType = screeningPlanService.getById(exportCondition.getPlanId()).getScreeningType();
        }
        return screeningType;
    }
}
