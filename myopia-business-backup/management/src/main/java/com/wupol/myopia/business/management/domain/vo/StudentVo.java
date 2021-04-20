package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生导入数据
 * @Author Chikong
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class StudentVo extends Student{
    /** 出生日期 */
    private String birthdayString;
    /** 年级名 */
    private String gradeName;
    /** 班级名 */
    private String className;
    /** 学生学校所在的区域层级 */
    private Integer districtId;

}