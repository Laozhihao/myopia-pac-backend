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
 * 导出筛查学生问卷账号
 *
 * @author Simple4H
 */
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class QuestionnaireAccountExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    @ExcelProperty({"姓名"})
    private String name;

    @ExcelProperty({"年级"})
    private String gradeName;

    @ExcelProperty({"班级"})
    private String className;

    @ExcelProperty({"账户"})
    private String planStudentId;

    @ExcelProperty({"密码"})
    private String screeningCode;


}