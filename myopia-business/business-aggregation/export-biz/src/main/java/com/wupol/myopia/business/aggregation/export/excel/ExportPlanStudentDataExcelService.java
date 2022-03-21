package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
public class ExportPlanStudentDataExcelService extends BaseExportExcelFileService{
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

    @Autowired
    private ExcelFacade excelFacade;


    @Override
    public List<StatConclusionExportDTO> getExcelData(ExportCondition exportCondition) {
        //这个地方有问题
        Integer screeningPlanId = exportCondition.getPlanId();
        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();

        List<StatConclusionExportDTO> statConclusionExportDTOs  = statConclusionService.selectExportVoBySPlanIdAndSOrgIdAndSChoolIdAndGradeNameAndClassanme(screeningPlanId, screeningOrgId,schoolId,gradeId,classId);
        statConclusionExportDTOs.forEach(vo ->
                vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));

        return statConclusionExportDTOs;
    }

    @Override
    public Class getHeadClass() {
        return VisionScreeningResultExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {

        return getFileNameTitle(exportCondition);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getFileNameTitle(exportCondition);
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
    public File generateExcelFile(String fileName, List data,ExportCondition exportCondition) throws IOException {

        List<StatConclusionExportDTO> statConclusionExportDTOs = data;
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        Map<Integer, List<StatConclusionExportDTO>> schoolMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolId));
        Map<Integer, List<StatConclusionExportDTO>> gradeMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getGradeId));


        //1.学校id为null,区域id为null,筛查计划id不为null按计划维度导出
        if (Objects.isNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getDistrictId()) && Objects.nonNull(exportCondition.getPlanId()) && Objects.nonNull(exportCondition.getScreeningOrgId()) && Objects.isNull(exportCondition.getClassId())){
            String packageFileName = getPackageFileName(exportCondition);

            schoolMap.forEach((key,value)->{
                School school = schoolService.getById(key);
                StringBuffer folder = new StringBuffer();
                folder.append(packageFileName);
                folder.append("/"+getFileNameTitle(exportCondition));
                try {
                    //生成文件
                    ExcelUtil.exportListToExcelWithFolder(folder.toString(), String.format(PLAN_STUDENT_FILE_NAME,school.getName()), excelFacade.genVisionScreeningResultExportVos(value), mergeStrategy, getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        //3.学校id不为null,年级id、班级id为null，则以学校维度导出
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.isNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            School school = schoolService.getById(exportCondition.getSchoolId());

            //先导出整个学校数据
            StringBuffer folder = new StringBuffer();
            folder.append(getPackageFileName(exportCondition));
            folder.append("/"+String.format(PLAN_STUDENT_FILE_NAME,school.getName()));
            ExcelUtil.exportListToExcelWithFolder(folder.toString(), String.format(PLAN_STUDENT_FILE_NAME,school.getName()), excelFacade.genVisionScreeningResultExportVos(schoolMap.get(exportCondition.getSchoolId())), mergeStrategy, getHeadClass());

            //再导出年级数据
            gradeMap.forEach((key,value)->{
                SchoolGrade grade = schoolGradeService.getById(key);
                StringBuffer gradeFolder = new StringBuffer();
                gradeFolder.append("/"+String.format(PLAN_STUDENT_FILE_NAME,grade.getName()));
                try {
                    ExcelUtil.exportListToExcelWithFolder(folder.toString()+ gradeFolder, String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()), excelFacade.genVisionScreeningResultExportVos(value), mergeStrategy, getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
               //再导出该年级的班级数据
                Map<Integer, List<StatConclusionExportDTO>> collect = value.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getClassId));
                collect.forEach((classKey,classValue) ->{
                    SchoolClass schoolClass = schoolClassService.getById(classKey);
                    try {
                        ExcelUtil.exportListToExcelWithFolder(folder.toString()+ gradeFolder, String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName()), excelFacade.genVisionScreeningResultExportVos(classValue), mergeStrategy, getHeadClass());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }

        //4.学校id不为null,年级id不为null、班级id为null，则以年级维度导出
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            School school = schoolService.getById(exportCondition.getSchoolId());
            StringBuffer folder = new StringBuffer();
            folder.append(getPackageFileName(exportCondition));
            //导出年级数据
            gradeMap.forEach((key,value)->{
                SchoolGrade grade = schoolGradeService.getById(key);
                StringBuffer gradeFolder = new StringBuffer();
                gradeFolder.append("/"+String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()));
                try {
                    ExcelUtil.exportListToExcelWithFolder(folder.toString()+ gradeFolder, String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()), excelFacade.genVisionScreeningResultExportVos(value), mergeStrategy, getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //再导出该年级的班级数据
                Map<Integer, List<StatConclusionExportDTO>> collect = value.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getClassId));
                collect.forEach((classKey,classValue) ->{
                    SchoolClass schoolClass = schoolClassService.getById(classKey);
                    try {
                        ExcelUtil.exportListToExcelWithFolder(folder.toString()+ gradeFolder, String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName()), excelFacade.genVisionScreeningResultExportVos(classValue), mergeStrategy, getHeadClass());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }

        //如果班级不为null,则以班级维度导出
        if (Objects.nonNull(exportCondition.getClassId())){
            File file = null;
            StringBuffer folder = new StringBuffer();
            folder.append(excelSavePath);
            folder.append(getPackageFileName(exportCondition));
            log.info("文件生成路径："+folder.toString());
            log.info("文件名："+getFileNameTitle(exportCondition));
            try {
                file = ExcelUtil.exportListToExcelWithFolder(folder.toString(), getFileNameTitle(exportCondition), excelFacade.genVisionScreeningResultExportVos(data), mergeStrategy, getHeadClass());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return file;
        }
        return null;
    }


    /**
     * 获取文件同步导出文件名称
     * @param exportCondition
     * @return
     */
    private String getFileNameTitle(ExportCondition exportCondition){
        School school = schoolService.getById(exportCondition.getSchoolId());
        SchoolGrade grade = schoolGradeService.getById(exportCondition.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());
        String screeningOrg = screeningOrganizationService.getNameById(exportCondition.getScreeningOrgId());
        //.获取计划维度导出文件名
        if (Objects.isNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getDistrictId()) && Objects.nonNull(exportCondition.getPlanId()) && Objects.nonNull(exportCondition.getScreeningOrgId())){
            return String.format(PLAN_STUDENT_FILE_NAME,screeningOrg);
        }
        //.获取学校导出文件名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.isNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            return String.format(PLAN_STUDENT_FILE_NAME,school.getName());
        }
        //.获取年级导出文件名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            return String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName());

        }
        //.获取班级导出文件名
        if (Objects.nonNull(exportCondition.getClassId())){
            return String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName());
        }

            return "";
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition){
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
    public String getPackageFileName(ExportCondition exportCondition){
        School school = schoolService.getById(exportCondition.getSchoolId());
        SchoolGrade grade = schoolGradeService.getById(exportCondition.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());

        //计划压缩包名
        if (Objects.isNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getDistrictId()) && Objects.nonNull(exportCondition.getPlanId()) && Objects.nonNull(exportCondition.getScreeningOrgId())){
            String screeningOrgName = screeningOrganizationService.getNameById(exportCondition.getScreeningOrgId());
            String format =UUID.randomUUID() + "/" + String.format(SCREENING_ORG_EXCEL_FILE_NAME, screeningOrgName);
            localVar.set(format);
            return format;
        }

        //学校压缩包名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.isNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            String format =UUID.randomUUID() + "/" + String.format(SCHOOL_EXCEL_FILE_NAME,school.getName());
            localVar.set(format);
            return format;
        }

        //年级压缩包名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            String format =UUID.randomUUID() + "/" + String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName());
            localVar.set(format);
            return format;
        }

        //班级压缩包名
        if (Objects.nonNull(exportCondition.getClassId())){
            String format =UUID.randomUUID() + "/" + String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName());
            localVar.set(format);
            return format;
        }

            return "";
  }

  @Override
  public Boolean isPackage(){
      return true;
  }
}
