package com.wupol.myopia.business.core.school.domain.dto;


import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 学校年级查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolGradeQueryDTO extends SchoolGrade {

}
