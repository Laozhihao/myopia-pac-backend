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

/**
 * 上海卫健委数据上报
 *
 * @author Simple4H
 */
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class ShangHaiHealthCommissionSubmitExportDTO {

    @ExcelProperty({"学校名称", "学校名称"})
    private String schoolName;
    @ExcelProperty({"年级", "年级"})
    private String gradeName;
    @ExcelProperty({"班级", "班级"})
    private String className;
    @ExcelProperty({"姓名", "姓名"})
    private String studentName;
    @ExcelProperty({"性别", "性别"})
    private String gender;
    @ExcelProperty({"出生日期", "出生日期"})
    private String birthday;
    @ExcelProperty({"证据号码", "证据号码"})
    private String idCard;

    @ExcelProperty({"检查日期", "检查日期"})
    private String checkData;
    @ExcelProperty({"视力筛（检）查", "裸眼（右）"})
    private String rightNakedVisions;
    @ExcelProperty({"视力筛（检）查", "裸眼（左）"})
    private String leftNakedVisions;
    @ExcelProperty({"视力筛（检）查", "是否配镜"})
    private String isGlasses;
    @ExcelProperty({"视力筛（检）查", "配镜类型"})
    private String glassesType;
    @ExcelProperty({"视力筛（检）查", "配镜(右)"})
    private String rightCorrectedVisions;
    @ExcelProperty({"视力筛（检）查", "配镜(左)"})
    private String leftCorrectedVisions;
    @ExcelProperty({"视力筛（检）查", "球镜S右眼视力"})
    private String rightSph;
    @ExcelProperty({"视力筛（检）查", "球镜S左眼视力"})
    private String leftSph;
    @ExcelProperty({"视力筛（检）查", "光柱镜C右眼视力"})
    private String rightCyl;
    @ExcelProperty({"视力筛（检）查", "光柱镜C左眼视力"})
    private String leftCyl;
    @ExcelProperty({"视力筛（检）查", "轴位A右眼视力"})
    private String rightAxial;
    @ExcelProperty({"视力筛（检）查", "轴位A左眼视力"})
    private String leftAxial;
}