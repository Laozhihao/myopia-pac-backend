package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
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
import java.nio.file.Paths;
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
    public File generateExcelFile(String filePath, List data,ExportCondition exportCondition){

        List<StatConclusionExportDTO> statConclusionExportDTOs = data;
        Map<Integer, List<StatConclusionExportDTO>> schoolMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolId));
        Map<Integer, List<StatConclusionExportDTO>> gradeMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getGradeId));


        //1.学校id为null,区域id为null,筛查计划id不为null按计划维度导出
        if (Objects.isNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getDistrictId()) && Objects.nonNull(exportCondition.getPlanId()) && Objects.nonNull(exportCondition.getScreeningOrgId()) && Objects.isNull(exportCondition.getClassId())){
            schoolMap.forEach((key,value)->{
                School school = schoolService.getById(key);
                String path = Paths.get(filePath, getFileNameTitle(exportCondition)).toString();
                makerExcel(path, String.format(PLAN_STUDENT_FILE_NAME,school.getName()), excelFacade.genVisionScreeningResultExportVos(value));
            });
        }

        //3.学校id不为null,年级id、班级id为null，则以学校维度导出
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.isNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            School school = schoolService.getById(exportCondition.getSchoolId());
            //先导出整个学校数据
            String folder = Paths.get(filePath,String.format(PLAN_STUDENT_FILE_NAME,school.getName())).toString();
            makerExcel(folder, String.format(PLAN_STUDENT_FILE_NAME,school.getName()), excelFacade.genVisionScreeningResultExportVos(schoolMap.get(exportCondition.getSchoolId())));
            //再导出年级数据
            gradeMap.forEach((key,value)->{
                SchoolGrade grade = schoolGradeService.getById(key);
                excelGraderAndClassData(key,value,String.format(PLAN_STUDENT_FILE_NAME,grade.getName()),folder,school);
            });
        }

        //4.学校id不为null,年级id不为null、班级id为null，则以年级维度导出
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            School school = schoolService.getById(exportCondition.getSchoolId());
            //导出年级数据
            gradeMap.forEach((key,value)->{
                SchoolGrade grade = schoolGradeService.getById(key);
                excelGraderAndClassData(key,value,String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()),filePath,school);
            });
        }

        //如果班级不为null,则以班级维度导出
        if (Objects.nonNull(exportCondition.getClassId())){
            return  makerExcel(filePath, getFileNameTitle(exportCondition), excelFacade.genVisionScreeningResultExportVos(data));
        }
        return null;
    }



    public void excelGraderAndClassData(Integer key,List<StatConclusionExportDTO> value,String gradeFolder,String filePath,School school){
        SchoolGrade grade = schoolGradeService.getById(key);
        String path = Paths.get(filePath,gradeFolder).toString();
        makerExcel(path, gradeFolder, excelFacade.genVisionScreeningResultExportVos(value));
        //再导出该年级的班级数据
        Map<Integer, List<StatConclusionExportDTO>> collect = value.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getClassId));
        collect.forEach((classKey,classValue) ->{
            SchoolClass schoolClass = schoolClassService.getById(classKey);
            makerExcel(path, String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName()), excelFacade.genVisionScreeningResultExportVos(classValue));
        });
    }

    public File makerExcel(String folder,String filePath,List<VisionScreeningResultExportDTO> data){
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        try {
            return  ExcelUtil.exportListToExcelWithFolder(folder,filePath, data, mergeStrategy, getHeadClass());
        } catch (IOException e) {
            log.error("【Excel生成异常】{}", data, e);
            e.printStackTrace();
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
            return String.format(SCREENING_ORG_EXCEL_FILE_NAME, screeningOrgName);
        }

        //学校压缩包名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.isNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            return String.format(SCHOOL_EXCEL_FILE_NAME,school.getName());

        }

        //年级压缩包名
        if (Objects.nonNull(exportCondition.getSchoolId()) && Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){
            return String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName());
        }

        //班级压缩包名
        if (Objects.nonNull(exportCondition.getClassId())){
            return String.format(PLAN_STUDENT_FILE_NAME,school.getName()+grade.getName()+schoolClass.getName());
        }
            return "";
  }

  @Override
  public Boolean isPackage(){
      return true;
  }
}
