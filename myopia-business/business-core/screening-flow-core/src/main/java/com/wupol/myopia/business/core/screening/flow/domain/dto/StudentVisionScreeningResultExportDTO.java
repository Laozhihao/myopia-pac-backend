package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 筛查数据导出
 *
 * @author 钓猫的小鱼
 * @Date 2021/03/12
 **/
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class StudentVisionScreeningResultExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String EXCEL_TITLE = "筛查学生表（*导出数据不包含身份证）";

    @ExcelProperty({EXCEL_TITLE, "编码"})
    private String screeningCode;

    @ExcelProperty({EXCEL_TITLE, "学号"})
    private String studentNo;

    @ExcelProperty({EXCEL_TITLE, "姓名"})
    private String studentName;

    @ExcelProperty({EXCEL_TITLE, "性别"})
    private String genderDesc;

    @ExcelProperty({EXCEL_TITLE, "年级"})
    private String gradeName;

    @ExcelProperty({EXCEL_TITLE, "班级"})
    private String className;

    @DateTimeFormat("yyyy/MM/dd")
    @ExcelProperty({EXCEL_TITLE, "出生日期"})
    private Date birthday;

    @ExcelProperty({EXCEL_TITLE, "民族"})
    private String nationDesc;

    @ExcelProperty({EXCEL_TITLE, "手机号码"})
    private String parentPhone;

    @ExcelProperty({EXCEL_TITLE, "地址"})
    private String address;

    @ExcelProperty({EXCEL_TITLE, "戴镜情况"})
    private String glassesType;

    @ExcelProperty({EXCEL_TITLE, "裸眼视力（右）"})
    private String rightReScreenNakedVisions;
    @ExcelProperty({EXCEL_TITLE, "裸眼视力（左）"})
    private String leftReScreenNakedVisions;
    @ExcelProperty({EXCEL_TITLE, "矫正视力（右）"})
    private String rightReScreenCorrectedVisions;
    @ExcelProperty({EXCEL_TITLE, "矫正视力（左）"})
    private String leftReScreenCorrectedVisions;

    @ExcelProperty({EXCEL_TITLE, "球镜（右）"})
    private String rightReScreenSphs;
    @ExcelProperty({EXCEL_TITLE, "柱镜（右）"})
    private String rightReScreenCyls;
    @ExcelProperty({EXCEL_TITLE, "轴位（右）"})
    private String rightReScreenAxials;
    @ExcelProperty({EXCEL_TITLE, "等效球镜（右）"})
    private String rightReScreenSphericalEquivalents;


    @ExcelProperty({EXCEL_TITLE, "球镜（左）"})
    private String leftReScreenSphs;
    @ExcelProperty({EXCEL_TITLE, "柱镜（左）"})
    private String leftReScreenCyls;
    @ExcelProperty({EXCEL_TITLE, "轴位（左）"})
    private String leftReScreenAxials;
    @ExcelProperty({EXCEL_TITLE, "等效球镜（左）"})
    private String leftReScreenSphericalEquivalents;

}