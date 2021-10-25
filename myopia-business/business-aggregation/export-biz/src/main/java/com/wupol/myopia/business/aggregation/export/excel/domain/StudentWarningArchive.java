package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.util.Date;
import java.util.List;

/**
 * 学生预警跟踪档案
 *
 * @Author HaoHao
 * @Date 2021/10/20
 **/
@HeadRowHeight(25)
@HeadStyle(fillPatternType = FillPatternType.NO_FILL, borderBottom = BorderStyle.NONE, borderLeft = BorderStyle.NONE, borderRight = BorderStyle.NONE)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(20)
@Accessors(chain = true)
@Data
public class StudentWarningArchive {

    public static final String TOP_HEADER = "学生预警跟踪数据表";

    /** 学号 */
    @ExcelProperty({TOP_HEADER, "学号"})
    private String schoolNo;

    /** 姓名 */
    @ExcelProperty({TOP_HEADER, "姓名"})
    private String studentName;

    /** 性别描述 */
    @ExcelProperty({TOP_HEADER, "性别"})
    private Integer genderDesc;

    /** 年级和班级名称 */
    @ExcelProperty({TOP_HEADER, "年级-班级"})
    private String gradeAndClassName;

    /** 视力情况 */
    @ExcelProperty({TOP_HEADER, "视力情况"})
    private String visionStatus;

    /** 视力预警 */
    @ExcelProperty({TOP_HEADER, "视力预警"})
    private String visionWarning;

    /** 是否已经就诊（医院复查） */
    @ExcelProperty({TOP_HEADER, "医院复查"})
    private String isVisited;

    /** 配镜建议 */
    @ExcelProperty({TOP_HEADER, "复查反馈", "配镜"})
    private String glassesSuggest;

    /** 就诊结论(医生反馈) */
    @ExcelProperty({TOP_HEADER, "复查反馈", "诊断"})
    private String visitResult;

    /** 课座椅型号建议 */
    @ExcelProperty({TOP_HEADER, "防控建议-课桌椅"})
    private String deskAndChairTypeSuggest;

    /** 座位与黑板距离建议 */
    @ExcelProperty({TOP_HEADER, "防控建议-座位调整"})
    private String seatDistanceSuggest;


    /** 筛查计划学生ID */
    private Integer screeningPlanSchoolStudentId;

    /** 学生ID */
    private Integer studentId;

    /** 学龄：5-幼儿园、0-小学、1-初中 */
    private Integer schoolAge;

    /** 年龄 */
    private Integer age;

    /** 性别 */
    private Integer gender;

    /** 身高 */
    private Float height;

    /** 近视预警等级 */
    private Integer myopiaLevel;

    /** 远视预警等级 */
    private Integer hyperopiaLevel;

    /** 散光预警等级 */
    private Integer astigmatismLevel;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

    /** 筛查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date screeningDate;

    /** 筛查标题 */
    private String screeningTitle;

    /** 课桌型号 */
    private List<Integer> deskType;

    /** 课桌建议高度 */
    private Integer deskAdviseHeight;

    /** 课椅型号 */
    private List<Integer> chairType;

    /** 课椅建议高度 */
    private Integer chairAdviseHeight;

}
