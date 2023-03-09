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
public class ChangShaDataSubmitExportDTO {

    private static final String HEADER_TITLE = "长沙市儿童青少年眼健康电子档案";
    private static final String HEADER_TWO = "裸眼远视力*";
    private static final String HEADER_1 = "非睫状肌麻痹屈光度（D）";
    private static final String HEADER_1_1 = "自动电脑验光";

    private static final String HEADER_2_1 = "戴镜视力";


    @ExcelProperty({HEADER_TITLE, "序号*", "序号*", "序号*"})
    private String sn;
    @ExcelProperty({HEADER_TITLE, "学校*", "学校*", "学校*"})
    private String schoolName;
    @ExcelProperty({HEADER_TITLE, "年级*", "年级*", "年级*"})
    private String gradeName;
    @ExcelProperty({HEADER_TITLE, "班级*", "班级*", "班级*"})
    private String className;
    @ExcelProperty({HEADER_TITLE, "姓名*", "姓名*", "姓名*"})
    private String studentName;
    @ExcelProperty({HEADER_TITLE, "性别*", "性别*", "性别*"})
    private String genderDesc;
    @ExcelProperty({HEADER_TITLE, "身份证号*", "身份证号*", "身份证号*"})
    private String idCard;
    @ExcelProperty({HEADER_TITLE, "全国学籍号", "全国学籍号", "全国学籍号"})
    private String studentSno;
    @ExcelProperty({HEADER_TITLE, "联系电话", "联系电话", "联系电话"})
    private String phone;
    @ExcelProperty({HEADER_TITLE, "检查日期*", "检查日期*", "检查日期*"})
    private String checkDate;
    @ExcelProperty({HEADER_TITLE, "双眼视力*", "双眼视力*", "双眼视力*"})
    private String eyeVisionDesc;

    @ExcelProperty({HEADER_TITLE, HEADER_TWO, HEADER_TWO, "右眼"})
    private String rightNakedVisions;
    @ExcelProperty({HEADER_TITLE, HEADER_TWO, HEADER_TWO, "左眼"})
    private String leftNakedVisions;


    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "右眼球镜"})
    private String rightSph;
    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "右眼柱镜"})
    private String rightCyl;
    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "右眼轴位"})
    private String rightAxial;
    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "左眼球镜"})
    private String leftSph;
    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "左眼柱镜"})
    private String leftCyl;
    @ExcelProperty({HEADER_TITLE, HEADER_1, HEADER_1_1, "左眼轴位"})
    private String leftAxial;

    @ExcelProperty({HEADER_TITLE, HEADER_2_1, HEADER_2_1, "戴镜类型"})
    private String glassesTypeDesc;
    @ExcelProperty({HEADER_TITLE, HEADER_2_1, HEADER_2_1, "右眼"})
    private String rightCorrectedVisions;
    @ExcelProperty({HEADER_TITLE, HEADER_2_1, HEADER_2_1, "左眼"})
    private String leftCorrectedVisions;

    @ExcelProperty({HEADER_TITLE, HEADER_1, "检查方式", "检查方式"})
    private String checkType;
    @ExcelProperty({HEADER_TITLE, HEADER_1, "串镜校正", "右眼"})
    private String leftCj;
    @ExcelProperty({HEADER_TITLE, HEADER_1, "串镜校正", "左眼"})
    private String rightCj;

    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "右眼球镜"})
    private String rightSphMydriasis;
    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "右眼柱镜"})
    private String rightCylMydriasis;
    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "右眼轴位"})
    private String rightAxialMydriasis;
    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "左眼球镜"})
    private String leftSphMydriasis;
    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "左眼柱镜"})
    private String leftCylMydriasis;
    @ExcelProperty({HEADER_TITLE, "散瞳后屈光度(D)", "自动电脑验光", "左眼轴位"})
    private String leftAxialMydriasis;


    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "右眼K1度数"})
    private String rightBiometricK1;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "右眼K1轴位"})
    private String rightBiometricK1Axis;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "右眼K2度数"})
    private String rightBiometricK2;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "右眼K2轴位"})
    private String rightBiometricK2Axis;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "左眼K1度数"})
    private String leftBiometricK1;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "左眼K1轴位"})
    private String leftBiometricK1Axis;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "左眼K2度数"})
    private String leftBiometricK2;
    @ExcelProperty({HEADER_TITLE, "角膜曲率（D）", "角膜曲率（D）", "左眼K2轴位"})
    private String leftBiometricK2Axis;


    @ExcelProperty({HEADER_TITLE, "眼轴（mm）", "眼轴（mm）", "右眼"})
    private String rightBiometricAL;
    @ExcelProperty({HEADER_TITLE, "眼轴（mm）", "眼轴（mm）", "左眼"})
    private String leftBiometricAL;

    @ExcelProperty({HEADER_TITLE, "眼压（mmHg）", "眼压（mmHg）", "右眼"})
    private String rightEyePressureDate;
    @ExcelProperty({HEADER_TITLE, "眼压（mmHg）", "眼压（mmHg）", "左眼"})
    private String leftEyePressureDate;

    @ExcelProperty({HEADER_TITLE, "备注", "备注", "备注"})
    private String remark;


}
