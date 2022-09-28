package com.wupol.myopia.business.api.school.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.export.service.ArchiveService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ArchiveExportCondition;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 *  档案卡/监测表
 *
 * @author hang.yuan
 * @date 2022/9/21
 */
@CrossOrigin
@ResponseResultBody
@RestController
@RequestMapping("/school/archive")
public class SchoolArchiveController {

    @Autowired
    private ArchiveService archiveService;


    /**
     * 导出档案卡/监测表
     *
     * @param archiveExportCondition 导出条件
     * @return com.wupol.myopia.base.domain.ApiResult<java.lang.String>
     **/
    @GetMapping("/export")
    public ApiResult<String> exportArchive(@Valid ArchiveExportCondition archiveExportCondition) throws IOException {
        return ApiResult.success(archiveService.exportArchive(archiveExportCondition));
    }

    /**
     * 获取档案卡/监测表数据
     *
     * @param archiveRequestParam 请求参数
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard>
     **/
    @GetMapping("/data")
    public List<CommonDiseaseArchiveCard> getArchiveData(@Valid ArchiveRequestParam archiveRequestParam) {
        return archiveService.getArchiveData(archiveRequestParam);
    }
}
