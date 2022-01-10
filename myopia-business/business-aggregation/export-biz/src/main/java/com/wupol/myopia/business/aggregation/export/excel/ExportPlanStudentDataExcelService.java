package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2021/12/30/17:13
 * @Description:
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
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private ResourceFileService resourceFileService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        Integer screeningPlanId = exportCondition.getPlanId();//这个地方有问题
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
        return StatConclusionExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {

        String noticeKeyContent = String.format(ExcelNoticeKeyContentConstant.EXPORT_PLAN_STUDENT_DATA, getFileNameTitle(exportCondition));

        return noticeKeyContent;
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
        String lockKey = String.format(RedisConstant.FILE_EXPORT_PLAN_DATA, screeningPlanId,screeningOrgId,schoolId, userId);

        return lockKey;
    }

    @Override
    public File generateExcelFile(String fileName, List data) throws IOException {

        List<StatConclusionExportDTO> statConclusionExportDTOs = data;

        String path = fileName;
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);

        List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = excelFacade.genVisionScreeningResultExportVos(statConclusionExportDTOs);
        File excelFile =   ExcelUtil.exportListToExcel(path, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);

        return ZipUtil.zip(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(excelFile.getAbsolutePath(), "/"), "/"), "/"));
    }

    /**
    * @Description: 导出文件部位ZIP
    * @Param: [fileName, data]
    * @return: java.io.File
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/10
    */
    public File generateExcelFileNoZip(String fileName, List data) throws IOException {

        List<StatConclusionExportDTO> statConclusionExportDTOs = data;

        String path = fileName;
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);

        List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = excelFacade.genVisionScreeningResultExportVos(statConclusionExportDTOs);
        File excelFile =   ExcelUtil.exportListToExcel(path, visionScreeningResultExportVos, mergeStrategy, VisionScreeningResultExportDTO.class);
        return excelFile;
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String parentPath = null;
        File excelFile = null;
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 3.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 2.获取文件保存父目录路径
            excelFile = generateExcelFile(fileName, data);
            String downloadUrl = resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), fileName).getId());
            return downloadUrl;
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成报告异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            deleteTempFile(parentPath);
        }
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

}
