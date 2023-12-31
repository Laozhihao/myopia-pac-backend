package com.wupol.myopia.business.aggregation.export.pdf;


import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.util.HtmlToPdfUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GeneratePdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanService screeningPlanService;


    /**
     * 生成筛查报告PDF文件 - 行政区域
     *
     * @param saveDirectory 文件保存目录
     * @param fileName      文件名
     * @param noticeId      筛查通知ID
     * @param districtId    行政区域ID
     * @return void
     **/
    public void generateDistrictScreeningReportPdfFile(String saveDirectory, String fileName, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.hasLength(fileName, "文件名为空");
        Assert.notNull(noticeId, BizMsgConstant.NOTICE_ID_IS_NULL);
        Assert.notNull(districtId, "行政区域ID为空");
        String htmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_REPORT_HTML_URL, htmlUrlHost, noticeId, districtId);
        boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(saveDirectory, fileName + ".pdf").toString());
        Assert.isTrue(isSuccessful, "【生成行政区域报告PDF文件异常】" + fileName);
    }

    /**
     * 根据筛查通知ID，生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     **/
    public void generateSchoolScreeningReportPdfFileByNoticeId(String saveDirectory, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(noticeId, BizMsgConstant.NOTICE_ID_IS_NULL);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<Integer> schoolIdList = statConclusionService.getSchoolIdsByScreeningNoticeIdAndDistrictIds(noticeId, districtIds);
        generateSchoolScreeningReportPdfFileBatch(saveDirectory, noticeId, null, schoolIdList);
    }

    /**
     * 生成筛查报告PDF文件 - 筛查机构
     *
     * @param saveDirectory 保存目录
     * @param planId        筛查计划ID
     **/
    public void generateScreeningOrgScreeningReportPdfFile(String saveDirectory, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(planId, BizMsgConstant.PLAN_ID_IS_NULL);
        generateSchoolScreeningReportPdfFile(saveDirectory, null, planId, schoolId);
    }

    /**
     * @Description: 导出筛查计划下的PDF报告
     * @Param: [saveDirectory, planId, schoolId]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2021/12/30
     */
    public void generateScreeningPlanSchoolReportPdfFile(String saveDirectory, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(planId, BizMsgConstant.PLAN_ID_IS_NULL);
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByPlanId(planId);

        if (!schoolIdList.contains(schoolId)) {
            throw new BusinessException("学校ID不在筛检计划中");
        }
        generateSchoolScreeningReportPdfFile(saveDirectory, null, planId, schoolId);
    }

    /**
     * 批量生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     * @param planId        筛查计划ID
     * @param schoolIdList  学校ID集合
     **/
    private void generateSchoolScreeningReportPdfFileBatch(String saveDirectory, Integer noticeId, Integer planId, List<Integer> schoolIdList) {
        Assert.notEmpty(schoolIdList, "学校ID集为空");
        schoolIdList.forEach(schoolId -> generateSchoolScreeningReportPdfFile(saveDirectory, noticeId, planId, schoolId));
    }

    /**
     * 生成筛查计划总报告
     *
     * @param saveDirectory
     * @param planId
     */
    public void generateScreeningPlanReportPdfFile(String saveDirectory, Integer planId) {
        log.info("生成筛查计划总报告, planId={}", planId);
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Assert.notNull(plan, "该计划不存在");
        String reportFileName = String.format(PDFFileNameConstant.PLAN_REPORT_PDF_FILE_NAME, DateUtil.getYear(plan.getStartTime()));
        String schoolPdfHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_HTML_URL_WITH_PLAN_ID, htmlUrlHost, planId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, reportFileName + ".pdf").toString()), "【生成计划报告PDF文件异常】：" + plan.getTitle());
    }

    /**
     * 生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     * @param planId        筛查计划ID
     * @param schoolId      学校ID
     **/
    public void generateSchoolScreeningReportPdfFile(String saveDirectory, Integer noticeId, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.isTrue(Objects.nonNull(noticeId) || Objects.nonNull(planId), "筛查通知ID和筛查计划ID都为空");
        Assert.notNull(schoolId, "学校ID不能为空");
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(PDFFileNameConstant.REPORT_PDF_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(Objects.isNull(noticeId) ? HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID : HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID, htmlUrlHost, Objects.isNull(noticeId) ? planId : noticeId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成报告PDF文件异常】：" + school.getName());
    }

}
