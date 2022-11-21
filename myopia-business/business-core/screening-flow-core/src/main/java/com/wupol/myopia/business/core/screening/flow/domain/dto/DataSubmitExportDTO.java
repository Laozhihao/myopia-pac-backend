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
 * 数据上传导出
 *
 * @author Simple4H
 */
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class DataSubmitExportDTO {

    @ExcelProperty({"年级编号"})
    private String gradeCode;

    @ExcelProperty({"班级编号"})
    private String classCode;

    @ExcelProperty({"班级名称"})
    private String className;

    @ExcelProperty({"学籍号"})
    private String studentNo;

    @ExcelProperty({"民族代码"})
    private String nation;

    @ExcelProperty({"姓名"})
    private String name;

    @ExcelProperty({"性别"})
    private String gender;

    @ExcelProperty({"出生日期"})
    private String birthday;

    @ExcelProperty({"家庭住址"})
    private String address;

    @ExcelProperty({"右眼裸眼视力"})
    private String righta;

    @ExcelProperty({"右眼裸眼视力"})
    private String rightb;

    @ExcelProperty({"右眼屈光度"})
    private String rightc;

    @ExcelProperty({"左眼裸眼视力"})
    private String lefta;

    @ExcelProperty({"左眼裸眼视力"})
    private String leftb;

    @ExcelProperty({"左眼裸眼视力"})
    private String leftc;

    @ExcelProperty({"是否为角膜塑形镜（OK镜）佩戴者"})
    private String isOk;

}
