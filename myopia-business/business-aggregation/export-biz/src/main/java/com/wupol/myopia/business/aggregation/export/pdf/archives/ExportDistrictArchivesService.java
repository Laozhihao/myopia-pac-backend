package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ExportDistrictArchivesService
 * @Description TODO
 * @Author TaoShuai
 * @Date 2022/3/8 10:35
 * @Version 1.0
 **/
@Service("exportDistrictArchivesService")
public class ExportDistrictArchivesService extends BaseExportPdfFileService {

    @Resource
    private ScreeningTaskService screeningTaskService;

    @Resource
    private VisionScreeningBizService visionScreeningBizService;

    @Resource
    private DistrictService districtService;

    @Resource
    private GeneratePdfFileService generateReportPdfService;

    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generateReportPdfService.generateDistrictArchivesPdfFile(fileSavePath, exportCondition);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.getById(exportCondition.getDistrictId());
        String districtFullName = districtService.getTopDistrictName(district.getCode());
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, districtFullName);
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {

        List<ScreeningTask> byList = screeningTaskService.findByList(new ScreeningTask()
                .setDistrictId(exportCondition.getDistrictId())
                .setScreeningNoticeId(exportCondition.getNotificationId())
        );
        List<Integer> taskIds = byList.stream().map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        int total = visionScreeningBizService.getScreeningResult(exportCondition.getDistrictId(), taskIds);
        if (total == 0) {
            throw new BusinessException("该区域下暂无筛查学生数据，无法导出档案卡");
        }
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_ARCHIVES_DISTRICT,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getClassId(),
                exportCondition.getGradeId(),
                exportCondition.getPlanStudentIds());
    }
}
