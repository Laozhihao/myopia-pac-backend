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
 * 学校导出数据
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class SchoolExportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "学校表";
    public static final String HEADER2 = "在线学生";
    public static final String HEADER3 = "住校学生";
    @ExcelProperty({TOP_HEADER, "ID"})
    private Integer no;
    @ExcelProperty({TOP_HEADER, "名称"})
    private String name;
    @ExcelProperty({TOP_HEADER, "性质"})
    private Integer kind;
    @ExcelProperty({TOP_HEADER, "是否寄宿"})
    private String lodgeStatus;
    @ExcelProperty({TOP_HEADER, "类型"})
    private String type;
    @ExcelProperty({TOP_HEADER, HEADER2, "总数"})
    private Integer onlineCount;
    @ExcelProperty({TOP_HEADER, HEADER2, "男生数"})
    private Integer onlineMaleCount;
    @ExcelProperty({TOP_HEADER, HEADER2, "女生数"})
    private Integer onlineFemaleCount;
    @ExcelProperty({TOP_HEADER, HEADER3, "总数"})
    private Integer lodgeCount;
    @ExcelProperty({TOP_HEADER, HEADER3, "男生数"})
    private Integer lodgeMaleCount;
    @ExcelProperty({TOP_HEADER, HEADER3, "女生数"})
    private Integer lodgeFemaleCount;
    @ExcelProperty({TOP_HEADER, "地址"})
    private String address;
    @ExcelProperty({TOP_HEADER, "年级班级"})
    private String className;
    @ExcelProperty({TOP_HEADER, "说明"})
    private String remark;
    @ExcelProperty({TOP_HEADER, "筛查次数"})
    private Integer screeningCount;

}