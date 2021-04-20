package com.wupol.myopia.business.management.export;

import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.export.constant.FileNameConstant;
import com.wupol.myopia.business.management.export.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
/**
 * 生成PDF报告
 *
 * @Author HaoHao
 * @Date 2021/3/26
 **/
@Service
public class GeneratePdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private DistrictService districtService;

    /**
     * 生成筛查报告PDF文件 - 行政区域
     *
     * @param saveDirectory 文件保存目录
     * @param fileName 文件名
     * @param noticeId 筛查通知ID
     * @param districtId 行政区域ID
     * @return void
     **/
    public void generateDistrictScreeningReportPdfFile(String saveDirectory, String fileName, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.hasLength(fileName, "文件名为空");
        Assert.notNull(noticeId, "筛查通知ID为空");
        Assert.notNull(districtId, "行政区域ID为空");
        String htmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_REPORT_HTML_URL, htmlUrlHost, noticeId, districtId);
        boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(saveDirectory, fileName + ".pdf").toString());
        Assert.isTrue(isSuccessful, "【生成行政区域报告PDF文件异常】" + fileName);
    }

    /**
     * 根据筛查通知ID，生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @return void
     **/
    public void generateSchoolScreeningReportPdfFileByNoticeId(String saveDirectory, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.notNull(noticeId, "筛查通知ID为空");
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<Integer> schoolIdList = statConclusionService.getSchoolIdsByScreeningNoticeIdAndDistrictIds(noticeId, districtIds);
        generateSchoolScreeningReportPdfFileBatch(saveDirectory, noticeId, null, schoolIdList);
    }

    /**
     * 生成筛查报告PDF文件 - 筛查机构
     *
     * @param saveDirectory 保存目录
     * @param planId 筛查计划ID
     * @return void
     **/
    public void generateScreeningOrgScreeningReportPdfFile(String saveDirectory, Integer planId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.notNull(planId, "筛查计划ID为空");
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByPlanId(planId);
        generateSchoolScreeningReportPdfFileBatch(saveDirectory, null, planId, schoolIdList);
    }

    /**
     * 批量生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolIdList 学校ID集合
     * @return void
     **/
    private void generateSchoolScreeningReportPdfFileBatch(String saveDirectory, Integer noticeId, Integer planId, List<Integer> schoolIdList) {
        Assert.notEmpty(schoolIdList, "学校ID集为空");
        schoolIdList.forEach(schoolId -> generateSchoolScreeningReportPdfFile(saveDirectory, noticeId, planId, schoolId));
    }

    /**
     * 生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return void
     **/
    public void generateSchoolScreeningReportPdfFile(String saveDirectory, Integer noticeId, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.isTrue(Objects.nonNull(noticeId) || Objects.nonNull(planId), "筛查通知ID和筛查计划ID都为空");
        Assert.notNull(schoolId, "学校ID不能为空");
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(FileNameConstant.REPORT_PDF_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(Objects.isNull(noticeId) ? HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID : HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID, htmlUrlHost, Objects.isNull(noticeId) ? planId : noticeId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成报告PDF文件异常】：" + school.getName());
    }

    /**
     * 生成档案卡PDF文件 - 筛查机构
     *
     * @param saveDirectory 保存目录
     * @param planId 筛查计划ID
     * @return void
     **/
    public void generateScreeningOrgArchivesPdfFile(String saveDirectory, Integer planId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.notNull(planId, "筛查计划ID为空");
        List<ScreeningPlanSchool> screeningPlanSchoolList = screeningPlanSchoolService.getSchoolListsByPlanId(planId);
        screeningPlanSchoolList.forEach(x -> generateSchoolArchivesPdfFile(saveDirectory, planId, x.getSchoolId()));
    }

    /**
     * 生成档案卡PDF文件 - 学校
     *
     * @param saveDirectory 保存目录
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return void
     **/
    public void generateSchoolArchivesPdfFile(String saveDirectory, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, "文件保存目录路径为空");
        Assert.notNull(planId, "筛查计划ID为空");
        Assert.notNull(schoolId, "学校ID不能为空");
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(FileNameConstant.ARCHIVES_PDF_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(HtmlPageUrlConstant.SCHOOL_ARCHIVES_HTML_URL , htmlUrlHost, planId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成学校档案卡PDF文件异常】：" + school.getName());
    }
}
