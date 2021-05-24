package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/4/23 17:07
 */
@Data
@Accessors(chain = true)
public class StudentExtraDTO extends Student {

    /** 出生日期 */
    private String birthdayString;
    /** 年级名 */
    private String gradeName;
    /** 班级名 */
    private String className;
    /** 学生学校所在的区域层级 */
    private Integer districtId;

}
