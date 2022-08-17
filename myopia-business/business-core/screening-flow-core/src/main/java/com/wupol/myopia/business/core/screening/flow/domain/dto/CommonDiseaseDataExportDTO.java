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
public class CommonDiseaseDataExportDTO implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    public static final String TOP_HEADER = "初筛检查";
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

    @ExcelProperty({"学号", "学号", "学号"})
    private String studentNo;

    @ExcelProperty({"家庭地址", "家庭地址", "家庭地址"})
    private String address;

    @ExcelProperty({"是否有做检查（未做检查的原因）", "是否有做检查（未做检查的原因）", "是否有做检查（未做检查的原因）"})
    private String state;

    @ExcelProperty({"是否有效数据", "是否有效数据", "是否有效数据"})
    private String isValid;

    @ExcelProperty({"是否复测", "是否复测", "是否复测"})
    private String isRescreenDesc;

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
    @ExcelProperty({TOP_HEADER, "电脑验光", "球镜（左）"})
    private String leftSphs;
    @ExcelProperty({TOP_HEADER, "电脑验光", "柱镜（左）"})
    private String leftCyls;
    @ExcelProperty({TOP_HEADER, "电脑验光", "轴位（左）"})
    private String leftAxials;

    @ExcelProperty({TOP_HEADER, "身高体重", "身高"})
    private String height;
    @ExcelProperty({TOP_HEADER, "身高体重", "体重"})
    private String weight;

    @ExcelProperty({TOP_HEADER, "龋齿检查", "乳牙 龋（d）：失（m）：补（f）"})
    private String deciduous;
    @ExcelProperty({TOP_HEADER, "龋齿检查", "恒牙 龋（D）：失（M）：补（F）"})
    private String permanent;

    @ExcelProperty({TOP_HEADER, "脊柱弯曲", "胸段侧弯"})
    private String chest;
    @ExcelProperty({TOP_HEADER, "脊柱弯曲", "胸腰段侧弯"})
    private String chestWaist;
    @ExcelProperty({TOP_HEADER, "脊柱弯曲", "腰段侧弯"})
    private String waist;
    @ExcelProperty({TOP_HEADER, "脊柱弯曲", "前后弯曲"})
    private String entirety;

    @ExcelProperty({TOP_HEADER, "血压", "舒张压"})
    private String dbp;
    @ExcelProperty({TOP_HEADER, "血压", "收缩压"})
    private String sbp;

    @ExcelProperty({TOP_HEADER, "疾病史", "疾病史"})
    private String diseasesHistory;
    @ExcelProperty({TOP_HEADER, "个人隐私", "个人隐私"})
    private String privacyData;

    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病左"})
    private String otherEyeDiseasesLeftEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "眼部疾病右"})
    private String otherEyeDiseasesRightEyeDiseases;
    @ExcelProperty({TOP_HEADER, "其他眼病", "全身疾病在眼部的表现"})
    private String otherEyeDiseasesSystemicDiseaseSymptom;


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
    @ExcelProperty({TOP_HEADER2, "电脑验光", "柱镜（左）"})
    private String leftReScreenCyls;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "轴位（右）"})
    private String rightReScreenAxials;
    @ExcelProperty({TOP_HEADER2, "电脑验光", "轴位（左）"})
    private String leftReScreenAxials;

    @ExcelProperty({TOP_HEADER2, "身高体重", "身高"})
    private String reHeight;
    @ExcelProperty({TOP_HEADER2, "身高体重", "体重"})
    private String reWeight;

    @ExcelProperty({TOP_HEADER2, "误差结果说明", "误差结果说明"})
    private String deviationData;

}