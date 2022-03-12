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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
        statConclusionExportDTOs.forEach(vo -> vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));

        return statConclusionExportDTOs;
    }

    @Override
    public Class getHeadClass() {
        return VisionScreeningResultExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {

        return String.format(ExcelNoticeKeyContentConstant.EXPORT_PLAN_STUDENT_DATA, getFileNameTitle(exportCondition));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getFileNameTitle(exportCondition)+ExcelFileNameConstant.PLAN_STUDENT_FILE_NAME;
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
        data.forEach(item->{
            log.info("数据："+item);
        });
        List<StatConclusionExportDTO> statConclusionExportDTOs = data;
        List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = excelFacade.genVisionScreeningResultExportVos(statConclusionExportDTOs);
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);

        //如果schoolId为null则证明是导出整个计划下的筛查数据
        ScreeningPlan plan = screeningPlanService.getById(exportCondition.getPlanId());
        if (Objects.isNull(exportCondition.getSchoolId())){
            Map<Integer, List<StatConclusionExportDTO>> collectMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolId));
            collectMap.forEach((key,value)->{
                log.info("key="+key +"===value="+value);
                List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(215);
                StringBuffer folder = new StringBuffer();
                folder.append(fileName);
                districtPositionDetailById.forEach(item->{
                    log.info("区域="+item.getName());
                    folder.append("/"+item.getName());
                });
                School school = schoolService.getById(key);
                folder.append("/"+plan.getTitle());
                folder.append("/"+school.getName());
                log.info("学校："+school.getName());
                String folders = folder.toString();
                log.info("导出文件目录路径======"+folders);
                try {
                    //生成文件
                    ExcelUtil.exportListToExcelWithFolder(folders,fileName,visionScreeningResultExportVos,mergeStrategy,getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }

        //如果年级id不为null,且班级id为null，则以年级维度导出
        if (Objects.nonNull(exportCondition.getGradeId()) && Objects.isNull(exportCondition.getClassId())){

            Map<Integer, List<StatConclusionExportDTO>> collectMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getGradeId));

            collectMap.forEach((key,value)->{
                List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(exportCondition.getDistrictId());
                StringBuffer folder = new StringBuffer();
                folder.append(fileName);
                districtPositionDetailById.forEach(item->{
                    folder.append("/"+item.getName());
                });
                School school = schoolService.getById(exportCondition.getSchoolId());
                SchoolGrade grade = schoolGradeService.getById(key);
                folder.append("/"+school.getName());
                folder.append("/"+grade.getName());
                String folders = folder.toString();
                try {
                    //生成文件
                    ExcelUtil.exportListToExcelWithFolder(folders,fileName,visionScreeningResultExportVos,mergeStrategy,getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }


        //如果年级id不为null,且班级id不为null，则以班级维度导出
        if (Objects.nonNull(exportCondition.getGradeId()) && Objects.nonNull(exportCondition.getClassId())){

            Map<Integer, List<StatConclusionExportDTO>> collectMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getClassId));

            collectMap.forEach((key,value)->{
                List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(215);
                StringBuffer folder = new StringBuffer();
                folder.append(fileName);
                districtPositionDetailById.forEach(item->{
                    log.info("区域="+item.getName());
                    folder.append("/"+item.getName());
                });
                School school = schoolService.getById(exportCondition.getSchoolId());
                SchoolGrade grade = schoolGradeService.getById(exportCondition.getGradeId());
                SchoolClass schoolClass = schoolClassService.getById(key);
                folder.append("/"+school.getName());
                folder.append("/"+grade.getName());
                folder.append("/"+schoolClass.getName());
                String folders = folder.toString();
                log.info("学校："+school.getName());
                log.info("年级："+grade.getName());
                log.info("班级："+schoolClass.getName());
                log.info("key="+key +"===value="+value);
                log.info("导出文件目录路径======"+folders);
                try {
                    //生成文件
                    ExcelUtil.exportListToExcelWithFolder(folders,fileName,visionScreeningResultExportVos,mergeStrategy,getHeadClass());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return null;
    }

    /**
     * 获取文件同步导出文件名称
     * @param exportCondition
     * @return
     */
    private String getFileNameTitle(ExportCondition exportCondition){

        String schoolName = "";
        if(Objects.nonNull(exportCondition.getSchoolId())){
            School school = schoolService.getById(exportCondition.getSchoolId());
            schoolName = school.getName();
        }
        String gradeName = "";
        Integer gradeId = exportCondition.getGradeId();
        if (Objects.nonNull(gradeId)) {
            gradeName = schoolGradeService.getById(gradeId).getName();
        }
        Integer classId = exportCondition.getClassId();
        String className = "";
        if (Objects.nonNull(classId)) {
            className = schoolClassService.getById(gradeId).getName();
        }
        return schoolName+gradeName+className;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition){
        ScreeningPlan screeningPlan = screeningPlanService.getById(exportCondition.getPlanId());
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("筛查计划不存在");
        }
    }
}
