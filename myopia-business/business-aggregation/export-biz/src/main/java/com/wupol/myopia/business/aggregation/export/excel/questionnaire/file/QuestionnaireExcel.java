package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 问卷excel接口
 *
 * @author hang.yuan 2022/7/18 11:29
 */
public interface QuestionnaireExcel {

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileName 文件保存路径
     * @param fileType 生成文件类型
     * @exception IOException IO异常
     */
    default void generateFile(ExportCondition exportCondition, String fileName,String fileType) throws IOException {
        if (Objects.equals(fileType, QuestionnaireConstant.EXCEL_FILE)){
            generateExcelFile(exportCondition,fileName);
        }else {
            generateRecFile(exportCondition,fileName);
        }
    }

    /**
     * 生成excel文件
     *
     * @param exportCondition 导出条件
     * @param fileName 文件保存路径
     * @exception IOException IO异常
     */
    default void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {

    }

    /**
     * 生成rec文件
     *
     * @param exportCondition 导出条件
     * @param fileName 文件保存路径
     */
    default void generateRecFile(ExportCondition exportCondition, String fileName) {

    }

    /**
     * 构建生成数据条件对象
     * @param exportCondition 导出条件对象
     * @param isAsc 是否顺序
     */
    default GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc){
        return null;
    }

    /**
     * 问卷类型
     *
     * @return 问卷类型
     */
    Integer getType();


    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件父路径
     * @param fileName   文件名
     * @return 文件保存路径
     **/
    default String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

}
