package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

/**
 * 长沙数据上传导出
 *
 * @author Simple4H
 */
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class ShangHaiDataSubmitExportDTO {

    @ExcelProperty({"年级编号", "年级编号", "年级编号"})
    private String gradeCodeSn;
    @ExcelProperty({"班号", "班号", "班号"})
    private String classNo;
    @ExcelProperty({"班级", "班级", "班级"})
    private String className;
    @ExcelProperty({"学号", "学号", "学号"})
    private String studentNo;
    @ExcelProperty({"民族代码", "民族代码", "民族代码"})
    private String nationDesc;
    @ExcelProperty({"姓名", "姓名", "姓名"})
    private String studentName;
    @ExcelProperty({"性别", "性别", "性别"})
    private String gender;
    @ExcelProperty({"出生日期", "出生日期", "出生日期"})
    private String birthday;
    @ExcelProperty({"家庭地址", "家庭地址", "家庭地址"})
    private String address;
    @ExcelProperty({"身份证号", "身份证号", "身份证号"})
    private String idCard;

    @ExcelProperty({"视力", "左眼", "左眼"})
    private String leftNakedVisions;
    @ExcelProperty({"视力", "右眼", "右眼"})
    private String rightNakedVisions;
    @ExcelProperty({"球镜S", "左眼", "左眼"})
    private String leftSph;
    @ExcelProperty({"球镜S", "右眼", "右眼"})
    private String rightSph;
    @ExcelProperty({"光柱镜C", "左眼", "左眼"})
    private String leftCyl;
    @ExcelProperty({"光柱镜C", "右眼", "右眼"})
    private String rightCyl;
    @ExcelProperty({"轴位A", "左眼", "左眼"})
    private String leftAxial;
    @ExcelProperty({"轴位A", "右眼", "右眼"})
    private String rightAxial;
    @ExcelProperty({"是否佩戴OK镜", "是否佩戴OK镜", "是否佩戴OK镜"})
    private String isOkGlasses;
}