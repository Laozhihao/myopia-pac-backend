package com.wupol.myopia.business.api.school.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.export.pdf.archives.SyncExportStudentScreeningArchivesService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

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
public class SchoolReportController {

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
