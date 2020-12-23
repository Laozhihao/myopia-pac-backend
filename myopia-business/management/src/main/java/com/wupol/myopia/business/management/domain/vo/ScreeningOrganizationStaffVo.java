package com.wupol.myopia.business.management.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.Serializable;

/**
 * 筛查机构人员
 * @Author Chikong
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class ScreeningOrganizationStaffVo extends ScreeningOrganizationStaff {

    /** 姓名 */
    private String name;
    /** 性别 */
    private Integer gender;
    /** 身份证 */
    private String idCard;
    /** 手机 */
    private String phone;
    /** 组织名 */
    private Integer organizationName;

}