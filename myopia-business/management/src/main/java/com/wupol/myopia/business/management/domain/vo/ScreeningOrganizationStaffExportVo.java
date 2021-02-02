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
 * 筛查机构人员导出数据
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class ScreeningOrganizationStaffExportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "筛查人员表";
    public static final String TOP_HEADER2 = "初始密码生成规则：手机号码后四位+身份证号后四位，共8位";
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "ID"})
    private Integer id;
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "姓名"})
    private String name;
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "性别"})
    private String gender;
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "身份证号"})
    private String idCard;
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "手机号码"})
    private String phone;
    @ExcelProperty({TOP_HEADER, TOP_HEADER2, "筛查机构"})
    private String organization;

}