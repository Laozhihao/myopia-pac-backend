package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.constant.ArchiveTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveExportCondition;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.api.management.service.ArchiveService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2022/4/25
 **/
@Log4j2
@CrossOrigin
@Validated
@ResponseResultBody
@RestController
@RequestMapping("/management/archive")
public class ArchiveController {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private ArchiveService archiveService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ExportStrategy exportStrategy;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;


    /**
     * 导出档案卡/监测表
     *
     * @param archiveExportCondition 导出条件
     * @return com.wupol.myopia.base.domain.ApiResult<java.lang.String>
     **/
    @GetMapping("/export")
    public ApiResult exportArchive(@Valid ArchiveExportCondition archiveExportCondition) throws IOException {
        log.info("导出档案卡/监测表：{}", JSONObject.toJSONString(archiveExportCondition));

        // TODO: 必要参数校验
        // 构建导出条件
        Integer type = archiveExportCondition.getType();
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(archiveExportCondition.getNoticeId())
                .setDistrictId(archiveExportCondition.getDistrictId())
                .setPlanId(archiveExportCondition.getPlanId())
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(archiveExportCondition.getSchoolId())
                .setGradeId(archiveExportCondition.getGradeId())
                .setClassId(archiveExportCondition.getClassId())
                .setPlanStudentIds(archiveExportCondition.getPlanStudentIdsStr())
                .setType(type);

        // TODO：合并if
        // 行政区域（异步）
        if (ArchiveTypeEnum.DISTRICT.getType().equals(type)) {
            ScreeningNotice screeningNotice = screeningNoticeService.getById(archiveExportCondition.getNoticeId());
            exportCondition.setScreeningType(screeningNotice.getScreeningType());
            exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_DISTRICT_ARCHIVES_SERVICE);
            return ApiResult.success();
        }

        ScreeningPlan screeningPlan = screeningPlanService.getById(archiveExportCondition.getPlanId());
        exportCondition.setScreeningOrgId(screeningPlan.getScreeningOrgId()).setScreeningType(screeningPlan.getScreeningType());

        // 班级、年级（同步）
        if (ArchiveTypeEnum.STUDENT.getType().equals(type) || ArchiveTypeEnum.CLASS.getType().equals(type)) {
            return ApiResult.success(exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.STUDENT_ARCHIVES_SERVICE));
        }

        // 学校、年级（异步）
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_ARCHIVES_SERVICE);
        return ApiResult.success();
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
