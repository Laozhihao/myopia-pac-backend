package com.wupol.myopia.business.core.school.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 眼健康数据导出
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
@HeadStyle(fillForegroundColor = 1)
public class EyeHealthDataExportDTO implements Serializable {

    private static final String TITLE = "眼健康中心";

    @ExcelProperty({TITLE, "学籍号", "学籍号"})
    private String sno;

    @ExcelProperty({TITLE, "姓名", "姓名"})
    private String name;

    @ExcelProperty({TITLE, "性别", "性别"})
    private String gender;

    @ExcelProperty({TITLE, "出生日期", "出生日期"})
    private String birthday;

    @ExcelProperty({TITLE, "年级情况", "年级情况"})
    private String gradeAndClass;

    @ExcelProperty({TITLE, "最新筛查日期", "最新筛查日期"})
    private String screeningTime;

    @ExcelProperty({TITLE, "戴镜情况", "戴镜情况"})
    private String wearingGlasses;

    @ExcelProperty({TITLE, "视力低下情况", "裸眼视力（右/左）"})
    private String lowVision;

    @ExcelProperty({TITLE, "视力低下情况", "初步结果"})
    private String lowVisionResult;

    @ExcelProperty({TITLE, "屈光情况", "球镜（右/左）"})
    private String sph;

    @ExcelProperty({TITLE, "屈光情况", "柱镜（右/左）"})
    private String cyl;

    @ExcelProperty({TITLE, "屈光情况", "轴位（右/左）"})
    private String axial;

    @ExcelProperty({TITLE, "屈光情况", "初步结果"})
    private String refractiveResult;

    @ExcelProperty({TITLE, "近视矫正", "矫正视力（右/左）"})
    private String correctedVision;

    @ExcelProperty({TITLE, "近视矫正", "初步结果"})
    private String correctedVisionResult;

    @ExcelProperty({TITLE, "视力预警", "视力预警"})
    private String warningLevel;

    @ExcelProperty({TITLE, "专业医疗机构复查", "专业医疗机构复查"})
    private String review;

    @ExcelProperty({TITLE, "佩戴/更换眼镜", "佩戴/更换眼镜"})
    private String glassesType;

    @ExcelProperty({TITLE, "课桌椅座位建议", "身高（cm）"})
    private String height;

    @ExcelProperty({TITLE, "课桌椅座位建议", "课桌"})
    private String desk;

    @ExcelProperty({TITLE, "课桌椅座位建议", "课椅"})
    private String chair;

    @ExcelProperty({TITLE, "课桌椅座位建议", "座位"})
    private String seat;

    @ExcelProperty({TITLE, "公众号在线档案", "公众号在线档案"})
    private String isBindMp;
}