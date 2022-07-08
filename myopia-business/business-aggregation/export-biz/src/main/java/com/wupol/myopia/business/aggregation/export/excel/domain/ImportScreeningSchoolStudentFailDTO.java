package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 导入筛查学校学生实体
 *
 * @author hang.yuan 2022/7/7 11:07
 */
@ContentFontStyle(color = 10)
@ContentRowHeight(20)
@Accessors(chain = true)
@Data
public class ImportScreeningSchoolStudentFailDTO implements Serializable {

    /**
     * 编码
     */
    @ExcelProperty(value = "编码")
    private String screeningCode;
    /**
     * 身份证号
     */
    @ExcelProperty(value = "身份证号")
    private String idCard;
    /**
     * 护照
     */
    @ExcelProperty(value = "护照")
    private String passport;
    /**
     * 姓名
     */
    @ExcelProperty(value = "姓名")
    private String name;
    /**
     * 性别
     */
    @ExcelProperty(value = "性别")
    private String gender;
    /**
     * 出生日期
     */
    @ExcelProperty(value = "出生日期")
    private String birthday;
    /**
     * 民族
     */
    @ExcelProperty(value = "民族")
    private String nation;
    /**
     * 年级
     */
    @ExcelProperty(value = "年级")
    private String gradeName;
    /**
     * 班级
     */
    @ExcelProperty(value = "班级")
    private String className;
    /**
     * 学号
     */
    @ExcelProperty(value = "学号")
    private String studentNo;
    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    private String phone;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "失败原因")
    private String errorMsg;
    

}
