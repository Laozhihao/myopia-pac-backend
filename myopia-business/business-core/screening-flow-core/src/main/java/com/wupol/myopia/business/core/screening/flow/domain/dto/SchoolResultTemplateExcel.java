package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

/**
 * 数据上传导出
 *
 * @author Simple4H
 */
@HeadRowHeight(30)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE, horizontalAlignment = HorizontalAlignment.LEFT, shrinkToFit = true)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class SchoolResultTemplateExcel {

    private final static String TITLE = "表格填写说明\n" +
            "1、 戴镜情况选项为  不佩戴眼镜、佩戴框架眼镜、佩戴隐形眼镜、夜戴角膜塑形镜；其他文本将不会导入系统中\n" +
            "2、 视力情况数据中，视力数值保留 一位小数 \n" +
            "3、 电脑验光数据中，球镜、柱镜数据保留2位小数点，单位为屈光度，使用 +/- 表示远视/近视。 +1.00 请填写 1.00、-1.00请填写 -1.00\n" +
            "4、 电脑验光数据中，轴位为整数，单位度（°）\n" +
            "5、 身高数据为整数，单位厘米（CM）\n" +
            "6、 体重数据保留两位小数，单位千克（KG）\n" +
            "7、 如果该学生没有对应的数据，这对应检查项请留空表示无数据\n" +
            "8、 视力检查、电脑验光、身高、体重 数据请填写数值，不带对应的单位，系统默认单位：视力检查（5分记录法）、电脑验光（球镜/柱镜：屈光度 D；轴位：度 °）、身高（厘米CM）、体重（千克KG）\n" +
            "9、 视力检查、电脑验光、身高、体重 请严格按照填写数值规范填写，如果不符合规范导致无法解析，该数值将被默认为无数据\n" +
            "10、如果该学生已有数据，导入表格中同样检查项存在数据，则使用表格中的数据覆盖原有数据\n" +
            "11、如果该学生已有数据，导入表格中同样检查项无数据，则使用原有数据。\n" +
            "12、请不要修改模板表格中默认导出的数据，如果改动可能会导致查找学生失败导致导入失败\n" +
            "===============================================================================================================================================\n" +
            "数据覆盖例子：\n" +
            "A：系统中原有数据：          裸眼（右）5.0、裸眼（左） 、    球镜（右）0.00、    柱镜（右）-10.00、  轴位（右）、    球镜（左）、       柱镜（左）、        轴位（左）  、  身高 175、 体重 60\n" +
            "B：表格中数据：             裸眼（右）5.0、裸眼（左）4.9、  球镜（右）-10.00、  柱镜（右）-10.00、  轴位（右）10、  球镜（左）-2.00、  柱镜（左）-10.00、  轴位（左）12、  身高 178、 体重 50\n" +
            "C：导入完成后最终数据：      裸眼（右）5.0、裸眼（左）4.9、  球镜（右）-10.00、  柱镜（右）-10.00、  轴位（右）10、  球镜（左）-2.00、  柱镜（左）-10.00、  轴位（左）12 、 身高 178、 体重 50";

    @ExcelProperty({TITLE, "筛查学生ID"})
    private String planStudentId;

    @ExcelProperty({TITLE, "学籍号"})
    private String sno;

    @ExcelProperty({TITLE, "姓名"})
    private String studentName;

    @ExcelProperty({TITLE, "证件号"})
    private String credentials;

    @ExcelProperty({TITLE, "性别"})
    private String gender;

    @ExcelProperty({TITLE, "出生日期"})
    private String bitrhday;

    @ExcelProperty({TITLE, "年级"})
    private String gradeName;

    @ExcelProperty({TITLE, "班级"})
    private String className;

    @ExcelProperty({TITLE, "护照号"})
    private String passport;

    @ExcelProperty({TITLE, "戴镜情况"})
    private String glassesType;

    @ExcelProperty({TITLE, "裸眼（右）"})
    private String rightNakedVision;

    @ExcelProperty({TITLE, "裸眼（左）"})
    private String leftNakedVision;

    @ExcelProperty({TITLE, "矫正（右）"})
    private String rightCorrection;

    @ExcelProperty({TITLE, "矫正（左）"})
    private String leftCorrection;

    @ExcelProperty({TITLE, "球镜（右）"})
    private String rightSph;

    @ExcelProperty({TITLE, "柱镜（右）"})
    private String rightCyl;

    @ExcelProperty({TITLE, "轴位（右）"})
    private String rightAxial;

    @ExcelProperty({TITLE, "球镜（左）"})
    private String leftSph;

    @ExcelProperty({TITLE, "柱镜（左）"})
    private String leftCyl;

    @ExcelProperty({TITLE, "轴位（左）"})
    private String leftAxial;

    @ExcelProperty({TITLE, "身高（单位CM）"})
    private String height;

    @ExcelProperty({TITLE, "体重（单位KG）"})
    private String weight;

}
