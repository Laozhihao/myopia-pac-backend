package com.wupol.myopia.business.core.school.domain.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
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
 *
 * @Author Chikong
 * @Date 2020/12/22
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class SchoolExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "学校表";
    @ExcelProperty({TOP_HEADER, "学校编码"})
    private String no;

    @ExcelProperty({TOP_HEADER, "名称"})
    private String name;

    @ExcelProperty({TOP_HEADER, "性质"})
    private String kind;

    @ExcelProperty({TOP_HEADER, "是否寄宿"})
    private String lodgeStatus;

    @ExcelProperty({TOP_HEADER, "类型"})
    private String type;

    @ExcelProperty({TOP_HEADER, "详细地址"})
    private String address;

    @ExcelProperty({TOP_HEADER, "年级班级"})
    private String className;

    @ExcelProperty({TOP_HEADER, "说明"})
    private String remark;

    @ExcelProperty({TOP_HEADER, "账号"})
    private String account;

    @ExcelProperty({TOP_HEADER, "合作类型"})
    private String cooperationType;

    @ExcelProperty({TOP_HEADER, "剩余合作时间"})
    private Integer cooperationRemainTime;

    @ExcelProperty({TOP_HEADER, "开始时间"})
    private String cooperationStartTime;

    @ExcelProperty({TOP_HEADER, "截止时间"})
    private String cooperationEndTime;

    @ExcelProperty({TOP_HEADER, "创建时间"})
    private String createTime;

}