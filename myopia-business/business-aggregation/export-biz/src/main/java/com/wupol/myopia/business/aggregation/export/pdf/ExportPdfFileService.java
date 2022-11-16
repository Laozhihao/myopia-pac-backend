package com.wupol.myopia.business.aggregation.export.pdf;

import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;

/**
 * 导出pdf文件
 *
 * @author hang.yuan 2022/6/23 15:51
 */
public interface ExportPdfFileService {

    /**
     * 获取类型
     *
     * @return 类型 {@link ScreeningTypeEnum}
     */
    Integer getScreeningType();

    /**
     * 获取文件名
     * @param exportCondition 导出条件
     * @return 文件名
     */
    String getFileName(ExportCondition exportCondition);

    /**
     * 生成区域报告pdf文件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名称
     * @param exportCondition 导出条件
     */
    default void generateDistrictReportPdfFile(String fileSavePath,String fileName, ExportCondition exportCondition){}

    /**
     * 生成学校报告pdf文件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名称
     * @param exportCondition 导出条件
     */
    default void generateSchoolReportPdfFile(String fileSavePath,String fileName, ExportCondition exportCondition){}

    PDFRequestDTO getDistrictReportPdfUrl(ExportCondition exportCondition);

    PDFRequestDTO getSchoolReportPdfUrl(ExportCondition exportCondition);
}
