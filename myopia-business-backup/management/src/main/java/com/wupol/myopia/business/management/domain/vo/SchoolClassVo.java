package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.SchoolClass;
import lombok.Data;
import lombok.experimental.Accessors;

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