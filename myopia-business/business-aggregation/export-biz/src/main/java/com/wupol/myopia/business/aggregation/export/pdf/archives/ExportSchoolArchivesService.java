package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * 导出学校的档案卡（压缩文件仅包含一个该学校的PDF文件）
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolArchivesService")
@Log4j2
public class ExportSchoolArchivesService extends BaseExportPdfFileService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private GeneratePdfFileService generateReportPdfService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private ResourceFileService resourceFileService;
    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generateReportPdfService.generateSchoolArchivesPdfFile(fileSavePath, exportCondition);
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        String gradeName = "";
        if (Objects.nonNull(exportCondition.getGradeId())){
            SchoolGrade schoolGrade = schoolGradeService.getById(exportCondition.getGradeId());
            gradeName = schoolGrade.getName();
        }
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, school.getName()+gradeName);
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        int total = visionScreeningResultService.count(new VisionScreeningResult().setPlanId(exportCondition.getPlanId()).setSchoolId(exportCondition.getSchoolId()).setIsDoubleScreen(Boolean.FALSE));
        if (total == 0) {
            throw new BusinessException("该计划下该学校暂无筛查学生数据，无法导出档案卡");
        }
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_ARCHIVES_SCHOOL,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getPlanId());
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String parentPath = null;
        try {
            // 1.获取文件名
            String fileName = getSyncFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);
            // 4.生成导出的文件
            generatePdfFile(exportCondition, fileSavePath, fileName);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(fileSavePath + ".pdf", fileName).getId());
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
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    public String getSyncFileName(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, school.getName());
    }
}
