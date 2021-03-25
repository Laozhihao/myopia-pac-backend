package com.wupol.myopia.business.management.export.archives;

import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.export.BaseExportFileService;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出学校的档案卡
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolArchivesService")
public class ExportSchoolArchivesService extends BaseExportFileService {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private StatConclusionService statConclusionService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void generateFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generateSchoolPdfFileByScreeningOrgId(fileSavePath, exportCondition.getScreeningOrgId(), exportCondition.getPlanId());
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(exportCondition.getScreeningOrgId());
        return String.format(PDF_REPORT_FILE_NAME, screeningOrganization.getName());
    }

    /**
     * 通过筛查ID，生成学校筛查报告PDF文件
     *
     * @param saveDirectory 保存目录
     * @param screeningOrgId 筛查机构ID
     * @param planId 筛查计划ID
     * @return void
     **/
    private void generateSchoolPdfFileByScreeningOrgId(String saveDirectory, Integer screeningOrgId, Integer planId) {
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByScreeningOrgId(screeningOrgId);
        generateSchoolPdfFileBatch(saveDirectory, null, planId, schoolIdList);
    }
}
