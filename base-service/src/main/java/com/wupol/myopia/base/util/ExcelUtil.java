package com.wupol.myopia.base.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Excel工具
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public class ExcelUtil {
    /** Excel文件名，占位符：前缀、时间戳 */
    private static final String EXCEL_FILE_NAME = "%s-%s.xlsx";

    /**
     * 导数据到Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param fileNamePrefix    文件名前缀
     * @param data              填充的数据
     * @param head              Excel表头定义类
     * @return java.io.File
     **/
    public static File exportListToExcel(String fileNamePrefix, List data, Class head) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix);
        EasyExcel.write(outputFile.getAbsolutePath(), head).sheet().doWrite(data);
        return outputFile;
    }

    /**
     * 获取输出文件
     *
     * @param fileNamePrefix 文件名前缀
     * @return java.io.File
     **/
    private static File getOutputFile(String fileNamePrefix) throws IOException {
        String tempSubPath = IOUtils.getTempSubPath("export/excel");
        String fileName = String.format(EXCEL_FILE_NAME, fileNamePrefix, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE))
                .replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
        File outputFile = new File(FilenameUtils.concat(tempSubPath, fileName));
        if (outputFile.exists()) {
            // same file name existed, generate new file name
            return getOutputFile(fileNamePrefix);
        }
        outputFile.createNewFile();
        return outputFile;
    }

    /**
     * 根据模板导出水平填充表格
     * @param fileNamePrefix 文件名前缀
     * @param data 填充的数据
     * @param template 模板
     * @return
     * @throws IOException
     */
    public static File exportHorizonListToExcel(String fileNamePrefix, List data, File template)
            throws IOException {
        File outputFile = getOutputFile(fileNamePrefix);
        ExcelWriter excelWriter = EasyExcel.write(outputFile).withTemplate(template).build();
        WriteSheet writeSheet = EasyExcel.writerSheet().build();
        FillConfig fillConfig =
                FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
        excelWriter.fill(data, fillConfig, writeSheet);
        excelWriter.finish();
        return outputFile;
    }
}