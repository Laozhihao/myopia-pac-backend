package com.wupol.myopia.base.util;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.vistel.Interface.util.ZipUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Objects;

/**
 * Excel工具
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@UtilityClass
public class ExcelUtil {
    /** Excel文件名，占位符：前缀、时间戳 */
    private static final String EXCEL_FILE_NAME = "%s-%s.xlsx";
    private static final String ZIP_FILE_NAME = "%s-%s.zip";

    private static final String EXCEL_FILE_NAME_XLS = "%s-%s.xls";

    /**
     * 导数据到Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param fileNamePrefix    文件名前缀
     * @param data              填充的数据
     * @param head              Excel表头定义类
     * @return java.io.File
     **/
    public static File exportListToExcel(String fileNamePrefix, List<?> data, Class<?> head) throws IOException {
       return exportListToExcel(fileNamePrefix, data, head, Boolean.TRUE);
    }

    /**
     * 导数据到Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param fileNamePrefix    文件名前缀
     * @param data              填充的数据
     * @param head              Excel表头定义类
     * @return java.io.File
     **/
    public static File exportListToExcel(String fileNamePrefix, List<?> data, Class<?> head, Boolean isXlsx) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, isXlsx);
        EasyExcelFactory.write(outputFile.getAbsolutePath(), head).sheet().doWrite(data);
        return outputFile;
    }

    /**
     * 根据模板导数据到Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param fileNamePrefix 文件名前缀
     * @param templateInputStream 模板流
     * @param data 数据集合
     */
    public static File exportListToExcel(String fileNamePrefix, InputStream templateInputStream, List<?> data) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, Boolean.TRUE);
        EasyExcelFactory.write(outputFile.getAbsolutePath()).withTemplate(templateInputStream).sheet().doFill(data);
        return outputFile;
    }

    /**
     * 导数据到Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param fileNamePrefix    文件名前缀
     * @param data              填充的数据
     * @param sheetWriteHandler 表格生成处理器
     * @param head              Excel表头定义类
     * @return java.io.File
     **/
    public static File exportListToExcel(String fileNamePrefix, List<?> data, SheetWriteHandler sheetWriteHandler,  Class<?> head) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, Boolean.TRUE);
        EasyExcelFactory.write(outputFile.getAbsolutePath(), head).registerWriteHandler(sheetWriteHandler).sheet().doWrite(data);
        return outputFile;
    }

    /**
     * 导数据到指定目录的Excel，返回Excel对应的File
     * （EasyExcel官方文档：https://www.yuque.com/easyexcel/doc/easyexcel）
     *
     * @param folder            指定目录
     * @param fileNamePrefix    文件名前缀
     * @param data              填充的数据
     * @param sheetWriteHandler 表格生成处理器
     * @param head              Excel表头定义类
     * @return java.io.File
     **/
    public static File exportListToExcelWithFolder(String folder, String fileNamePrefix, List<?> data, SheetWriteHandler sheetWriteHandler,  Class<?> head) throws IOException {
        File outputFile = getOutputFileWithFolder(folder, fileNamePrefix);
        EasyExcelFactory.write(outputFile.getAbsolutePath(), head).registerWriteHandler(sheetWriteHandler).sheet().doWrite(data);
        return outputFile;
    }

    /**
     * 获取输出文件
     *
     * @param fileNamePrefix 文件名前缀
     * @param isXlsx
     * @return java.io.File
     **/
    private static File getOutputFile(String fileNamePrefix, Boolean isXlsx) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, "export/excel", isXlsx);
        if (outputFile.exists()) {
            // same file name existed, generate new file name
            return getOutputFile(fileNamePrefix, isXlsx);
        }
        createNewFile(outputFile);
        return outputFile;
    }

    private static File getFile(String fileName) throws IOException {
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            return outputFile;
        }
        createNewFile(outputFile);
        return outputFile;
    }

    /**
     * 获取输出文件（指定目录）
     *
     * @param folder 指定目录名（export/目录名）
     * @param fileNamePrefix 文件名前缀
     * @return java.io.File
     **/
    private static File getOutputFileWithFolder(String folder, String fileNamePrefix) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, String.format("export/%s", folder), Boolean.TRUE);
        if (outputFile.exists()) {
            // same file name existed, generate new file name
            return getOutputFileWithFolder(folder, fileNamePrefix);
        }
        createNewFile(outputFile);
        return outputFile;
    }

    /**
     * 创建新文件
     * @param outputFile
     * @return
     * @throws IOException
     */
    private static void createNewFile(File outputFile) throws IOException {
        boolean newFile = outputFile.createNewFile();
        if (!newFile) {
            throw new FileSystemException("创建新文件失败,fileName = " + outputFile.getName());
        }
    }

    /**
     * 获取外输文件
     *
     * @param fileNamePrefix
     * @param folderString
     * @param isXlsx
     * @return
     */
    private static File getOutputFile(String fileNamePrefix, String folderString, Boolean isXlsx) {
        String tempSubPath = IOUtils.getTempSubPath(folderString);
        String format = Objects.equals(isXlsx, Boolean.TRUE) ? EXCEL_FILE_NAME : EXCEL_FILE_NAME_XLS;
        String fileName = String.format(format, fileNamePrefix, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE))
                .replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
        return new File(FilenameUtils.concat(tempSubPath, fileName));
    }

    /**
     * 获取输出文件（指定目录）
     *
     * @param folder 指定目录名（export/目录名）
     * @param fileNamePrefix 文件名前缀
     * @return java.io.File
     **/
    public static File zip(String folder, String fileNamePrefix) throws UtilException {
        String tempSubPath = IOUtils.getTempSubPath(String.format("export/%s", folder));
        String zipTempSubPath = IOUtils.getTempSubPath(String.format("export/zip/%s", folder));

        String fileName = String.format(ZIP_FILE_NAME, fileNamePrefix, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE))
                .replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
        String zipFileName = FilenameUtils.concat(zipTempSubPath, fileName);
        ZipUtil.compressFolderToZip(tempSubPath, zipFileName);
        return new File(zipFileName);
    }

    /**
     * 根据模板导出水平填充表格
     * @param fileNamePrefix 文件名前缀
     * @param data 填充的数据
     * @param template 模板
     * @return
     * @throws IOException
     */
    public static File exportHorizonListToExcel(
            String fileNamePrefix, List<?> data, InputStream template) throws IOException {
        File outputFile = getOutputFile(fileNamePrefix, Boolean.TRUE);
        ExcelWriter excelWriter = EasyExcelFactory.write(outputFile).withTemplate(template).build();
        WriteSheet writeSheet = EasyExcelFactory.writerSheet().build();
        FillConfig fillConfig =
                FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
        excelWriter.fill(data, fillConfig, writeSheet);
        excelWriter.finish();
        return outputFile;
    }

    /**
     * 根据模板导出excel
     * @param fileNamePrefix 文件或文件夹
     * @param templateInputStream 模板
     * @param data 数据
     */
    public static File exportExcel(String fileNamePrefix, InputStream templateInputStream, List<?> data) throws IOException {
        File outputFile = getFile(fileNamePrefix);
        EasyExcelFactory.write(outputFile.getAbsolutePath()).withTemplate(templateInputStream).sheet().doFill(data);
        return outputFile;
    }

    /**
     * 导出excel,表头动态生成
     * @param fileNamePrefix 文件名前缀
     * @param data 数据
     * @param head 表头
     */
    public static File exportListToExcel(String fileNamePrefix, List<?> data, List<List<String>> head) throws IOException {
        File outputFile = getFile(fileNamePrefix);
        ExcelWriterSheetBuilder writerSheetBuilder = EasyExcelFactory.write(outputFile.getAbsolutePath()).sheet();
        writerSheetBuilder.head(head);
        writerSheetBuilder.registerWriteHandler(getHeadStyle());
        writerSheetBuilder.doWrite(data);
        return outputFile;
    }

    private HorizontalCellStyleStrategy getHeadStyle(){
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();

        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("宋体");
        headWriteFont.setFontHeightInPoints((short)14);
        headWriteFont.setBold(false);
        headWriteCellStyle.setWriteFont(headWriteFont);

        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName("宋体");
        contentWriteFont.setFontHeightInPoints((short)12);
        contentWriteFont.setBold(false);
        headWriteCellStyle.setWriteFont(contentWriteFont);
        return new HorizontalCellStyleStrategy(headWriteCellStyle, Lists.newArrayList(contentWriteCellStyle));
    }

}