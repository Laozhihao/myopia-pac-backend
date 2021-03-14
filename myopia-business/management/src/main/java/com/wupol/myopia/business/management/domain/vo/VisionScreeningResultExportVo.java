package com.wupol.myopia.business.management.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.Serializable;

/**
 * 筛查数据导出
 * @author Alix
 * @Date 2021/03/12
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
// 将第1-2行的2-3列合并成一个单元格
@OnceAbsoluteMerge(firstRowIndex = 0, lastRowIndex = 1, firstColumnIndex = 0, lastColumnIndex = 12)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class VisionScreeningResultExportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "视力筛查";
    public static final String TOP_HEADER2 = "视力复测";

    @ExcelProperty("序号")
    private String name;

    @ExcelProperty("姓名")
    private String type;

    @ExcelProperty("性别")
    private String configType;

    @ExcelProperty("出生日期")
    private String phone;

    @ExcelProperty("民族")
    private Integer personSituation;

    @ExcelProperty("学校编码")
    private String remark;

    @ExcelProperty("学校名称")
    private Integer screeningCount;

    @ExcelProperty("年级")
    private String districtName;

    @ExcelProperty("班级")
    private String province;

    @ExcelProperty("学号")
    private String city;

    @ExcelProperty("身份证号")
    private String area;

    @ExcelProperty("手机号码")
    private String town;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty({TOP_HEADER, "戴镜情况"})
    private String createUser;

    @ExcelProperty({TOP_HEADER, "裸眼（右/左）"})
    private String createTime1;

    @ExcelProperty({TOP_HEADER, "矫正（右/左）"})
    private String createTime2;

    @ExcelProperty({TOP_HEADER, "球镜（右/左）"})
    private String createTime3;

    @ExcelProperty({TOP_HEADER, "柱镜（右/左）"})
    private String createTime4;

    @ExcelProperty({TOP_HEADER, "轴位（右/左）"})
    private String createTime5;

    @ExcelProperty({TOP_HEADER, "等效球镜（右/左）"})
    private String createTime6;

    @ExcelProperty({TOP_HEADER, "串镜（右/左）"})
    private String createTime7;

    @ExcelProperty({"预警级别", "预警级别"})
    private String createTime14;

    @ExcelProperty({"是否复测", "是否复测"})
    private String createTime15;

    @ExcelProperty({TOP_HEADER2, "裸眼（右/左）"})
    private String createTime8;

    @ExcelProperty({TOP_HEADER2, "矫正（右/左）"})
    private String createTime9;

    @ExcelProperty({TOP_HEADER2, "球镜（右/左）"})
    private String createTime10;

    @ExcelProperty({TOP_HEADER2, "柱镜（右/左）"})
    private String createTime11;

    @ExcelProperty({TOP_HEADER2, "轴位（右/左）"})
    private String createTime12;

    @ExcelProperty({TOP_HEADER2, "等效球镜（右/左）"})
    private String createTime13;
}