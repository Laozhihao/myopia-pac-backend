package com.wupol.myopia.business.api.school.management.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.archives.SyncExportStudentScreeningArchivesService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/3/18
 **/

@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/report")
@Log4j2
public class ReportController {

    @Autowired
    private SyncExportStudentScreeningArchivesService syncExportStudentScreeningArchivesService;

    /**
     *
     * 学生档案卡路径
     * @param resultId 结果ID
     * @param templateId 模板ID
     * @return
     */
    @GetMapping("/student/archivesUrl")
    public ApiResult<String> syncExportArchivesPdfUrl(@NotNull(message = "结果ID") Integer resultId,
                                                       @NotNull(message = "模板ID") Integer templateId){

        return ApiResult.success(syncExportStudentScreeningArchivesService.generateArchivesPdfUrl(resultId,templateId));
    }


}
