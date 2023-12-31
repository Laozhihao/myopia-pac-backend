package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 导出视力筛查
 *
 * @author Simple4H
 */
@Service
public class ExportScreeningVisionService implements ExportPdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Resource
    private Html2PdfService html2PdfService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;
    @Resource
    private ScreeningPlanService screeningPlanService;

    private static final String EXPORT_FILE_NAME = "筛查报告-视力分析";

    private static final String PDF_NAME = "%s-%s";

    @Override
    public Integer getScreeningType() {
        return ScreeningTypeEnum.VISION.getType();
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.DISTRICT)) {
            String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
            return districtName + EXPORT_FILE_NAME;
        }

        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.SCHOOL)) {
            String title = screeningPlanService.getById(exportCondition.getPlanId()).getTitle();
            String name = schoolService.getById(exportCondition.getSchoolId()).getName();
            return String.format(PDF_NAME, title, name) + EXPORT_FILE_NAME;
        }
        return StrUtil.EMPTY;
    }

    @Override
    public void generateDistrictReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        generateDistrictVisionReport(exportCondition.getNotificationId(), exportCondition.getDistrictId(), fileSavePath, fileName);
    }

    private void generateDistrictVisionReport(Integer noticeId, Integer districtId, String fileSavePath, String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId);
        String pdfUrl = html2PdfService.syncGeneratorPdfSpecial(reportHtmlUrl, fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath, fileName + ".pdf").toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    @Override
    public PDFRequestDTO getDistrictReportPdfUrl(ExportCondition exportCondition) {
        District district = districtService.getById(exportCondition.getDistrictId());
        return new PDFRequestDTO()
                .setItems(Lists.newArrayList(getDistrictVisionReportUrl(exportCondition.getNotificationId(), exportCondition.getDistrictId(), getFileName(exportCondition))))
                .setZipFileName(district.getName() + "筛查报告");
    }

    private PDFRequestDTO.Item getDistrictVisionReportUrl(Integer noticeId, Integer districtId, String fileName) {
        return new PDFRequestDTO.Item().setUrl(String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId)).setFileName(fileName + ".pdf");
    }

    @Override
    public void generateSchoolReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        Set<Integer> preProcess = preProcess(exportCondition);
        preProcess.forEach(s -> generateReport(exportCondition.getPlanId(), exportCondition.getSchoolId(), fileSavePath, getName(exportCondition, s), s));
    }

    @Override
    public PDFRequestDTO getSchoolReportPdfUrl(ExportCondition exportCondition) {
        Set<Integer> preProcess = preProcess(exportCondition);
        List<PDFRequestDTO.Item> collect = preProcess.stream().map(s -> generateReportUrl(exportCondition.getPlanId(), exportCondition.getSchoolId(), s, getName(exportCondition, s))).collect(Collectors.toList());
        return new PDFRequestDTO().setItems(collect).
                setZipFileName(schoolService.getNameById(exportCondition.getSchoolId()) + "筛查报告");
    }

    private Set<Integer> preProcess(ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId);

        Set<Integer> sets = Sets.newHashSet();
        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            sets.add(SchoolAge.KINDERGARTEN.code);
        }
        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            sets.add(SchoolAge.PRIMARY.code);
        }
        return sets;
    }

    private String getName(ExportCondition exportCondition, Integer schoolAge) {
        String schoolName = schoolService.getById(exportCondition.getSchoolId()).getName();
        // 幼儿园
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            return schoolName + "筛查报告-视力分析【幼儿园】";
        } else {
            return schoolName + "筛查报告-视力分析【小学及以上】";
        }
    }

    private void generateReport(Integer planId, Integer schoolId, String fileSavePath, String fileName, Integer schoolAge) {

        String reportHtmlUrl;
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, planId, schoolId);
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, planId, schoolId);
        }
        String pdfUrl = html2PdfService.syncGeneratorPdfSpecial(reportHtmlUrl, fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath, fileName + ".pdf").toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }
    private PDFRequestDTO.Item generateReportUrl(Integer planId, Integer schoolId, Integer schoolAge, String fileName) {
        String reportHtmlUrl;
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, planId, schoolId);
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, planId, schoolId);
        }
        return new PDFRequestDTO.Item().setFileName(fileName + ".pdf").setUrl(reportHtmlUrl);
    }

}
