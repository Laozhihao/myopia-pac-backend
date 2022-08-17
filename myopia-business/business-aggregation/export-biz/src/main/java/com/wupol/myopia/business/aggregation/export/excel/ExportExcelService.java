package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.fastjson.JSON;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.ExportScreeningSchoolStudentCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Excel
 *
 * @author hang.yuan 2022/7/7 10:00
 */
@Slf4j
@Component
public class ExportExcelService  {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Value("classpath:excel/ImportStudentExceptionTable.xlsx")
    private Resource importStudentExceptionTableTemplate;

    /**
     *  【提示】{{筛查计划名称}}-{{筛查学校}}-筛查学生数据【{{上传数据的表名}}】导入存在异常，请及时修正补全数据，点击下载《筛查学生数据导入异常表》，如已处理，请不要重复上传。
     */
    public  static final String NOTICE_KEY_CONTENT =  "【提示】%s-%s-筛查学生数据【%s】导入存在异常，请及时修正补全数据，点击下载《筛查学生数据导入异常表》，如已处理，请不要重复上传。";

    /**
     * 处理导入失败的数据，获取下载链接
     *
     * @param condition 导出条件
     * @param data 数据集合
     */
    public String process(ExportScreeningSchoolStudentCondition condition,List<?> data){
        File file = null;
        try {
            //生成excel
            file = generateExcelFile(condition.getFileName(), importStudentExceptionTableTemplate.getInputStream(), data);
            //上传文件到S3
            ResourceFile resourceFile = uploadFile(file);
            //获取文件链接
            String url = getUrl(resourceFile.getId());
            //发送通知
            sendSuccessNotice(condition,resourceFile.getId());
            return url;
        }catch (Exception e){
            log.error("【处理导入失败的数据生成Excel异常】{}", JSON.toJSONString(condition), e);
            throw new BusinessException("处理导入数据失败！");
        }finally {
            if (Objects.nonNull(file)){
                FileUtils.deleteQuietly(file);
            }
        }
    }

    /**
     * 获取文件链接
     * @param fileId 文件ID
     */
    private String getUrl(Integer fileId){
        return resourceFileService.getResourcePath(fileId);
    }
    /**
     * 获取通知内容
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     * @param fileName 原文件名称
     */
    public String getNoticeKeyContent(Integer screeningPlanId,Integer schoolId,String fileName){
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        School school = schoolService.getById(schoolId);
        return String.format(NOTICE_KEY_CONTENT,
                screeningPlan.getTitle(),
                school.getName(),
                fileName);
    }

    /**
     * 导出excel文件
     *
     * @param fileName 文件名称
     * @param templateInputStream 模板流
     * @param data 数据集合
     */
    private static File generateExcelFile(String fileName, InputStream templateInputStream, List<?> data) throws IOException {
        return ExcelUtil.exportListToExcel(fileName, templateInputStream,data);
    }

    /**
     * 上传文件到S3
     * @param file 文件
     */
    private ResourceFile uploadFile(File file) throws UtilException {
        return s3Utils.uploadS3AndGetResourceFile(file);
    }
    /**
     * 发送成功通知
     *
     * @param condition 条件
     * @param fileId 文件ID
     */
    private void sendSuccessNotice(ExportScreeningSchoolStudentCondition condition,Integer fileId){
        String fullContent = getNoticeKeyContent(condition.getScreeningPlanId(), condition.getSchoolId(), condition.getFileName());
        noticeService.createExportNotice(condition.getUserId(), condition.getUserId(), fullContent, fullContent, fileId, CommonConst.NOTICE_STATION_LETTER);
    }
}
