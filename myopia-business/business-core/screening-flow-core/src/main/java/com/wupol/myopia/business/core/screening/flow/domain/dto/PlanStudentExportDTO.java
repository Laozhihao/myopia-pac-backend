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

import java.io.Serializable;

/**
 * 计划学生导出数据
 *
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class PlanStudentExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER_FOR_INPUT = "筛查信息";

    public static final String TOP_HEADER = "筛查学生表";

    @ExcelProperty({TOP_HEADER_FOR_INPUT, TOP_HEADER, "编码"})
    private String screeningCode;

    @ExcelProperty({TOP_HEADER_FOR_INPUT, TOP_HEADER, "身份证"})
    private String idCard;

    @ExcelProperty({TOP_HEADER_FOR_INPUT, TOP_HEADER, "姓名"})
    private String name;

    @ExcelProperty({TOP_HEADER_FOR_INPUT, TOP_HEADER, "性别"})
    private String gender;

    @ExcelProperty({TOP_HEADER_FOR_INPUT, TOP_HEADER, "出生日期"})
    private String birthday;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "民族"})
    private String nation;

//    @ExcelProperty({TOP_HEADER, "学校名称"})
//    private String schoolName;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "年级"})
    private String gradeName;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "班级"})
    private String className;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "学号"})
    private String studentNo;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "手机号码"})
    private String phone;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "省"})
    private String province;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "市"})
    private String city;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "区"})
    private String area;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "镇"})
    private String town;

    @ExcelProperty({TOP_HEADER_FOR_INPUT,TOP_HEADER, "详细地址"})
    private String address;


}