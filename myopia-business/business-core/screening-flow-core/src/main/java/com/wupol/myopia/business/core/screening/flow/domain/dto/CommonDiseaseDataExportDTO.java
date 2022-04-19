package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.Serializable;
import java.util.Date;

/**
 * 筛查数据导出
 *
 * @author Alix
 * @Date 2021/03/12
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL,
        borderBottom = BorderStyle.THIN,
        borderLeft = BorderStyle.THIN,
        borderRight = BorderStyle.THIN,
        borderTop = BorderStyle.THIN)
@HeadFontStyle(fontHeightInPoints = 11)
@ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER)
// 将第1-2行的2-3列合并成一个单元格
@OnceAbsoluteMerge(firstRowIndex = 0, lastRowIndex = 1, firstColumnIndex = 0, lastColumnIndex = 12)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class CommonDiseaseDataExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "视力筛查";
    public static final String TOP_HEADER2 = "视力复测";

    @ExcelProperty({"姓名", "姓名", "姓名"})
    private String studentName;

    @ExcelProperty({"性别", "性别", "性别"})
    private String genderDesc;

    @DateTimeFormat("yyyy/MM/dd")
    @ExcelProperty({"出生日期", "出生日期", "出生日期"})
    private Date birthday;

    @ExcelProperty({"民族", "民族", "民族"})
    private String nationDesc;

    @ExcelProperty({"学校名称", "学校名称", "学校名称"})
    private String schoolName;

    @ExcelProperty({"年级", "年级", "年级"})
    private String gradeName;

    @ExcelProperty({"班级", "班级", "班级"})
    private String className;

    @ExcelProperty({"学号", "学号", "学号"})
    private String studentNo;

    @ExcelProperty({"地址", "地址", "地址"})
    private String address;

    @ExcelProperty({"是否有效数据", "是否有效数据", "是否有效数据"})
    private String isValid;

}