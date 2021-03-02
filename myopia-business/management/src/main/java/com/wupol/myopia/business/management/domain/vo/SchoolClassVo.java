package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 班级数据
 * @author Alix
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class SchoolClassVo extends SchoolClass{
    /** 年级名称 */
    private String gradeName;
}