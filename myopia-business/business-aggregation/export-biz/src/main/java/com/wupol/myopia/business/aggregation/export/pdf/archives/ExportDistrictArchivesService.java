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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ExportDistrictArchivesService
 * @Description
 * @Author TaoShuai
 * @Date 2022/3/8 10:35
 * @Version 1.0
 **/
@Slf4j
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

        //获取筛查任务
        List<Integer> districtIdList = districtService.getSpecificDistrictTreeAllDistrictIds(exportCondition.getDistrictId());
        List<ScreeningTask> screeningTaskList = screeningTaskService.getScreeningTaskByDistrictIdAndNotificationId(districtIdList, exportCondition.getNotificationId());

        List<Integer> taskIds = screeningTaskList.stream().map(item -> {
            return item.getId();
        }).collect(Collectors.toList());


        if (CollectionUtils.isEmpty(taskIds)){
            throw new BusinessException("该区域下暂无筛查任务，无法导出档案卡");
        }
        int total = visionScreeningBizService.getScreeningResult(districtIdList, taskIds);
        if (total <= 0) {
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
