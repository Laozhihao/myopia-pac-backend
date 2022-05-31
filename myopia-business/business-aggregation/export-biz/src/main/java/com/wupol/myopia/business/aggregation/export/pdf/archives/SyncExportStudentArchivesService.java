package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 【班级/多个学生维度】同步导出档案卡
 *
 * @author Simple4H
 */
@Service("studentArchivesService")
public class SyncExportStudentArchivesService extends BaseExportPdfFileService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ArchivePdfGenerator archivePdfGenerator;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     */
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        archivePdfGenerator.generateClassOrStudentsArchivesPdfFile(exportCondition, fileSavePath, fileName);
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        String schoolName = schoolService.getById(exportCondition.getSchoolId()).getName();
        String gradeName = schoolGradeService.getById(exportCondition.getGradeId()).getName();
        String className = schoolClassService.getById(exportCondition.getClassId()).getName();
        return String.format(PDFFileNameConstant.CLASS_ARCHIVES_PDF_FILE_NAME, schoolName, gradeName, className, ScreeningTypeEnum.isVisionScreeningType(exportCondition.getScreeningType()) ? PDFFileNameConstant.VISION_ARCHIVE : PDFFileNameConstant.COMMON_DISEASE_ARCHIVE);
    }

    /**
     * 导出前参数校验
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.notNull(exportCondition.getPlanId(), BizMsgConstant.PLAN_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getSchoolId(), BizMsgConstant.SCHOOL_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getGradeId(), BizMsgConstant.GRADE_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getClassId(), BizMsgConstant.CLASS_ID_IS_EMPTY);
    }

    /**
     * 获取锁
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PLAN_ARCHIVES_DATA,
                exportCondition.getPlanId(),
                exportCondition.getScreeningOrgId(),
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getClassId(),
                exportCondition.getGradeId(),
                exportCondition.getPlanStudentIds());
    }

}
