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
 * @author 钓猫的小鱼
 * @Date 2021/03/12
 **/
//@HeadRowHeight(25)
//@HeadStyle(fillPatternType = FillPatternType.NO_FILL,
//        borderBottom = BorderStyle.THIN,
//        borderLeft = BorderStyle.THIN,
//        borderRight = BorderStyle.THIN,
//        borderTop= BorderStyle.THIN)
//@HeadFontStyle(fontHeightInPoints = 11)
//@ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER)
//// 将第1-2行的2-3列合并成一个单元格
//@OnceAbsoluteMerge(firstRowIndex = 0, lastRowIndex = 1, firstColumnIndex = 0, lastColumnIndex = 12)
//@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class StudentVisionScreeningResultExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;


    @ExcelProperty("编码")
    private Integer id;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("性别")
    private String genderDesc;

    @ExcelProperty("年级")
    private String gradeName;

    @ExcelProperty("班级")
    private String className;

    @DateTimeFormat("yyyy/MM/dd")
    @ExcelProperty("出生日期")
    private Date birthday;

    @ExcelProperty(value = "民族")
    private String nationDesc;

    @ExcelProperty("手机号码")
    private String parentPhone;

    @ExcelProperty("地址")
    private String address;

//    @ExcelProperty("戴镜情况")


    @ExcelProperty("裸眼视力（右）")
    private String rightReScreenNakedVisions;
    @ExcelProperty("裸眼视力（左）")
    private String leftReScreenNakedVisions;
    @ExcelProperty("矫正视力（右）")
    private String rightReScreenCorrectedVisions;
    @ExcelProperty("矫正视力（左）")
    private String leftReScreenCorrectedVisions;

    @ExcelProperty("球镜（右）")
    private String rightReScreenSphs;
    @ExcelProperty("柱镜（右）")
    private String rightReScreenCyls;
    @ExcelProperty("轴位（右）")
    private String rightReScreenAxials;
    @ExcelProperty("等效球镜（右）")
    private String rightReScreenSphericalEquivalents;


    @ExcelProperty("球镜（左）")
    private String leftReScreenSphs;
    @ExcelProperty("柱镜（左）")
    private String leftReScreenCyls;
    @ExcelProperty("轴位（左）")
    private String leftReScreenAxials;
    @ExcelProperty("等效球镜（左）")
    private String leftReScreenSphericalEquivalents;




}