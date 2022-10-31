package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 导出常见病筛查报告
 *
 * @author hang.yuan 2022/6/23 10:31
 */
@Service
public class ExportScreeningCommonDiseaseServiceImpl implements ExportPdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private Html2PdfService html2PdfService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;

    private static final String FOLDER_SUFFIX = "筛查报告";
    private static final String FILE_COMMON_DISEASE_SUFFIX = "筛查报告-常见病分析.pdf";
    private static final String FILE_VISION_SUFFIX = "筛查报告-视力分析.pdf";


    private Set<Integer> preProcess(ExportCondition exportCondition) {
        Set<Integer> sets = Sets.newHashSet();
        //区域预处理
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.DISTRICT)) {
            sets.add(ScreeningTypeEnum.VISION.getType());
            sets.add(ScreeningTypeEnum.COMMON_DISEASE.getType());
            return sets;
        }
        //学校预处理
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.SCHOOL)) {
            List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolId(exportCondition.getPlanId(), exportCondition.getSchoolId());
            if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
                sets.add(SchoolAge.KINDERGARTEN.code);
            }
            if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
                sets.add(SchoolAge.PRIMARY.code);
            }

        }
        return sets;
    }

    /**
     * 生成区域pdf报告
     *
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     * @param exportCondition 导出条件
     */
    @Override
    public void generateDistrictReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        Set<Integer> preProcess = preProcess(exportCondition);
        if (!CollectionUtils.isEmpty(preProcess)) {
            preProcess.forEach(screeningType -> generateDistrictReport(exportCondition.getNotificationId(), exportCondition.getDistrictId(), fileSavePath, getName(exportCondition, screeningType), screeningType));
        }
    }

    private void generateDistrictReport(Integer noticeId, Integer districtId, String fileSavePath, String fileName, Integer screeningType) {
        String reportHtmlUrl;
        if (Objects.equals(screeningType, ScreeningTypeEnum.VISION.getType())) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId);
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_COMMON_DISEASE, htmlUrlHost, districtId, noticeId);
        }
        String pdfUrl = html2PdfService.syncGeneratorPdfSpecial(reportHtmlUrl, fileName).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath, fileName).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    @Override
    public PDFRequestDTO getDistrictReportPdfUrl(ExportCondition exportCondition) {
        Set<Integer> preProcess = preProcess(exportCondition);
        if (!CollectionUtils.isEmpty(preProcess)) {
            List<PDFRequestDTO.Item> collect = preProcess.stream().map(screeningType -> getDistrictReportUrl(exportCondition, screeningType)).collect(Collectors.toList());
            return new PDFRequestDTO().setItems(collect);
        }
        return new PDFRequestDTO();
    }

    private PDFRequestDTO.Item getDistrictReportUrl(ExportCondition exportCondition, Integer screeningType) {
        String reportHtmlUrl;
        if (Objects.equals(screeningType, ScreeningTypeEnum.VISION.getType())) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, exportCondition.getNotificationId(), exportCondition.getDistrictId());
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_COMMON_DISEASE, htmlUrlHost, exportCondition.getDistrictId(), exportCondition.getNotificationId());
        }
        return new PDFRequestDTO.Item().setFileName(getName(exportCondition, screeningType)).setUrl(reportHtmlUrl);
    }

    @Override
    public void generateSchoolReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        Set<Integer> preProcess = preProcess(exportCondition);
        if (!CollectionUtils.isEmpty(preProcess)) {
            preProcess.forEach(schoolAge -> generateSchoolVisionReport(exportCondition.getPlanId(), exportCondition.getSchoolId(), fileSavePath, getSchoolVisionName(exportCondition, schoolAge), schoolAge));
        }

        if (!CollectionUtils.isEmpty(preProcess) && Objects.equals(preProcess.size(), 1) && preProcess.contains(SchoolAge.KINDERGARTEN.code)) {
            return;
        }
        generateSchoolCommonDiseaseReport(exportCondition.getPlanId(), exportCondition.getSchoolId(), fileSavePath, getCommonName(exportCondition, Boolean.TRUE));
    }

    @Override
    public PDFRequestDTO getSchoolReportPdfUrl(ExportCondition exportCondition) {
        PDFRequestDTO pdfRequestDTO = new PDFRequestDTO();
        List<PDFRequestDTO.Item> items = new ArrayList<>();

        Set<Integer> preProcess = preProcess(exportCondition);
        if (!CollectionUtils.isEmpty(preProcess)) {
            items = preProcess.stream().map(schoolAge -> generateSchoolVisionReportUrl(exportCondition, schoolAge)).collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(preProcess) && Objects.equals(preProcess.size(), 1) && preProcess.contains(SchoolAge.KINDERGARTEN.code)) {
            return pdfRequestDTO.setItems(items);
        }
        items.add(getSchoolCommonDiseaseReportUrl(exportCondition));
        return pdfRequestDTO.setItems(items);
    }


    private void generateSchoolCommonDiseaseReport(Integer planId, Integer schoolId, String fileSavePath, String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.SCHOOL_COMMON_DISEASE, htmlUrlHost, schoolId, planId);
        String pdfUrl = html2PdfService.syncGeneratorPdfSpecial(reportHtmlUrl, fileName).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath, fileName).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成学校报告PDF文件异常", e);
        }
    }

    private PDFRequestDTO.Item getSchoolCommonDiseaseReportUrl(ExportCondition exportCondition) {
        return new PDFRequestDTO.Item()
                .setFileName(getCommonName(exportCondition, Boolean.TRUE))
                .setUrl(String.format(HtmlPageUrlConstant.SCHOOL_COMMON_DISEASE, htmlUrlHost, exportCondition.getSchoolId(), exportCondition.getPlanId()));
    }

    private void generateSchoolVisionReport(Integer planId, Integer schoolId, String fileSavePath, String fileName, Integer schoolAge) {

        String reportHtmlUrl;
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, planId, schoolId);
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, planId, schoolId);
        }
        String pdfUrl = html2PdfService.syncGeneratorPdfSpecial(reportHtmlUrl, fileName).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath, fileName).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    private PDFRequestDTO.Item generateSchoolVisionReportUrl(ExportCondition exportCondition, Integer schoolAge) {
        String reportHtmlUrl;
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, exportCondition.getPlanId(), exportCondition.getSchoolId());
        } else {
            reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, exportCondition.getPlanId(), exportCondition.getSchoolId());
        }
        return new PDFRequestDTO.Item().setUrl(reportHtmlUrl).setFileName(getSchoolVisionName(exportCondition, schoolAge));
    }

    private String getSchoolVisionName(ExportCondition exportCondition, Integer schoolAge) {
        String schoolName = schoolService.getById(exportCondition.getSchoolId()).getName();
        // 幼儿园
        if (Objects.equals(SchoolAge.KINDERGARTEN.code, schoolAge)) {
            return schoolName + "筛查报告-视力分析【幼儿园】.pdf";
        } else {
            return schoolName + "筛查报告-视力分析【小学及以上】.pdf";
        }
    }

    @Override
    public Integer getScreeningType() {
        return ScreeningTypeEnum.COMMON_DISEASE.getType();
    }

    private String getName(ExportCondition exportCondition, Integer screeningType) {
        if (Objects.equals(screeningType, ScreeningTypeEnum.VISION.getType())) {
            return getDistrictVisionName(exportCondition);
        } else {
            return getDistrictCommonDiseaseName(exportCondition);
        }
    }

    /**
     * 获取常见病报告名称
     *
     * @param exportCondition 导出条件
     */
    private String getDistrictCommonDiseaseName(ExportCondition exportCondition) {
        exportCondition.setScreeningType(ScreeningTypeEnum.COMMON_DISEASE.getType());
        return getCommonName(exportCondition, Boolean.TRUE);
    }

    private String getDistrictVisionName(ExportCondition exportCondition) {
        exportCondition.setScreeningType(ScreeningTypeEnum.VISION.getType());
        return getCommonName(exportCondition, Boolean.TRUE);
    }


    /**
     * 获取文件夹名称
     *
     * @param exportCondition 导出条件
     */
    @Override
    public String getFileName(ExportCondition exportCondition) {
        return getCommonName(exportCondition, Boolean.FALSE);
    }


    private String getCommonName(ExportCondition exportCondition, Boolean isFile) {
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.DISTRICT)) {
            String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
            if (Objects.equals(isFile, Boolean.TRUE)) {
                return Objects.equals(exportCondition.getScreeningType(), ScreeningTypeEnum.VISION.getType()) ? districtName + FILE_VISION_SUFFIX : districtName + FILE_COMMON_DISEASE_SUFFIX;
            } else {
                return districtName + FOLDER_SUFFIX;
            }
        }
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.SCHOOL)) {
            School school = schoolService.getById(exportCondition.getSchoolId());
            return Objects.equals(isFile, Boolean.TRUE) ? school.getName() + FILE_COMMON_DISEASE_SUFFIX : school.getName() + FOLDER_SUFFIX;
        }
        return StrUtil.EMPTY;
    }
}

