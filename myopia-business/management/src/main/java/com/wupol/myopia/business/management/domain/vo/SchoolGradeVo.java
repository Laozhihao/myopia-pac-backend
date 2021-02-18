package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 年级数据（包括班级）
 * @author Alix
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class SchoolGradeVo extends SchoolGrade{
    /** 班级列表 */
    private List<SchoolClass> classes;
}