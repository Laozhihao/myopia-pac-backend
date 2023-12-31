package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.Serializable;
import java.util.Date;

/**
 * 筛查数据导出
 *
 * @author Alix
 * @Date 2021/03/12
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL,
        borderBottom = BorderStyle.THIN,
        borderLeft = BorderStyle.THIN,
        borderRight = BorderStyle.THIN,
        borderTop = BorderStyle.THIN)
@HeadFontStyle(fontHeightInPoints = 11)
@ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER)
// 将第1-2行的2-3列合并成一个单元格
@OnceAbsoluteMerge(firstRowIndex = 0, lastRowIndex = 1, firstColumnIndex = 0, lastColumnIndex = 12)
@ColumnWidth(20)
@Data
@Accessors(chain = true)
public class VisionScreeningResultExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "视力筛查";
    public static final String TOP_HEADER2 = "视力复测";

    @ExcelProperty({"姓名", "姓名", "姓名"})
    private String studentName;

    @ExcelProperty({"性别", "性别", "性别"})
    private String genderDesc;

    @DateTimeFormat("yyyy/MM/dd")
    @ExcelProperty({"出生日期", "出生日期", "出生日期"})
    private Date birthday;

    @ExcelProperty({"民族", "民族", "民族"})
    private String nationDesc;

    @ExcelProperty({"学校名称", "学校名称", "学校名称"})
    private String schoolName;

    @ExcelProperty({"年级", "年级", "年级"})
    private String gradeName;

    @ExcelProperty({"班级", "班级", "班级"})
    private String className;

    @ExcelProperty({"学籍号", "学籍号", "学籍号"})
    private String studentNo;

    @ExcelProperty({"家庭地址", "家庭地址", "家庭地址"})
    private String address;

    @ExcelProperty({"是否有做检查（未做检查的原因）", "是否有做检查（未做检查的原因）", "是否有做检查（未做检查的原因）"})
    private String state;

    @ExcelProperty({"是否有效数据", "是否有效数据", "是否有效数据"})
    private String isValid;

    @ExcelProperty({"是否复测", "是否复测", "是否复测"})
    private String isRescreenDesc;

    @ExcelProperty({TOP_HEADER, "33cm眼位", "内斜"})
    private String ocularInspectionSotropia;
    @ExcelProperty({TOP_HEADER, "33cm眼位", "外斜"})
    private String ocularInspectionXotropia;
    @ExcelProperty({TOP_HEADER, "33cm眼位", "垂直位斜视"})
    private String ocularInspectionVerticalStrabismus;

    @ExcelProperty({TOP_HEADER, "视力检查", "戴镜情况"})
    private String glassesTypeDesc;
    @ExcelProperty({TOP_HEADER, "视力检查", "裸眼（右）"})
    private String rightNakedVisions;
    @ExcelProperty({TOP_HEADER, "视力检查", "裸眼（左）"})
    private String leftNakedVisions;
    @ExcelProperty({TOP_HEADER, "视力检查", "矫正（右）"})
    private String rightCorrectedVisions;
    @ExcelProperty({TOP_HEADER, "视力检查", "矫正（左）"})
    private String leftCorrectedVisions;

    @ExcelProperty({TOP_HEADER, "电脑验光", "球镜（右）"})
    private String rightSphs;
    @ExcelProperty({TOP_HEADER, "电脑验光", "柱镜（右）"})
    private String rightCyls;
    @ExcelProperty({TOP_HEADER, "电脑验光", "轴位（右）"})
    private String rightAxials;
    @ExcelProperty({TOP_HEADER, "电脑验光", "K1（右）"})
    private String rightK1;
    @ExcelProperty({TOP_HEADER, "电脑验光", "K2（右）"})
    private String rightK2;
    @ExcelProperty({TOP_HEADER, "电脑验光", "球镜（左）"})
    private String leftSphs;
    @ExcelProperty({TOP_HEADER, "电脑验光", "柱镜（左）"})
    private String leftCyls;
    @ExcelProperty({TOP_HEADER, "电脑验光", "轴位（左）"})
    private String leftAxials;
    @ExcelProperty({TOP_HEADER, "电脑验光", "K1（左）"})
    private String leftK1;
    @ExcelProperty({TOP_HEADER, "电脑验光", "K2（左）"})
    private String leftK2;

    @ExcelProperty({TOP_HEADER, "体测检查", "身高"})
    private String height;
    @ExcelProperty({TOP_HEADER, "体测检查", "体重"})
    private String weight;

    @ExcelProperty({TOP_HEADER, "裂隙灯", "左眼"})
    private String slitLampLeftEye;
    @ExcelProperty({TOP_HEADER, "裂隙灯", "右眼"})
    private String slitLampRightEye;

    @ExcelProperty({TOP_HEADER, "小瞳验光", "球镜（右）"})
    private String rightPupilOptometrySph;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "球镜（左）"})
    private String leftPupilOptometrySph;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "柱镜（右）"})
    private String rightPupilOptometryCyl;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "柱镜（左）"})
    private String leftPupilOptometryCyl;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "轴位（右）"})
    private String rightPupilOptometryAxial;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "轴位（左）"})
    private String leftPupilOptometryAxial;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "矫正视力（右）"})
    private String rightPupilOptometryCorrectedVision;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "矫正视力（左）"})
    private String leftPupilOptometryCorrectedVision;

    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K1（右）"})
    private String rightBiometricK1;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K1（右）"})
    private String rightBiometricK1Axis;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K1（左）"})
    private String leftBiometricK1;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K1（左）"})
    private String leftBiometricK1Axis;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K2（右）"})
    private String rightBiometricK2;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K2（右）"})
    private String rightBiometricK2Axis;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K2（左）"})
    private String leftBiometricK2;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K2（左）"})
    private String leftBiometricK2Axis;
    @ExcelProperty({TOP_HEADER, "生物测量", "垂直方向角膜散光度数AST（右）"})
    private String rightBiometricAST;
    @ExcelProperty({TOP_HEADER, "生物测量", "垂直方向角膜散光度数AST（右）"})
    private String rightBiometricASTAxis;
    @ExcelProperty({TOP_HEADER, "生物测量", "垂直方向角膜散光度数AST（左）"})
    private String leftBiometricAST;
    @ExcelProperty({TOP_HEADER, "生物测量", "垂直方向角膜散光度数AST（左）"})
    private String leftBiometricASTAxis;
    @ExcelProperty({TOP_HEADER, "生物测量", "瞳孔直径PD（右）"})
    private String rightBiometricPD;
    @ExcelProperty({TOP_HEADER, "生物测量", "瞳孔直径PD（左）"})
    private String leftBiometricPD;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜直径WTW（右）"})
    private String rightBiometricWTW;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜直径WTW（左）"})
    private String leftBiometricWTW;
    @ExcelProperty({TOP_HEADER, "生物测量", "眼轴总长度AL（右）"})
    private String rightBiometricAL;
    @ExcelProperty({TOP_HEADER, "生物测量", "眼轴总长度AL（左）"})
    private String leftBiometricAL;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜中央厚度CCT（右）"})
    private String rightBiometricCCT;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜中央厚度CCT（左）"})
    private String leftBiometricCCT;
    @ExcelProperty({TOP_HEADER, "生物测量", "前房深度AD（右）"})
    private String rightBiometricAD;
    @ExcelProperty({TOP_HEADER, "生物测量", "前房深度AD（左）"})
    private String leftBiometricAD;
    @ExcelProperty({TOP_HEADER, "生物测量", "晶体厚度LT（右）"})
    private String rightBiometricLT;
    @ExcelProperty({TOP_HEADER, "生物测量", "晶体厚度LT（左）"})
    private String leftBiometricLT;
    @ExcelProperty({TOP_HEADER, "生物测量", "玻璃体厚度VT（右）"})
    private String rightBiometricVT;
    @ExcelProperty({TOP_HEADER, "生物测量", "玻璃体厚度VT（左）"})
    private String leftBiometricVT;

    @ExcelProperty({TOP_HEADER, "眼压", "眼压（右）"})
    private String rightEyePressureDate;
    @ExcelProperty({TOP_HEADER, "眼压", "眼压（左）"})
    private String leftEyePressureDate;
    @ExcelProperty({TOP_HEADER, "眼底", "眼底（右）"})
    private String rightFundusData;
    @ExcelProperty({TOP_HEADER, "眼底", "眼底（左）"})
    private String leftFundusData;

    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病左"})
    private String otherEyeDiseasesLeftEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病右"})
    private String otherEyeDiseasesRightEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "全身疾病在眼部的表现"})
    private String otherEyeDiseasesSystemicDiseaseSymptom;
    @ExcelProperty({TOP_HEADER, "其他眼病", "盲及视力损害分类（右）"})
    private String rightOtherEyeDiseasesLevel;
    @ExcelProperty({TOP_HEADER, "其他眼病", "盲及视力损害分类（左）"})
    private String leftOtherEyeDiseasesLevel;

    @ExcelProperty({TOP_HEADER, "预警级别", "预警级别"})
    private String warningLevelDesc;

    @ExcelProperty({TOP_HEADER2, "视力检查", "戴镜情况"})
    private String reScreenGlassesTypeDesc;
    @ExcelProperty({TOP_HEADER2, "视力检查", "裸眼（右）"})
    private String rightReScreenNakedVisions;
    @ExcelProperty({TOP_HEADER2, "视力检查", "裸眼（左）"})
    private String leftReScreenNakedVisions;
    @ExcelProperty({TOP_HEADER2, "视力检查", "矫正（右）"})
    private String rightReScreenCorrectedVisions;
    @ExcelProperty({TOP_HEADER2, "视力检查", "矫正（左）"})
    private String leftReScreenCorrectedVisions;

    @ExcelProperty({TOP_HEADER2, "电脑验光", "球镜（右）"})
    private String rightReScreenSphs;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "球镜（左）"})
    private String leftReScreenSphs;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "柱镜（右）"})
    private String rightReScreenCyls;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "K1（右）"})
    private String rightReScreenK1;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "K2（右）"})
    private String rightReScreenK2;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "柱镜（左）"})
    private String leftReScreenCyls;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "轴位（右）"})
    private String rightReScreenAxials;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "轴位（左）"})
    private String leftReScreenAxials;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "K1（左）"})
    private String leftReScreenK1;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "K2（左）"})
    private String leftReScreenK2;
}