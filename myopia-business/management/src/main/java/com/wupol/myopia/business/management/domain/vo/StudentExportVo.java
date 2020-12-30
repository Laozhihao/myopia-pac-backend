package com.wupol.myopia.business.management.domain.vo;

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
 * 学生导出数据
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class StudentExportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;
    public static final String TOP_HEADER = "学生表";
    @ExcelProperty({TOP_HEADER, "序号"})
    private Integer id;
    @ExcelProperty({TOP_HEADER, "姓名"})
    private String name;
    @ExcelProperty({TOP_HEADER, "性别"})
    private String gender;
    @ExcelProperty({TOP_HEADER, "出生日期"})
    private String birthday;
    @ExcelProperty({TOP_HEADER, "民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )"})
    private String nation;
    @ExcelProperty({TOP_HEADER, "学校"})
    private String school;
    @ExcelProperty({TOP_HEADER, "年级"})
    private String grade;
    @ExcelProperty({TOP_HEADER, "班级"})
    private String className;
    @ExcelProperty({TOP_HEADER, "学号"})
    private Integer no;
    @ExcelProperty({TOP_HEADER, "身份证号"})
    private String idCard;
    @ExcelProperty({TOP_HEADER, "手机号码"})
    private String phone;
    @ExcelProperty({TOP_HEADER, "居住地址"})
    private String address;
//    @ExcelProperty({TOP_HEADER, "首次配镜年龄"})
//    private String firstGlassesAge;
//    @ExcelProperty({TOP_HEADER, "配镜时长（年）"})
//    private String glassesAge;
//    @ExcelProperty({TOP_HEADER, "初始度数（左右）"})
//    private String firstDegree;
//    @ExcelProperty({TOP_HEADER, "度数增长情况（左右）"})
//    private String degreeRemark;
//    @ExcelProperty({TOP_HEADER, "当前情况"})
//    private String situation;
//    @ExcelProperty({TOP_HEADER, "视力标签"})
//    private String label;
//    @ExcelProperty({TOP_HEADER, "视力筛查次数"})
//    private String screeningCount;
//    @ExcelProperty({TOP_HEADER, "问卷次数"})
//    private String questionCount;
//    @ExcelProperty({TOP_HEADER, "最新筛查日期"})
//    private String lastScreeningTime;
//    @ExcelProperty({TOP_HEADER, "绑定公众手机号"})
//    private String wxPhone;
//    @ExcelProperty({TOP_HEADER, "绑定时间"})
//    private String bindTime;

}