package com.wupol.myopia.business.management.export.report;

import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.export.constant.FileNameConstant;
import com.wupol.myopia.business.management.export.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
public class GenerateReportPdfService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;

    /**
     * 生成行政区区域PDF报告文件
     *
     * @param saveDirectory 文件保存目录
     * @param fileName 文件名
     * @param noticeId 筛查通知ID
     * @param districtId 行政区域ID
     * @return void
     **/
    public void generateDistrictPdfFile(String saveDirectory, String fileName, Integer noticeId, Integer districtId) {
        String htmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_REPORT_HTML_URL, htmlUrlHost, noticeId, districtId);
        boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(saveDirectory, fileName + ".pdf").toString());
        Assert.isTrue(isSuccessful, "【生成行政区域报告异常】" + fileName);
    }

    /**
     * 根据筛查通知ID，生成学校的筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @return void
     **/
    public void generateSchoolPdfFileByNoticeId(String saveDirectory, Integer noticeId) {
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByNoticeId(noticeId);
        generateSchoolPdfFileBatch(saveDirectory, noticeId, null, schoolIdList);
    }

    /**
     * 通过筛查ID，生成学校筛查报告PDF文件
     *
     * @param saveDirectory 保存目录
     * @param screeningOrgId 筛查机构ID
     * @param planId 筛查计划ID
     * @return void
     **/
    public void generateSchoolPdfFileByScreeningOrgId(String saveDirectory, Integer screeningOrgId, Integer planId) {
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByScreeningOrgId(screeningOrgId);
        generateSchoolPdfFileBatch(saveDirectory, null, planId, schoolIdList);
    }

    /**
     * 批量生成学校筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolIdList 学校ID集合
     * @return void
     **/
    public void generateSchoolPdfFileBatch(String saveDirectory, Integer noticeId, Integer planId, List<Integer> schoolIdList) {
        schoolIdList.forEach(schoolId -> generateSchoolPdfFile(saveDirectory, noticeId, planId, schoolId));
    }

    /**
     * 生成学校筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return void
     **/
    public void generateSchoolPdfFile(String saveDirectory, Integer noticeId, Integer planId, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(FileNameConstant.PDF_REPORT_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(Objects.isNull(noticeId) ? HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID : HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID, htmlUrlHost, Objects.isNull(noticeId) ? planId : noticeId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成区域报告异常】：" + school.getName());
    }
}
