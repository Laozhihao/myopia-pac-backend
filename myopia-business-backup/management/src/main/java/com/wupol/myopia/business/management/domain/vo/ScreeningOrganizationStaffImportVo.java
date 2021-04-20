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
 * 筛查机构人员导入数据
 * @Author Chikong
 * @Date 2020/12/22
 **/
@Data
@Accessors(chain = true)
public class ScreeningOrganizationStaffImportVo implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    @ExcelProperty("序号")
    private Integer id;
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("身份证号")
    private String idCard;
    @ExcelProperty("手机号码")
    private String phone;
    @ExcelProperty("说明")
    private String remark;

}