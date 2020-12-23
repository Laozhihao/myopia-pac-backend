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
 * 医院导出数据
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class HospitalExportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "医院表";
    @ExcelProperty({TOP_HEADER, "ID"})
    private Integer no;
    @ExcelProperty({TOP_HEADER, "医院名称"})
    private String name;
    @ExcelProperty({TOP_HEADER, "详细地址"})
    private String address;
    @ExcelProperty({TOP_HEADER, "等级"})
    private String level;
    @ExcelProperty({TOP_HEADER, "类型"})
    private String type;
    @ExcelProperty({TOP_HEADER, "性质"})
    private Integer kind;
    @ExcelProperty({TOP_HEADER, "说明"})
    private String remark;

}