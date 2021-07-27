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
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
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

    @ExcelProperty("序号")
    private Integer id;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("性别")
    private String genderDesc;

    @DateTimeFormat("yyyy/MM/dd")
    @ExcelProperty("出生日期")
    private Date birthday;

    @ExcelProperty(value = "民族")
    private String nationDesc;

    @ExcelProperty("学校编码")
    private String schoolNo;

    @ExcelProperty("学校名称")
    private String schoolName;

    @ExcelProperty("年级")
    private String gradeName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("手机号码")
    private String parentPhone;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty({TOP_HEADER, "33cm眼位", "内斜"})
    private Integer esotropia;
    @ExcelProperty({TOP_HEADER, "33cm眼位", "外斜"})
    private Integer exotropia;
    @ExcelProperty({TOP_HEADER, "33cm眼位", "垂直位斜视"})
    private Integer verticalStrabismus;
    @ExcelProperty({TOP_HEADER, "33cm眼位", "初步结果"})
    private Integer oDiagnosis;

    @ExcelProperty({TOP_HEADER, "视力检查", "戴镜情况"})
    private String glassesTypeDesc;
    @ExcelProperty({TOP_HEADER, "视力检查", "裸眼（右/左）"})
    private String nakedVisions;
    @ExcelProperty({TOP_HEADER, "视力检查", "矫正（右/左）"})
    private String correctedVisions;
    @ExcelProperty({TOP_HEADER, "视力检查", "初步结果"})
    private String vDiagnosis;

    @ExcelProperty({TOP_HEADER, "电脑验光", "球镜（右/左）"})
    private String sphs;
    @ExcelProperty({TOP_HEADER, "电脑验光", "柱镜（右/左）"})
    private String cyls;
    @ExcelProperty({TOP_HEADER, "电脑验光", "轴位（右/左）"})
    private String axials;
    @ExcelProperty({TOP_HEADER, "电脑验光", "初步结果"})
    private String cDiagnosis;
    @ExcelProperty({TOP_HEADER, "电脑验光", "屈光结果"})
    private String cResult;

    @ExcelProperty({TOP_HEADER, "裂隙灯", "左眼"})
    private String sLeftEye;
    @ExcelProperty({TOP_HEADER, "裂隙灯", "初步结果"})
    private String sLeftResult;
    @ExcelProperty({TOP_HEADER, "裂隙灯", "右眼"})
    private String sRightEye;
    @ExcelProperty({TOP_HEADER, "裂隙灯", "初步结果"})
    private String sRightResult;

    @ExcelProperty({TOP_HEADER, "小瞳验光", "球镜（右/左）"})
    private String pSph;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "柱镜（右/左）"})
    private String pCyl;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "轴位（右/左）"})
    private String pAxial;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "最佳视力（右/左）"})
    private String pCorrectedVision;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "初步结果"})
    private String pDiagnosis;
    @ExcelProperty({TOP_HEADER, "小瞳验光", "屈光结果"})
    private String pResult;

    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K1"})
    private String dbK1;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜前表面曲率K2"})
    private String dbK2;
    @ExcelProperty({TOP_HEADER, "生物测量", "垂直方向角膜散光度数AST"})
    private String dbAST;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜折射率n"})
    private String dbN;
    @ExcelProperty({TOP_HEADER, "生物测量", "瞳孔直径PD"})
    private String dbPD;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜直径WTW"})
    private String dbWTW;
    @ExcelProperty({TOP_HEADER, "生物测量", "眼轴总长度AL"})
    private String dbAL;
    @ExcelProperty({TOP_HEADER, "生物测量", "角膜中央厚度CCT"})
    private String dbCCT;
    @ExcelProperty({TOP_HEADER, "生物测量", "前房深度AD"})
    private String dbAD;
    @ExcelProperty({TOP_HEADER, "生物测量", "晶体厚度LT"})
    private String dbLT;
    @ExcelProperty({TOP_HEADER, "生物测量", "玻璃体厚度VT"})
    private String dbVT;

    @ExcelProperty("眼压（右/左）")
    private String ipDate;
    @ExcelProperty("眼底（右/左）")
    private String fdDate;

    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病右"})
    private String leftEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病左"})
    private String rightEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "全身疾病在眼部的表现"})
    private String systemicDiseaseSymptom;
    @ExcelProperty({TOP_HEADER, "其他眼病", "盲及视力损害分类（右/左）"})
    private String level;


    @ExcelProperty({TOP_HEADER, "预警级别", "预警级别"})
    private String warningLevelDesc;

    @ExcelProperty({TOP_HEADER, "是否复测", "是否复测"})
    private String isRescreenDesc;

    @ExcelProperty({TOP_HEADER2, "裸眼（右/左）"})
    private String reScreenNakedVisions;
    @ExcelProperty({TOP_HEADER2, "矫正（右/左）"})
    private String reScreenCorrectedVisions;
    @ExcelProperty({TOP_HEADER2, "球镜（右/左）"})
    private String reScreenSphs;
    @ExcelProperty({TOP_HEADER2, "柱镜（右/左）"})
    private String reScreenCyls;
    @ExcelProperty({TOP_HEADER2, "轴位（右/左）"})
    private String reScreenAxials;
    @ExcelProperty({TOP_HEADER2, "等效球镜（右/左）"})
    private String reScreenSphericalEquivalents;
}